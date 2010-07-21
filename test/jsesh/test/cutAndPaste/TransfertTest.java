package jsesh.test.cutAndPaste;

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Technical test for java cut and paste capabilities. Test the possibility for
 * Java to copy typed data on the clipboard, and to retrieve the data from the
 * clipboard too.
 * 
 * @author rosmord
 */

public class TransfertTest implements ClipboardOwner {
	/**
	 * Select the type of data to copy.
	 */
	JComboBox transfertTypeCB = new JComboBox();
	/**
	 * Select the file which contains the data to copy.
	 */
	JButton fileSelectButton = new JButton("Select file");
	/**
	 * Copy data.
	 */
	JButton copyButton = new JButton("copy");
	/**
	 * Sends a java image to the clipboard.
	 */
	JButton imageCopyButton = new JButton("Image copy");

	/**
	 * Retrieve data from the clipboard. Data is displayed in the messageArea,
	 * and can be saved afterward.
	 */
	JButton pasteButton = new JButton("Paste");

	/**
	 * Save pasted data in a file.
	 */
	JButton saveButton = new JButton("Save");

	JButton listButton = new JButton("List Clipboard data types");

	JButton resetButton = new JButton("Reset Flavors");

	JTextArea messageArea = new JTextArea(20, 80);
	/**
	 * Saved data.
	 */
	byte[] data;
	private JFrame frame;
	private File copyFile;

	public TransfertTest() {
		enableUI();
		layout();
	}

	private void layout() {
		frame = new JFrame("Copy and Paste tests");
		Container content = frame.getContentPane();

		JPanel buttonsPanel = new JPanel();
		buttonsPanel
				.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));
		buttonsPanel.add(transfertTypeCB);
		buttonsPanel.add(fileSelectButton);
		buttonsPanel.add(copyButton);
		buttonsPanel.add(imageCopyButton);
		buttonsPanel.add(pasteButton);
		buttonsPanel.add(saveButton);
		buttonsPanel.add(listButton);
		buttonsPanel.add(resetButton);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				buttonsPanel, new JScrollPane(messageArea));
		content.add(splitPane);
		splitPane.resetToPreferredSizes();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void enableUI() {

		resetFlavors();

		transfertTypeCB.setToolTipText("<html>Format for cutting or pasting data<br>Either select one or enter a mime type.");

		listButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listClipboard();
			}
		});

		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetFlavors();
			}
		});

		pasteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		
		fileSelectButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
			selectCopyFile();
			}});
		
		copyButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
			copy();
		}});
	}

	protected void copy() {
		if (copyFile == null) return;
		Clipboard clipboard  = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new MyTransfert(), this);
	}

	protected void selectCopyFile() {
		FileDialog fileDialog= new FileDialog(frame);
		fileDialog.setMode(FileDialog.LOAD);
		if (copyFile== null) {
			fileDialog.setDirectory(".");
		} else {
			fileDialog.setDirectory(copyFile.getParent());
			fileDialog.setFile(copyFile.getName());
		}
		fileDialog.setVisible(true);
		String result = fileDialog.getFile();
		if (result != null) {
			copyFile= new File(new File(fileDialog.getDirectory()), result);
		}
	}

	
	protected void save() {
		FileDialog fileDialog= new FileDialog(frame);
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setVisible(true);
		String file = fileDialog.getFile();
		try {
			if (file != null) {
				File directory = new File(fileDialog.getDirectory());
				File outFile= new File(directory, file);
				FileOutputStream out= new FileOutputStream(outFile);
				out.write(data);
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private DataFlavor getSelectedFlavor() {
		Object selected = transfertTypeCB.getSelectedItem();
		DataFlavor selectedFlavor;
		if (selected instanceof DataFlavor) {
			selectedFlavor = (DataFlavor) selected;
		} else {
			try {
				selectedFlavor = new DataFlavor(selected.toString());		
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(transfertTypeCB,
						"Sorry can't make " + selected + " into a flavour.",
						"Error", JOptionPane.ERROR_MESSAGE);
				throw new RuntimeException(e);
			}
		}
		return selectedFlavor;
	}

	/**
	 * Paste the data available on the clipboard.
	 */
	protected void paste() {
		DataFlavor flavor = getSelectedFlavor();
		Clipboard clipboard  = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable content = clipboard.getContents(this);
		try {
			messageArea.setText("");
			messageArea.append("Primary "+ flavor.getPrimaryType());
			messageArea.append(" Secondary "+ flavor.getSubType());
			messageArea.append("\n");
			Object transfertData = content.getTransferData(flavor);
			messageArea.append(transfertData.getClass().toString()+ "\n"+ transfertData.toString());
			if (flavor.isFlavorSerializedObjectType()) {
				pasteSerializedObject(transfertData);
			} else if (flavor.isFlavorJavaFileListType()) {
				pasteFileList(transfertData);
			} else if (flavor.isRepresentationClassInputStream()) {
				pasteInputStream(transfertData);
			} else if (flavor.isRepresentationClassByteBuffer()) {
				pasteByteBuffer((ByteBuffer)transfertData);
			} else if (flavor== DataFlavor.stringFlavor) {
				
			}	
		} catch (Exception e) {
			messageArea.setText(e.getMessage());
			e.printStackTrace();
		}
	}

	private void pasteByteBuffer(ByteBuffer buffer) {
		messageArea.append("Byte Array.... todo");
	}

	private void pasteFileList(Object transfertData) {
		List files= (List) transfertData;
		messageArea.append("\nFile list\n\n");
		for (int i= 0; i < files.size(); i++) {
			File file = (File) files.get(i);
			messageArea.append(file.getAbsolutePath()+ "\n");
		}
	}

	private void pasteInputStream(Object transfertData) throws IOException {
		messageArea.append("\nStream of data\n\n");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in= (InputStream) transfertData;
		int c;
		while ((c= in.read())!= -1) {
			out.write(c);
			messageArea.append(""+(char)c);
		}
		in.close();
		out.close();
		data= out.toByteArray();
	}

	private void pasteSerializedObject(Object transfertData) {
		messageArea.append("Serialized object \n");
		messageArea.append(transfertData.toString());
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream= new ObjectOutputStream(out);
			objectOutputStream.writeObject(transfertData);
			objectOutputStream.close();
			data= out.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void resetFlavors() {
		DefaultComboBoxModel dataflavorList = new DefaultComboBoxModel();

		dataflavorList.addElement("YOUR OWN TYPE");
		dataflavorList.addElement(DataFlavor.stringFlavor);
		dataflavorList.addElement(DataFlavor.imageFlavor);

		dataflavorList.addElement(new DataFlavor("application/pdf", "PDF"));
		dataflavorList.addElement(new DataFlavor("application/x-msmetafile",
				"wmf"));
		dataflavorList.addElement(new DataFlavor("text/rtf", "rtf"));
		dataflavorList.addElement(new DataFlavor(
				"application/vnd.oasis.opendocument.graphics", null));
		transfertTypeCB.setModel(dataflavorList);
		transfertTypeCB.setEditable(true);

	}

	protected void listClipboard() {
	
		/*Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		DataFlavor[] flavors = clipboard.getAvailableDataFlavors();
		messageArea.setText("");
		DefaultComboBoxModel dataflavorList = new DefaultComboBoxModel();

		for (int i = 0; i < flavors.length; i++) {
			dataflavorList.addElement(flavors[i]);
			messageArea.append(flavors[i].toString() + "\n");
		}
		transfertTypeCB.setModel(dataflavorList);*/
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new TransfertTest();
			}

		});
	}
	
	class MyTransfert implements Transferable{

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			// Take 
			InputStream in= new FileInputStream(copyFile);
			return in;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {getSelectedFlavor()};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return getSelectedFlavor().match(flavor);
		}
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Nothing to do in fact (data is already saved).
	}
}
