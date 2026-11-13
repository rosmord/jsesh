package jsesh.jhotdraw.actions.generic;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.JSheet;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Worker Representing the long operation of loading a document in an existing view.
 */
public class ViewOpenerWorker extends Worker<Object> {

    private final URI uri;
    private final View view;
    private final Application app;

    public ViewOpenerWorker(URI uri, View view, Application app) {
        this.uri = uri;
        this.view = view;
        this.app = app;
    }

    @Override
    public Object construct() throws IOException {
        boolean exists = true;
        try {
            exists = new File(uri).exists();
        } catch (IllegalArgumentException e) {
        }
        if (exists) {
            view.read(uri, null);
            return null;
        } else {
            ResourceBundleUtil labels = ResourceBundleUtil
                    .getBundle("org.jhotdraw_7_6.app.Labels");
            throw new IOException(labels.getFormatted(
                    "file.open.fileDoesNotExist.message",
                    URIUtil.getName(uri)));
        }
    }

    @Override
    protected void done(Object value) {
        // A text from the library is not tied to a file: no URI (so "Save" asks for a name),
        // and it doesn't belong to the recent files.
        boolean fromLibrary = TextLibraryURI.SCHEME.equals(uri.getScheme());
        view.setURI(fromLibrary ? null : uri);
        view.setEnabled(true);
        Frame w = (Frame) SwingUtilities.getWindowAncestor(view
                .getComponent());
        if (w != null) {
            w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
            w.toFront();
        }
        view.getComponent().requestFocus();
        if (!fromLibrary) {
            app.addRecentURI(uri);
        }
        app.setEnabled(true);
    }

    @Override
    protected void failed(Throwable value) {
        value.printStackTrace();
        view.setEnabled(true);
        app.setEnabled(true);
        String message;
        if ((value instanceof Throwable)
                && ((Throwable) value).getMessage() != null) {
            message = ((Throwable) value).getMessage();
            ((Throwable) value).printStackTrace();
        } else if ((value instanceof Throwable)) {
            message = value.toString();
            ((Throwable) value).printStackTrace();
        } else {
            message = value.toString();
        }
        ResourceBundleUtil labels = ResourceBundleUtil
                .getBundle("org.jhotdraw_7_6.app.Labels");
        JSheet.showMessageSheet(
                view.getComponent(),
                "<html>"
                        + UIManager.getString("OptionPane.css")
                        + "<b>"
                        + labels.getFormatted(
                        "file.open.couldntOpen.message",
                        URIUtil.getName(uri)) + "</b><p>"
                        + ((message == null) ? "" : message),
                JOptionPane.ERROR_MESSAGE);
    }
}
