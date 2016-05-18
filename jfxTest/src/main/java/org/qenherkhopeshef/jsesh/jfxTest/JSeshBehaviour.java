/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import java.util.List;

/**
 *
 * @author rosmord
 */
class JSeshBehaviour extends BehaviorBase<JSeshControl> {
    
    public JSeshBehaviour(JSeshControl control, List<KeyBinding> keyBindings) {
        super(control, keyBindings);
    }
    
}
