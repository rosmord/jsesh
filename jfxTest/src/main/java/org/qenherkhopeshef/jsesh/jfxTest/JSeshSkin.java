/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;

/**
 *
 * @author rosmord
 */

class JSeshSkin extends SkinBase<JSeshControl> implements Skin<JSeshControl>{

    public JSeshSkin(JSeshControl control) {
        super(control);
    }
    
    
    
    
}
//class JSeshSkin extends VirtualContainerBase<JSeshControl, JSeshBehaviour, IndexedCell> {
//
//    public JSeshSkin(JSeshControl control, JSeshBehaviour behavior) {
//        super(control, behavior);
//    }
//
//
//    @Override
//    public IndexedCell createCell() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public int getItemCount() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    protected void updateRowCount() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//    
//}
