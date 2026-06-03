package org.qenherkhopeshef.swingUtils;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A base class for list cell renderers that delegate part of the rendering to the default renderer.
 */
public class LabelDelegateListCellRenderer<E> implements ListCellRenderer<E> {

    private final DefaultListCellRenderer delegate = new DefaultListCellRenderer();
    private final LabelCustomizer<E>  labelCustomizer;

    public LabelDelegateListCellRenderer(LabelCustomizer<E> labelCustomizer) {
        this.labelCustomizer = labelCustomizer;
    }

    @Override
    public final Component getListCellRendererComponent(
            JList<? extends E> list, E value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) delegate.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        labelCustomizer.customize(label, new CellData<>(list, value, index, isSelected, cellHasFocus));
        return label;
    }

    public interface LabelCustomizer<E> {
        void customize(JLabel label, CellData<E> cellData);
    }

    public static record CellData<E>(
            JList<? extends E> list, E value,
            int index, boolean isSelected, boolean cellHasFocus) {
    }
}
