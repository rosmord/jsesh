package jsesh.io.importer.rtf;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;

/**
 * Class for importing Manuel de codage code hidden in a RTF File. In fact, the
 * MdC Code will be hidden in EMF pictures comments.
 * 
 * @author rosmord
 */
public class RTFImporter {

	private File outFile = new File("Unnamed.gly");
	private MDCDocument mdcDocument;

	private RTFImporter(InputStream in, File outFile) throws RTFImportException {
		mdcDocument = readStream(in);
		this.outFile = outFile;
	}

	private MDCDocument readStream(InputStream in) throws RTFImportException {
		try {
			RTFReader reader = new RTFReader(in);
			String mdcData = reader.getMDC();
			if (mdcData == null)
				throw new RTFImportException("NO_MDC_DATA");

			MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
			return mdcDocumentReader.readString(mdcData, outFile);
		} catch (IOException e) {
			throw new RTFImportException("FILE_READING_PROBLEM");
		} catch (MDCSyntaxError e) {
			throw new RTFImportException("ERROR_IN_DOCUMENT", e);
		}
	}

	/**
	 * Factory method to build a RTFImporter which reads from the clipboard.
	 * 
	 * @return
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 * @throws RTFImportException
	 */
	public static RTFImporter createRTFPasteImporter(File outFile)
			throws RTFImportException {
		try {
			DataFlavor flavor = new DataFlavor("text/rtf");
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			Transferable content = clipboard.getContents(null);

			Object transfertData = content.getTransferData(flavor);

			return new RTFImporter((InputStream) transfertData, outFile);
		} catch (HeadlessException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		} catch (ClassNotFoundException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		} catch (UnsupportedFlavorException e) {
			// throw new RTFImportException("NO_RTF_AVAILABLE",e);
			return createRTFPasteImporterForLinuxFix(outFile);
		} catch (IOException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		}
	}

	/**
	 * A secondary factory method used to fix an openoffice bug in linux.
	 * 
	 * @param outFile
	 * @return
	 * @throws RTFImportException
	 */
	private static RTFImporter createRTFPasteImporterForLinuxFix(File outFile)
			throws RTFImportException {
		try {
			// On linux, OpenOffice uses (incorrectly) text/richtext
			// instead of text/rtf. This is very wrong, as richtext
			// is absolutely different from rtf.
			//
			// So a) the following line is incorrect but should work. 
			// and b) we currently don't care.
			
			// Note that the representation class is needed here.
			DataFlavor flavor = new DataFlavor("text/richtext;class=java.io.InputStream;charset=ASCII");
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			Transferable content = clipboard.getContents(null);

			Object transfertData = content.getTransferData(flavor);

			return new RTFImporter((InputStream) transfertData, outFile);
		} catch (HeadlessException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		} catch (ClassNotFoundException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		} catch (UnsupportedFlavorException e) {
			throw new RTFImportException("NO_RTF_AVAILABLE", e);
		} catch (IOException e) {
			throw new RTFImportException("PROBLEM_PASTING", e);
		}
	}

	/**
	 * Factory method to build a RTFImporter which reads a stream.
	 * 
	 * @param in
	 * @return
	 * @throws RTFImportException
	 */
	public static RTFImporter createRTFStreamImporter(InputStream in,
			File outFile) throws RTFImportException {
		return new RTFImporter(in, outFile);
	}

	public MDCDocument getMdcDocument() {
		return mdcDocument;
	}
}
