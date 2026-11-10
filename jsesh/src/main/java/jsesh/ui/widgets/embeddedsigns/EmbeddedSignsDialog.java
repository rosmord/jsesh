/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.widgets.embeddedsigns;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.function.Consumer;

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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.qenherkhopeshef.observable.ObservableEventListener;

import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.document.embeddedsigns.EmbeddedSignListChangedEvent;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.model.HieroglyphicText;
import jsesh.render.draw.MDCIconFactory;
import jsesh.resources.JSeshMessages;

/// A modal dialog listing the signs embedded in a document, to add, insert,
/// edit, remove and export them.
///
/// The dialog is modal so that it always shows the signs of the document
/// it was opened for.
public final class EmbeddedSignsDialog {

    private final Component parent;
    private final EmbeddedSignList signs;
    private final HieroglyphicText text;
    private final Consumer<String> inserter;
    private final MDCIconFactory icons;
    private File directory;

    private final DefaultListModel<EmbeddedSign> model = new DefaultListModel<>();
    private final JList<EmbeddedSign> list = new JList<>(model);
    private final JButton addButton = new JButton(JSeshMessages.getString("embeddedSigns.button.add"));
    private final JButton insertButton = new JButton(JSeshMessages.getString("embeddedSigns.button.insert"));
    private final JButton editButton = new JButton(JSeshMessages.getString("embeddedSigns.button.edit"));
    private final JButton removeButton = new JButton(JSeshMessages.getString("embeddedSigns.button.remove"));
    private final JButton exportButton = new JButton(JSeshMessages.getString("embeddedSigns.button.export"));
    private final JButton closeButton = new JButton(JSeshMessages.getString("embeddedSigns.button.close"));
    private JDialog dialog;

    private final ObservableEventListener<EmbeddedSignListChangedEvent> listener = e -> SwingUtilities
            .invokeLater(this::reload);

    /// @param parent    the component the dialog is centred on.
    /// @param signs     the signs of the document.
    /// @param shapes    the shapes used to draw the icons (they must include
    ///                  the embedded signs).
    /// @param text      the text of the document, to count the uses of a
    ///                  sign before removing it.
    /// @param inserter  inserts a sign code in the text (the dialog closes
    ///                  after inserting).
    /// @param directory the directory file dialogs start from, or `null`.
    public EmbeddedSignsDialog(Component parent, EmbeddedSignList signs, HieroglyphShapeRepository shapes,
            HieroglyphicText text, Consumer<String> inserter, File directory) {
        this.parent = parent;
        this.signs = signs;
        this.text = text;
        this.inserter = inserter;
        this.icons = new MDCIconFactory(shapes);
        this.directory = directory;
    }

    /// The directory last used by the file dialogs.
    public File getDirectory() {
        return directory;
    }

    /// Shows the dialog, and returns when it is closed.
    public void show() {
        dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                JSeshMessages.getString("embeddedSigns.dialog.title"), JDialog.DEFAULT_MODALITY_TYPE);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(buildContent());
        dialog.getRootPane().setDefaultButton(insertButton);
        reload();
        signs.addListener(listener);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        try {
            dialog.setVisible(true);
        } finally {
            signs.removeListener(listener);
        }
    }

    private JComponent buildContent() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);
        list.setCellRenderer(new SignRenderer());
        list.addListSelectionListener(e -> updateButtons());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    insert();
                }
            }
        });

        addButton.addActionListener(e -> add());
        insertButton.addActionListener(e -> insert());
        editButton.addActionListener(e -> edit());
        removeButton.addActionListener(e -> remove());
        exportButton.addActionListener(e -> export());
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttons.add(addButton);
        buttons.add(insertButton);
        buttons.add(editButton);
        buttons.add(removeButton);
        buttons.add(exportButton);
        buttons.add(closeButton);

        JPanel content = new JPanel(new BorderLayout(4, 4));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new java.awt.Dimension(460, 300));
        content.add(scroll, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        return content;
    }

    private void reload() {
        String selected = list.getSelectedValue() == null ? null : list.getSelectedValue().code();
        model.clear();
        for (EmbeddedSign s : signs.asList()) {
            model.addElement(s);
            if (s.code().equals(selected)) {
                list.setSelectedIndex(model.size() - 1);
            }
        }
        if (list.getSelectedIndex() < 0 && !model.isEmpty()) {
            list.setSelectedIndex(0);
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean hasSelection = list.getSelectedValue() != null;
        insertButton.setEnabled(hasSelection);
        editButton.setEnabled(hasSelection);
        removeButton.setEnabled(hasSelection);
        exportButton.setEnabled(!signs.isEmpty());
    }

    private void add() {
        EmbeddedSignOperations.addFromSvgFile(dialog, signs, directory).ifPresent(s -> {
            reload();
            list.setSelectedValue(s, true);
        });
    }

    private void insert() {
        EmbeddedSign s = list.getSelectedValue();
        if (s != null) {
            dialog.dispose();
            inserter.accept(s.code());
        }
    }

    private void edit() {
        EmbeddedSign s = list.getSelectedValue();
        if (s != null) {
            EmbeddedSignOperations.editProperties(dialog, signs, s.code());
            reload();
        }
    }

    private void remove() {
        EmbeddedSign s = list.getSelectedValue();
        if (s != null) {
            EmbeddedSignOperations.remove(dialog, signs, text, s.code());
            reload();
        }
    }

    private void export() {
        EmbeddedSignOperations.exportSvgFiles(dialog, signs, directory).ifPresent(f -> directory = f);
    }

    /// One line per sign: its picture, code, "variant of" code and description.
    private final class SignRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
            EmbeddedSign s = (EmbeddedSign) value;
            StringBuilder b = new StringBuilder("<html><b>").append(s.code()).append("</b>");
            if (s.hasVariantOf()) {
                b.append(" — ").append(JSeshMessages.format("embeddedSigns.variantOf.short", escape(s.variantOf())));
            }
            if (!s.description().isEmpty()) {
                b.append("<br>").append(escape(s.description()));
            }
            label.setText(b.append("</html>").toString());
            label.setIcon(icons.buildGlyphImage(s.code()));
            label.setIconTextGap(12);
            return label;
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }
}
