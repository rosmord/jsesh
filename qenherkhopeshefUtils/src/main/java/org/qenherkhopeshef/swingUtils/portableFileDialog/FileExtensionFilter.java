package org.qenherkhopeshef.swingUtils.portableFileDialog;

import java.io.File;
import java.util.LinkedHashSet;

import javax.swing.filechooser.FileFilter;

public class FileExtensionFilter extends FileFilter {

	private String description;
	private LinkedHashSet<String> extensionSet = new LinkedHashSet<String>();

	public FileExtensionFilter(String extension, String description) {
		super();
		this.extensionSet.add(extension);
		this.description = description;
	}

	public FileExtensionFilter(String extensions[], String description) {
		super();
		for (String e : extensions) {
			this.extensionSet.add(e);
		}
		this.description = description;
	}

	@Override
	public boolean accept(File f) {
		String name = f.getName();
		int extPos = name.lastIndexOf(".");
		if (extPos == -1)
			return false;
		else {
			String extension = name.substring(extPos+1);
			return extensionSet.contains(extension);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

}
