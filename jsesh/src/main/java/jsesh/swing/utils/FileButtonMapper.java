package jsesh.swing.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.filechooser.FileFilter;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Binds a button to a JTextField displaying a file (or directory) name.
 *
 * TODO : this architecture is not very pretty. Explore alternatives.
 *
 * @author rosmord
 *
 */
public class FileButtonMapper {

    public static final int OPEN = 1;

    public static final int SAVE = 2;

    private final JButton browseButton;

    private final JFormattedTextField fileField;

    private int mode;

    private int fileSelectionMode;

    private List<FileFilter> fileFilters;

    private String dialogTitle;

    private String approveButtonLabel;

    /**
     * Binds a "browse" button to a file field.
     * <p>
     * The button will normally open a dialog for selecting an output file.
     *
     * @param browseButton
     * @param fileField a formatted text field suitable to display directories.
     */
    public FileButtonMapper(JButton browseButton, JFormattedTextField fileField) {
        super();
        // TODO Auto-generated constructor stub
        this.browseButton = browseButton;
        this.fileField = fileField;
        browseButton.addActionListener((e) -> browse());
        this.mode = SAVE;
        this.fileSelectionMode = JFileChooser.FILES_ONLY;
        fileFilters = null;
        dialogTitle = null;
        approveButtonLabel = null;

    }

    public void browse() {
        File f;

        try {
            f = (File) fileField.getValue();
        } catch (ClassCastException exc) {
            f = new File(fileField.getText());
        }

        PortableFileDialog chooser = createFileDialog();
        chooser.setSelectedFile(f);

        FileOperationResult result = chooser.show();
        if (result == FileOperationResult.OK) {
            fileField.setValue(chooser.getSelectedFile());
        }
    }

    private PortableFileDialog createFileDialog() {
        PortableFileDialog chooser;
        //fileSelectionMode one of JFileChooser.DIRECTORIES_ONLY,
        //* JFileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES.

        if (fileSelectionMode == JFileChooser.FILES_ONLY) {
            if (mode == OPEN) {
                chooser = PortableFileDialogFactory.createFileOpenDialog(fileField);
            } else {
                chooser = PortableFileDialogFactory.createFileSaveDialog(fileField);
            }
        } else {
            if (mode == OPEN) {
                chooser = PortableFileDialogFactory.createDirectoryOpenDialog(fileField);
            } else {
                chooser = PortableFileDialogFactory.createDirectorySaveDialog(fileField);
            }
        }

        if (getDialogTitle() != null) {
            chooser.setTitle(getDialogTitle());
        }

//        if (getApproveButtonLabel() != null) {
//            chooser.setApproveButtonText(getApproveButtonLabel());
//        }
        if (fileFilters != null) {
            chooser.setFileFilters(fileFilters.toArray(new FileFilter[fileFilters.size()]));
        }
        return chooser;
    }

    /**
     * @return Returns the fileSelectionMode.
     */
    public int getFileSelectionMode() {
        return fileSelectionMode;
    }

    /**
     * @param fileSelectionMode one of JFileChooser.DIRECTORIES_ONLY,
     * JFileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES.
     * @see JFileChooser
     */
    public void setFileSelectionMode(int fileSelectionMode) {
        this.fileSelectionMode = fileSelectionMode;
    }

    /**
     * @return Returns the mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * @param mode one of OPEN or SAVE
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    public void addFileFilter(FileFilter filter) {
        if (fileFilters == null) {
            fileFilters = new ArrayList<>();
        }
        fileFilters.add(filter);
    }

    /**
     * @return Returns the okLabel.
     */
    public String getApproveButtonLabel() {
        return approveButtonLabel;
    }

    /**
     * @param okLabel The okLabel to set.
     */
    public void setApproveButtonLabel(String okLabel) {
        this.approveButtonLabel = okLabel;
    }

    /**
     * @return Returns the title.
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    /**
     * @param title The title to set.
     */
    public void setDialogTitle(String title) {
        this.dialogTitle = title;
    }

}
