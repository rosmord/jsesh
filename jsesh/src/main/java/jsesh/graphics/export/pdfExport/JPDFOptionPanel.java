/**
 * 
 */
package jsesh.graphics.export.pdfExport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import jsesh.graphics.export.ExportOptionPanel;
import jsesh.utils.FileUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class JPDFOptionPanel extends ExportOptionPanel implements
		ActionListener {

	private static final long serialVersionUID = -1571634707470234047L;

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

	JButton browse;

	JPDFOptionPanel(Component parent, String paneltitle, PDFExportPreferences exportPreferences) {
		super(parent, paneltitle);
		this.exportPreferences= exportPreferences;

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
		browse = new JButton("Browse");
		browse.addActionListener(this);

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

		encapsulatedBox= new JCheckBox("Encapsulated PDF", exportPreferences.isEncapsulated());
		
		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// GridBagLayout layout = new GridBagLayout();
		FormLayout layout = new FormLayout(
				// "right:p,4dlu,p,4dlu,p,4dlu,p,p,p,p,p,p,p",
				"right:p,4dlu,left:max(40dlu;pref),4dlu,left:max(20dlu;pref)",
				"");

		DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);

		formBuilder.setDefaultDialogBorder();
		formBuilder.append("Document Title", titleField, 3);
		formBuilder.nextLine();
		formBuilder.append("Author", authorField, 3);
		formBuilder.append("Subject", subjectField, 3);
		formBuilder.append("Keywords", keywordsField, 3);

		formBuilder.append("Line Height", lineHeightField);
		lineHeightField
				.setToolTipText("height of standard hieroglyphic cadrats, in points");
		formBuilder.nextLine();
		// formBuilder.appendRow(new RowSpec(""));
		formBuilder.append("Page Format", formatField, 1);
		formBuilder.nextLine();
		formBuilder.append(showPageNumberBox);
		formBuilder.nextLine();
		formBuilder.append(wysiwygBox);
		formBuilder.nextLine();
		formBuilder.append(encapsulatedBox);
		formBuilder.nextLine();
		formBuilder.appendSeparator("Output");
		formBuilder.nextLine();
		formBuilder.append("File", fileField, browse);

		setLayout(new BorderLayout());
		add(formBuilder.getPanel(), BorderLayout.CENTER);

	}

        @Override
	public void setOptions() {
		exportPreferences.setFile(new File(fileField.getText()));
		exportPreferences.setTitle(titleField.getText());
		exportPreferences.setAuthor(authorField.getText());
		exportPreferences.setSubject(subjectField.getText());		
		exportPreferences.setKeywords(keywordsField.getText());
		exportPreferences.setLineHeight(Integer.parseInt(lineHeightField.getText()));
		exportPreferences.setPageSize( (String) formatField.getSelectedItem());
		exportPreferences.setRespectPages(true);
		exportPreferences.setShowPageNumbers(showPageNumberBox.isSelected());
		exportPreferences.setRespectTextLayout(wysiwygBox.isSelected());
		exportPreferences.setEncapsulated(encapsulatedBox.isSelected());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
	 * )
	 */
        @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browse) {
			JFileChooser chooser = new JFileChooser();
			chooser.setSelectedFile(exportPreferences.getFile());
			chooser.setApproveButtonText("Choose file");
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
			int res = chooser.showSaveDialog(this);
			if (res == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				selected = FileUtils
						.buildFileWithExtension(selected, "pdf");
				fileField.setValue(selected);
			}
		}
	}
}