package jsesh.mdcDisplayer.layout;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * The purpose of this class is to build a view for a ModelElement hierarchy.
 * After visiting a ModelElement, the currentView will be a correct view for the
 * said element.
 * <P>
 * <b>Concerning various text orientations</b> : views are
 * geometrically-oriented. They currently know nothing about text orientation.
 * This means that for a right-to-left text, for instance, the subviews should
 * be correctly placed by this builder.
 *
 * <P>
 * This file is free Software (c) Serge Rosmorduc
 *
 * @author rosmord
 *
 */
public class SimpleViewBuilder implements ViewBuilder {

    /**
     * The view which is being built. The method should set this value before
     * returning.
     */
    private MDCView currentView;

    private Layout layout;

    public SimpleViewBuilder() {
        this(MDCEditorKit.getBasicMDCEditorKit().createLayout());
    }

    public SimpleViewBuilder(Layout layout) {
        currentView = null;
        this.layout = layout;
    }

    public MDCView buildView(ModelElement elt, DrawingSpecification drawingSpecifications) {
        return buildView(elt, 0, elt.getNumberOfChildren(), drawingSpecifications);
    }

    /*
	 * (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.ViewBuilder#buildView(jsesh.mdc.model.ModelElement, int, int)
     */
    public MDCView buildView(ModelElement elt, int start, int end, DrawingSpecification drawingSpecifications) {
        // prepare for work :
        getLayout().reset(drawingSpecifications);
        // lay out the element.
        ViewBuilderAux visitor = new ViewBuilderAux(start, end);
        elt.accept(visitor);
        MDCView result = currentView;
        currentView = null;
        getLayout().cleanup();
        return result;
    }

    /**
     * Recompute the layout of a top level view. If a view has been modified
     * since it was built, this methods recomputes its layout.
     *
     * @param v
     */
    public void reLayout(MDCView view, DrawingSpecification drawingSpecifications) {
        //	  prepare for work :
        getLayout().reset(drawingSpecifications);
        getLayout().layout(view, 0);
        getLayout().cleanup();
    }

    private class ViewBuilderAux extends ModelElementAdapter {

        /**
         * Index for the first position to take into account.
         */
        int start;

        /**
         * Index for the last position to take into account.
         */
        int end;

        /**
         * Are we at the top level of the model ?
         */
        boolean topLevel;

        int depth;

        /**
         * @param start
         * @param end
         */
        public ViewBuilderAux(int start, int end) {
            this.start = start;
            this.end = end;
            topLevel = true;
            depth = 0;
        }

        /**
         * @see
         * jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
         */

        public void visitModifier(Modifier mod) {
            throw new RuntimeException("this method shouldn't be called");
        }

        /**
         * @see
         * jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
         */
        public void visitModifierList(ModifiersList l) {
            throw new RuntimeException("this method shouldn't be called");
        }

        /**
         * The default method is to build a view and subviews for the
         * subelements.
         *
         * @param t
         */
        public void visitDefault(ModelElement t) {
            depth++;
            MDCView result = new MDCView(t);
            getLayout().preLayoutHook(result, depth);

            int a, b;
            if (topLevel) {
                a = start;
                b = end;
            } else {
                a = 0;
                b = t.getNumberOfChildren();
            }
            topLevel = false;

            for (int i = a; i < b; i++) {
                ModelElement elt = t.getChildAt(i);
                // Build the next element in currentView :
                elt.accept(this);
                // add it the the list of child views.
                result.add(currentView);
            }
            // actual layout of the view. The sub views are already laid out :
            getLayout().layout(result, depth);
            getLayout().postLayoutHook(result, depth);
            // visiting must write the view it builds in currentView : 
            currentView = result;
            depth--;
        }

        /**
         * @see
         * jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
         * We don't want to build a view for modifiers.
         */
        public void visitHieroglyph(Hieroglyph h) {
            MDCView result = new MDCView(h);
            getLayout().preLayoutHook(result, depth + 1);
            getLayout().layout(result, depth + 1);
            currentView = result;
        }
    }

    /**
     * @return the current layout.
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * @param layout
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

}
