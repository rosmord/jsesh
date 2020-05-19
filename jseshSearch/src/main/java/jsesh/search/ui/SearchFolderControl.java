/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import jsesh.editor.MdCSearchQuery;
import jsesh.resources.JSeshMessages;
import jsesh.search.clientApi.CorpusSearchHit;
import jsesh.search.clientApi.CorpusSearchTarget;
import jsesh.search.corpus.CorpusSearch;
import jsesh.search.corpus.PartialResults;
import jsesh.utils.JSeshWorkingDirectory;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Control class for JSearchFolderPanel.
 *
 * @author rosmord
 */
class SearchFolderControl {

    private CorpusSearchTarget corpusSearchTarget;

    private JSearchFolderPanel ui;

    private SearchWorker currentSearchWorker = null;

    private class SearchWorker extends SwingWorker<List<CorpusSearchHit>, PartialResults> {

        private Path rootPath;
        private CorpusSearch corpusSearch;

        public SearchWorker(MdCSearchQuery query, Path rootPath) {
            corpusSearch = new CorpusSearch(rootPath, query);
            this.rootPath = rootPath;
        }

        @Override
        protected List<CorpusSearchHit> doInBackground() {
            try {
                int fileCount = 0;
                while (corpusSearch.hasNext()) {
                    fileCount++;
                    // This sleep method allows one to call interrupt().
                    Thread.sleep(1);
                    List<CorpusSearchHit> hits = corpusSearch.searchNext();
                    publish(new PartialResults(fileCount, hits));
                }
                return corpusSearch.getResult();
            } catch (InterruptedException e) {
                // System.err.println("Interrupted");
                return corpusSearch.getResult();
            }
        }

        @Override
        protected void process(List<PartialResults> partialResultList) {
            ResultTableModel tableModel = (ResultTableModel) ui.getResultTable().getModel();
            int currentCount = 0;
            for (PartialResults partialResult : partialResultList) {
                tableModel.addAll(partialResult.getHits());
                currentCount = Math.max(currentCount, partialResult.getFileCount());
            }
            String countText = JSeshMessages.format("jsesh.search.folder.files_searched", Integer.toString(currentCount));
            ui.getMessageField().setText(countText);
        }

        @Override
        protected void done() {
            try {
                List<CorpusSearchHit> res = get();
                ResultTableModel model = new ResultTableModel(rootPath, res);
                ui.getResultTable().setModel(model);
                ui.getMessageField().setText(JSeshMessages.format("jsesh.search.folder.done", Integer.toString(res.size())));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public SearchFolderControl(CorpusSearchTarget corpusSearchTarget) {
        this.ui = new JSearchFolderPanel();
        this.corpusSearchTarget = corpusSearchTarget;
        clearTable();
        activateButtons();
    }

    private void activateButtons() {
        ui.getFolderField().setValue(JSeshWorkingDirectory.getWorkingDirectory());
        ui.getSearchButton().addActionListener((e) -> this.doSearch());
        ui.getChooseFolderButton().addActionListener((e) -> this.chooseFolder());
        ui.getCancelButton().addActionListener((e) -> {
            ui.getMessageField().setText(JSeshMessages.getString("jsesh.search.folder.canceled"));
            this.stopSearch();
        });
        ui.getResultTable().addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectResult(e);
            }
        }
        );
    }

    /**
     * Handle mouse double click on the result table.
     *
     * @param e
     */
    private void selectResult(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            ResultTableModel model = (ResultTableModel) ui.getResultTable().getModel();
            Optional<CorpusSearchHit> selected = model.getCorpusSearchHit(ui.getResultTable().getSelectedRow());
            selected.ifPresent(
                    hit -> corpusSearchTarget.showCorpusSearchHit(hit)
            );
        }
    }
    
    private MdCSearchQuery getQuery() {
        return ui.getSearchForm().getQuery();
    }

    
    private void chooseFolder() {
        File rootFile = (File) ui.getFolderField().getValue();
        PortableFileDialog selector = PortableFileDialogFactory.createDirectorySaveDialog(ui);
        selector.setCurrentDirectory(rootFile);
        selector.setTitle("Choose folder to search");
        if (selector.show() == FileOperationResult.OK) {
            File newRoot = selector.getSelectedFile();
            ui.getFolderField().setValue(newRoot);
            JSeshWorkingDirectory.setWorkingDirectory(newRoot);
        }
    }

    private void stopSearch() {
        if (currentSearchWorker != null) {
            currentSearchWorker.cancel(true);
        }
    }


    private Path getRootPath() {
        File f = ui.getFolder();
        if (f != null && f.isDirectory()) {
            return f.toPath();
        } else {
            return Paths.get(".");
        }
    }

    private void doSearch() {
        ui.getMessageField().setText(JSeshMessages.getString("jsesh.search.folder.started"));
        if (currentSearchWorker != null) {
            cancelAndWaitForCompletion(currentSearchWorker);
        }
        clearTable();
        currentSearchWorker = new SearchWorker(getQuery(), getRootPath());
        currentSearchWorker.execute();
    }

    private void cancelAndWaitForCompletion(SearchWorker w) {
        stopSearch();
        // wait for stop :
        try {
            w.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (CancellationException e) {
            // DO NOTHING. EXPECTED RESULT
        }
    }

    private void clearTable() {
        ui.getResultTable().setModel(new ResultTableModel(getRootPath()));
    }

    public JSearchFolderPanel getPanel() {
        return ui;
    }

    private static final String[] columnNames = {JSeshMessages.getString("generic.file.text"), JSeshMessages.getString("jsesh.search.folder.table.position.text")};

    private class ResultTableModel extends AbstractTableModel {

        private final Path rootPath;
        private final List<CorpusSearchHit> resultData;

        public ResultTableModel(Path rootPath, List<CorpusSearchHit> resultData) {
            this.rootPath = rootPath;
            this.resultData = new ArrayList<>(resultData);
        }

        public ResultTableModel(Path rootPath) {
            resultData = new ArrayList<>();
            this.rootPath = rootPath;
        }

        @Override
        public int getRowCount() {
            return resultData.size();
        }

        @Override
        public String getColumnName(int column) {
            if (column >= 0 && column < columnNames.length) {
                return columnNames[column];
            } else {
                return "";
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        public void addAll(Collection<CorpusSearchHit> newHits) {
            if (newHits.size() > 0) {
                int oldSize = getRowCount();
                resultData.addAll(newHits);
                fireTableRowsInserted(oldSize, oldSize + newHits.size() - 1);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (0 <= rowIndex && rowIndex < getRowCount()) {
                CorpusSearchHit hit = resultData.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return pathToString(hit.getFile());
                    case 1:
                        return hit.getPosition();
                    default:
                        return null;
                }
            } else {
                return null;
            }
        }

        private String pathToString(Path file) {
            Path relativePath = rootPath.relativize(file);
            return relativePath.toString();
        }

        /**
         * Returns the item at row row, if any.
         *
         * @param row
         * @return
         */
        public Optional<CorpusSearchHit> getCorpusSearchHit(int row) {
            if (0 <= row && row < resultData.size()) {
                return Optional.of(resultData.get(row));
            } else {
                return Optional.empty();
            }
        }
    }

}
