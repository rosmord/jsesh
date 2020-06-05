package jsesh.graphics.glyphs.control;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.graphics.glyphs.model.ExternalSignImporterModel;
import jsesh.graphics.glyphs.ui.ExternalSignImporterUI;
import jsesh.graphics.glyphs.ui.UIEventListener;
import jsesh.hieroglyphs.data.GardinerCode;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Presenter for the external font importer. Gets events, update GUI, modify
 * model.
 * <p>
 * This use the design pattern Model/View/Presenter. Basically: all input is
 * sent to the presenter, which is responsible both for updating the model and
 * the view. Data is mostly copied between the model and the views, which is
 * often practical for forms.
 *
 * TODO this is ugly, antiquated code, especially the listener.
 * @author rosmord
 *
 */
public class ExternalSignImporterPresenter implements UIEventListener {

    ExternalSignImporterModel model;
    ExternalSignImporterUI ui;

    public ExternalSignImporterPresenter(ExternalSignImporterModel model, ExternalSignImporterUI ui) {
        this.model = model;
        this.ui = ui;
        refreshUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        doAction(src);
    }

    private void doAction(Object src) {
        boolean signChanged = false;
        if (src == ui.getCloseButton()) {

        } else if (src == ui.getCodeField()) {

        } else if (src == ui.getImportButton()) {
            importSigns();
            signChanged = true;
        } else if (src == ui.getInsertButton()) {
            model.insertSign(ui.getCodeField().getText());
        } else if (src == ui.getNextButton()) {
            signChanged = true;
            model.nextSign();
        } else if (src == ui.getPreviousButton()) {
            signChanged = true;
            model.previousSign();
        } else if (src == ui.getFlipHorizontallyButton()) {
            model.flipHorizontally();
        } else if (src == ui.getFlipVerticallyButton()) {
            model.flipVertically();
        } else if (src == ui.getFullCadratHeightButton()) {
            //model.resizeVerticallyTo(model.getReferenceShape().getBbox().getHeight());
            resizeVerticallyTo(model.getReferenceShape().getBbox().getHeight());
        }
        if (signChanged) {
            //ui.getCodeField().setText("");
            ui.getCodeField().setText(model.getDefaultSignCode());
        }
        refreshUI();
    }

    private void importSigns() {
        PortableFileDialog fileChooser = PortableFileDialogFactory.createFileOpenDialog(ui.getPanel());
        fileChooser.setCurrentDirectory(model.getSourceDirectory());

        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return model.fileFormatIsKnown(f);
            }

            @Override
            public String getDescription() {
                return model.getKnownFormats();
            }

        });
        FileOperationResult answer = fileChooser.show();
        if (answer == FileOperationResult.OK) {
            try {
                File selected = fileChooser.getSelectedFile();
                if (!selected.isDirectory()) {
                    model.setSourceDirectory(selected.getParentFile());
                    model.loadSigns(selected);
                } else {
                    model.setSourceDirectory(selected);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(ui.getPanel(), "Could not load file " + fileChooser.getSelectedFile().getPath() + "\nbecause: " + e.getMessage(), "File not loaded", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Update all elements of the Graphic User Interface from the current model
     * state.
     *
     */
    public void refreshUI() {
        // activation
        ui.getNextButton().setEnabled(model.hasNext());
        ui.getPreviousButton().setEnabled(model.hasPrevious());
        ui.getInsertButton().setEnabled(codeFieldIsComplete());
        ui.getFlipHorizontallyButton().setEnabled(model.getShapeChar() != null);
        ui.getFlipVerticallyButton().setEnabled(model.getShapeChar() != null);
        ui.getCodeField().setEnabled(model.getShapeChar() != null);
        ui.getFullCadratHeightButton().setEnabled(model.getShapeChar() != null);
        // Fill data :
        ui.getImportedSignDisplay().setShape(model.getShapeChar());
        ui.getReferenceSignDisplay().setShape(model.getReferenceShape());
    }

    private boolean codeFieldIsComplete() {
        String txt = ui.getCodeField().getText();
        return (!"".equals(txt)) && (model.getShapeChar() != null)
                && GardinerCode.isCanonicalCode(txt);
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        doAction(e.getSource());
    }

    @Override
    public void resizeVerticallyTo(double y) {
        model.resizeVerticallyTo(y);
        ui.getImportedSignDisplay().setShapeScale(model.getShapeScale());
        refreshUI();
    }

}
