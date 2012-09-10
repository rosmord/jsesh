package jsesh.glossary;

import java.io.File;

import jsesh.mdc.MDCParserFacade;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;
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

	private void read() {
		try {
			// Dummy code for testing....
			this.glossary = new JSeshGlossary();
			MDCParserModelGenerator parser = new MDCParserModelGenerator();
			glossary.add("stpnra", parser.parse("stp&n&ra"));
			glossary.add("ramss", parser.parse("ra-ms-z:z"));
			glossary.add("iw", parser.parse("D54^^^w"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {
		for (GlossaryEntry e : glossary) {
			TopItemList topItemList = new TopItemList();			
			topItemList.addAll(e.getTopItems());
			String mdc = topItemList.toMdC();
			mdc = mdc.replace('\0', ' ');
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
