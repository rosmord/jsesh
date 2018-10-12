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
package jsesh.jhotdraw.applicationPreferences.ui;


import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.utils.JSimpleDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Presenter for application preferences.
 * <p>
 * choice to make: use a modal dialog or not ?
 * <p>
 * Modal dialogs are simpler to manage, in particular their content don't need
 * updating when the data is modified in another way.
 * <p>
 * Currently, preferences are
 * <ul>
 * <li>export preferences
 * <li>export formats
 * <li>fonts preferences (move directly to DOCUMENT preferences)...
 * <li>or replicate here
 * <li>
 * </ul>
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class ApplicationPreferencesPresenter {

    private JTabbedPane tabbedPane;
    private JClipboardFormatSelector clipboardFormatSelector;
    private JExportPreferences exportPreferences;
    private JFontPreferences fontPreferences;
    private JOtherPreferences otherPreferences;
    
    public ApplicationPreferencesPresenter() {
        init();
    }

    /**
     * Create the global UI.
     */
    private void init() {
        tabbedPane = new JTabbedPane();
        clipboardFormatSelector = new JClipboardFormatSelector();
        exportPreferences = new JExportPreferences();
        fontPreferences = new JFontPreferences();
        otherPreferences= new JOtherPreferences();
        tabbedPane.add(Messages
                .getString("applicationPreferences.exportPreferences.label"),
                exportPreferences.getPanel());
        tabbedPane.add(Messages
                .getString("applicationPreferences.clipboardFormat.label"),
                clipboardFormatSelector.getPanel());
        tabbedPane.add(Messages
                .getString("applicationPreferences.fontPreferences.label"),
                fontPreferences.getPanel());
         tabbedPane.add(Messages
                .getString("applicationPreferences.others.label"),
                otherPreferences.getPanel());
    }

    /**
     * Fetches the preferences from the application.
     * @param app
     */
    public void loadPreferences(JSeshApplicationModel app) {
        clipboardFormatSelector.loadPreferences(app);
        exportPreferences.loadPreferences(app);
        fontPreferences.setFontInfo(app.getFontInfo());
        otherPreferences.loadPreferences();
    }

    /**
     * Sets the application preferences.
     * @param app
     */
    public void updatePreferences(JSeshApplicationModel app) {
        clipboardFormatSelector.updatePreferences(app);
        exportPreferences.updatePreferences(app);
        app.setFontInfo(fontPreferences.getFontInfo());
        otherPreferences.savePreferences();
    }

    /**
     * Display the dialog (and block the software).
     *
     * @param parent
     * @return one of JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    public int showDialog(Component parent) {
        JSimpleDialog dialog = new JSimpleDialog(parent, tabbedPane,
                Messages.getString("appPreferencesDialog.text"));
        int result = dialog.show();
        dialog.dispose();
        return result;
    }

    public static void main(String[] args) {
        ApplicationPreferencesPresenter p= new ApplicationPreferencesPresenter();
        p.showDialog(null);
    }
}
