/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor;

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
import jsesh.mdcDisplayer.mdcView.MDCView;


/**
 * Updates an editor's view to keep it synchronized with its model.
 * @author S. Rosmorduc
 *
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
		for (k = 0; k < this.editor.mdcView.getNumberOfSubviews()
				&& this.editor.mdcView.getSubView(k).getModel() != operation
						.getChildOperation().getElement(); k++)
			;
		if (this.editor.mdcView.getSubView(k).getModel() == operation
				.getChildOperation().getElement()) {
			MDCView subv = this.editor.builder.buildView(operation.getChildOperation()
					.getElement(), editor.getDrawingSpecifications());
			this.editor.mdcView.replaceSubView(k, subv);
		}
		this.editor.builder.reLayout(this.editor.mdcView, editor.getDrawingSpecifications());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitDeletion(jsesh.mdc.model.operations.Deletion)
	 */
	public void visitDeletion(Deletion deletion) {
		this.editor.mdcView.remove(deletion.getStart(), deletion.getEnd());
		this.editor.builder.reLayout(this.editor.mdcView,editor.getDrawingSpecifications());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitInsertion(jsesh.mdc.model.operations.Insertion)
	 */
	public void visitInsertion(Insertion insertion) {
		int index= insertion.getIndex();
		for (Iterator i= insertion.getChildren().iterator(); i.hasNext();) {
			MDCView subView = this.editor.builder.buildView((ModelElement) i.next(),editor.getDrawingSpecifications());
			this.editor.mdcView.addAt(index++, subView);
		}
		this.editor.builder.reLayout(this.editor.mdcView,editor.getDrawingSpecifications());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitModification(jsesh.mdc.model.operations.Modification)
	 */
	public void visitModification(Modification modification) {
		this.editor.mdcView = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitReplacement(jsesh.mdc.model.operations.Replacement)
	 */
	public void visitReplacement(Replacement replacement) {
		this.editor.mdcView = null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitZoneModification(jsesh.mdc.model.operations.ZoneModification)
	 */
	public void visitZoneModification(ZoneModification modification) {
		for (int i= modification.getStart(); i< modification.getEnd(); i++) {
			TopItem it= this.editor.getHieroglyphicTextModel().getModel().getTopItemAt(i);
			MDCView v= this.editor.builder.buildView(it,editor.getDrawingSpecifications());
			editor.mdcView.replaceSubView(i,v);
		}
		
		this.editor.builder.reLayout(this.editor.mdcView,editor.getDrawingSpecifications());
	}

}