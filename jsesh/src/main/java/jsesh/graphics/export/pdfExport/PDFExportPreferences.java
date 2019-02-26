package jsesh.graphics.export.pdfExport;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

public class PDFExportPreferences {
	
	
	/**
	 * Output file.
	 */
	private File file;

	/**
	 * PDF Document title.
	 */
	private String title;

	private String author;

	private String subject;

	private String keywords;

	private boolean showPageNumbers;

	private DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();

	private Color backgroundColor = Color.WHITE;

	/**
	 * The possible page sizes. keys are formats, values are rectangles. Could
	 * move to a class of its own if needed.
	 */

	private Map<String,Rectangle> pageFormats;

	/**
	 * The size of the current document (as a textual format : A4, A3...).
	 */

	private String pageSize;

	private int lineHeight;
	
	/**
	 * Export as encapsulated PDF : only one page, no line number and restrict to the bounding box of the text. 
	 */
	private boolean encapsulated= false;

	/**
	 * Starts a new page when a "page break" is met.
	 */
	private boolean respectPages;

	/**
	 * Respect original text layout : left-to-right, columns, and line breaks.
	 */
	private boolean respectTextLayout;
	
	public PDFExportPreferences() {
		fillPageSizes();
		setDefaults();
	}
	/**
	 * @return the respectPages
	 */
	public boolean isRespectPages() {
		return respectPages && ! encapsulated;
	}

	/**
	 * @param respectPages the respectPages to set
	 */
	public void setRespectPages(boolean respectPages) {
		this.respectPages = respectPages;
	}

	/**
	 * @return the respectTextLayout
	 */
	public boolean isRespectTextLayout() {
		return respectTextLayout || encapsulated;
	}

	/**
	 * @param respectTextLayout the respectTextLayout to set
	 */
	public void setRespectTextLayout(boolean respectTextLayout) {
		this.respectTextLayout = respectTextLayout;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the showPageNumbers
	 */
	public boolean isShowPageNumbers() {
		return showPageNumbers && ! encapsulated;
	}

	/**
	 * @param showPageNumbers the showPageNumbers to set
	 */
	public void setShowPageNumbers(boolean showPageNumbers) {
		this.showPageNumbers = showPageNumbers;
	}

	/**
	 * @return the drawingSpecifications
	 */
	public DrawingSpecification getDrawingSpecifications() {
		return drawingSpecifications;
	}

	/**
	 * @param drawingSpecifications the drawingSpecifications to set
	 */
	public void setDrawingSpecifications(DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

	/**
	 * @return the backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * @return the pageFormats
	 */
	public Map<String,Rectangle> getPageFormats() {
		return pageFormats;
	}

	/**
	 * @param pageFormats the pageFormats to set
	 */
	public void setPageFormats(TreeMap pageFormats) {
		this.pageFormats = pageFormats;
	}

	/**
	 * @return the pageSize
	 */
	public String getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the lineHeight
	 */
	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * @param lineHeight the lineHeight to set
	 */
	public void setLineHeight(int lineHeight) {
		this.lineHeight = lineHeight;
	}

	/**
	 * gets the selected page size.
	 * @return 
	 */
	
	public Rectangle getPageRectangle() {
		return (Rectangle) pageFormats.get(pageSize);
	}
	
	/**
	 * Do we export as encapsulated PDF : only one page, no line number and restrict to the bounding box of the text. 
	 */
	public boolean isEncapsulated() {
		return encapsulated;
	}
	
	/**
	 * Should this PDF be encapsulated ?
	 * @param embedded true iff the pdf code should be a single large picture.
	 */
	public void setEncapsulated(boolean embedded) {
		this.encapsulated = embedded;
	}
	
	/**
	 * We fill a table mapping names of page dimensions to actual values. this
	 * is done using reflection on the class PageSize.
	 */
	private void fillPageSizes() {
		pageFormats = new TreeMap<>();
		Field[] fields = PageSize.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getType().equals(Rectangle.class)) {
				try {
					pageFormats.put(fields[i].getName(), (Rectangle)fields[i].get(null));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	public void setDefaults() {
		file = new File("default.pdf");
		title = "";
		keywords = "";
		subject = "";
		author = System.getProperty("user.name");
		respectPages = true;
		respectTextLayout = false;
		showPageNumbers = true;
		encapsulated= false;
		pageSize = "A4";
		lineHeight = 14;
	}
}
