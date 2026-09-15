package jsesh.ui.export.pdfExport;

import java.awt.Component;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import jsesh.ui.export.generic.ExportOptionPanel;
import jsesh.utils.io.FileUtils;

import net.miginfocom.swing.MigLayout;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

public class JPDFOptionPanel extends ExportOptionPanel {

    PDFExportPreferences exportPreferences;

    JFormattedTextField fileField;

    JTextField titleField;
    JTextField authorField;
    JTextField subjectField;
    JTextField keywordsField;

    JFormattedTextField lineHeightField;

    JCheckBox showPageNumberBox;

    JCheckBox wysiwygBox;

    JCheckBox encapsulatedBox;

    JComboBox formatField;

    JButton browseButton;

    JPDFOptionPanel(Component parent, String paneltitle, PDFExportPreferences exportPreferences) {
        super(parent, paneltitle);
        this.exportPreferences = exportPreferences;

        // Title :
        titleField = new JTextField(exportPreferences.getTitle(), 40);
        titleField.setToolTipText("Title of the pdf document");

        authorField = new JTextField(40);
        authorField.setText(exportPreferences.getAuthor());

        subjectField = new JTextField(40);

        keywordsField = new JTextField(40);

        // Directory
        fileField = new JFormattedTextField();
        fileField.setValue(exportPreferences.getFile());
        fileField.setColumns(40);
        fileField.setEditable(false);
        browseButton = new JButton("Browse");
        browseButton.addActionListener((e)-> browse());

        // line height.
        // NOTE : I use a java 1.4 specific class.
        lineHeightField = new JFormattedTextField();
        lineHeightField.setValue(exportPreferences
                .getLineHeight());
        lineHeightField
                .setToolTipText("height of a typical line of hieroglyphs");

        // Page dimensions
        formatField = new JComboBox(exportPreferences.getPageFormats()
                .keySet().toArray());
        formatField.setSelectedItem(exportPreferences.getPageSize());

        showPageNumberBox = new JCheckBox("Show page numbers",
                exportPreferences.isShowPageNumbers());

        wysiwygBox = new JCheckBox("Respect text layout",
                exportPreferences.isRespectTextLayout());

        encapsulatedBox = new JCheckBox("Encapsulated PDF", exportPreferences.isEncapsulated());

        setLayout(new MigLayout("insets dialog, wrap 3", "[right][grow][]"));

        add(new JLabel("Document Title"));
        add(titleField, "span 2, growx");

        add(new JLabel("Author"));
        add(authorField, "span 2, growx");

        add(new JLabel("Subject"));
        add(subjectField, "span 2, growx");

        add(new JLabel("Keywords"));
        add(keywordsField, "span 2, growx");

        lineHeightField
                .setToolTipText("height of standard hieroglyphic cadrats, in points");
        add(new JLabel("Line Height"));
        add(lineHeightField, "span 2, growx");

        add(new JLabel("Page Format"));
        add(formatField, "span 2, growx");

        add(showPageNumberBox, "span 3");

        add(wysiwygBox, "span 3");

        add(encapsulatedBox, "span 3");

        add(new JLabel("Output"), "split 2, span 3, gaptop unrelated");
        add(new JSeparator(), "growx, wrap");

        add(new JLabel("File"));
        add(fileField, "growx");
        add(browseButton);
    }

    @Override
    public void setOptions() {
        exportPreferences.setFile(new File(fileField.getText()));
        exportPreferences.setTitle(titleField.getText());
        exportPreferences.setAuthor(authorField.getText());
        exportPreferences.setSubject(subjectField.getText());
        exportPreferences.setKeywords(keywordsField.getText());
        exportPreferences.setLineHeight(Integer.parseInt(lineHeightField.getText()));
        exportPreferences.setPageSize((String) formatField.getSelectedItem());
        exportPreferences.setRespectPages(true);
        exportPreferences.setShowPageNumbers(showPageNumberBox.isSelected());
        exportPreferences.setRespectTextLayout(wysiwygBox.isSelected());
        exportPreferences.setEncapsulated(encapsulatedBox.isSelected());
    }

    public void browse() {
        PortableFileDialog chooser = PortableFileDialogFactory.createFileSaveDialog(this);
        chooser.setSelectedFile(exportPreferences.getFile());
        //chooser.setApproveButtonText("Choose file"); // see if we can do something for this.
        chooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "pdf files";
            }

            @Override
            public boolean accept(File f) {
                return (f.getName().endsWith(".pdf") || f.getName()
                        .endsWith(".PDF"))
                        || f.isDirectory();
            }
        });
        FileOperationResult res = chooser.show();
        if (res == FileOperationResult.OK) {
            File selected = chooser.getSelectedFile();
            selected = FileUtils
                    .buildFileWithExtension(selected, "pdf");
            fileField.setValue(selected);
        }
    }
}
