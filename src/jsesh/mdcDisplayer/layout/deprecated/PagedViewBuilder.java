/*
 * Created on 14 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout.deprecated;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * This class builds a view for a hieroglyphic text in which each page
 * stands in a different subview.
 * 
 * <p> Currently, it's used for printing purposes and bitmap files creation.
 * 
*  
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 * @deprecated
 */
public class PagedViewBuilder implements ViewBuilder
{

	private PagedLayout pagedLayout;

	/**
	 * The view which is being built.
	 * The method should set this value before returning.
	 */
	private MDCView currentView;


	public PagedViewBuilder()
	{
		this(
			MDCPagedEditorKit.getDefaultMDCPagedEditorKit().createPagedLayout());
	}

	public PagedViewBuilder(PagedLayout layout)
	{
		this.pagedLayout = layout;
		currentView = null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.ViewBuilder#buildView(jsesh.mdc.model.ModelElement)
	 */
	public MDCView buildView(ModelElement elt, DrawingSpecification drawingSpecifications) {
		return buildView(elt, 0, elt.getNumberOfChildren(),drawingSpecifications);
	}
	
	public MDCView buildView(ModelElement elt, int start, int end, DrawingSpecification drawingSpecifications)
	{
		ViewBuilderAux visitor = new ViewBuilderAux(start, end);
		// prepare for work :
		getPagedLayout().reset(drawingSpecifications);
		// lay out the element.
		elt.accept(visitor);
		MDCView result = currentView;
		currentView = null;
		getPagedLayout().cleanup();
		return result;
	}

	private class ViewBuilderAux extends ModelElementAdapter
	{

		/**
		 * First position in the topitemlist
		 */
		int start;
		
		/**
		 * Last position if the topitemlist.
		 */
		int end;
		
		boolean topLevel;
		
		
		/**
		 * @param start
		 * @param end
		 */
		public ViewBuilderAux(int start, int end) {
			this.start = start;
			this.end = end;
			topLevel= true;
		}
		/**
		 * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
		 */
		public void visitModifier(Modifier mod)
		{
			throw new RuntimeException("this method shouldn't be called");
		}

		/**
		 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
		 */
		public void visitModifierList(ModifiersList l)
		{
			throw new RuntimeException("this method shouldn't be called");
		}

		/**
		 * The default method is to build a view and subviews for the subelements.
		 * @param t
		 */

		public void visitDefault(ModelElement t)
		{
			MDCView result = new MDCView(t);
			getPagedLayout().preLayoutHook(result, 0);

			int a, b;
			if (topLevel) {
				a= start;
				b= end;
			} else {
				a= 0;
				b= t.getNumberOfChildren();
			}
			topLevel= false;
			for (int i= a; i  <b; i++)
			{
				ModelElement elt = t.getChildAt(i);
				// Build the next element in currentView :
				elt.accept(this);
				// add it the the list of child views.
				result.add(currentView);
			}
			// visiting must write the view it builds in currentView : 
			currentView = result;
			// actual layout of the view. The sub views are already laid out :
			getPagedLayout().layout(currentView, 0);
		}

		/**
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 * We don't want to build a view for modifiers.
		 */

		public void visitHieroglyph(Hieroglyph h)
		{
			MDCView result = new MDCView(h);
			getPagedLayout().layout(result, 0);
			currentView = result;
		}

		/**
		 * Create the view for the whole model.
		 * @param t
		 */
		public void visitTopItemList(TopItemList t)
		{
			MDCView result = new MDCView(t);
			getPagedLayout().preLayoutHook(result, 0);

			topLevel= false;
			for (int i= start; i < end; i++) {
				ModelElement elt = t.getTopItemAt(i);
				// Build the next element in currentView :
				elt.accept(this);
				// add it the the list of child views.
				result.add(currentView);
			}
			// visiting must write the view it builds in currentView : 
			currentView = result;
			// actual layout of the view. The sub views are already laid out :
			getPagedLayout().layout(currentView, 0);
		}

	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.ViewBuilderInterface#reLayout(jsesh.mdcDisplayer.mdcView.MDCView)
	 */
	public void reLayout(MDCView v, DrawingSpecification drawingSpecifications)
	{
	}

	public PagedLayout getPagedLayout()
	{
		return pagedLayout;
	}

	/**
	 * @param layout
	 */
	public void setPagedLayout(PagedLayout layout)
	{
		pagedLayout = layout;
	}

}
