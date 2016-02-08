/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.jhotdrawCustom;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.jhotdraw_7_6.gui.URIChooser;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * AT the present moment, this class is not finished.
 *
 * <p>
 * I should change a lot the JHotdraw framework for this. The point is : in some
 * cases, I want to use the AWT dialog. But JHotDraw insists on having a
 * component it can display in its JSheet.
 *
 * A URI chooser for JSesh. It uses the PortableFileDialog (Mac OS Look and feel
 * is nice, but I want to manipulate comboboxes with the keyboard.
 * <p>
 * Some methods are never called and so not written. Note that the URI chooser
 * should be cut at least in two, as it is not abstract enough.
 *
 * @author rosmord
 */
public class QenherkhURIChooser implements URIChooser {

    private PortableFileDialog portableOpenDialog;
    private PortableFileDialog portableSaveDialog;
    /**
     * One of {@link URIChooser#OPEN_DIALOG}, URIChooser.SAVE_DIALOG or
     * URIChooser.CUSTOM_DIALOG
     */
    private int dialogType;
    private FileFilter[] openFilter;
    private FileFilter[] closeFilters;
    private URI selectedURI;

    @Override
    public URI getSelectedURI() {
        return selectedURI;
    }

    @Override
    public void setSelectedURI(URI uri) {
        selectedURI = uri;
    }

    @Override
    public int getDialogType() {
        return dialogType;
    }

    @Override
    /**
     * One of One of {@link URIChooser#OPEN_DIALOG}, URIChooser.SAVE_DIALOG or
     * URIChooser.CUSTOM_DIALOG
     */
    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }

    @Override
    public String getApproveButtonText() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setApproveButtonText(String approveButtonText) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getApproveButtonMnemonic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setApproveButtonMnemonic(int mnemonic) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getComponent() {
        return null;
    }

    @Override
    public void addActionListener(ActionListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeActionListener(ActionListener l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDialogTitle(String dialogTitle) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDialogTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rescanCurrentDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setOpenFileFilters(FileFilter[] filters) {
        this.openFilter = filters;
    }

    public void setCloseFileFilters(FileFilter[] filters) {
        this.closeFilters = filters;
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        portableOpenDialog = PortableFileDialogFactory.createFileOpenDialog(parent);
        if (selectedURI != null) {
            portableOpenDialog.setSelectedFile(new File(selectedURI));
        }
        if (openFilter != null) {
            portableOpenDialog.setFileFilters(openFilter);
        }
        FileOperationResult res = portableOpenDialog.show();
        if (res == FileOperationResult.OK) {
            selectedURI = portableOpenDialog.getSelectedFile().toURI();
            return URIChooser.APPROVE_OPTION;
        } else {
            return URIChooser.CANCEL_OPTION;
        }
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        portableSaveDialog = PortableFileDialogFactory.createFileSaveDialog(parent);
        if (selectedURI != null) {
            portableSaveDialog.setSelectedFile(new File(selectedURI));
        }

        if (closeFilters != null) {
            portableSaveDialog.setFileFilters(closeFilters);
        }
        FileOperationResult res = portableSaveDialog.show();
        if (res == FileOperationResult.OK) {
            selectedURI = portableSaveDialog.getSelectedFile().toURI();
            return URIChooser.APPROVE_OPTION;
        } else {
            return URIChooser.CANCEL_OPTION;
        }
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
