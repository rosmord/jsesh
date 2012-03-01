package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import org.qenherkhopeshef.graphics.pdf.QenherPDFGraphics2D;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

/**
 * A front-end to IText for copy/paste purposes.
 * <p>
 * This class is here to avoid adding a compile-time dependency of
 * jvect-clipboard on IText.
 * <p>
 * We use IText 2.1.5 and not later versions, as we want to keep them under
 * LGPL.
 * 
 * @author rosmord
 * 
 */
public class PDFPicture implements TransferablePicture {

	/**
	 * The picture dimension, in typographical points.
	 */
	private float width, height;

	/**
	 * The array containing the pdf data.
	 */

	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * The PDF document we will produce.
	 */
	private Document document;

	/**
	 * The comment we will attach to the picture for future references.
	 */
	private String comment = "";

	/**
	 * Create a PDF document.
	 * 
	 * 
	 * @throws FileNotFoundException
	 */
	public PDFPicture(float width, float height) throws FileNotFoundException {
		Rectangle pageSize = new Rectangle(width, height);
		document = new Document(pageSize);
		this.width = width;
		this.height = height;
	}

	/**
	 * Sets a free text comment in the PDF.
	 * 
	 * @param comment
	 *            a comment added to the PDF header (if null or empty, no
	 *            comment is set)
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void close() {
		document.close();
	}

	public byte[] getByteArray() {
		return out.toByteArray();
	}

	public Graphics2D getGraphics() {
		PdfWriter pdfWriter;
		try {
			pdfWriter = PdfWriter.getInstance(document, out);
		} catch (DocumentException e) {
			// Should not happen.
			throw new RuntimeException(e);
		}
		if (comment != null && !"".equals(comment))
			document.addSubject(comment);
		document.open();
		Graphics2D g = pdfWriter.getDirectContent().createGraphicsShapes(width, height);
		return new QenherPDFGraphics2D(g);
		//return g;
	}

	public Transferable buildTransferable() {
		close();
		return new PDFTransferable(this);
	}

}
