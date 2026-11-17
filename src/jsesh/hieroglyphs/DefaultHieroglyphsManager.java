/*
 * DefaultHieroglyphsManager.java
 *
 * Created on 27 sept. 2007, 17:40:08
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsesh.hieroglyphs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jsesh.resources.ResourcesManager;

import org.xml.sax.SAXException;

/**
 * 
 * @author rosmord
 */
public class DefaultHieroglyphsManager implements HieroglyphDatabaseInterface {

	private static DefaultHieroglyphsManager instance;

	private BasicManuelDeCodageManager basicManuelDeCodageManager = new BasicManuelDeCodageManager();

	private HieroglyphsManager distributionInfoManager = new HieroglyphsManager(
			basicManuelDeCodageManager);

	private HieroglyphsManager userInfoManager = new HieroglyphsManager(
			basicManuelDeCodageManager);

	private boolean userFileCorrect= true;

	private String userFileMessage="";
	
	private Map possibilityListsForKeyBoard= new HashMap();
	
	public static DefaultHieroglyphsManager getInstance() {
		if (instance == null) {
			instance = new DefaultHieroglyphsManager();
		}

		return instance;
	}

	/**
	 * Build the default hieroglyphic manager, filled using two different files.
	 */
	public DefaultHieroglyphsManager() {
		try {
			readFiles();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read the XML files containing the signs descriptions.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * 
	 */
	private void readFiles() throws SAXException, IOException {

		SignDescriptionReader reader = new SignDescriptionReader(
				new HieroglyphManagerToSignDescriptionBuilderAdapter(
						distributionInfoManager));

		// Read "standard" signs description
		InputStream in1 = this.getClass().getResourceAsStream(
				"signs_description.xml");
		if (in1 != null) {
			reader.readSignDescription(in1);
		} else {
			System.err.println("Sign description file not found.");
		}

		// We don't want errors in the user-defined file to 
		// prevent the software from starting
		try {
			// Read user signs descriptions (if any is available)
			reader = new SignDescriptionReader(
					new HieroglyphManagerToSignDescriptionBuilderAdapter(
							userInfoManager));

			File f = getUserSignDefinitionFile();
			if (f.exists()) {
				System.err.println("Reading user sign definition");
				InputStream in2 = new FileInputStream(f);
				reader.readSignDescription(in2);
			}
		} catch (Exception e) {
			userFileCorrect= false;
			userFileMessage= e.getMessage();
		}
	}

	/**
	 * Returns true if the user sign definition file is correct (or if there is no user sign definition file).
	 * This file is the XML file used to define the signs properties. 
	 * @return
	 */
	public boolean isUserFileCorrect() {
		return userFileCorrect;
	}
	
	/**
	 * Return the error message created when failing to read userMessage.
	 * This is only meaningful if the user sign definition file is not correct.
	 * @return
	 * @see #isUserFileCorrect()
	 */
	public String getUserFileMessage() {
		return userFileMessage;
	}
	
	public static File getUserSignDefinitionFile() {

		File f = ResourcesManager.getInstance().getUserPrefsDirectory();
		f = new File(f, "signs_definition.xml");
		return f;
	}

	public List getBasicGardinerCodesForFamily(String familyCode) {
		TreeSet s = new TreeSet(GardinerCode.getCodeComparator());
		s.addAll(distributionInfoManager
				.getBasicGardinerCodesForFamily(familyCode));
		s.addAll(userInfoManager.getBasicGardinerCodesForFamily(familyCode));
		return new ArrayList(s);
	}

	public String getCanonicalCode(String code) {
		// standard procedure. If we create a super abstract class, could be in
		// it.
		return distributionInfoManager.getCanonicalCode(code);
	}

	public String[] getCodesForFamily(String familyCode, boolean userCodes) {
		TreeSet s = new TreeSet(GardinerCode.getCodeComparator());
		s.addAll(Arrays.asList(distributionInfoManager.getCodesForFamily(
				familyCode, userCodes)));
		s.addAll(Arrays.asList(userInfoManager.getCodesForFamily(familyCode,
				userCodes)));
		return (String[]) s.toArray(new String[s.size()]);
	}

	public Set getCodesSet() {
		TreeSet s = new TreeSet(GardinerCode.getCodeComparator());
		s.addAll(distributionInfoManager.getCodesSet());
		s.addAll(userInfoManager.getCodesSet());
		return s;
	}

	public String getDescriptionFor(String code) {
		String result = userInfoManager.getDescriptionFor(code);
		if ("".equals(result)) {
			result = distributionInfoManager.getDescriptionFor(code);
		}
		return result;
	}

	// TODO move this method, as it depends only on the Manuel structure, not
	// content.
	public List getFamilies() {
		return distributionInfoManager.getFamilies();
	}

	// Return a possibility list built from both (NOTE TO SELF: KEEP THE POSSIBILITY LISTS...)
	// TODO: separate the database aspect, which is stateless, from
	// the dynamic aspect (keeping track of possibilitylists, mainly).
	public PossibilitiesList getPossibilityFor(String phoneticValue, String level) {
		PossibilitiesList result= null;
		if (SignDescriptionConstants.KEYBOARD.equals(level)) {	
			if (! possibilityListsForKeyBoard.containsKey(phoneticValue))
				possibilityListsForKeyBoard.put(phoneticValue, fetchPossibilityListFromBases(phoneticValue, level));
			result= (PossibilitiesList) possibilityListsForKeyBoard.get(phoneticValue);
		} else
			result= fetchPossibilityListFromBases(phoneticValue, level);
		
		return result;
		
	}

	private PossibilitiesList fetchPossibilityListFromBases(String phoneticValue, String level) {
		PossibilitiesList result = new PossibilitiesList(phoneticValue);
		
		List temp= distributionInfoManager
				.getPossibilityFor(phoneticValue,level).asList();
		Iterator it = temp.iterator();
		while (it.hasNext()) {
			String c = (String) it.next();
			result.add(c);
		}
		temp = userInfoManager.getPossibilityFor(phoneticValue,level).asList();
		it = temp.iterator();
		while (it.hasNext()) {
			String c = (String) it.next();
			result.add(c);
		}
		return result;
	}
	
	public Collection getSignsContaining(String code) {
		TreeSet result = new TreeSet(GardinerCode.getCodeComparator());
		result.addAll(distributionInfoManager.getSignsContaining(code));
		result.addAll(userInfoManager.getSignsContaining(code));
		return result;
	}

	public Collection getSignsWithTagInFamily(String currentTag,
			String familyName) {
		TreeSet result = new TreeSet(GardinerCode.getCodeComparator());
		result.addAll(distributionInfoManager.getSignsWithTagInFamily(
				currentTag, familyName));
		result.addAll(userInfoManager.getSignsWithTagInFamily(currentTag,
				familyName));
		return result;
	}

	public Collection getSignsWithoutTagInFamily(String familyName) {
		TreeSet result = new TreeSet(GardinerCode.getCodeComparator());
		result.addAll(distributionInfoManager
				.getSignsWithoutTagInFamily(familyName));
		result.addAll(userInfoManager.getSignsWithoutTagInFamily(familyName));
		return result;
	}

	public Collection getTagsForFamily(String familyName) {
		TreeSet result = new TreeSet();
		result.addAll(distributionInfoManager.getTagsForFamily(familyName));
		result.addAll(userInfoManager.getTagsForFamily(familyName));
		return result;
	}

	public List getValuesFor(String gardinerCode) {
		ArrayList result = new ArrayList();
		result.addAll(distributionInfoManager.getValuesFor(gardinerCode));
		result.addAll(userInfoManager.getValuesFor(gardinerCode));
		return result;
	}

	public Collection getVariants(String code) {
		TreeSet result = new TreeSet(GardinerCode.getCodeComparator());
		result.addAll(distributionInfoManager.getVariants(code));
		result.addAll(userInfoManager.getVariants(code));
		return result;
	}
	
	public boolean isAlwaysDisplayed(String code) {
		return distributionInfoManager.isAlwaysDisplayed(code) || userInfoManager.isAlwaysDisplayed(code);
	}
}