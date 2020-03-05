/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * I18n ressources and text for this module.
 *
 * @author rosmord
 */
public class Messages {
    private static final String BUNDLE_NAME = "jsesh.search.labels"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
