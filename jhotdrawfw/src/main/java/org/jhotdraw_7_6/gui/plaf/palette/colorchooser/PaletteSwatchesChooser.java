/*
 * @(#)PaletteSwatchesChooser.java
 * 
 * Copyright (c) 2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.gui.plaf.palette.colorchooser;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ListModel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;

import org.jhotdraw_7_6.color.HSBColorSpace;
import org.jhotdraw_7_6.gui.plaf.palette.PaletteListUI;
import org.jhotdraw_7_6.gui.plaf.palette.PaletteLookAndFeel;
import org.jhotdraw_7_6.gui.plaf.palette.PalettePanelUI;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * PaletteSwatchesChooser.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class PaletteSwatchesChooser extends AbstractColorChooserPanel {

    private int updateRecursion = 0;


    /**
     * HSB color palette with a set of colors chosen based on a physical criteria.
     * <p>
     * This is a 'human friendly' color palette which arranges the color in a
     * way that makes it easy for humans to select the desired
     * color. The colors are ordered in a way which minimizes the color contrast
     * effect in the human visual system.
     * <p>
     * This palette has 12 columns and 10 rows.
     * <p>
     * The topmost row contains a null-color and a gray scale from white to
     * black in 10 percent steps.
     * <p>
     * The remaining rows contain colors taken from the outer hull of the HSB
     * color model:
     * <p>
     * The columns are ordered by hue starting with red - the lowest wavelength -
     * and ending with purple - the highest wavelength. There are 12 different
     * hues, so that all primary colors with their additive complements can be
     * selected.
     * <p>
     * The rows are orderd by brightness with the brightest color at the top (sky)
     * and the darkest color at the bottom (earth).
     * The first 5 rows contain colors with maximal brightness and a saturation
     * ranging form 20% up to 100%. The remaining 4 rows contain colors with
     * maximal saturation and a brightness ranging from 90% to 20% (this also
     * makes for a range from 100% to 20% if the 5th row is taken into account).
     */
    private final static java.util.List<ColorIcon> HSB_COLORS;
    private final static int HSB_COLORS_COLUMN_COUNT = 12;
    /**
     * This is the same palette as HSB_COLORS, but all color values are
     * specified in the sRGB color space.
     */
    private final static java.util.List<ColorIcon> HSB_COLORS_AS_RGB;
    private final static int HSB_COLORS_AS_RGB_COLUMN_COUNT = 12;

    static {
        HSBColorSpace hsbCS = HSBColorSpace.getInstance();
        LinkedList<ColorIcon> m = new LinkedList<ColorIcon>();
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.gui.Labels");
        m.add(new ColorIcon(new Color(0, true), labels.getToolTipTextProperty("ColorChooser.colorSwatch.noColor")));

        for (int s = 2; s <= 8; s += 2) {
            for (int h = 0; h < 12; h++) {
                Color c = new Color(hsbCS, new float[]{(h) / 12f, s * 0.1f, 1f}, 1f);
                m.add(new ColorIcon(c,//
                        labels.getFormatted("ColorChooser.colorSwatch.hsbComponents.toolTipText", h * 360 / 12, s * 10, 100)));
            }
        }
        for (int b = 10; b >= 2; b -= 2) {
            for (int h = 0; h < 12; h++) {
                Color c = new Color(hsbCS, new float[]{(h) / 12f, 1f, b * 0.1f}, 1f);
                m.add(new ColorIcon(new Color(hsbCS, new float[]{(h) / 12f, 1f, b * 0.1f}, 1f),//
                        labels.getFormatted("ColorChooser.colorSwatch.hsbComponents.toolTipText", h * 360 / 12, 100, b * 10)));
            }
        }
        HSB_COLORS = Collections.unmodifiableList(m);

        m = new LinkedList<ColorIcon>();
        for (ColorIcon ci : HSB_COLORS) {
            if (ci.getColor() == null) {
                m.add(new ColorIcon(new Color(0, true), labels.getToolTipTextProperty("ColorChooser.colorSwatch.noColor")));
            } else {
                Color c = ci.getColor();
                m.add(new ColorIcon(c,//
                        labels.getFormatted("ColorChooser.colorSwatch.rgbComponents.toolTipText", c.getRed(), c.getGreen(), c.getBlue())));
            }
        }

        HSB_COLORS_AS_RGB = Collections.unmodifiableList(m);
    }

    /** Creates new form PaletteSwatchesChooser */
    public PaletteSwatchesChooser() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jList = new javax.swing.JList();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 6, 6));
        setLayout(new java.awt.GridBagLayout());

        jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        add(jList, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList;
    // End of variables declaration//GEN-END:variables

    
    public void updateChooser() {
        if (updateRecursion++ == 0) {
            ListModel m = jList.getModel();
            Color mc = getColorFromModel();
            if (mc != null) {
                int ma = mc.getAlpha();
                int mr = mc.getRed();
                int mg = mc.getGreen();
                int mb = mc.getBlue();
                int closestSquaredDistance = Integer.MAX_VALUE;
                int closestIndex = -1;
                for (int i = 0, n = m.getSize(); i < n; i++) {
                    ColorIcon item = (ColorIcon) m.getElementAt(i);
                    Color ic = item.getColor();
                    int squaredDistance;
                    if (ic == null||ic.getAlpha()!=ma) {
                        squaredDistance = Integer.MAX_VALUE;
                    } else {
                        squaredDistance = (mr - ic.getRed()) * (mr - ic.getRed())
                                + (mg - ic.getGreen()) * (mg - ic.getGreen())
                                + (mb - ic.getBlue()) * (mb - ic.getBlue());
                    }
                    if (squaredDistance <= closestSquaredDistance) {
                        closestSquaredDistance = squaredDistance;
                        closestIndex = i;
                        if (squaredDistance == 0) {
                            break;
                        }
                    }
                }
                if (closestIndex == -1) {
                    jList.clearSelection();
                } else {
                    jList.setSelectedIndex(closestIndex);
                }
            }
        }
            updateRecursion--;
    }

    
    public String getDisplayName() {
        return PaletteLookAndFeel.getInstance().getString("ColorChooser.colorSwatches");
    }

    
    public Icon getSmallDisplayIcon() {
        return PaletteLookAndFeel.getInstance().getIcon("ColorChooser.colorSwatchesIcon");
    }

    
    public Icon getLargeDisplayIcon() {
        return getSmallDisplayIcon();
    }

    
    protected void buildChooser() {
        initComponents();
        setUI(PalettePanelUI.createUI(this));
        jList.setUI((ListUI) PaletteListUI.createUI(jList));
        jList.setListData(HSB_COLORS_AS_RGB.toArray());
        jList.setVisibleRowCount(HSB_COLORS_AS_RGB.size() / HSB_COLORS_AS_RGB_COLUMN_COUNT);
        jList.addListSelectionListener(new ListSelectionListener() {

            
            public void valueChanged(ListSelectionEvent e) {
                if (updateRecursion++ == 0) {
                    ColorIcon item = (ColorIcon) jList.getSelectedValue();
                    setColorToModel(item == null ? null : item.getColor());
                }
                updateRecursion--;
            }
        });
    }

    public void setColorToModel( Color color) {
        getColorSelectionModel().setSelectedColor(color);
    }
}
