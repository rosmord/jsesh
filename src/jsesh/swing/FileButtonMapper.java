package jsesh.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Binds a button to a JTextField displaying a file (or directory) name.
 * 
 * @author rosmord
 * 
 */
public class FileButtonMapper implements ActionListener {

	public static final int OPEN = 1;

	public static final int SAVE = 2;

	private JButton button;

	private JFormattedTextField fileField;

	private int mode;

	private int fileSelectionMode;

	private List fileFilters;
	
	private String dialogTitle;
	
	private String approveButtonLabel;

	/**
	 * Binds a "browse" button to a file field.
	 * <p>
	 * The button will normally open a dialog for selecting an output file.
	 * 
	 * @param button
	 * @param field
	 *            a formatted text field suitable to display directories.
	 * @param mode
	 *            OPEN or SAVE
	 */
	public FileButtonMapper(JButton button, JFormattedTextField field) {
		super();
		// TODO Auto-generated constructor stub
		this.button = button;
		fileField = field;
		button.addActionListener(this);
		this.mode = SAVE;
		this.fileSelectionMode = JFileChooser.FILES_ONLY;
		fileFilters = null;
		dialogTitle= null;
		approveButtonLabel= null;

	}

	/**
	 * If this method is called, the system will ask if the corresponding
	 * directory must be created.
	 * 
	 */
	public void askForCreation() {
		fileField.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {			
				if ("value".equals(evt.getPropertyName())) {
					File f = (File) fileField.getValue();
					if (f == null)
						return;
					if (!f.exists()) {
						int result = JOptionPane
								.showConfirmDialog(
										button,
										"The directory "
												+ f.getAbsolutePath()
												+ " does not exist. Should I Create it ?",
										"Directory does not exist",
										JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							f.mkdirs();
						}
					}
				}

			}

		});
	}

	public void actionPerformed(ActionEvent e) {
		File f;

		try {
			f = (File) fileField.getValue();
		} catch (ClassCastException exc) {
			f = new File(fileField.getText());
		}

		JFileChooser chooser;
		
		chooser = new JFileChooser(f) {
			
		};
		chooser.setFileSelectionMode(fileSelectionMode);
	
		if (getDialogTitle() != null)
			chooser.setDialogTitle(getDialogTitle());
		
		if (getApproveButtonLabel() != null)
			chooser.setApproveButtonText(getApproveButtonLabel());
		
		
		if (fileFilters != null) {
			for (int i = 0; i < fileFilters.size(); i++)
				chooser.addChoosableFileFilter((FileFilter) fileFilters.get(i));
		}
		int result;
		if (mode == OPEN)
			result = chooser.showOpenDialog(button);
		else
			result = chooser.showSaveDialog(button);
		if (result == JFileChooser.APPROVE_OPTION) {
			fileField.setValue(chooser.getSelectedFile());
		}

	}

	/**
	 * @return Returns the fileSelectionMode.
	 */
	public int getFileSelectionMode() {
		return fileSelectionMode;
	}

	/**
	 * @param fileSelectionMode
	 *            one of JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY
	 *            or JFileChooser.FILES_AND_DIRECTORIES.
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
	 * @param mode
	 *            one of OPEN or SAVE
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public void addFileFilter(FileFilter filter) {
		if (fileFilters == null)
			fileFilters = new ArrayList();
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
