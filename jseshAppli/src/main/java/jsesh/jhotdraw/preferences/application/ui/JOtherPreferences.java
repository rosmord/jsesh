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
package jsesh.jhotdraw.preferences.application.ui;

import java.util.Locale;

import jsesh.jhotdraw.preferences.application.model.ApplicationUIPreferences;
import jsesh.jhotdraw.utils.PanelBuilder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import jsesh.resources.JSeshMessages;

/**
 * Panel for various (ui linked) preferences.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class JOtherPreferences {

    /**
     * Locales the user interface is available in.
     * <p>
     * {@code null} stands for "use the system default locale".
     */
    private static final Locale[] AVAILABLE_LOCALES = {
        null,
        Locale.ENGLISH,
        Locale.FRENCH,
        Locale.ITALIAN,
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("pt"),
        Locale.forLanguageTag("sv")
    };

    private JPanel panel;
    private JFormattedTextField iconHeight;
    private JComboBox<LocaleItem> localeSelector;


    public JOtherPreferences() {
        init();
        layout();
    }


    private void init() {
        panel = new JPanel();
        iconHeight= new JFormattedTextField();
        iconHeight.setValue(30);
        localeSelector = new JComboBox<>();
        for (Locale locale : AVAILABLE_LOCALES) {
            localeSelector.addItem(new LocaleItem(locale));
        }
    }

    private void layout() {
        panel.setLayout(new MigLayout());
        PanelBuilder helper = new PanelBuilder(panel);
        helper.addLabel("otherPrefs.iconHeight.label");
        panel.add(iconHeight, "span, grow, wrap");
        JLabel comment= new JLabel(JSeshMessages.getString("otherPrefs.iconHeight.comment"));
        panel.add(comment, "span, grow, wrap");
        helper.addLabel("otherPrefs.locale.label");
        panel.add(localeSelector, "span, grow, wrap");
        JLabel localeComment= new JLabel(JSeshMessages.getString("otherPrefs.locale.comment"));
        panel.add(localeComment, "span, grow, wrap");
    }

    public JPanel getPanel() {
        return panel;
    }


    public void loadPreferences() {
        ApplicationUIPreferences prefs = ApplicationUIPreferences.getFromPreferences();
        iconHeight.setValue(prefs.getIconHeight());
        localeSelector.setSelectedItem(new LocaleItem(prefs.getLocale()));
    }

    void savePreferences() {
        ApplicationUIPreferences prefs = new ApplicationUIPreferences();
        prefs.setIconHeight((Integer)iconHeight.getValue());
        LocaleItem selected = (LocaleItem) localeSelector.getSelectedItem();
        prefs.setLocale(selected == null ? null : selected.getLocale());
        prefs.savetoPreferences();
    }

    /**
     * Wraps a locale together with a human-readable label for the combo box.
     * <p>
     * A {@code null} locale represents the system default.
     */
    private static final class LocaleItem {
        private final Locale locale;

        LocaleItem(Locale locale) {
            this.locale = locale;
        }

        Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            if (locale == null) {
                return JSeshMessages.getString("otherPrefs.locale.systemDefault");
            }
            // Display the language name in its own language.
            String name = locale.getDisplayLanguage(locale);
            if (name.isEmpty()) {
                name = locale.toLanguageTag();
            }
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LocaleItem)) {
                return false;
            }
            LocaleItem other = (LocaleItem) obj;
            if (locale == null) {
                return other.locale == null;
            }
            return locale.equals(other.locale);
        }

        @Override
        public int hashCode() {
            return locale == null ? 0 : locale.hashCode();
        }
    }
}
