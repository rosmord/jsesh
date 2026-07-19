package jsesh.io.document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.model.constants.Dialect;
import jsesh.model.constants.JSeshInfoConstants;
import jsesh.utils.io.FileUtils;
import jsesh.utils.io.SystemUtils;
import jsesh.document.DocumentPreferences;
import jsesh.document.MDCDocument;
import jsesh.model.TopItemList;

/**
 * Writes a {@link MDCDocument} in the JSesh "manuel de codage" file format.
 *
 * This is the counterpart of {@link MDCDocumentReader}; the "++JSeshInfo"
 * header it produces is the one read back by {@link JSeshInfoReader}.
 *
 * <p>
 * Stream ownership follows the usual convention: this class closes the streams
 * it opens itself, and only flushes the ones it is given.
 *
 * @author rosmord
 */
public class MDCDocumentWriter {

	/**
	 * Saves the document to its own file.
	 *
	 * The file will always be saved with an extension. Tksesh files are kept
	 * "as is", but other files are saved in the latest JSesh format, with a
	 * "gly" extension.
	 *
	 * <b>Note that this normalizes the document first, and hence may change its
	 * file, encoding and dialect (see {@link #prepareForSaving(MDCDocument)}).</b>
	 *
	 * @param document the document to save.
	 * @throws IOException
	 */
	public void write(MDCDocument document) throws IOException {
		if (document.getFile().getName().toLowerCase(Locale.ENGLISH)
				.endsWith(".pdf"))
			throw new IOException("THIS METHOD CAN NOT SAVE PDF");
		prepareForSaving(document);
		try (OutputStream out = new FileOutputStream(document.getFile())) {
			write(document, out);
		}
	}

	/**
	 * Writes the document on a binary stream, using the document encoding.
	 *
	 * The stream is flushed, but not closed: it belongs to the caller.
	 * Normally, {@link #write(MDCDocument)} is preferred.
	 *
	 * @param document the document to write.
	 * @param out      the stream to write to.
	 * @throws IOException
	 */
	public void write(MDCDocument document, OutputStream out)
			throws IOException {
		prepareForSaving(document);
		Writer writer = new OutputStreamWriter(out, document.getEncoding());
		write(document, writer);
		writer.flush();
	}

	/**
	 * Writes the document on a text writer.
	 *
	 * The writer is flushed, but not closed: it belongs to the caller.
	 * Normally, {@link #write(MDCDocument)} is preferred.
	 *
	 * @param document the document to write.
	 * @param writer   the writer to write to.
	 * @throws IOException
	 */
	public void write(MDCDocument document, Writer writer) throws IOException {
		prepareForSaving(document);
		writeHeader(document, writer);
		new MdCModelWriter().write(writer,
				document.getHieroglyphicTextModel().getModel());
		writer.flush();
		document.getHieroglyphicTextModel().setClean();
	}

	/**
	 * Renders the document as a "manuel de codage" string, header included.
	 *
	 * @param document the document to render.
	 * @return the document content in MdC format.
	 */
	public String toMdC(MDCDocument document) {
		StringWriter writer = new StringWriter();
		try {
			write(document, writer);
		} catch (IOException e) {
			// Can't happen on a StringWriter.
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	/**
	 * Renders a bare text as MdC, using the given preferences for the header.
	 *
	 * A convenience for callers which have a text and a set of preferences but
	 * no actual document, such as the graphic exporters embedding MdC source in
	 * their output.
	 *
	 * @param text        the text to render.
	 * @param preferences the preferences to write in the header.
	 * @return the text in MdC format, header included.
	 */
	public String toMdC(TopItemList text, DocumentPreferences preferences) {
		MDCDocument document = new MDCDocument();
		document.setTopItemList(text);
		document.setDocumentPreferences(preferences);
		return toMdC(document);
	}

	/**
	 * Normalizes the document so that it can be saved.
	 *
	 * Beware: this <em>modifies</em> the document, aligning its encoding,
	 * dialect and file extension with the format it will be written in.
	 *
	 * @param document the document to normalize.
	 */
	public void prepareForSaving(MDCDocument document) {
		String correctExtension;
		// Ensure there is an encoding !
		if (!document.isPhilologyIsSign()) {
			document.setEncoding("iso-8859-1");
			correctExtension = "hie";
		} else {
			document.setDialect(Dialect.JSESH1);
			correctExtension = "gly";
			document.setEncoding("UTF-8");
		}
		if (document.getEncoding() == null) {
			document.setEncoding(SystemUtils.getDefaultEncoding());
		}
		document.setFile(FileUtils.buildFileWithExtension(document.getFile(),
				correctExtension));
	}

	private void writeHeader(MDCDocument document, Writer f)
			throws IOException {
		if (Dialect.JSESH1.equals(document.getDialect())) {
			writeEntry(f, JSeshInfoConstants.JSESH_INFO, "1.0");
			Map<String, String> map = document.getDocumentPreferences()
					.getStringRepresentation();
			for (String key : map.keySet()) {
				writeEntry(f, key, map.get(key));
			}
		}
	}

	private void writeEntry(Writer f, String propertyName, String value)
			throws IOException {
		f.write("++" + propertyName);
		if (value != null)
			f.write(" " + value);
		f.write(" +s\n");
	}
}
