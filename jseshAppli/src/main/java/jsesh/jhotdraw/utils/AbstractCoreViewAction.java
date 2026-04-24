package jsesh.jhotdraw.utils;

import java.util.Optional;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

import jsesh.jhotdraw.JSeshApplicationCore;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;

/**
 * An extension of AbstractViewAction, which is aware of the "core" part of the application.
 */
public abstract class AbstractCoreViewAction extends AbstractViewAction{

    /**
     * @param app
     * @param view
     */
    protected AbstractCoreViewAction(Application app, View view) {
        super(app, view);
    }

    protected JSeshApplicationCore appCore() {
        return ((JSeshApplicationModel) getApplication().getModel()).core();
    }

    protected Optional<JSeshViewCore> viewCore() {
        return Optional.ofNullable(getActiveView())
                .filter(v -> v instanceof JSeshView)
                .map(v -> ((JSeshView) v).core());
    }
    
}
