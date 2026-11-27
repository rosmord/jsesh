/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jsesh.jseshtemplates.JSeshTemplateLibrary.Template;
import jsesh.resources.JSeshMessages;

/// Modal dialog choosing a template: a filter field above the list of template
/// names, the description of the selected one, and a button to create a text
/// from it.
///
/// Double-click or Enter chooses the selected template; Up/Down in the filter
/// field move the selection; Escape cancels.
public class NewFromTemplateDialog extends JDialog {

    private final List<Template> allTemplates;
    private final DefaultListModel<Template> listModel = new DefaultListModel<>();
    private final JList<Template> list = new JList<>(listModel);
    private final JTextField filterField = new JTextField();
    private final JTextArea description = new JTextArea();
    private final JButton createButton = new JButton(JSeshMessages.getString("file.newFromTemplate.create"));

    private Template chosen;

    public NewFromTemplateDialog(Window owner, List<Template> templates) {
        super(owner, JSeshMessages.getString("file.newFromTemplate.title"), ModalityType.APPLICATION_MODAL);
        this.allTemplates = templates;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initList();
        initFilterField();

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setFont(UIManager.getFont("Label.font"));
        description.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        top.add(new JLabel(JSeshMessages.getString("file.newFromTemplate.filter")), BorderLayout.WEST);
        top.add(filterField, BorderLayout.CENTER);

        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list), new JScrollPane(description));
        center.setDividerLocation(260);
        center.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton cancelButton = new JButton(JSeshMessages.getString("file.newFromTemplate.cancel"));
        cancelButton.addActionListener(e -> dispose());
        createButton.addActionListener(e -> chooseSelected());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(createButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // Enter, wherever the focus is.
        getRootPane().setDefaultButton(createButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                filterField.requestFocusInWindow();
            }
        });

        setPreferredSize(new Dimension(640, 420));
        applyFilter();
        pack();
        setLocationRelativeTo(owner);
    }

    /// Show the dialog, and wait until it is closed.
    ///
    /// @return the chosen template, empty if the user cancelled.
    public Optional<Template> showDialog() {
        setVisible(true);
        return Optional.ofNullable(chosen);
    }

    private void initList() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                    boolean selected, boolean focus) {
                String text = value instanceof Template t ? t.name() : String.valueOf(value);
                return super.getListCellRendererComponent(l, text, index, selected, focus);
            }
        });
        list.addListSelectionListener(e -> showSelection());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    chooseSelected();
                }
            }
        });
    }

    private void initFilterField() {
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> moveSelection(1);
                    case KeyEvent.VK_UP -> moveSelection(-1);
                    default -> {
                    }
                }
            }
        });
    }

    /// Keep the templates whose name or description contains the filter text.
    private void applyFilter() {
        String query = SearchText.normalize(filterField.getText());
        listModel.clear();
        for (Template template : allTemplates) {
            if (SearchText.normalize(template.name()).contains(query)
                    || SearchText.normalize(template.description()).contains(query)) {
                listModel.addElement(template);
            }
        }
        if (!listModel.isEmpty()) {
            list.setSelectedIndex(0);
        }
        showSelection();
    }

    private void showSelection() {
        Template template = list.getSelectedValue();
        description.setText(template == null ? "" : template.description());
        description.setCaretPosition(0);
        createButton.setEnabled(template != null);
    }

    private void moveSelection(int delta) {
        int size = listModel.getSize();
        if (size == 0) {
            return;
        }
        int index = Math.max(0, Math.min(size - 1, list.getSelectedIndex() + delta));
        list.setSelectedIndex(index);
        list.ensureIndexIsVisible(index);
    }

    private void chooseSelected() {
        Template template = list.getSelectedValue();
        if (template != null) {
            chosen = template;
            dispose();
        }
    }
}
