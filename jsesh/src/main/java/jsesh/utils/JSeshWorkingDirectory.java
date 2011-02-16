package jsesh.utils;

import java.io.File;
import java.util.prefs.Preferences;

public class JSeshWorkingDirectory {
	private static final String WORKING_DIRECTORY_PREF = "workingDirectory";
	private static File workingDirectory= null;
	
	/**
	 * Returns the currently defined user directory.
	 * @return
	 */
	public synchronized static File getWorkingDirectory() {
		if (workingDirectory == null) {
			workingDirectory= getFromResources();
			if (workingDirectory == null)
				workingDirectory= getDefault();
		}
		return workingDirectory;
	}
	
	/**
	 * Returns a reasonable default value.
	 * @return
	 */
	private static File getDefault() {
		File directory;
		directory= new File(System.getProperty("user.home"));
		File docs= new File(directory, "Documents");
		if (docs.isDirectory() && docs.canWrite()) {
			directory= docs;
		}
		return directory;
	}

	/**
	 * Get a saved value for the current directory.
	 * @return
	 */
	private static File getFromResources() {
		File result= null;
		Preferences prefs= getPrefNode();
		String path= prefs.get(WORKING_DIRECTORY_PREF, null);
		if (path != null) {
			result= new File(path);
			if (! result.isDirectory() || ! result.canWrite())
				result= null;
		}
		return result;
	}

	private static Preferences getPrefNode() {
		return Preferences.userNodeForPackage(JSeshWorkingDirectory.class);
	}

	/**
	 * Sets (and remember) the current working directory.
	 * Will have no effect (should probably throw an exception) if directory doesn't exist.
	 * @param directory an existing and writable directory.
	 */
	public synchronized static void setWorkingDirectory(File directory) {
		if (directory.isDirectory() && directory.canWrite()) {
			workingDirectory= directory;
			getPrefNode().put(WORKING_DIRECTORY_PREF, directory.getAbsolutePath());
		}
	}
	
}
