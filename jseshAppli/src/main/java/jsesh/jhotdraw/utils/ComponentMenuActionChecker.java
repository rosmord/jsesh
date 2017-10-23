package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.action.ActionUtil;

/**
 * Check (or uncheck) a menu depending on a component. This class is not proper
 * OO style. Should be refactored.
 *
 * In fact, it could be seen as a facet of either action or component.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class ComponentMenuActionChecker extends ComponentAdapter {

    private final Action action;

    /**
     * create a component adapter which will govern the action's checked state
     * depending on the component visibility.
     * @param action 
     */
    public ComponentMenuActionChecker(Action action) {
        this.action = action;
    }

    @Override
    public void componentShown(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> action.putValue(ActionUtil.SELECTED_KEY, true));
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> action.putValue(ActionUtil.SELECTED_KEY, false));
    }
}
