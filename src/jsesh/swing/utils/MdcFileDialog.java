package jsesh.swing.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import jsesh.swing.HiddenPanel;


public class MdcFileDialog {
	private JFileChooser fileChooser;
	private MdcFileOptionPanel mdcFileOptionPanel;
	
	public MdcFileDialog(File currentDirectory, String dialogTitle) {
		fileChooser= new JFileChooser();
		fileChooser.setDialogTitle(dialogTitle);
		FileFilter filter = new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".hie")
						|| pathname.getName().endsWith(".HIE")
						|| pathname.getName().endsWith(".gly")
						|| pathname.getName().endsWith(".GLY")
						|| pathname.isDirectory();
			}

			public String getDescription() {
				return "Manuel de codage files";
			}
		};

		buildPanel();
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setCurrentDirectory(currentDirectory);
	}
	
	private void buildPanel() {
	
		mdcFileOptionPanel= new MdcFileOptionPanel();
		
		HiddenPanel accessoryPanel= new HiddenPanel("show advanced >>","<< hide advanced", mdcFileOptionPanel);
		
		fileChooser.setAccessory(accessoryPanel);
	}

	
	public int showOpenDialog(Component parent) {
		int result = fileChooser.showOpenDialog(parent);
		return result;
	}

	public int showSaveDialog(Component parent) {
		return fileChooser.showSaveDialog(parent);
	}
	
	public File getCurrentDirectory() {
		return fileChooser.getCurrentDirectory();
	}

	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}
	
	
	
	public String getEncoding() {
		String encoding= mdcFileOptionPanel.getEncoding();
		if (encoding == null || "Default".equals(encoding)) {
			// jdk 1.5 proposes a defaultCharSet() method.
			// encoding= SystemUtils.getDefaultEncoding();
			encoding= null; // we use "null" for default value.
		} 
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		mdcFileOptionPanel.setEncoding(encoding);
	}

	/**
	 * Sets the selected file or directory.
	 * @param file
	 * @see javax.swing.JFileChooser#setSelectedFile(java.io.File)
	 */
	public void setSelectedFile(File file) {
		fileChooser.setSelectedFile(file);
	}
	
}
