package jsesh.ui.widgets.renderers;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jsesh.render.draw.MDCIconFactory;

/**
 * Renders mdc codes as hieroglyphic texts in tables.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
@SuppressWarnings("serial")
public class MdCTableCellRenderer extends DefaultTableCellRenderer {

	private MDCIconFactory mdcIconFactory;

	public MdCTableCellRenderer(MDCIconFactory mdcIconFactory) {
		this.mdcIconFactory = mdcIconFactory;
	}

	/// Vertical room kept around the icon.
	private static final int ROW_MARGIN = 4;

	/// Grows the row if its picture doesn't fit. Hieroglyphic texts can be
	/// taller than a line (columns, scaled interface...), and the row height of
	/// a table is fixed otherwise.
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Icon icon = getIcon();
		if (icon != null) {
			int needed = icon.getIconHeight() + ROW_MARGIN;
			if (table.getRowHeight(row) < needed) {
				table.setRowHeight(row, needed);
			}
		}
		return result;
	}

	@Override
	protected void setValue(Object value) {
		
		if (value instanceof String) {
			String mdc= (String) value;
			setIcon(mdcIconFactory.buildImage(mdc));
		}
	}

}
