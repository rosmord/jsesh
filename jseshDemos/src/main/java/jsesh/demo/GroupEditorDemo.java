/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCField;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.AbsoluteGroupBuilder;
import jsesh.swing.groupEditor.GroupEditorDialog;

/**
 * Demo of the Group editor (mainly for development purposes).
 * @author rosmord
 */
public final class GroupEditorDemo extends JFrame{
    
    JMDCField editor;
    GroupEditorDialog groupEditor;
    JButton validateButton;
    
    
    public GroupEditorDemo() throws HeadlessException {
        editor = new JMDCField();
        groupEditor = new GroupEditorDialog();
        validateButton = new JButton("ok");
        validateButton.addActionListener(e -> getBackGroup());
        groupEditor.setDrawingSpecifications(editor.getDrawingSpecifications());
        setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1;
        add(editor,cc);
        cc.fill = GridBagConstraints.BOTH;
        cc.weighty = 1;
        cc.gridy = 1;
        add(groupEditor,cc);
        cc.gridy = 2;
        add(validateButton, cc);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editor.addActionListener(e -> editGroup());
        editor.setMDCText("p*t:pt");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GroupEditorDemo());
    }

    private void editGroup() {
        TopItemList topItems = editor.getHieroglyphicTextModel().getModel();
        AbsoluteGroup group = AbsoluteGroupBuilder.createAbsoluteGroupFrom(topItems.asList(), editor.getDrawingSpecifications());
        groupEditor.setGroup(group);
    }
    
    
    public void getBackGroup() {
        AbsoluteGroup g = groupEditor.getGroup();
        // editor.getWorkflow().clear(); ok, but we want to fix the bug...
        try {
            editor.getWorkflow().setMDCCode("");
        } catch (MDCSyntaxError ex) {
            Logger.getLogger(GroupEditorDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        editor.getWorkflow().insertElement(g.buildTopItem());
    }

}
