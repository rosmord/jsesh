/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.render.view;


import jsesh.model.Hieroglyph;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.Modifier;
import jsesh.model.ModifiersList;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.model.operations.ChildOperation;
import jsesh.model.operations.Deletion;
import jsesh.model.operations.Insertion;
import jsesh.model.operations.ModelOperation;
import jsesh.model.operations.Replacement;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.layout.Layout;


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
public class ViewBuilder {

    /**
     * The view which is being built. The method should set this value before
     * returning.
     */
    private MDCView currentView;
    
    /**
     * The layout, which is mostly the actual logic for the view builder...
     */
    private Layout layout;


    /**
     * Create a view builder.
     */
    public ViewBuilder() {
        this.currentView = null;
        setLayout(new Layout());
    }

    /// Builds the view of an element and of its descendants in the middle of
    /// a layout, with the layout in its current state (see
    /// [Layout#setSubViewBuilder(java.util.function.Function)]).
    private MDCView buildSubView(ModelElement elt) {
        MDCView saved = currentView;
        ViewBuilderAux visitor = new ViewBuilderAux(0, 0);
        visitor.topLevel = false;
        elt.accept(visitor);
        MDCView result = currentView;
        currentView = saved;
        return result;
    }

    public MDCView buildView(ModelElement elt, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
        return buildView(elt, 0, elt.getNumberOfChildren(), renderContext, techRenderContext);
    }


    /**
     * Build a partial view of an element.
     * <p>
     * The view will correspond to all sub elements of model between indexes
     * start and end.
     * <p>
     * mode will typically be a HieroglyphicText which we want to render partially.
     *
     * @param model
     * @param start
     * @param end
     * @return the view built for this element part.
     */
    public MDCView buildView(ModelElement elt, int start, int end, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
        // prepare for work :
        getLayout().reset(renderContext, techRenderContext);
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
    public void reLayout(MDCView view, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
        //	  prepare for work :
        getLayout().reset(renderContext, techRenderContext);
        getLayout().layout(view);
        getLayout().cleanup();
    }

    /// Updates the view of a zone after an operation on its content, keeping
    /// the views of the items which didn't change.
    ///
    /// Only the items touched by the operation are built again; the content of
    /// the zone is then laid out again from the views of its items (lines,
    /// columns, tabs...), and so is the zone. Zones nested in the zone are
    /// updated in the same way. This avoids rebuilding the whole content of a
    /// large zone at each edit. The parent of the zone (text or zone) is *not*
    /// laid out again: call [#reLayout] on the text afterwards.
    ///
    /// @param zoneView the view of the zone, as built by this class.
    /// @param operation the operation on the zone, as notified by the zone to
    ///        its observers: a [ChildOperation] whose child operation is on the
    ///        content of the zone.
    /// @param renderContext the context of the parent of the zone (the one
    ///        given to [#buildView] for the text).
    /// @param techRenderContext the technical rendering context.
    /// @return true if the view was updated; false if the operation is not one
    ///         this method handles, in which case the view is unchanged, and
    ///         should be built again.
    public boolean updateZoneView(MDCView zoneView, ModelOperation operation, JSeshRenderContext renderContext,
            JSeshTechRenderContext techRenderContext) {
        if (!canUpdateZone(zoneView, operation)) {
            return false;
        }
        getLayout().reset(renderContext, techRenderContext);
        try {
            updateZone(zoneView, (ChildOperation) operation);
        } finally {
            getLayout().cleanup();
        }
        return true;
    }

    /// Can [#updateZone] deal with an operation on a zone?
    private static boolean canUpdateZone(MDCView zoneView, ModelOperation operation) {
        if (!(zoneView.getModel() instanceof Zone zone)
                || operation.getElement() != zone
                || !(operation instanceof ChildOperation zoneOperation)
                || zoneView.getNumberOfSubviews() != 1
                || zoneView.getFirstSubView().getModel() != zone.getContent()) {
            return false;
        }
        ModelOperation contentOperation = zoneOperation.getChildOperation();
        return contentOperation.getElement() == zone.getContent()
                && (contentOperation instanceof Insertion
                        || contentOperation instanceof Deletion
                        || contentOperation instanceof Replacement
                        || contentOperation instanceof ChildOperation);
    }

    /// Updates a zone, in the state of the layout of its parent.
    private void updateZone(MDCView zoneView, ChildOperation zoneOperation) {
        MDCView contentView = zoneView.getFirstSubView();
        ZoneContent content = (ZoneContent) contentView.getModel();
        ModelOperation operation = zoneOperation.getChildOperation();
        // Enters the context of the zone (see Layout.preLayoutHook).
        getLayout().preLayoutHook(contentView);
        if (operation instanceof Insertion insertion) {
            int index = insertion.getIndex();
            for (ModelElement child : insertion.getChildren()) {
                contentView.addAt(index++, buildSubView(child));
            }
        } else if (operation instanceof Deletion deletion) {
            contentView.remove(deletion.getStart(), deletion.getEnd());
        } else if (operation instanceof Replacement replacement) {
            contentView.replaceSubView(replacement.getIndex(), buildSubView(content.getChildAt(replacement.getIndex())));
        } else if (operation instanceof ChildOperation childOperation) {
            updateItem(contentView, childOperation.getChildOperation());
        }
        // The layout of the content starts by resetting its view (size,
        // scale...), so laying it out again is safe.
        getLayout().layout(contentView);
        getLayout().postLayoutHook(contentView);
        getLayout().layout(zoneView);
    }

    /// Updates the view of an item of a zone, after an operation on it.
    ///
    /// @param contentView the view of the content of the zone.
    /// @param itemOperation the operation on the item.
    private void updateItem(MDCView contentView, ModelOperation itemOperation) {
        ModelElement item = itemOperation.getElement();
        for (int i = 0; i < contentView.getNumberOfSubviews(); i++) {
            MDCView itemView = contentView.getSubView(i);
            if (itemView.getModel() == item) {
                if (canUpdateZone(itemView, itemOperation)) {
                    updateZone(itemView, (ChildOperation) itemOperation);
                } else {
                    contentView.replaceSubView(i, buildSubView(item));
                }
                return;
            }
        }
        // Unknown item: should not happen; the whole content is built again.
        ZoneContent content = (ZoneContent) contentView.getModel();
        contentView.clear();
        for (int i = 0; i < content.getNumberOfChildren(); i++) {
            contentView.add(buildSubView(content.getChildAt(i)));
        }
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

        /**
         * @param start
         * @param end
         */
        public ViewBuilderAux(int start, int end) {
            this.start = start;
            this.end = end;
            topLevel = true;
        }

        /**
         * @see
         * jsesh.model.ModelElementVisitor#visitModifier(jsesh.model.Modifier)
         */

        public void visitModifier(Modifier mod) {
            throw new RuntimeException("this method shouldn't be called");
        }

        /**
         * @see
         * jsesh.model.ModelElementVisitor#visitModifierList(jsesh.model.ModifiersList)
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
            MDCView result = new MDCView(t);
            getLayout().preLayoutHook(result);

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
            getLayout().layout(result);
            getLayout().postLayoutHook(result);
            // visiting must write the view it builds in currentView : 
            currentView = result;
        }

        /**
         * @see
         * jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
         * We don't want to build a view for modifiers.
         */
        public void visitHieroglyph(Hieroglyph h) {
            MDCView result = new MDCView(h);
            getLayout().preLayoutHook(result);
            getLayout().layout(result);
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
        layout.setSubViewBuilder(this::buildSubView);
    }
}
