/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/// Helpers to build forms which fit on small screens.
///
/// The settings dialogs have many rows with long labels. To keep them usable
/// on a laptop screen, they are split into titled sections, shown side by side
/// when the screen is wide enough, and their labels wrap instead of widening
/// the dialog. [JSimpleDialog] adds the scroll bars when the form is still
/// too large.
public final class FormLayoutHelper {

    /// The width at which the labels of the forms wrap, in pixels.
    public static final int LABEL_WIDTH = 220;

    /// The part of the screen a dialog may use.
    private static final double SCREEN_FRACTION = 0.9;

    private FormLayoutHelper() {
    }

    /// @param text the text of the label (plain text, not HTML).
    /// @return a label which wraps its text at [#LABEL_WIDTH].
    public static JLabel wrappingLabel(String text) {
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return new JLabel("<html><body style='width:" + LABEL_WIDTH + "px'>" + escaped + "</body></html>");
    }

    /// @param title the title of the section, already translated.
    /// @return a titled panel with two columns: labels, then fields.
    public static JPanel section(String title) {
        JPanel panel = new JPanel(new MigLayout("insets 4, fillx", "[][grow, fill]"));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    /// @param component a component of the window, or null.
    /// @return the part of the screen a dialog may use: the screen of the
    ///         component (or the default screen) minus the menu bar, the dock
    ///         or the task bar, and a margin.
    public static Rectangle usableScreenBounds(Component component) {
        try {
            GraphicsConfiguration gc = component == null ? null : component.getGraphicsConfiguration();
            Rectangle bounds;
            if (gc == null) {
                bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            } else {
                bounds = gc.getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                bounds = new Rectangle(bounds.x + insets.left, bounds.y + insets.top,
                        bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
            }
            return new Rectangle(bounds.x, bounds.y, (int) (bounds.width * SCREEN_FRACTION),
                    (int) (bounds.height * SCREEN_FRACTION));
        } catch (HeadlessException e) {
            return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    /// Tells if sections can be placed side by side.
    ///
    /// @param width the width the sections would need side by side.
    /// @return true if the default screen is wide enough.
    public static boolean hasRoomFor(int width) {
        return width <= usableScreenBounds(null).width;
    }
}
