package jsesh.document;

import java.io.File;
import java.io.Reader;

import jsesh.model.constants.Dialect;
import jsesh.model.HieroglyphicText;
import jsesh.parser.MDCSyntaxError;

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
	private DocumentPreferences documentPreferences = new DocumentPreferences();
	private HieroglyphicTextModel hieroglyphicTextModel = new HieroglyphicTextModel();

	public MDCDocument() {
		hieroglyphicTextModel.setPhilologyIsSign(true);
	}

	public MDCDocument(HieroglyphicTextModel hieroglyphicTextModel) {
		this.hieroglyphicTextModel = hieroglyphicTextModel;
	}

	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return hieroglyphicTextModel;
	}

	/// Returns the text of this document.
	public HieroglyphicText getHieroglyphicText() {
		return hieroglyphicTextModel.getHieroglyphicText();
	}

	/// Replaces the text of this document.
	public void setHieroglyphicText(HieroglyphicText text) {
		hieroglyphicTextModel.setHieroglyphicText(text);
	}

	// /**
	//  * Appends items at the end of the current top-level model.
	//  */
	// public void appendTopItems(HieroglyphicText items) {
	// 	hieroglyphicTextModel.insertElementsAt(
	// 			hieroglyphicTextModel.getLastPosition(),
	// 			items.asList());
	// }

	/**
	 * Parses and replaces current content according to the document dialect.
	 */
	public void readHieroglyphicText(Reader reader) throws MDCSyntaxError {
		hieroglyphicTextModel.readHieroglyphicText(reader, dialect);
	}

	public boolean isPhilologyIsSign() {
		return hieroglyphicTextModel.isPhilologyIsSign();
	}

	public void setPhilologyIsSign(boolean philologyIsSign) {
		hieroglyphicTextModel.setPhilologyIsSign(philologyIsSign);
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

	public DocumentPreferences getDocumentPreferences() {
		return documentPreferences;
	}

	public void setDocumentPreferences(
			DocumentPreferences newDocumentPreferences) {
		if (newDocumentPreferences == null)
			throw new NullPointerException(
					"DocumentPreferences must not be null");
		this.documentPreferences = newDocumentPreferences;
	}

}
