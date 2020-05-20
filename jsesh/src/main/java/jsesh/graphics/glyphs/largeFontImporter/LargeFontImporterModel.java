package jsesh.graphics.glyphs.largeFontImporter;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.ShapeChar;

public class LargeFontImporterModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1527856524326946311L;

	private String fontName;

	private Font font;

	private HashMap fontCodes = new HashMap();

	private HashSet fullHeightSigns = new HashSet();

	private boolean saved = true;

	private double shapeScale= 1.0;
	
	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		int oldSize = 0;
		if (this.font != null)
			oldSize = getRowCount();
		fireTableRowsDeleted(0, oldSize);
		this.font = font;
		fontCodes.clear();
		shapeScale= 1.0;
		fullHeightSigns.clear();
		fireTableRowsInserted(0, getRowCount());
		saved = false;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public String getMDC(int pos) {
		if (fontCodes.containsKey(new Integer(pos))) {
			return ((String) fontCodes.get(new Integer(pos))).trim();
		} else
			return ""; //$NON-NLS-1$
	}

	public void setMdC(int pos, String mdc) {
		fontCodes.put(new Integer(pos), mdc);
		saved = false;
	}

	public Integer[] getSetPositions() {
		Integer[] codes = (Integer[]) fontCodes.keySet()
				.toArray(new Integer[0]);
		Arrays.sort(codes);
		return codes;
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		if (font == null)
			return 0;
		else
			return font.getNumGlyphs();
		// ATTENTION: NOMBRE DES SIGNES Définis, et pas index maximal des signes !!!!
		// Le problème est que certaines versions de java ne peuvent pas indiquer quand un signe appartient à 
		// la fonte et quand il est fourni par le système.
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 2) {
			setMdC(rowIndex, (String) aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
			saved = false;
		} else if (columnIndex == 3) {
			Integer i = new Integer(rowIndex);
			if (fullHeightSigns.contains(i))
				fullHeightSigns.remove(i);
			else
				fullHeightSigns.add(i);
			fireTableCellUpdated(rowIndex, columnIndex);
			saved = false;
		}
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return arg1 == 2 || arg1 == 3;
	}

	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return new Integer(row);
		case 1:
			return new FontGlyph(font, row);
		case 2:
			return getMDC(row);
		case 3:
			return new Boolean(fullHeightSigns.contains(new Integer(row)));
		}
		return null;
	}

	public Class getColumnClass(int col) {
		final Class[] classes = { Integer.class, FontGlyph.class, String.class,
				Boolean.class };
		return classes[col];
	}

	/**
	 * Load a file describing the font.
	 * @throws IOException 
	 * 
	 */
	public void loadFile(InputStream in) throws IOException {
		BufferedReader r= new BufferedReader(new InputStreamReader(in,"UTF-8")); //$NON-NLS-1$
		String line;
		
		// A) font file
		line= r.readLine();
		int sep= line.indexOf(' ');
		fontName= line.substring(sep+1).trim();
		
		setFont(new Font(fontName,Font.PLAIN, 48));
		// B) font scale
		line= r.readLine();
		shapeScale= Double.parseDouble(line);
		
		// data
		while ((line= r.readLine()) != null) {
			String parts[]= line.split(" "); //$NON-NLS-1$
			int code= Integer.parseInt(parts[0]);
			String mdc= parts[1];
			setMdC(code, mdc);
		}
		saved = true;
	}

	/**
	 * 1ere ligne, le nom de la fonte (system/file + font name or file path)
	 * 2ieme :l'�chelle
	 * Ensuite, liste : number GardinerCode
	 * @throws IOException 
	 */

	public void saveFile(OutputStream out) throws IOException {
		PrintWriter w= new PrintWriter(new OutputStreamWriter(out,"UTF-8")); //$NON-NLS-1$
		w.println("SYSTEM "+ fontName); //$NON-NLS-1$
		w.println(shapeScale);
		Iterator it= fontCodes.keySet().iterator();
		while (it.hasNext()) {
			Integer pos= (Integer) it.next();
			String code= getMDC(pos.intValue());
			w.print(pos);
			w.print(" "); //$NON-NLS-1$
			w.println(code);
		}
		w.close();
		saved = true;
	}

	/**
	 * Import signs into the JSesh database.
	 * 
	 * @throws DuplicateEntriesException
	 */
	public void exportSigns() throws DuplicateEntriesException {
		ArrayList messages = new ArrayList();
		// A) test that no code is repeated.
		HashSet set = new HashSet();
		Iterator it = fontCodes.values().iterator();
		while (it.hasNext()) {
			String e = (String) it.next();
			if (!e.equals("")) { //$NON-NLS-1$
				if (set.contains(e)) {
					messages.add(e);
				}
				set.add(e);
			}
		}
		if (!messages.isEmpty())
			throw new DuplicateEntriesException(messages);
		// Solve the scaling problem
		// C) Actual JSesh import.
		it = fontCodes.keySet().iterator();
		while (it.hasNext()) {
			int pos = ((Integer) it.next()).intValue();
			String code = (String) fontCodes.get(new Integer(pos));
			if (code != null && !code.equals("")) { //$NON-NLS-1$
				ShapeChar s = getShapeCharForPos(pos);
				// s.fixShape();
				Rectangle2D bbox = s.getBbox();
				if (bbox.getWidth() != 0 || bbox.getHeight() != 0) {
					s.scaleGlyph(shapeScale);
					DefaultHieroglyphicFontManager.getInstance().insertNewSign(
							code, s);
				}
			}
		}
	}

	public ShapeChar getShapeCharForPos(int pos) {
		ShapeChar s = new ShapeChar();
		FontRenderContext renderContext= new FontRenderContext(null, true, false);
		TextLayout layout= new TextLayout("" + (char) pos,font.deriveFont(10f),renderContext); //$NON-NLS-1$
		Shape shape = layout.getOutline(null);
		s.setShape(shape);
		return s;
	}

	
	
	public String getColumnName(int column) {
		String[] names = { Messages.getString("LargeFontImporterModel.POSITION"), Messages.getString("LargeFontImporterModel.GLYPHE"), Messages.getString("LargeFontImporterModel.MANUEL_DE_CODAGE"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Messages.getString("LargeFontImporterModel.FULL_HEIGHT") }; //$NON-NLS-1$
		return names[column];
	}

	public boolean isSaved() {
		return saved;
	}

	public double getShapeScale() {
		return shapeScale;
	}

	public void setShapeScale(double shapeScale) {
		this.shapeScale = shapeScale;
	}
	
	
}
