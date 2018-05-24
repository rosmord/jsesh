/*
 * Created on 17 oct. 2004 (!)
 * Ok, this file is veeery old.
 */
package jsesh.graphics.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.filechooser.FileFilter;

import jsesh.swing.utils.RestrictedCharFormatter;
import jsesh.utils.FileUtils;
import jsesh.utils.JSeshWorkingDirectory;

import org.qenherkhopeshef.graphics.bitmaps.BitmapStreamGraphics;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Export to graphic bitmaps.
 *
 * @author Serge Rosmorduc
 *
 * This file is free software under the CECILL License. And is WAAAY too large.
 */
public class BitmapExporter {

    private final Component parent;

    /**
     * The export file name
     */
    private String fileName;

    /**
     * The export directory.
     */
    private File directory;

    /**
     * The base name for multiple output.
     */
    private String basename;

    boolean multiFile;

    boolean transparency;

    int cadratHeight;

    Color backgroundColor = Color.WHITE;

    /**
     * The index of the selected format in the outputFormat table.
     */
    int outputFormatIndex;

    /**
     * The list of possible output formats.
     */
    public static final String[] OUTPUT_FORMATS = {"png", "jpg"};

    /**
     * Create a new bitmap exporter.
     *
     * @param parent the main window.
     */
    public BitmapExporter(Component parent) {
        this.parent = parent;
        outputFormatIndex = 0;
        setSingleOutputFile(new File("unnamed.png"));
        multiFile = false;
        basename = "unnamed";
        transparency = true;
        cadratHeight = 32;
        initFromPreferences();
    }

    /**
     * Ask the user for a file name, file format and options.
     *
     * @param onlySelection if true, we will prepare to export only the
     * selection.
     * @return one of JOptionPane.CANCEL_OPTION or JOptionPane.OK_OPTION,
     * depending on user input.
     */
    public int askUser(boolean onlySelection) {
        this.multiFile = !onlySelection;
        BitmapOptionPanel panel = new BitmapOptionPanel(parent,
                "Export as bitmap file");
        return panel.askAndSet();
    }

    public void export(ExportData data) {
        try {
            double length = data.getDrawingSpecifications()
                    .getHieroglyphsDrawer().getHeightOfA1();
            data.setScale(this.cadratHeight / length);
            if (multiFile) {
                exportAll(data);
            } else {
                exportSelection(data);
            }
        } catch (IOException e) {
            throw new UserMessage(e);
        }
    }

    public void exportAll(ExportData data) throws IOException {

        SelectionExporter exporter = new SelectionExporter(data,
                new MultipleGraphicsFactory());
        if (getEffectiveTransparency()) {
            exporter.setBackground(new Color(0, 0, 0, 0));
        } else {
            exporter.setBackground(Color.WHITE);
        }
        exporter.setTransparency(getEffectiveTransparency());
        exporter.exportToPages();

    }

    public void exportSelection(ExportData data) throws IOException {

        SelectionExporter exporter = new SelectionExporter(data,
                new SingleGraphicsFactory());
        if (getEffectiveTransparency()) {
            exporter.setBackground(new Color(0, 0, 0, 0));
        } else {
            exporter.setBackground(Color.WHITE);
        }
        exporter.setTransparency(getEffectiveTransparency());
        exporter.exportSelection();
    }

    public File getSingleOutputFile() {
        return new File(directory, fileName);
    }

    /**
     * @return Returns the directory.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public final void setSingleOutputFile(File f) {
        directory = f.getParentFile();
        this.fileName = f.getName();
    }

    /**
     * Generates graphics for files.
     *
     * @author rosmord
     */
    private class SingleGraphicsFactory implements BaseGraphics2DFactory {

        private Dimension2D scaledDimensions;

        public void setDimension(Dimension2D deviceDimensions) {
            scaledDimensions = deviceDimensions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.graphics.export.BaseGraphics2DFactory#buildGraphics(java.io
         * .File, java.awt.Dimension)
         */
        public Graphics2D buildGraphics() throws IOException {
            OutputStream out = new FileOutputStream(getSingleOutputFile());
            BitmapStreamGraphics g = new BitmapStreamGraphics(out,
                    scaledDimensions, OUTPUT_FORMATS[outputFormatIndex],
                    getEffectiveTransparency());
            if (!getEffectiveTransparency()) {
                g.fillWith(backgroundColor);
            }
            return g;
        }

        public void writeGraphics() throws IOException {
            // NO-OP.
        }

        @Override
        public void newPage() throws IOException {
            // NO-OP.
        }

    }

    private boolean getEffectiveTransparency() {
        return outputFormatIndex == 0 && transparency;
    }

    /**
     * Generates graphics for files.
     *
     * @author rosmord
     */
    private class MultipleGraphicsFactory implements BaseGraphics2DFactory {

        private int n = 1;

        private Dimension2D scaledDimensions;

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.graphics.export.BaseGraphics2DFactory#buildGraphics(java.io
         * .File, java.awt.Dimension)
         */
        @Override
        public Graphics2D buildGraphics() throws IOException {
            String num = "" + n;
            n++;
            if (num.length() == 1) {
                num = "00" + num;
            } else if (num.length() == 2) {
                num = "0" + num;
            }
            File f = new File(directory, basename + num + "."
                    + OUTPUT_FORMATS[outputFormatIndex]);
            OutputStream out = new FileOutputStream(f);
            BitmapStreamGraphics g = new BitmapStreamGraphics(out,
                    scaledDimensions, OUTPUT_FORMATS[outputFormatIndex],
                    getEffectiveTransparency());
            if (!getEffectiveTransparency()) {
                g.fillWith(backgroundColor);
            }
            return g;
        }

        @Override
        public void setDimension(Dimension2D scaledDimensions) {
            this.scaledDimensions = scaledDimensions;
        }

        @Override
        public void writeGraphics() throws IOException {
            // NO-OP
        }

        @Override
        public void newPage() throws IOException {
        }

    }

    private class BitmapOptionPanel extends ExportOptionPanel {

        /**
         * The directory where the files will be saved, or the file itself if we
         * don't multipage.
         */
        JFormattedTextField fileField;

        /**
         * A button use for launching a file browser.
         */
        JButton browseButton;

        /**
         * The base name for the file used in case of multipaging.
         */
        // JTextField baseNameField;
        JFormattedTextField baseNameField;

        /**
         * The output format.
         */
        JComboBox outputFormatField;

        /**
         * Height of a cadrat, in pixels.
         */
        JFormattedTextField cadratHeightField;

        /**
         * Should the picture be transparent ?
         */
        JCheckBox transparentField;

        public final int[] FORBIDDEN_CHARS = {':', '/', '.'};

        public BitmapOptionPanel(Component parent, String title) {
            super(parent, title);
            // Directory
            fileField = new JFormattedTextField();
            fileField.setColumns(40);
            if (multiFile) {
                fileField.setValue(directory);
            } else {
                fileField.setValue(getSingleOutputFile());
            }

            // When not in multiFile mode, fileField contains the output file.
            // We see to it that the format is coherent with the file name.
            browseButton = new JButton("Browse");
            browseButton.addActionListener((e) -> browse());

            String fileFieldLabel = "Output file";
            if (multiFile) {
                // Base name.
                baseNameField = new JFormattedTextField(
                        new RestrictedCharFormatter(FORBIDDEN_CHARS));
                baseNameField.setValue(basename);
                baseNameField.setColumns(15);
                baseNameField
                        .setToolTipText("Picture file name will start with this string.");

                fileFieldLabel = "Output directory";
            } else {
                baseNameField = null;
            }

            // Respect pages :
            transparentField = new JCheckBox("Transparent Images", transparency);
            transparentField
                    .setToolTipText("Should the output images have a transparent background ?");

            // line height.
            // NOTE : I use a java 1.4 specific class.
            cadratHeightField = new JFormattedTextField(new java.lang.Integer(
                    cadratHeight));
            cadratHeightField.setColumns(3);
            cadratHeightField
                    .setToolTipText("height of a typical line of hieroglyphs");

            outputFormatField = new JComboBox(OUTPUT_FORMATS);
            outputFormatField.setSelectedIndex(outputFormatIndex);
            outputFormatField
                    .setToolTipText("Format of the saved images.\n Explicit extensions like .jpg or .png will have precedence over this field.");
            outputFormatField.addActionListener((e) -> selectOutputFormat());
            prepareLayout(fileFieldLabel);
        }

        private void prepareLayout(String fileFieldLabel) {
            FormLayout formLayout = new FormLayout(
                    "right:p,4dlu,left:max(40dlu;pref),4dlu,left:max(20dlu;pref),4dlu,left:max(20dlu;pref)",
                    "");

            DefaultFormBuilder formBuilder = new DefaultFormBuilder(formLayout,
                    this);

            formBuilder.setDefaultDialogBorder();
            formBuilder.append("Cadrat Height", cadratHeightField);
            formBuilder.append(transparentField);
            formBuilder.nextLine();
            formBuilder.appendSeparator("Output");
            formBuilder.nextLine();
            formBuilder.append(fileFieldLabel, fileField, 3);
            formBuilder.append(browseButton);
            formBuilder.nextLine();
            if (multiFile) {
                formBuilder.append("Base name for output files", baseNameField);
            }
            formBuilder.append("Output format", outputFormatField);
        }

        private void selectOutputFormat() {
            if (!multiFile) {
                File f = (File) fileField.getValue();
                f = FileUtils.buildFileWithExtension(f,
                        (String) outputFormatField.getSelectedItem());
                fileField.setValue(f);
            }
        }

        private void browse() {
            if (!multiFile) {
                browseSingleFile();
            } else {
                browseMultiFile();
            }
        }

        private void browseSingleFile() {
            FileOperationResult userResult;
            PortableFileDialog chooser = PortableFileDialogFactory.createFileSaveDialog(parent);
            chooser.setSelectedFile((File) fileField.getValue());

            // Select only our formats.
            chooser.setFileFilter(new FileFilterImpl());

            chooser.setSelectedFile((File) fileField.getValue());
            // Display.
            userResult = chooser.show();
            if (userResult == FileOperationResult.OK) {
                fileField.setValue(chooser.getSelectedFile());
                fixExtension();
            }
        }

        private void browseMultiFile() {
            FileOperationResult userResult;
            PortableFileDialog chooser = PortableFileDialogFactory.createDirectorySaveDialog(parent);
            chooser.setSelectedFile((File) fileField.getValue());
            chooser.setTitle("Choose output directory"); // TODO : I18N
            userResult = chooser.show();
            if (userResult == FileOperationResult.OK) {
                fileField.setValue(chooser.getSelectedFile());
            }
        }

        private void fixExtension() {
            if (!multiFile) {
                boolean mustCorrectExtension = true;
                File file = (File) fileField.getValue();
                String ext = FileUtils.getExtension(file);
                if (ext != null) {
                    List<String> l = Arrays.asList(OUTPUT_FORMATS);
                    if (l.contains(ext)) {
                        // Known extensions
                        mustCorrectExtension = false;
                        // Get sure the "selected format" agrees with the
                        // extensions :
                        outputFormatField.setSelectedItem(ext);
                    }
                }
                // In case of missing or incorrect extensions, provide a new one.
                if (mustCorrectExtension) {
                    String correctFormat = (String) outputFormatField
                            .getSelectedItem();
                    file = FileUtils
                            .buildFileWithExtension(file, correctFormat);
                    fileField.setValue(file);
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.graphics.export.ExportOptionPanel#setOptions()
         */
        @Override
        public void setOptions() {
            if (multiFile) {
                directory = (File) fileField.getValue();
                basename = (String) baseNameField.getValue();
            } else {
                setSingleOutputFile((File) fileField.getValue());
            }
            transparency = transparentField.isSelected();
            cadratHeight = ((Number) cadratHeightField.getValue()).intValue();
            outputFormatIndex = outputFormatField.getSelectedIndex();
        }

        private class FileFilterImpl extends FileFilter {

            public FileFilterImpl() {
            }

            public boolean accept(File f) {
                boolean result = false;
                result = f.isDirectory();
                int i = 0;
                while (!result && i < OUTPUT_FORMATS.length) {
                    result = result
                            || f.getName()
                                    .toLowerCase()
                                    .endsWith("." + OUTPUT_FORMATS[i]);
                    i++;
                }
                return result;
            }

            public String getDescription() {
                String s = OUTPUT_FORMATS[0];

                for (int i = 1; i < OUTPUT_FORMATS.length; i++) {
                    s += ", " + OUTPUT_FORMATS[i];
                }
                return s;
            }
        }
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Saves the current preferences. TODO : create a more robust preferences
     * system.
     * <p>
     * Some preferences should be shared.
     * <p>
     * There should be preferences objects, with a way to see them all. later.
     */
    public void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.putBoolean("bitmap.multiFile", multiFile);
        if (multiFile) {
            JSeshWorkingDirectory.setWorkingDirectory(directory);
            prefs.put("bitmap.baseName", basename);
        } else {
            prefs.put("bitmap.file", getSingleOutputFile().getAbsolutePath());
            JSeshWorkingDirectory.setWorkingDirectory(getSingleOutputFile()
                    .getParentFile());
        }
        prefs.putBoolean("bitmap.transparent", transparency);
        prefs.putInt("bitmap.cadratHeight", cadratHeight);
        prefs.putInt("bitmap.outputFormatIndex", outputFormatIndex);
    }

    /**
     * Initialize the preferences using saved values.
     */
    public final void initFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        transparency = prefs.getBoolean("bitmap.transparent", true);
        cadratHeight = prefs.getInt("bitmap.cadratHeight", 18);
        outputFormatIndex = prefs.getInt("bitmap.outputFormatIndex", 0);
        if (outputFormatIndex >= OUTPUT_FORMATS.length) {
            outputFormatIndex = 0;
        }

        multiFile = prefs.getBoolean("bitmap.multiFile", false);
        directory = JSeshWorkingDirectory.getWorkingDirectory();
        if (multiFile) {
            basename = prefs.get("bitmap.baseName", "unnamed");
        } else {
            File defaultOutput = new File(directory, "unnamed" + "." + OUTPUT_FORMATS[outputFormatIndex]);
            setSingleOutputFile(new File(prefs.get("bitmap.file", defaultOutput.getAbsolutePath())));
        }
    }
}
