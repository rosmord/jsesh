package jsesh.mdc.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

import jsesh.editor.HieroglyphicTextModel;
import jsesh.editor.caret.MDCCaret;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.mdc.constants.Dialect;
import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.utils.FileUtils;
import jsesh.utils.SystemUtils;

/**
 * A document containing Manuel de codage text.
 * 
 * A document might be associated with a file, or not. In this case, the file
 * property is null.
 * 
 * @author rosmord
 * 
 */
public class MDCDocument {

	private File file = null;
	private String encoding = "UTF-8";
	private Dialect dialect = Dialect.JSESH;
	private TextOrientation mainOrientation = TextOrientation.HORIZONTAL;

	/**
	 * kind of statistical bias.
	 */
	private TextDirection mainDirection = TextDirection.LEFT_TO_RIGHT;

	private boolean smallSignsCentred = false;

	private HieroglyphicTextModel hieroglyphicTextModel = new HieroglyphicTextModel();

	public MDCDocument() {
		hieroglyphicTextModel.setPhilologyIsSign(true);
	}

	public MDCDocument(HieroglyphicTextModel hieroglyphicTextModel) {
		this.hieroglyphicTextModel = hieroglyphicTextModel;
	}

	/**
	 * Create a document from a top item list and specifications.
	 * 
	 * @param topItemList
	 * @param drawingSpecifications
	 */
	public MDCDocument(TopItemList topItemList,
			DrawingSpecification drawingSpecifications) {
		this();
		hieroglyphicTextModel = new HieroglyphicTextModel();
		hieroglyphicTextModel.setTopItemList(topItemList);
		setMainDirection(drawingSpecifications.getTextDirection());
		setMainOrientation(drawingSpecifications.getTextOrientation());
	}

	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return hieroglyphicTextModel;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public void setHieroglyphicTextModel(
			HieroglyphicTextModel hieroglyphicTextModel) {
		this.hieroglyphicTextModel = hieroglyphicTextModel;
	}

	public TextOrientation getMainOrientation() {
		return mainOrientation;
	}

	public void setMainOrientation(TextOrientation mainOrientation) {
		this.mainOrientation = mainOrientation;
	}

	public TextDirection getMainDirection() {
		return mainDirection;
	}

	public void setMainDirection(TextDirection mainDirection) {
		this.mainDirection = mainDirection;
	}

	/**
	 * Save the document to the correct file. Will currently always save with an
	 * extensions. Tksesh files will be kept "as is", but other files will be
	 * saved in latest JSesh format, with a GLY extensions.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		if (getFile().getName().toLowerCase().endsWith(".pdf"))
			throw new UserMessage("THIS METHOD CAN NOT SAVE PDF");
		fixDocumentEncoding();
		saveTo(new FileOutputStream(getFile()));
	}

	/**
	 * Save the document on a binary stream. Only use this method if you want to
	 * save the document in an unusual place. Normally, the save() method is
	 * preferred.
	 * 
	 * @param out
	 * @throws IOException
	 * @see {@link #save()}
	 */
	public void saveTo(OutputStream out) throws IOException {
		saveTo(new OutputStreamWriter(out, encoding));
	}

	/**
	 * Save the document on a text writer. Only use this method if you want to
	 * save the document in an unusual place. Normally, the save() method is
	 * preferred.
	 * 
	 * @param writer
	 * @throws IOException
	 * @see {@link #save()}
	 */
	public void saveTo(Writer writer) throws IOException {
		fixDocumentEncoding();
		try {
			writeHeader(writer);
			MdCModelWriter w = new MdCModelWriter();
			w.write(writer, hieroglyphicTextModel.getModel());
			hieroglyphicTextModel.setClean();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void fixDocumentEncoding() {
		String correctExtension;
		// Ensure there is an encoding !

		if (!hieroglyphicTextModel.isPhilologyIsSign()) {
			setEncoding("iso-8859-1");
			correctExtension = "hie";
		} else {
			setDialect(Dialect.JSESH1);
			correctExtension = "gly";
			setEncoding("UTF-8");
		}
		if (encoding == null) {
			encoding = SystemUtils.getDefaultEncoding();
		}
		setFile(FileUtils.buildFileWithExtension(file, correctExtension));
	}

	private void writeHeader(Writer f) throws IOException {
		if (Dialect.JSESH1.equals(getDialect())) {
			writeEntry(f, JSeshInfoConstants.JSESH_INFO, "1.0");
			writeEntry(f, JSeshInfoConstants.JSESH_MAIN_DIRECTION,
					getMainDirection().toString());
			writeEntry(f, JSeshInfoConstants.JSESH_MAIN_ORIENTATION,
					getMainOrientation().toString());
			writeEntry(f, JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED,
					isSmallSignsCentred() ? "1" : "0");
		}
	}

	private void writeEntry(Writer f, String propertyName, String value)
			throws IOException {
		f.write("++" + propertyName);
		if (value != null)
			f.write(" " + value);
		f.write(" +s\n");
	}

	public void setSmallSignCentered(boolean centered) {
		this.smallSignsCentred = centered;
	}

	public boolean isSmallSignsCentred() {
		return smallSignsCentred;
	}

	public String getMdC() {
		StringWriter writer = new StringWriter();
		try {
			saveTo(writer);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

}
