package jsesh.jhotdraw.search;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.actions.generic.ViewOpenerForDocument;
import jsesh.jhotdraw.actions.generic.ViewOpenerWorker;
import jsesh.jhotdraw.dialogs.JSearchFolderPanel;
import jsesh.jhotdraw.utils.WindowsHelper;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.mdc.model.TopItemList;
import jsesh.search.*;
import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.qenherkhopeshef.jhotdrawChanges.ActiveViewAwareApplication;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * Manages searches in the corpus.
 */
public class CorpusSearchPresenter {

    /**
     * The application (we need to ask it to open or display files).
     */
    private ActiveViewAwareApplication app;
    private JSearchFolderPanel ui;
    private Optional<SearchWorker> currentSearchWorker = Optional.empty();
    private boolean canOpenNewView = true;

    public CorpusSearchPresenter(ActiveViewAwareApplication app) {
        this.app = app;
        ui = new JSearchFolderPanel();
        clearTable();
        activateButtons();
        ui.getFolderField().setValue(JSeshWorkingDirectory.getWorkingDirectory());
    }

    private void activateButtons() {
        ui.getSearchButton().addActionListener((e) -> this.doSearch());
        ui.getChooseFolderButton().addActionListener((e) -> this.chooseFolder());
        ui.getCancelButton().addActionListener((e) -> {
            ui.getMessageField().setText(Messages.getString("searchFolder.canceled"));
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
                    hit -> showCorpusSearchHit(hit)
            );
        }
    }

    /**
     * Display the data of a given corpus hit.
     *
     * @param hit
     */
    private void showCorpusSearchHit(final CorpusSearchHit hit) {
        Path hitPath = hit.getFile();
        JSeshView selectedView = null;
        for (View view : app.views()) {
            if (view.getURI() == null) {
                continue; // We don't try (for the moment) to load the document
            }            // in the existing view... it might contain unsaved data.
            Path viewPath = Paths.get(view.getURI());
            try {
                if (Files.isSameFile(viewPath, hitPath)) {
                    System.out.println("FOUND FILE " + viewPath);
                    selectedView = (JSeshView) view;
                }
            } catch (IOException e) {
                e.printStackTrace(); // Don't stop for this, but display just in case.
            }
        }
        if (selectedView == null) {
            if (canOpenNewView) { // Else, nothing... do it later.
                canOpenNewView = false;
                View newView = app.createView();
                app.add(newView);
                newView.setEnabled(false);
                app.show(newView);
                app.setActiveView(newView);
                newView.execute(new ViewOpenerWorker(hit.getFile().toUri(), newView, app) {
                    // When the file is loaded, move to the correct position.
                    @Override
                    protected void done(Object value) {
                        super.done(value);
                        JSeshView v = (JSeshView) newView;
                        v.getEditor().setInsertPosition(hit.getPosition());
                        canOpenNewView = true;
                    }
                });
            }
        } else {
            // TODO : reorganize MDCPosition. It's too tightly coupled to the text.
            if (selectedView.isEnabled()) {
                selectedView.setEnabled(false);
                app.show(selectedView);
                WindowsHelper.toFront(selectedView);
                selectedView.getEditor().setInsertPosition(hit.getPosition());
                selectedView.setEnabled(true);
            }
        }
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

    public void show() {
        ui.setVisible(true);
    }

    private void stopSearch() {
        currentSearchWorker.ifPresent(w -> {
            try {
                w.cancel(true);
            } catch (CancellationException e) {/* NOTHING*/
            }
        });
    }

    public MdCSearchQuery getQuery() {
        if (ui.getSearchGlyphsCheckBox().isSelected()) {
            TopItemList topitems = ui.getmDCField().getHieroglyphicTextModel().getModel();
            List<String> codes = topitems.getCodes();
            return new SignStringSearchQuery(codes);
        } else {
            TopItemList quadrants = ui.getmDCField().getHieroglyphicTextModel().getModel();
            return new QuadrantSearchQuery(quadrants);
        }
    }

    public Path getRootPath() {
        File f = ui.getFolder();
        if (f != null && f.isDirectory()) {
            return f.toPath();
        } else {
            return Paths.get(".");
        }
    }

    public JSearchFolderPanel getDialog() {
        return ui;
    }

    private void doSearch() {
        ui.getMessageField().setText(Messages.getString("searchFolder.started"));
        currentSearchWorker.ifPresent(this::cancelAndWaitForCompletion);
        clearTable();
        currentSearchWorker = Optional.of(new SearchWorker(getQuery(), getRootPath()));
        currentSearchWorker.ifPresent(w -> w.execute());
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
                System.err.println("Interrupted");
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
            String countText = Messages.format("searchFolder.files_searched", "" + currentCount);
            ui.getMessageField().setText(countText);
        }

        @Override
        protected void done() {
            try {
                List<CorpusSearchHit> res = get();
                ResultTableModel model = new ResultTableModel(rootPath, res);
                ui.getResultTable().setModel(model);
                ui.getMessageField().setText(Messages.format("searchFolder.done", Integer.toString(res.size())));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String[] columnNames = {Messages.getString("file"), Messages.getString("searchFolder.table.position.text")};

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
