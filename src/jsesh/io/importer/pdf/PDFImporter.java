package jsesh.io.importer.pdf;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import jsesh.graphics.export.pdfExport.PDFExportConstants;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;

import com.lowagie.text.pdf.PdfReader;

public class PDFImporter {

	private File outFile= new File("Unnamed.gly");
	private MDCDocument mdcDocument;
	
	private PDFImporter(InputStream in, File outFile) throws PDFImportException {
		this.outFile= outFile;
		mdcDocument = readStream(in);
	}

	private MDCDocument readStream(InputStream in) throws PDFImportException {
		try {
			PdfReader reader = new PdfReader(in);
			HashMap info = reader.getInfo();
			String subject = (String) info.get("Subject");
			reader.close();
			if (subject == null
					|| !subject
							.startsWith(PDFExportConstants.CONTENT_TYPE_APPLICATION_JSESH)) {
				throw new PDFImportException("NO_MDC_DATA");
			}
			subject = subject.substring(subject.indexOf("\n\n") +2 );
			
			MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
			byte[] bytes = subject.getBytes("UTF-8");
			return mdcDocumentReader.readStream(new ByteArrayInputStream(bytes), outFile);
		} catch (IOException e) {
			throw new PDFImportException("FILE_READING_PROBLEM");
		} catch (MDCSyntaxError e) {
			throw new PDFImportException("ERROR_IN_DOCUMENT", e);
		}
	}

	/**
	 * Factory method to build a RTFImporter which reads from the clipboard.
	 * 
	 * @return
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 * @throws PDFImportException 
	 */
	public static PDFImporter createPDFPasteImporter(File outFile) throws  PDFImportException {
		try {
			DataFlavor flavor = new DataFlavor("application/pdf");
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			Transferable content = clipboard.getContents(null);

			Object transfertData = content.getTransferData(flavor);

			return new PDFImporter((InputStream) transfertData, outFile);
		} catch (HeadlessException e) {
			throw new PDFImportException("PROBLEM_PASTING",e);
		} catch (ClassNotFoundException e) {
			throw new PDFImportException("PROBLEM_PASTING",e);
		} catch (UnsupportedFlavorException e) {
			throw new PDFImportException("NO_PDF_AVAILABLE",e);
		} catch (IOException e) {
			throw new PDFImportException("PROBLEM_PASTING",e);
		}
	}

	/**
	 * Factory method to build a RTFImporter which reads a stream.
	 * 
	 * @param in
	 * @return
	 * @throws PDFImportException 
	 */
	public static PDFImporter createPDFStreamImporter(InputStream in, File outFile) throws PDFImportException {
		return new PDFImporter(in, outFile);
	}
	
	public MDCDocument getMdcDocument() {
		return mdcDocument;
	}
}
