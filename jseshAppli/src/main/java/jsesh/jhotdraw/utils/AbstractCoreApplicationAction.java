package jsesh.jhotdraw.utils;

import java.util.Optional;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;

import jsesh.jhotdraw.JSeshApplicationCore;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;

/**
 * An extension of AbstractApplicationAction, which is aware of the "core" part of the application.
 */
public abstract class AbstractCoreApplicationAction extends AbstractApplicationAction {

    protected AbstractCoreApplicationAction(Application app) {
        super(app);
    }

    protected Optional<JSeshViewCore> activeViewCore() {
        if (getApplication().getActiveView() instanceof JSeshView jsv) {
            return Optional.of(jsv.core());
        } else {
            return Optional.empty();
        }
    }

    public boolean hasActiveView() {
        return getApplication().getActiveView() != null;
    }


    protected JSeshApplicationCore appCore() {
        return ((JSeshApplicationModel) getApplication().getModel()).core();
    }
    
    
}
