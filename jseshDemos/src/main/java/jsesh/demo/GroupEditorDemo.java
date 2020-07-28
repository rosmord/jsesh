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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCField;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.AbsoluteGroupBuilder;
import jsesh.swing.groupEditor.GroupEditorDialog;

/**
 * Demo of the Group editor (mainly for development purposes).
 * @author rosmord
 */
public final class GroupEditorDemo extends JFrame{
    
    JMDCField editor = new JMDCField();
    GroupEditorDialog groupEditor = new GroupEditorDialog();;
    
    public GroupEditorDemo() throws HeadlessException {
        setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1;
        add(editor,cc);
        cc.fill = GridBagConstraints.BOTH;
        cc.weighty = 1;
        cc.gridy = 1;
        add(groupEditor,cc);
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

}
