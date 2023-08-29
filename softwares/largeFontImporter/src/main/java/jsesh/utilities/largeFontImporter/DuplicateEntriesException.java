package jsesh.utilities.largeFontImporter;


import java.util.ArrayList;

public class DuplicateEntriesException extends Exception {

	ArrayList entries;
	public DuplicateEntriesException(ArrayList entries) {
		this.entries= entries;
	}

	public ArrayList getEntries() {
		return entries;
	}
}
