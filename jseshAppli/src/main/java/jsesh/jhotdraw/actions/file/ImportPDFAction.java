package jsesh.jhotdraw.actions.file;

import java.net.URI;
import java.net.URISyntaxException;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.generic.AbstractOpenDocumentAction;

import org.jhotdraw_7_4_1.app.Application;

/**
 * Import a PDF pasted on the clipboard.
 * @author rosmord
 *
 */
@SuppressWarnings("serial")
public class ImportPDFAction extends AbstractOpenDocumentAction {
	public static final String ID= "file.import.pdf";

	public ImportPDFAction(Application app) {
		super(app);
		BundleHelper.configure(this);
	}

	
	@Override
	protected URI getDocumentURI() {
		try {
			// Return pseudo-uri for pdf on clipboard.
			return new URI("clipboard:pdf");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


}
