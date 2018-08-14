/*
 * Created on 19 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphics.export.generic;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A base class for option setting panels.
 *
 * @author rosmord
 */
@SuppressWarnings("serial")
public abstract class ExportOptionPanel extends JPanel {

    Component popupParent;
    String title;

    public ExportOptionPanel(Component popupParent, String title) {
        this.popupParent = popupParent;
        this.title = title;
    }

    public Component getPopupParent() {
        return popupParent;
    }

    /**
     * saves the options as set in the panel.
     */
    abstract public void setOptions();

    /**
     * shows the panel in a modal dialogue, and sets the values accordingly. An
     * utility function, usable in most cases. returns one of
     * JOptionPane.CANCEL_OPTION (if the user cancels the options) or
     * JOptionPane.OK_OPTION, if the user validates.
     *
     * @return JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    public int askAndSet() {
        int rc
                = JOptionPane.showConfirmDialog(popupParent,
                        this,
                        title,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
        if (rc == JOptionPane.OK_OPTION) {
            setOptions();
        }
        return rc;
    }
}
