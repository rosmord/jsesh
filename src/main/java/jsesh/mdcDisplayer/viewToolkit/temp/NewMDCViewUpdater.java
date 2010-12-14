/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.viewToolkit.temp;

import java.util.Iterator;

import javax.print.Doc;

import org.qenherkhopeshef.algo.ReversibleMultiHashMap;

import jsesh.editor.JMDCEditor;
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
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;


/**
 * Updates an editor's view to keep it synchronized with its model.
 * @author S. Rosmorduc
 */
class NewMDCViewUpdater implements ModelOperationVisitor {

	private final JMDCEditor editor;

	private MDCDocumentView mdcDocumentView;
	
	/**
	 * @param editor
	 */
	NewMDCViewUpdater(JMDCEditor editor) {
		this.editor = editor;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitChildOperation(jsesh.mdc.model.operations.ChildOperation)
	 */
	public void visitChildOperation(ChildOperation operation) {
		mdcDocumentView.recomputeViewFor(operation.getElement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitDeletion(jsesh.mdc.model.operations.Deletion)
	 */
	public void visitDeletion(Deletion deletion) {
		mdcDocumentView.removeViewForElementRange(deletion.getStart(), deletion.getEnd());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitInsertion(jsesh.mdc.model.operations.Insertion)
	 */
	public void visitInsertion(Insertion insertion) {
		mdcDocumentView.insertViewFor(insertion.getElement(), insertion.getIndex());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitModification(jsesh.mdc.model.operations.Modification)
	 */
	public void visitModification(Modification modification) {
		mdcDocumentView= null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitReplacement(jsesh.mdc.model.operations.Replacement)
	 */
	public void visitReplacement(Replacement replacement) {
		mdcDocumentView= null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitZoneModification(jsesh.mdc.model.operations.ZoneModification)
	 */
	public void visitZoneModification(ZoneModification modification) {
				mdcDocumentView.replaceViews(modification.getStart(), modification.getEnd());
	}

}