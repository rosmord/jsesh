/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

/**
 * Useful static methods for manipulating views and the like.
 */
public class WindowsHelper {

    /**
     * Moves a view to the foreground if possible.
     * From an heavily replicated piece of code in JHotDraw.
     *
     * @param view
     */
    public static void toFront(View view) {
        Frame w = (Frame) SwingUtilities.getWindowAncestor(view.getComponent());
        if (w != null) {
            w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
            w.toFront();
        }
        view.getComponent().requestFocus();
    }

    /**
     * Gets the root frame for an application and a view
     *
     * @param application the application
     * @param view        the view
     * @return a frame, or null if non could be found.
     */
    public static Frame getRootFrame(Application application, View view) {
        Frame result = null;
        try {
            Component component = null;
            if (view != null) {
                component = SwingUtilities.getRoot((Component) view);
            } else if (application.getActiveView() != null) {
                component = SwingUtilities.getRoot((Component) application.getActiveView());
            }
            result = (Frame) component;
        } catch (Exception e) {
            // Those messages might be removed (see method documentation).
            System.err.println("problem getting window root. Will return null");
            e.printStackTrace();
        }
        return result;
    }

    // Helper class : no instance.
    private WindowsHelper() {}
}
