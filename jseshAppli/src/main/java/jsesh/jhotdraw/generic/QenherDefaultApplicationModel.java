package jsesh.jhotdraw.generic;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.DefaultApplicationModel;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.URIChooser;

/**
 * Default application model customized for my taste.
 * @author rosmord
 *
 */
@SuppressWarnings("serial")
public class QenherDefaultApplicationModel extends DefaultApplicationModel {

	@Override
	public URIChooser createOpenChooser(Application a, View v) {
		// TODO Auto-generated method stub
		return super.createOpenChooser(a, v);
	}
	
	@Override
	public URIChooser createSaveChooser(Application a, View v) {
		// TODO Auto-generated method stub
		return super.createSaveChooser(a, v);
	}
	
	@Override
	public URIChooser createOpenDirectoryChooser(Application a, View v) {
		// TODO Auto-generated method stub
		return super.createOpenDirectoryChooser(a, v);
	}
	
}
