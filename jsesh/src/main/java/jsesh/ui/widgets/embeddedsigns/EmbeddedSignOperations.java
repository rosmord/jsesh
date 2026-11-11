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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.qenherkhopeshef.swingUtils.portableFileDialog.FileExtensionFilter;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.document.embeddedsigns.EmbeddedSignShapes;
import jsesh.model.HieroglyphicText;
import jsesh.model.tools.HieroglyphCodesExtractor;
import jsesh.resources.JSeshMessages;

/// The user interactions on the signs embedded in a document: adding a sign
/// from an SVG file, editing its properties, removing it, exporting the
/// drawings.
///
/// Each method shows its own dialogs, relative to a parent component. They
/// are shared by [EmbeddedSignsDialog] and the menu actions of the
/// application.
public final class EmbeddedSignOperations {

    private EmbeddedSignOperations() {
    }

    /// Asks for an SVG file, then for the properties of the new sign, and adds it.
    ///
    /// @param parent    the parent of the dialogs.
    /// @param signs     the list to add the sign to.
    /// @param directory the directory to start from, or `null`.
    /// @return the new sign, or nothing if the user cancelled or the file
    ///         couldn't be read (an error message has then been shown).
    public static Optional<EmbeddedSign> addFromSvgFile(Component parent, EmbeddedSignList signs, File directory) {
        PortableFileDialog chooser = PortableFileDialogFactory.createFileOpenDialog(parent);
        chooser.setTitle(JSeshMessages.getString("embeddedSigns.chooseSvg.title"));
        chooser.setFileFilters(new FileExtensionFilter[] {
                new FileExtensionFilter("svg", JSeshMessages.getString("embeddedSigns.svgFiles")) });
        if (directory != null) {
            chooser.setCurrentDirectory(directory);
        }
        if (chooser.show() != FileOperationResult.OK || chooser.getSelectedFile() == null) {
            return Optional.empty();
        }
        File file = chooser.getSelectedFile();
        byte[] svg;
        try {
            svg = Files.readAllBytes(file.toPath());
            EmbeddedSignShapes.checkSvg(svg, file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    JSeshMessages.format("embeddedSigns.readError.message", file.getName()),
                    JSeshMessages.getString("embeddedSigns.readError.title"), JOptionPane.ERROR_MESSAGE);
            return Optional.empty();
        }
        return askProperties(parent, JSeshMessages.getString("embeddedSigns.add.title"), null, "")
                .map(p -> signs.add(svg, p.variantOf(), p.description()));
    }

    /// Lets the user change the "variant of" code and the description of a sign.
    ///
    /// @return the updated sign, or nothing if the user cancelled.
    public static Optional<EmbeddedSign> editProperties(Component parent, EmbeddedSignList signs, String code) {
        Optional<EmbeddedSign> sign = signs.get(code);
        if (sign.isEmpty()) {
            return Optional.empty();
        }
        return askProperties(parent, JSeshMessages.format("embeddedSigns.edit.title", code),
                sign.get().variantOf(), sign.get().description())
                .map(p -> signs.update(code, p.variantOf(), p.description()));
    }

    /// Removes a sign, after a warning if the text uses it.
    ///
    /// @param text the text of the document, to count the uses of the sign.
    /// @return true if the sign was removed.
    public static boolean remove(Component parent, EmbeddedSignList signs, HieroglyphicText text, String code) {
        int uses = countUses(text, code);
        String message = uses == 0
                ? JSeshMessages.format("embeddedSigns.remove.unused", code)
                : JSeshMessages.format("embeddedSigns.remove.used", code, Integer.toString(uses));
        int answer = JOptionPane.showConfirmDialog(parent, message,
                JSeshMessages.getString("embeddedSigns.remove.title"), JOptionPane.OK_CANCEL_OPTION,
                uses == 0 ? JOptionPane.QUESTION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.OK_OPTION && signs.remove(code);
    }

    /// The number of occurrences of a sign code in a text.
    public static int countUses(HieroglyphicText text, String code) {
        return Collections.frequency(new HieroglyphCodesExtractor(false).extractHieroglyphs(text), code);
    }

    /// Asks for a folder and writes one `sign<n>.svg` file per sign in it (the
    /// user's original files).
    ///
    /// @param directory the directory to start from, or `null`.
    /// @return the folder written to, or nothing if the user cancelled or
    ///         writing failed (an error message has then been shown).
    public static Optional<File> exportSvgFiles(Component parent, EmbeddedSignList signs, File directory) {
        if (signs.isEmpty()) {
            JOptionPane.showMessageDialog(parent, JSeshMessages.getString("embeddedSigns.export.none"));
            return Optional.empty();
        }
        PortableFileDialog chooser = PortableFileDialogFactory.createDirectorySaveDialog(parent);
        chooser.setTitle(JSeshMessages.getString("embeddedSigns.export.title"));
        if (directory != null) {
            chooser.setCurrentDirectory(directory);
        }
        if (chooser.show() != FileOperationResult.OK || chooser.getSelectedFile() == null) {
            return Optional.empty();
        }
        File folder = chooser.getSelectedFile();
        long existing = signs.asList().stream().filter(s -> svgFile(folder, s).exists()).count();
        if (existing > 0) {
            int answer = JOptionPane.showConfirmDialog(parent,
                    JSeshMessages.format("embeddedSigns.export.overwrite", Long.toString(existing),
                            folder.getName()),
                    JSeshMessages.getString("embeddedSigns.export.title"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }
        }
        try {
            for (EmbeddedSign s : signs.asList()) {
                Files.write(svgFile(folder, s).toPath(), s.svg());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    JSeshMessages.format("embeddedSigns.export.error", e.getMessage()),
                    JSeshMessages.getString("embeddedSigns.export.title"), JOptionPane.ERROR_MESSAGE);
            return Optional.empty();
        }
        JOptionPane.showMessageDialog(parent,
                JSeshMessages.format("embeddedSigns.export.done", Integer.toString(signs.size()),
                        folder.getPath()));
        return Optional.of(folder);
    }

    private static File svgFile(File folder, EmbeddedSign sign) {
        return new File(folder, sign.code() + ".svg");
    }

    private record Properties(String variantOf, String description) {
    }

    /// Asks for the "variant of" code and the description, until the code is
    /// valid or the user cancels.
    private static Optional<Properties> askProperties(Component parent, String title, String variantOf,
            String description) {
        JTextField variantField = new JTextField(variantOf == null ? "" : variantOf, 10);
        JTextArea descriptionArea = new JTextArea(description, 4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel(JSeshMessages.getString("embeddedSigns.variantOf.label")), c);
        c.gridx = 1;
        panel.add(variantField, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        panel.add(new JLabel(JSeshMessages.getString("embeddedSigns.description.label")), c);
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        JPanel areaPanel = new JPanel(new BorderLayout());
        areaPanel.add(new JScrollPane(descriptionArea));
        panel.add(areaPanel, c);

        while (true) {
            int answer = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }
            String variant = variantField.getText().trim();
            if (!EmbeddedSign.isValidVariantOf(variant)) {
                JOptionPane.showMessageDialog(parent, JSeshMessages.getString("embeddedSigns.variantOf.error"),
                        title, JOptionPane.ERROR_MESSAGE);
            } else {
                return Optional.of(new Properties(variant, descriptionArea.getText()));
            }
        }
    }
}
