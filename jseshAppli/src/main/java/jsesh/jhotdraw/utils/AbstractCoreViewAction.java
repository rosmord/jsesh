package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Logger;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

import jsesh.jhotdraw.JSeshApplicationCore;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;

/**
 * An extension of AbstractViewAction, which is aware of the "core" part of the
 * application.
 */
public abstract class AbstractCoreViewAction extends AbstractViewAction {

    private static final Logger LOGGER = Logger.getLogger(AbstractCoreViewAction.class.getName());

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
        Optional<JSeshViewCore> result = Optional.ofNullable(getActiveView())
                .filter(v -> v instanceof JSeshView)
                .map(v -> ((JSeshView) v).core());
        LOGGER.finer(() -> 
            result.map(c -> "View core found")
                  .orElse("No view core found, or active view is not a JSeshView.")
        );    
        return result;
    }

    /**
     * Returns a reference component for the view, usable to build dialog panels.
     * 
     * <p>
     * Java dialog panels need a reference component. They will be hidden when it's
     * hidden, etc.
     * <p>
     * This returns the component panel (if any).
     * <p>
     * Normally, the code will be called when there is a view, so null is not really
     * an issue here.
     * 
     * @return a reference component, or null if there is no active view.
     */
    protected Component referenceComponent() {
        if (getActiveView() != null) {
            return getActiveView().getComponent();
        } else {
            LOGGER.finer(() -> "No active view, reference component is null.");
            return null;
        }
    }

    /**
     * A reasonable file name as a basis for exports.
     * <p> Built on the name of the JSesh gly file.
     * <p> Only defined if there is an active view.
     * @return the base name file or throws a null pointer exception if there is no active view.
      * @throws NullPointerException if there is no active view.
      * @see JSeshView#getBaseFileName()
     */
    protected String baseFileName() {
        if (getActiveView() != null) {
            return ((JSeshView) getActiveView()).baseFileName();
        } else {
            LOGGER.finer(() -> "No active view, base file name is null.");
            throw new NullPointerException("No active view, cannot determine base file name.");
        }
    }

    /**
     * Build an export file name, based on the base file name and the given extension.
     * <p>e.g. if the current file is "sinouhe.gly", and the extension is "pdf", 
     * this will return "sinouhe.pdf".
     * @param extension the extension to use for the export file, without dot.
     * @return a file name for export.
     */
    protected File createDefaultExportFile(String extension) {
        return ((JSeshView) getActiveView()).createDefaultExportFile(extension);
    }


    protected void setCurrentDirectory(File directory) {
        appCore().setCurrentDirectory(directory);
    }


    protected File getCurrentDirectory() {
        return appCore().getCurrentDirectory();
    }

    /**
     * Gets the URI of the current document, if any.
     * Should probably be removed, as it's very specific to JHotdraw approach.
     * @return
     */
    protected URI getURI() {
        return ((JSeshView) getActiveView()).getURI();
    }
}
