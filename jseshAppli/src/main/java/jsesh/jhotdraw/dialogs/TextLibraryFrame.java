/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jsesh.jseshtexts.JSeshTextLibrary;
import jsesh.resources.JSeshMessages;

/// Browser for the JSesh text library: a search field above an alphabetical list of titles.
///
/// Double-click or Enter opens the selected text; Up/Down in the search field move the selection.
public class TextLibraryFrame extends JFrame {

    private final List<JSeshTextLibrary.Entry> allEntries;
    private final Consumer<JSeshTextLibrary.Entry> opener;
    private final DefaultListModel<JSeshTextLibrary.Entry> listModel = new DefaultListModel<>();
    private final JList<JSeshTextLibrary.Entry> list = new JList<>(listModel);
    private final JTextField searchField = new JTextField();

    public TextLibraryFrame(List<JSeshTextLibrary.Entry> entries, Consumer<JSeshTextLibrary.Entry> opener) {
        super(JSeshMessages.getString("file.textLibrary.title"));
        this.allEntries = entries;
        this.opener = opener;
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object value, int index,
                    boolean selected, boolean focus) {
                String text = value instanceof JSeshTextLibrary.Entry e ? e.title() : String.valueOf(value);
                return super.getListCellRendererComponent(l, text, index, selected, focus);
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelected();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
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
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER -> openSelected();
                    case KeyEvent.VK_DOWN -> moveSelection(1);
                    case KeyEvent.VK_UP -> moveSelection(-1);
                    default -> {
                    }
                }
            }
        });

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        top.add(new JLabel(JSeshMessages.getString("file.textLibrary.search")), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        setPreferredSize(new Dimension(520, 520));
        applyFilter();
        pack();
        setLocationByPlatform(true);
    }

    public void focusSearchField() {
        searchField.requestFocusInWindow();
        searchField.selectAll();
    }

    private void applyFilter() {
        String query = normalize(searchField.getText());
        listModel.clear();
        for (JSeshTextLibrary.Entry entry : allEntries) {
            if (query.isEmpty() || normalize(entry.title()).contains(query)
                    || normalize(entry.path()).contains(query)) {
                listModel.addElement(entry);
            }
        }
        if (!listModel.isEmpty()) {
            list.setSelectedIndex(0);
        }
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

    private void openSelected() {
        JSeshTextLibrary.Entry entry = list.getSelectedValue();
        if (entry != null) {
            opener.accept(entry);
        }
    }

    /// Case- and accent-insensitive form of a string, for searches.
    private static String normalize(String s) {
        String decomposed = Normalizer.normalize(s, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
