package org.qenherkhopeshef.swingUtils.portableFileDialog;

import java.awt.Frame;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Factory for creating portable file dialogs depending on needs.
 * 
 * @author rosmord
 * 
 */
public class PortableFileDialogFactory {

	/**
	 * Create a dialog for selecting a file for saving data.
	 */
	public static PortableFileDialog createFileSaveDialog(Frame parent) {
		PortableFileDialog dialog= createDialog(parent);		
		dialog.setOperation(FileOperation.SAVE_FILE);
		return dialog;
	}

	private static PortableFileDialog createDialog(Frame parent) {
		PortableFileDialog dialog;
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			dialog = new AwtPortableFileDialog(parent);
		} else 
			dialog= new SwingFileDialog(parent);
		return dialog;
	}
	

	/**
	 * Create a dialog for selecting a file for reading data.
	 */
	public static PortableFileDialog createFileOpenDialog(Frame parent) {
		PortableFileDialog dialog= createDialog(parent);		
		dialog.setOperation(FileOperation.OPEN_FILE);
		return dialog;
	}

	
	/**
	 * Create a dialog for selecting a directory for saving data. The directory
	 * might or might not exist yet.
	 * 
	 * @return
	 */
	public static PortableFileDialog createDirectorySaveDialog(Frame parent) {
		PortableFileDialog dialog= createDialog(parent);		
		dialog.setOperation(FileOperation.SAVE_DIRECTORY);
		return dialog;		
	}

}
