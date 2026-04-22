package jsesh.jhotdraw.jhotdrawCustom;

import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.URIChooser;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXLikeApplication;

/**
 * Custumized version of the application class.
 * @author rosmord
 */
public class CustomApplicationBase extends QenherOSXLikeApplication {

    @Override
    public URIChooser getSaveChooser(View v) {
        return null;
        
    }

    @Override
    public URIChooser getOpenChooser(View v) {
        return null;
    }
    
}
