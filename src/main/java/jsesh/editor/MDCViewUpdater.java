/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor;

import java.util.Iterator;

import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.operations.ChildOperation;
import jsesh.mdc.model.operations.Deletion;
import jsesh.mdc.model.operations.Insertion;
import jsesh.mdc.model.operations.ModelOperationVisitor;
import jsesh.mdc.model.operations.Modification;
import jsesh.mdc.model.operations.Replacement;
import jsesh.mdc.model.operations.ZoneModification;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;


/**
 * Updates an editor's view to keep it synchronized with its model.
 * @author S. Rosmorduc
 */
class MDCViewUpdater implements ModelOperationVisitor {

	private final JMDCEditor editor;

	/**
	 * @param editor
	 */
	MDCViewUpdater(JMDCEditor editor) {
		this.editor = editor;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitChildOperation(jsesh.mdc.model.operations.ChildOperation)
	 */
	public void visitChildOperation(ChildOperation operation) {
		int k;
		SimpleViewBuilder builder= new SimpleViewBuilder();
		// Find the index for the element which was modified...
		for (k = 0; k < this.editor.documentView.getNumberOfSubviews()
				&& this.editor.documentView.getSubView(k).getModel() != operation
						.getChildOperation().getElement(); k++)
			;
		// This is k. rebuild view for k, and replace it.
		if (this.editor.documentView.getSubView(k).getModel() == operation
				.getChildOperation().getElement()) {
			MDCView subv = builder.buildView(operation.getChildOperation()
					.getElement(), editor.getDrawingSpecifications());
			this.editor.documentView.replaceSubView(k, subv);
		}
		// update the page layout.
		builder.reLayout(this.editor.documentView, editor.getDrawingSpecifications());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitDeletion(jsesh.mdc.model.operations.Deletion)
	 */
	public void visitDeletion(Deletion deletion) {
		// Remove the modified views, and update page layout.
		this.editor.documentView.remove(deletion.getStart(), deletion.getEnd());
		new SimpleViewBuilder().reLayout(this.editor.documentView,editor.getDrawingSpecifications());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitInsertion(jsesh.mdc.model.operations.Insertion)
	 */
	public void visitInsertion(Insertion insertion) {
		SimpleViewBuilder builder= new SimpleViewBuilder();
		int index= insertion.getIndex();
		// Create and insert views at the proper position.
		for (Iterator i= insertion.getChildren().iterator(); i.hasNext();) {
			MDCView subView = builder.buildView((ModelElement) i.next(),editor.getDrawingSpecifications());
			this.editor.documentView.addAt(index++, subView);
		}
		builder.reLayout(this.editor.documentView,editor.getDrawingSpecifications());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitModification(jsesh.mdc.model.operations.Modification)
	 */
	public void visitModification(Modification modification) {
		// Huge change: we suppress the whole view, which will cause the complete recomputation of the page.
		this.editor.documentView = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitReplacement(jsesh.mdc.model.operations.Replacement)
	 */
	public void visitReplacement(Replacement replacement) {
		this.editor.documentView = null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitZoneModification(jsesh.mdc.model.operations.ZoneModification)
	 */
	public void visitZoneModification(ZoneModification modification) {
		SimpleViewBuilder builder= new SimpleViewBuilder();
		for (int i= modification.getStart(); i< modification.getEnd(); i++) {
			TopItem it= this.editor.getHieroglyphicTextModel().getModel().getTopItemAt(i);
			MDCView v= builder.buildView(it,editor.getDrawingSpecifications());
			editor.documentView.replaceSubView(i,v);
		}
		
		builder.reLayout(this.editor.documentView,editor.getDrawingSpecifications());
	}

}