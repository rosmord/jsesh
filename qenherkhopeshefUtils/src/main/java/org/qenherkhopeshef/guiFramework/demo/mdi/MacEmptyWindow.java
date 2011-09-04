/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.Dimension;

import javax.swing.JFrame;

/**
 * an hidden window used on the mac as a "menu holder".
 * @author rosmord
 */
public class MacEmptyWindow extends JFrame {
	private static final long serialVersionUID = 7295573756327729723L;

	public MacEmptyWindow(String title) {
        super(title);
        setUndecorated(true);
        setSize(new Dimension(0, 0));
        setLocation(-100, -100);
    }
}
