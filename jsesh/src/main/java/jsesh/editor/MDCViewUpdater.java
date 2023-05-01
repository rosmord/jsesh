/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 1 nov. 2004 
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
import jsesh.mdcDisplayer.layout.ViewBuilder;
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

        private MDCView getView() {
            return editor.getView();
        }
	
        @Override
	public void visitChildOperation(ChildOperation operation) {
		int k;
		ViewBuilder builder= new ViewBuilder();
		// Find the index for the element which was modified...
		for (k = 0; k < getView().getNumberOfSubviews()
				&& getView().getSubView(k).getModel() != operation
						.getChildOperation().getElement(); k++)
			/*empty*/;
		// This is k. rebuild view for k, and replace it.
		if (this.getView().getSubView(k).getModel() == operation
				.getChildOperation().getElement()) {
			MDCView subv = builder.buildView(operation.getChildOperation()
					.getElement(), editor.getDrawingSpecifications());
			this.getView().replaceSubView(k, subv);
		}
		// update the page layout.
		builder.reLayout(getView(), editor.getDrawingSpecifications());
	}

	
        @Override
	public void visitDeletion(Deletion deletion) {
		// Remove the modified views, and update page layout.
		getView().remove(deletion.getStart(), deletion.getEnd());
		new ViewBuilder().reLayout(getView(),editor.getDrawingSpecifications());
	}

	
        @Override
	public void visitInsertion(Insertion insertion) {
		ViewBuilder builder= new ViewBuilder();
		int index= insertion.getIndex();
		// Create and insert views at the proper position.
		for (Iterator i= insertion.getChildren().iterator(); i.hasNext();) {
			MDCView subView = builder.buildView((ModelElement) i.next(),editor.getDrawingSpecifications());
			getView().addAt(index++, subView);
		}
		builder.reLayout(getView(),editor.getDrawingSpecifications());
	}

	
        @Override
	public void visitModification(Modification modification) {
		// Huge change: we suppress the whole view, which will cause the complete recomputation of the page.		
                editor.invalidateView();
	}

	
        @Override
	public void visitReplacement(Replacement replacement) {
		editor.invalidateView();
	}

	
        @Override
	public void visitZoneModification(ZoneModification modification) {
		ViewBuilder builder= new ViewBuilder();
		for (int i= modification.getStart(); i< modification.getEnd(); i++) {
			TopItem it= this.editor.getHieroglyphicTextModel().getModel().getTopItemAt(i);
			MDCView v= builder.buildView(it,editor.getDrawingSpecifications());
			getView().replaceSubView(i,v);
		}
		
		builder.reLayout(getView(),editor.getDrawingSpecifications());
	}

}