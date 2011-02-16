/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphics.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.swing.RestrictedCharFormatter;
import jsesh.utils.FileUtils;
import jsesh.utils.JSeshWorkingDirectory;

import org.qenherkhopeshef.graphics.bitmaps.BitmapStreamGraphics;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Export to graphic bitmaps.
 * 
 * @author Serge Rosmorduc
 * 
 *         This file is free software under the Gnu Lesser Public License.
 */
public class BitmapExporter {

	private Component parent;

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
	public static final String outputFormats[] = { "png", "jpg" };

	/**
	 * Create a new bitmap exporter.
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
	 * @param onlySelection
	 *            if true, we will prepare to export only the selection.
	 * @return one of JOptionPane.CANCEL_OPTION or JOptionPane.OK_OPTION,
	 *         depending on user input.
	 */
	public int askUser(boolean onlySelection) {
		int result = JOptionPane.CANCEL_OPTION;

		this.multiFile = !onlySelection;

		BitmapOptionPanel panel = new BitmapOptionPanel(parent,
				"Export as bitmap file");
		result = panel.askAndSet();
		if (result == JOptionPane.CANCEL_OPTION)
			return result;

		return result;
	}

	public void export(ExportData data) {
		try {
			double length = data.getDrawingSpecifications()
					.getHieroglyphsDrawer().getHeightOfA1();
			data.setScale(this.cadratHeight / length);
			if (multiFile)
				exportAll(data);
			else
				exportSelection(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exportAll(ExportData data) throws IOException {

		SelectionExporter exporter = new SelectionExporter(data,
				new MultipleGraphicsFactory());
		if (getEffectiveTransparency())
			exporter.setBackground(new Color(0, 0, 0, 0));
		else
			exporter.setBackground(Color.WHITE);
		exporter.setTransparency(getEffectiveTransparency());
		exporter.exportToPages();

	}

	public void exportSelection(ExportData data) throws IOException {

		SelectionExporter exporter = new SelectionExporter(data,
				new SingleGraphicsFactory());
		if (getEffectiveTransparency())
			exporter.setBackground(new Color(0, 0, 0, 0));
		else
			exporter.setBackground(Color.WHITE);
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
	 * @param directory
	 *            The directory to set.
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public void setSingleOutputFile(File f) {
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
					scaledDimensions, outputFormats[outputFormatIndex],
					getEffectiveTransparency());
			if (!getEffectiveTransparency())
				g.fillWith(backgroundColor);
			return g;
		}

		public void writeGraphics() throws IOException {
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
		public Graphics2D buildGraphics() throws IOException {
			String num = "" + n;
			n++;
			if (num.length() == 1)
				num = "00" + num;
			else if (num.length() == 2)
				num = "0" + num;
			File f = new File(directory, basename + num + "."
					+ outputFormats[outputFormatIndex]);
			OutputStream out = new FileOutputStream(f);
			BitmapStreamGraphics g = new BitmapStreamGraphics(out,
					scaledDimensions, outputFormats[outputFormatIndex],
					getEffectiveTransparency());
			if (!getEffectiveTransparency())
				g.fillWith(backgroundColor);
			return g;
		}

		public void setDimension(Dimension2D scaledDimensions) {
			this.scaledDimensions = scaledDimensions;
		}

		public void writeGraphics() throws IOException {
			// NO-OP
		}
	}

	private class BitmapOptionPanel extends ExportOptionPanel implements
			ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8713249612816729039L;

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

		public final int[] FORBIDDEN_CHARS = { ':', '/', '.' };

		public BitmapOptionPanel(Component parent, String title) {
			super(parent, title);
			// Directory
			fileField = new JFormattedTextField();
			fileField.setColumns(40);
			if (multiFile)
				fileField.setValue(directory);
			else
				fileField.setValue(getSingleOutputFile());

			// When not in multiFile mode, fileField contains the output file.
			// We see to it that the format is coherent with the file name.

			browseButton = new JButton("Browse");
			browseButton.addActionListener(this);

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

			outputFormatField = new JComboBox(outputFormats);
			outputFormatField.setSelectedIndex(outputFormatIndex);
			outputFormatField
					.setToolTipText("Format of the saved images.\n Explicit extensions like .jpg or .png will have precedence over this field.");
			outputFormatField.addActionListener(this);
			// Layout :
			// We will try to be a bit consistent here (in the future, and to
			// adapt this for older dialogues)
			// General stuff about rendering will be in the upper part. File
			// related stuff will be in a lower part.
			// cadratHeight transparent
			// file... browse
			// (basename) outputformat

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
			if (multiFile)
				formBuilder.append("Base name for output files", baseNameField);
			formBuilder.append("Output format", outputFormatField);
			// setLayout(new BorderLayout());
			// add(formBuilder.getPanel(), BorderLayout.CENTER);

		}

		/*
		 * (non-Javadoc) called when using the "browse button".
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == browseButton) {
				int userResult;
				JFileChooser chooser = new JFileChooser(
						(File) fileField.getValue());

				if (!multiFile) {
					// Select only our formats.
					chooser.addChoosableFileFilter(new FileFilter() {

						public boolean accept(File f) {
							boolean result = false;
							result = f.isDirectory();
							int i = 0;
							while (!result && i < outputFormats.length) {
								result = result
										|| f.getName()
												.toLowerCase()
												.endsWith(
														"." + outputFormats[i]);
								i++;
							}
							return result;
						}

						public String getDescription() {
							String s = outputFormats[0];

							for (int i = 1; i < outputFormats.length; i++)
								s += ", " + outputFormats[i];
							return s;
						}
					});

					chooser.setSelectedFile((File) fileField.getValue());
					// Display.
					userResult = chooser.showSaveDialog(null);
					if (userResult == JFileChooser.APPROVE_OPTION) {
						fileField.setValue(chooser.getSelectedFile());
						fixExtension();
					}

				} else {
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					userResult = chooser.showDialog(null,
							"Choose output directory");
					if (userResult == JFileChooser.APPROVE_OPTION)
						fileField.setValue(chooser.getSelectedFile());
				}
			} else if (e.getSource() == outputFormatField) {
				if (!multiFile) {
					File f = (File) fileField.getValue();
					f = FileUtils.buildFileWithExtension(f,
							(String) outputFormatField.getSelectedItem());
					fileField.setValue(f);
				}
			}
		}

		private void fixExtension() {
			if (!multiFile) {
				boolean mustCorrectExtension = true;
				File file = (File) fileField.getValue();
				String ext = FileUtils.getExtension(file);
				if (ext != null) {
					List<String> l = Arrays.asList(outputFormats);
					if (l.contains(ext)) {
						// Known extension
						mustCorrectExtension = false;
						// Get sure the "selected format" agrees with the
						// extension :
						outputFormatField.setSelectedItem(ext);
					}
				}
				// In case of missing or incorrect extension, provide a new one.
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
	public void initFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		transparency= prefs.getBoolean("bitmap.transparent", true);
		cadratHeight= prefs.getInt("bitmap.cadratHeight", 18);
		outputFormatIndex= prefs.getInt("bitmap.outputFormatIndex", 0);
		if (outputFormatIndex>= outputFormats.length)
			outputFormatIndex= 0;

		multiFile = prefs.getBoolean("bitmap.multiFile", false);
		directory = JSeshWorkingDirectory.getWorkingDirectory();
		if (multiFile) {
			basename = prefs.get("bitmap.baseName", "unnamed");
		} else {
			File defaultOutput= new File(directory, "unnamed" + "."+ outputFormats[outputFormatIndex]);
			setSingleOutputFile(new File(prefs.get("bitmap.file", defaultOutput.getAbsolutePath())));
		}
	}
}
