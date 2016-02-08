/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.jhotdrawCustom;

import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.URIChooser;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXApplication;

/**
 * Customized version of the application class.
 * @author rosmord
 */
public class CustomApplicationBaseOSX extends QenherOSXApplication {

    @Override
    public URIChooser getSaveChooser(View v) {
        return new QenherkhURIChooser();
        
    }

    @Override
    public URIChooser getOpenChooser(View v) {
        return new QenherkhURIChooser();
    }
    
}
