package jsesh.editor.actions;

import org.qenherkhopeshef.guiFramework.AppDefaults;

public class AppDefaultFactory {
	public static AppDefaults getAppDefaults() {
		AppDefaults appDefaults= new AppDefaults();
		appDefaults.addResourceBundle("jsesh.editor.labels");
		return appDefaults;
	}
}
