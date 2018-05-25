package jsesh.jhotdraw;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import jsesh.jhotdraw.actions.BundleHelper;

public class Messages {
	private static final String BUNDLE_NAME = "jsesh.jhotdraw.labels"; //$NON-NLS-1$

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
	
        public static String format(String key, String ...args) {
            BundleHelper bundleHelper= BundleHelper.getInstance();
            return bundleHelper.getFormatedLabel(key, args);
        }
	
}
