package jsesh.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {

	private static final String BUNDLE_NAME = "jsesh.editor.labels"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private I18n() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	
}
