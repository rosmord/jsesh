package org.qenherkhopeshef.swingUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

/**
 * Demo and test software for this dialog. Needed because we want to try all
 * options on all platforms.
 * 
 * @author rosmord
 * 
 */
class TestPortableFileDialog {

	private JFrame frame;
	private File currentFile;

	public File getCurrentDir() {
		File currentDir = new File(System.getProperty("user.dir"));
		if (currentFile != null)
			currentDir = currentFile.getParentFile();
		return currentDir;
	}

	public TestPortableFileDialog() {
		frame = new JFrame();
		JToolBar bar = new JToolBar();
		JButton buttons[] = { new JButton("save file (swing)"),
				new JButton("save file (awt)"),
				new JButton("save file (default)"),
				new JButton("open file (swing)"),
				new JButton("open file (awt)"),
				new JButton("open file (default)"),
				new JButton("save directory (swing)"),
				new JButton("save directory (awt)"),
				new JButton("save directory (default)") };
		bar.setOrientation(JToolBar.HORIZONTAL);
		for (JButton b : buttons) {
			bar.add(b);
			b.addActionListener(new ActionListener() {
				FileFilter textFilters[] = {
						new FileExtensionFilter(new String[] { "txt", "html" },
								"text files"),
						new FileExtensionFilter("doc", "word files") };

				@Override
				public void actionPerformed(ActionEvent e) {
					JButton b = (JButton) e.getSource();
					File result = null;
					if (b.getText().contains("swing")) {
						SwingFileDialog dialog = new SwingFileDialog(frame);
						dialog.setCurrentDirectory(getCurrentDir());
						dialog.setSelectedFile(currentFile);
						if (!b.getText().contains("directory")) {
							dialog.setFileFilters(textFilters);
						}
						if (b.getText().contains("save file")) {
							dialog.setOperation(FileOperation.SAVE_FILE);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						} else if (b.getText().contains("open file")) {
							dialog.setOperation(FileOperation.OPEN_FILE);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						} else if (b.getText().contains("save directory")) {
							dialog.setOperation(FileOperation.SAVE_DIRECTORY);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						}
						if (result != null)
							currentFile = result;
						dialog.dispose();
					} else if (b.getText().contains("awt")) {
						AwtPortableFileDialog dialog = new AwtPortableFileDialog(
								frame);
						dialog.setCurrentDirectory(getCurrentDir());
						dialog.setSelectedFile(currentFile);
						if (!b.getText().contains("directory")) {
							dialog.setFileFilters(textFilters);
						}
						if (b.getText().contains("save file")) {
							dialog.setOperation(FileOperation.SAVE_FILE);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						} else if (b.getText().contains("open file")) {
							dialog.setOperation(FileOperation.OPEN_FILE);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						} else if (b.getText().contains("save directory")) {
							dialog.setOperation(FileOperation.SAVE_DIRECTORY);
							FileOperationResult opResult = dialog.show();
							if (opResult == FileOperationResult.OK) {
								result = dialog.getSelectedFile();
							}
						}
						dialog.dispose();

					} else {
						// Normal system : let the factory decide...
						PortableFileDialog dialog;
						// The code is a bit longer, but it's not the normal use
						// case...
						if (b.getText().contains("save file")) {
							dialog = PortableFileDialogFactory
									.createFileSaveDialog(frame);
						} else if (b.getText().contains("open file")) {
							dialog = PortableFileDialogFactory
									.createFileOpenDialog(frame);
						} else // if (b.getText().contains("save directory"))
						{
							dialog = PortableFileDialogFactory
									.createDirectorySaveDialog(frame);
						}
						dialog.setCurrentDirectory(getCurrentDir());
						dialog.setSelectedFile(currentFile);
						if (!b.getText().contains("directory")) {
							dialog.setFileFilters(textFilters);
						}
						FileOperationResult opResult = dialog.show();
						if (opResult == FileOperationResult.OK) {
							result = dialog.getSelectedFile();
						}
						dialog.dispose();
						if (result != null)
							currentFile = result;
					}

					JOptionPane.showMessageDialog(null, "selected " + result);
				}
			});
		}
		frame.add(bar);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Tests the various dialogs.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new TestPortableFileDialog();
	}
}
