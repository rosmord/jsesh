/*
 * Created on 28 nov. 2004
 */
package jsesh.swing.groupEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStreamWriter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;

/**
 * Dialog panel for editing groups.
 * 
 * @author S. Rosmorduc
 */
public class GroupEditorDialog extends JPanel {

    private JButton next, previous, reset;

    
    private JToggleButton rotate;

    private JToggleButton resize;
    
    private JToggleButton move;

    private GroupEditor editor;

    public GroupEditorDialog() {
        setBackground(Color.WHITE);
        editor = new GroupEditor();
        GroupEditorControl control = new GroupEditorControl(editor);

        ActionListener l = new GroupEditorDialogListener();
        next= new JButton("next");
        previous= new JButton("previous");
        reset= new JButton("reset");
        ButtonGroup buttonGroup= new ButtonGroup();
        rotate= new JToggleButton("rotate");
        resize= new JToggleButton("resize");
        move= new JToggleButton("move");
        buttonGroup.add(rotate);
        buttonGroup.add(resize);
        buttonGroup.add(move);
        JToolBar sub= new JToolBar(SwingConstants.HORIZONTAL);
        //sub.setLayout(new BoxLayout(sub, BoxLayout.X_AXIS));
        sub.add(previous);
        previous.addActionListener(l);
        sub.add(next);
        next.addActionListener(l);
        sub.add(reset);
        reset.addActionListener(l);
        sub.add(rotate);
        rotate.addActionListener(l);
        sub.add(resize);
        resize.addActionListener(l);
        sub.add(move);
        move.addActionListener(l);
        
        setLayout(new BorderLayout());

        add(new JScrollPane(editor), BorderLayout.CENTER);
        add(sub, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(640, 480));
        
    }

    public void setGroup(AbsoluteGroup group) {
        editor.setGroup(group);
    }

    public AbsoluteGroup getGroup() {
        return editor.getGroup();
    }

    private class GroupEditorDialogListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == next) {
                editor.next();
            }
            else if (e.getSource() == previous)
                editor.previous();
            else if (e.getSource() == rotate)
                editor.setMode(GroupEditor.ROTATION);
            else if (e.getSource()== resize)
                editor.setMode(GroupEditor.RESIZE);
            else if (e.getSource() == reset)
                editor.resetSign();
            else if (e.getSource() == move)
            	editor.setMode(GroupEditor.MOVE);
        }

    }

    public static void main(String[] args) {
        AbsoluteGroup g = new AbsoluteGroup();
        g.addHieroglyph(new Hieroglyph("t"));
        g.addHieroglyph(new Hieroglyph("A"));
        g.addHieroglyph(new Hieroglyph("x"));
        g.addHieroglyph(new Hieroglyph("anx"));
        final GroupEditorDialog editor = new GroupEditorDialog();
        editor.setGroup(g);
        JFrame f = new JFrame();
        f.getContentPane().add(editor);
        f.pack();
        f.addWindowListener(new WindowAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosing(WindowEvent e) {
                MdCModelWriter m= new MdCModelWriter();
                TopItemList l= new TopItemList();
                editor.getGroup().compact();
                l.addTopItem(editor.getGroup().buildTopItem());            
                try {
                    OutputStreamWriter w = new OutputStreamWriter(System.out);
                    m.write(w, l);
                    w.flush();
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        f.setVisible(true);
    }

}