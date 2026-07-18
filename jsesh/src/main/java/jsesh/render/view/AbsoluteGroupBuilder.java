/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 7 déc. 2004
 *
 */
package jsesh.render.view;

import java.util.List;

import jsesh.model.AbsoluteGroup;
import jsesh.model.Hieroglyph;
import jsesh.model.InnerGroup;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.TopItem;
import jsesh.model.tools.InnerGroupLister;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;

/**
 * @author Serge Rosmorduc
 */
public class AbsoluteGroupBuilder {

	public AbsoluteGroupBuilder() {
		super();
	}

	/**
	 * Build an absolute group from a list of top items. The group will try to
	 * be as close as possible to the original top items disposition.
	 * 
	 * @param topItems
	 * @param jseshStyle
	 * @param hieroglyphsDrawer
	 * @return a new AbsoluteGroup.
	 */
	
	public AbsoluteGroup createAbsoluteGroupFrom(List<TopItem> topItems, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
		AbsoluteGroup result = null;

		if (topItems.size() == 1) {
			TopItem item = topItems.get(0);
			InnerGroup groups[] = InnerGroupLister.getInnerGroups(item);
			if (groups.length == 1 && groups[0] instanceof AbsoluteGroup) {
				result = (AbsoluteGroup) groups[0].deepCopy();
			}
		}
		if (result == null) {
			AbsoluteGroupComposer composer = new AbsoluteGroupComposer(renderContext, techRenderContext);
			composer.createAbsoluteGroupFrom(topItems);
			result = composer.result;

		}
		return result;
	}

	private  class AbsoluteGroupComposer extends ModelElementAdapter {

		AbsoluteGroup result;
		JSeshRenderContext renderContext;
		JSeshTechRenderContext techRenderContext;
		HieroglyphDrawer hieroglyphsDrawer;

		/**
		 * Current view origin position.
		 */
		float origx, origy;

		float scale;

		//double sizeofA1;
		
		double sizeOfGroupUnit;

		AbsoluteGroupComposer(JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
			this.techRenderContext = techRenderContext;
			this.renderContext = renderContext;
			this.hieroglyphsDrawer = new HieroglyphDrawer(renderContext.hieroglyphShapeRepository());
		}

		/**
		 * Build an absolute group from a list of top items.
		 * 
		 * @param topItems
		 * @param specs
		 * @return
		 */
		public AbsoluteGroup createAbsoluteGroupFrom(List<TopItem> topItems) {
			result = new AbsoluteGroup();
			ViewBuilder builder = new ViewBuilder();
			sizeOfGroupUnit= hieroglyphsDrawer.getGroupUnitLength();
			origx = 0;
			origy = 0;
			for (int i = 0; i < topItems.size(); i++) {
				scale = 1;
				// Build a view of this item
				MDCView v = builder.buildView((ModelElement) topItems.get(i), renderContext, techRenderContext);
				// extract the actual coordinates of the view elements
				scanView(v);                                
				origx += v.getWidth();
				origy = 0;
			}
			return result;
		}

		/**
		 * Extract the actual coordinates from a view element and inject them
		 * into hieroglyphs.
		 * 
		 * @param v
		 */
		private void scanView(MDCView v) {
			scale = scale * v.getXScale();

			// store the coordinates into hieroglyphs, if necessary.
			v.getModel().accept(this);

			for (int i = 0; i < v.getNumberOfSubviews(); i++) {
				MDCView subv = v.getSubView(i);
				// Save position before entering the subview.
				float oldScale = scale;
				float oldx = origx;
				float oldy = origy;
				origx += subv.getPosition().x * scale;
				origy += subv.getPosition().y * scale;
				// Process subview
				scanView(subv);
				// Restore.
				origx = oldx;
				origy = oldy;
				scale = oldScale;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.model.ModelElementAdapter#visitHieroglyph(jsesh.model.Hieroglyph)
		 */
		public void visitHieroglyph(Hieroglyph oldh) {
			Hieroglyph h = (Hieroglyph) oldh.deepCopy();
			int xx = (int) (origx /sizeOfGroupUnit);
			int yy = (int) (origy /sizeOfGroupUnit);
			// Don't take into account the sign explicit size, as it is already 
			// taken into account in the view scale.
			// int s = (int) (scale * h.getRelativeSize());
			int s = (int) (scale*100);
			
			h.setExplicitPosition(xx, yy, s);
			result.addHieroglyph(h);
		}
	}
}
