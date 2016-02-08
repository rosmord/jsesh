/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.jhotdrawCustom;

import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.JFileURIChooser;
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
