package jsesh.glossary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.model.TopItemList;
import jsesh.resources.ResourcesManager;

/**
 * Manages, saves and load the user glossary.
 * 
 * The file will be saved in UTF-8 in the user's preference folder, with the
 * name <code>jsesh_glossary.txt</code>.
 * 
 * File format is : entry key TABULATION MdC code char(0)
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class GlossaryManager {
	private static GlossaryManager instance = new GlossaryManager();
	private JSeshGlossary glossary;

	private GlossaryManager() {
		read();
	}

	private File getJSeshGlossaryFile() {
		return new File(ResourcesManager.getInstance().getUserPrefsDirectory(),
				"glossary.txt");
	}

	private void read() {
		this.glossary = new JSeshGlossary();
		if (!getJSeshGlossaryFile().exists()
				&& !getJSeshGlossaryFile().canRead())
			return;

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(getJSeshGlossaryFile()), "utf-8"));
			MDCParserModelGenerator parser = new MDCParserModelGenerator();
			try {
				String line;
				while ((line = r.readLine()) != null) {
					int pos = line.indexOf('\t');
					String code = line.substring(0, pos).trim();
					String mdc = line.substring(pos + 1).trim();
					glossary.add(code, parser.parse(mdc));
				}
			} finally {
				if (r != null)
					r.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {
		try {
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(getJSeshGlossaryFile()), "utf-8"));
			try {
				for (GlossaryEntry e : glossary) {
					// Ensure there is no "tab" char in keys.
					String key= e.getKey().replaceAll("\t", "");
					// Ensure that 
					TopItemList topItemList = new TopItemList();
					topItemList.addAll(e.getTopItems());
					String mdc = topItemList.toMdC();
					mdc = mdc.replace('\0', ' ');
					w.write(key);
					w.write('\t');
					w.write(mdc);
				}
			} finally {
				w.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the file containing the user's glossary.
	 * 
	 * @return
	 */
	public static File getUserSignDefinitionFile() {
		File f = ResourcesManager.getInstance().getUserPrefsDirectory();
		f = new File(f, "jsesh_glossary.txt");
		return f;
	}

	public static GlossaryManager getInstance() {
		return instance;
	}

	public JSeshGlossary getGlossary() {
		return glossary;
	}

}
