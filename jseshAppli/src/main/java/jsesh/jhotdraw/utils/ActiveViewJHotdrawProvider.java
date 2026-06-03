package jsesh.jhotdraw.utils;

import org.jhotdraw_7_6.app.Application;

import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;

/**
 * An active view provider which is in fact the JHotdraw application itself.
 */
public class ActiveViewJHotdrawProvider implements ActiveViewProvider {

    private final Application application;

    public ActiveViewJHotdrawProvider(Application application) {
        this.application = application;
    }

    @Override
    public boolean hasActiveView() {
        return application.getActiveView() != null;
    }

    @Override
    public JSeshViewCore activeView() {
        if (application.getActiveView() instanceof JSeshView jsv) {
            return jsv.core();
        } else {
            return null;
        }
    }

}
