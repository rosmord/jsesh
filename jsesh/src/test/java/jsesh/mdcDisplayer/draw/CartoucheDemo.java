
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.mdcDisplayer.draw;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCEditor;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

/**
 * Informal test for enclosures drawing.
 * @author rosmord
 */
public class CartoucheDemo {
    JFrame frame = new JFrame("Enclosure demo");
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
                "<f-ra:xpr-kA*q:D140-A28-w:w-0>-!"
                ;
        editor.setMDCText(mdc);
        editor1.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        editor1.setMDCText(mdc);
        editor2.setTextOrientation(TextOrientation.VERTICAL);
        editor2.setMDCText(mdc);
        editor3.setTextOrientation(TextOrientation.VERTICAL);
        editor3.setTextDirection(TextDirection.RIGHT_TO_LEFT);
        editor3.setMDCText(mdc);
        
        frame.setLayout(new GridBagLayout());
        GridBagConstraints cc= new GridBagConstraints();
        
        frame.add(editor,cc);
        cc.gridy = 1;
        frame.add(editor1,cc);
         cc.gridy = 2;
        frame.add(editor2,cc);
         cc.gridy = 3;
        frame.add(editor3,cc);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Example from the White chapel of Sesostris the first.        
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CartoucheDemo());
    }
}
