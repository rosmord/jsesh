/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.demo.mdi;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;

/**
 * 
 * A demonstration for an external palette whose control will be redirected to
 * the current focused window (if any).
 * 
 * @author rosmord
 */
public class JTextBasicPalette extends JDialog {

    JList list= new JList();
    public JTextBasicPalette() {
        DefaultListModel model= new DefaultListModel();
        model.addElement("foo");
        model.addElement("bar");
        add(list);
        pack();
    }
}
