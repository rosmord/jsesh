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
package jsesh.glossary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
			try {
				String line;
				while ((line = r.readLine()) != null) {
					int pos = line.indexOf('\t');
					if (pos == -1)
						continue;
					String code = line.substring(0, pos).trim();
					String mdc = line.substring(pos + 1).trim();
					glossary.add(code, mdc);
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
					w.write(key);
					w.write('\t');
					w.write(e.getMdc());
					w.newLine();
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
