
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.demo;

import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCEditor;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.TopItemList;

/**
 * A demo for a bug we have currently. It should move to a test file at some
 * point, but we need to isolate it first.
 *
 * @author rosmord
 */
public class ViewUpdateBug {

    JFrame frame;
    JMDCEditor editor;
    JButton replaceWithG1Button;

    public ViewUpdateBug() {
        frame = new JFrame("demo");
        editor = new JMDCEditor();
        replaceWithG1Button = new JButton("replace with G1 and insert A1");
        frame.setLayout(new FlowLayout());
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setSize(400, 300);
        editor.setMDCText("i-w-A1-m-pr:Z1-k");

        frame.add(scroll);
        frame.add(replaceWithG1Button);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        replaceWithG1Button.addActionListener(e -> replace());
    }

    public void replace() {
        try {
            // editor.getWorkflow().setHieroglyphicTextModel(new HieroglyphicTextModel());
            TopItemList topItemList = new TopItemList();
            topItemList.addTopItem(new Hieroglyph("B1").buildTopItem());
            editor.getWorkflow().getHieroglyphicTextModel().setTopItemList(topItemList);
            //editor.getWorkflow().getHieroglyphicTextModel().setTopItemList(new TopItemList());
             //editor.getWorkflow().setMDCCode("m-n-x");
            editor.getWorkflow().insertElement(new Hieroglyph("A1")); // g is some kind of element...
        } catch (Exception ex) {
            Logger.getLogger(ViewUpdateBug.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewUpdateBug());
    }
}
