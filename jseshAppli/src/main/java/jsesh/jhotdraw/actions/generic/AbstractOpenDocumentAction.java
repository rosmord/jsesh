package jsesh.jhotdraw.actions.generic;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.app.action.file.OpenFileAction;
import org.qenherkhopeshef.jhotdrawChanges.ApplicationHelper;

/**
 * ONGOING WORK!!!!
 * 
 * Generic document opener action. loads a document into an empty view. If no
 * empty view is available, a new view is created.
 * 
 * <p>
 * Adapted from Werner Randelshofer's OpenFileAction, but without the document
 * selection part.
 * 
 * <p>
 * We take care of keeping a code which might be used in a compatible way with
 * OpenFileAction (i.e. it should be possible to retrofit OpenFileAction to use
 * this code).
 * 
 *  see {@link OpenFileAction}
 * @author Werner Randelshofer, changes by S. Rosmorduc
 */
@SuppressWarnings("serial")
public abstract class AbstractOpenDocumentAction extends
		AbstractApplicationAction {

	/** Creates a new instance. */
	public AbstractOpenDocumentAction(Application app) {
		super(app);
	}

	/**
	 * perform the actual document opening.
	 */
	public void actionPerformed(ActionEvent e) {
		final Application app = getApplication();
		final View view = ApplicationHelper.findEmptyView(app);
		URI uri = getDocumentURI();

		if (uri != null) {
			app.show(view);
			new ViewOpenerForDocument(app).openViewFromURI(view, uri);
		}
	}

	/**
	 * Returns the document which should be opened, or null if no document was
	 * selected. This is where a document can be selected, for instance. Some
	 * subclasses might have constant URI returned.
	 * 
	 * @return an URI, or null if no document should be opened after all.
	 */
	protected abstract URI getDocumentURI();
}
