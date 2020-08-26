/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 * Created on 28 nov. 2004
 */
package jsesh.swing.groupEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import jsesh.mdc.model.AbsoluteGroup;

/**
 * Dialog panel for editing groups.
 *
 * @author S. Rosmorduc
 */
public final class GroupEditorDialog extends JPanel {

    private final JButton next;

    private final JButton previous, reset;

    private final JToggleButton rotate;

    private final JToggleButton resize;

    private final JToggleButton move;

    private final GroupEditor editor;

    public GroupEditorDialog() {
        setBackground(Color.WHITE);
        editor = new GroupEditor();
        next = new JButton("next");
        previous = new JButton("previous");
        reset = new JButton("reset");
        rotate = new JToggleButton("rotate");
        resize = new JToggleButton("resize");
        move = new JToggleButton("move");

        prepareLayout();
        activateControls();
        setPreferredSize(new Dimension(640, 480));
    }

    private void prepareLayout() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rotate);
        buttonGroup.add(resize);
        buttonGroup.add(move);
        JToolBar sub = new JToolBar(SwingConstants.HORIZONTAL);
        sub.add(previous);
        sub.add(next);
        sub.add(reset);
        sub.add(rotate);
        sub.add(resize);
        sub.add(move);
        setLayout(new BorderLayout());
        add(new JScrollPane(editor), BorderLayout.CENTER);
        add(sub, BorderLayout.SOUTH);

    }

    private void activateControls() {
        editor.setGroupEditorMode(new MoveMode());
        previous.addActionListener(e -> editor.previous());
        next.addActionListener(e -> editor.next());
        reset.addActionListener(e -> editor.resetSign());
        rotate.addActionListener(e -> editor.setGroupEditorMode(new RotateMode()));
        resize.addActionListener(e -> editor.setGroupEditorMode(new ResizeMode()));
        move.addActionListener(e -> editor.setGroupEditorMode(new MoveMode()));
    }

    public void setGroup(AbsoluteGroup group) {
        editor.setGroup(group);
    }

    public AbsoluteGroup getGroup() {
        return editor.getGroup();
    }
}
