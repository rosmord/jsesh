package jsesh.ui.widgets.renderers;

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

	@Override
	protected void setValue(Object value) {
		
		if (value instanceof String) {
			String mdc= (String) value;
			setIcon(mdcIconFactory.buildImage(mdc));
		}
	}

}
