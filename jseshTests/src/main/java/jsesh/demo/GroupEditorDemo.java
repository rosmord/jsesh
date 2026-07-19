package jsesh.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsesh.render.style.JSeshStyle;
import jsesh.ui.editor.JMDCField;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.parser.MDCSyntaxError;
import jsesh.model.AbsoluteGroup;
import jsesh.model.TopItemList;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.view.AbsoluteGroupBuilder;
import jsesh.ui.widgets.groupEditor.GroupEditorDialog;

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
    
    HieroglyphShapeRepository shapeRepository;
    
    
    public GroupEditorDemo() throws HeadlessException {
    	shapeRepository = PredefinedFonts.buildAllEmbeddedFonts();
        editor = new JMDCField();
        groupEditor = new GroupEditorDialog(new JSeshRenderContext(JSeshStyle.DEFAULT, shapeRepository));
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
        JSeshRenderContext renderContext = new JSeshRenderContext(JSeshStyle.DEFAULT, shapeRepository);  
        AbsoluteGroup group = groupBuilder.createAbsoluteGroupFrom(topItems.asList(), renderContext, JSeshTechRenderContext.VECTOR_CONTEXT);
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
