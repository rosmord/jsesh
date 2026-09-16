
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.demo.swingdemos;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.ui.editor.JMDCEditor;

/**
 * Informal test for enclosures drawing.
 * @author rosmord
 */
public class CartoucheDemo {
    JFrame frame = new JFrame("Enclosure demo");
    JPanel root = new JPanel();
    JMDCEditor editor= new JMDCEditor();
    JMDCEditor editor1 = new JMDCEditor();
    JMDCEditor editor2= new JMDCEditor();
    JMDCEditor editor3 = new JMDCEditor();

    public CartoucheDemo() {
        String mdc = 
                "<-ra-mn:n-xpr->-!" +
                "<S-E2-A24-m-k:D40-km:t->-!"+
                "<H-p:t-H-kA->-!"+
                "<F-ra:xpr-kA*q:D140-A28-w:w->-!"+
                "<f0-ra:xpr-kA*q:D140-A28-w:w->-!"+
                "<f-ra:xpr-kA*q:D140-A28-w:w-0>-!"+
                "tA:n-<G-n-h-r:y-n:xAst->-m-Htp-!"
                ;
        editor.setMDCText(mdc);
        editor1.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        editor1.setMDCText(mdc);
        editor2.setTextOrientation(TextOrientation.VERTICAL);
        editor2.setMDCText(mdc);
        editor3.setTextOrientation(TextOrientation.VERTICAL);
        editor3.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        editor3.setMDCText(mdc);
        
        root.setLayout(new GridBagLayout());
        GridBagConstraints cc= new GridBagConstraints();
        
        root.add(editor,cc);
        cc.gridy = 1;
        root.add(editor1,cc);
         cc.gridy = 2;
        root.add(editor2,cc);
         cc.gridy = 3;
        root.add(editor3,cc);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(root), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);              
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CartoucheDemo());
    }
}
