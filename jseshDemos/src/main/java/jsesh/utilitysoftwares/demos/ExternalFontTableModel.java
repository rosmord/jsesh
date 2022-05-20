/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 3 juin 2005
 *
 */
package jsesh.utilitysoftwares.demos;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 * @author Serge Rosmorduc
 */
public class ExternalFontTableModel extends AbstractTableModel {

    private static String fcolumnNames[]= {"glyph position", "image", "mdc code"}; 
    private ArrayList rows;

    /**
     *  
     */
    public ExternalFontTableModel() {
        rows = new ArrayList();
    }

    public void clear() {
        rows.clear();
        /* the following is a bit exessive, but simple. */
        fireTableStructureChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * @return Returns the columnNames.
     */
    public static String[] getColumnNames() {
        return fcolumnNames;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        FontTableGlyphModel glyphModel = (FontTableGlyphModel) rows
                .get(rowIndex);
        switch (columnIndex) {
        case 0:
            return glyphModel.getIndex();
        case 1:
            return glyphModel.getImage();
        case 2:
            return glyphModel.getMdc();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
     *      int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FontTableGlyphModel glyphModel = (FontTableGlyphModel) rows
                .get(rowIndex);
        switch (columnIndex) {
        case 0:
            glyphModel.setIndex((Integer) aValue);
        case 1:
            glyphModel.setImage((ImageIcon) aValue);
        case 2:
            glyphModel.setMdc((String) aValue);
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return ImageIcon.class;
        case 2:
            return String.class;
        }
        return null;
    }

    public void add(int charCode, ImageIcon image, String mdc) {
        rows.add(new FontTableGlyphModel(charCode, image, mdc));
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    
    public static class FontTableGlyphModel {
        private Integer index;

        private String mdc;

        private ImageIcon image;

        /**
         * @param index
         * @param mdc
         * @param image
         */
        public FontTableGlyphModel(Integer index, ImageIcon image, String mdc) {
            super();
            this.index = index;
            this.mdc = mdc;
            this.image = image;
        }

        public ImageIcon getImage() {
            return image;
        }

        
        
        public void setImage(ImageIcon image) {
            this.image = image;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getMdc() {
            return mdc;
        }

        public void setMdc(String mdc) {
            this.mdc = mdc;
        }
    }
}