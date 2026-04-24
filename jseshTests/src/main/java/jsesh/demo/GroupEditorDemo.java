package jsesh.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.editor.JMDCField;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.mdcDisplayer.context.JSeshTechRenderContext;
import jsesh.mdcDisplayer.mdcView.AbsoluteGroupBuilder;
import jsesh.swing.groupEditor.GroupEditorDialog;

/**
 * Demonstration of the Group editor (mainly for development purposes).
 * 
 * We use it when writing code for the group editor to check it works correctly,
 * The advantage is that it starts faster than the whole JSesh software.
 * @author rosmord
 */
class GroupEditorDemo extends JFrame {
     
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GroupEditorDemo());
    }

    JMDCField editor;
    GroupEditorDialog groupEditor;
    JButton validateButton;
    
    
    public GroupEditorDemo() throws HeadlessException {
        editor = new JMDCField();
        groupEditor = new GroupEditorDialog(new JSeshRenderContext(JSeshStyle.DEFAULT, HieroglyphShapeRepository.getStandardShapeRepository()));
        validateButton = new JButton("ok");
        validateButton.addActionListener(e -> getBackGroup());
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
    
    private void editGroup() {
        TopItemList topItems = editor.getHieroglyphicTextModel().getModel();
        // AbsoluteGroup group = AbsoluteGroupBuilder.createAbsoluteGroupFrom(topItems.asList(), editor.getDrawingSpecifications());
        // The absolute group builder has a problem when using alternate specifications,
        // especially specs.setStandardSignHeight(textHeight);
        AbsoluteGroupBuilder groupBuilder = new AbsoluteGroupBuilder();
        //topItems.asList(),MDCEditorKit.getBasicMDCEditorKit()
        AbsoluteGroup group = groupBuilder.createAbsoluteGroupFrom(topItems.asList(), new JSeshRenderContext(), JSeshTechRenderContext.VECTOR_CONTEXT);
        groupEditor.setGroup(group);
        TopItemList top = new TopItemList();
        top.addTopItem(group.buildTopItem());
        editor.getWorkflow().getHieroglyphicTextModel().setTopItemList(top);
    }
    
    
    public void getBackGroup() {
        AbsoluteGroup g = groupEditor.getGroup();
        // editor.getWorkflow().clear(); ok, but we want to fix the bug...
        try {
            editor.getWorkflow().setMDCCode("");
        } catch (MDCSyntaxError ex) {
            Logger.getLogger(GroupEditorDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        editor.getWorkflow().insertElement(g);
    }
}
