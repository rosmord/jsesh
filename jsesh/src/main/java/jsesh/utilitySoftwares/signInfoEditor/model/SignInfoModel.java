/**
 * 
 */
package jsesh.utilitySoftwares.signInfoEditor.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.events.SignInfoModelEvent;
import jsesh.utilitySoftwares.signInfoEditor.events.SignInfoModelEventListener;
import jsesh.utilitySoftwares.signInfoEditor.events.TagEvent;

/**
 * Editing model for sign informations. As the currently used model for sign
 * information is not complete, we have decided to create another one for
 * editing purposes.
 * 
 * Note: we have a lot of type unsecure stuff here, because we will eventually
 * move it to JDK1.5.
 * 
 * @author rosmord
 */

public class SignInfoModel {

	/**
	 * We could use a weak HashSet for this.
	 */
	private List listeners= new ArrayList();
	
	/**
	 * A map from sign codes to sign info datas. Should be Map<String,EditableSignInfo>.
	 */
	private Map signInfoDataList = new TreeMap(GardinerCode.getCodeComparator());

	/**
	 * The tags. A map String -> TagInfo
	 */

	private Map tags = new TreeMap();

	/**
	 * All know signs.
	 */
	private TreeSet codes = new TreeSet(GardinerCode.getCodeComparator());

	/**
	 * Has the model been changed since the last save ?
	 */
	private boolean dirty= false;
	
	/**
	 * Keep track of changes in the various data..
	 */
	PropertyChangeListener propertyChangeListener= new PropertyChangeListener();
	
	public SignInfoModel() {
		Iterator it = DefaultHieroglyphicFontManager.getInstance().getCodes()
				.iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (GardinerCode.isCorrectGardinerCode(code)) {
				codes.add(code);
				EditableSignInfo editableSignInfo = new EditableSignInfo(code);
				editableSignInfo.setParent(propertyChangeListener);
				signInfoDataList.put(code, editableSignInfo);
			}
		}
		clearDirtyFlag();
	}

	public void add(SignInfoProperty signInfoProperty) {
		EditableSignInfo editableSignInfo = getOrCreateSign(signInfoProperty
				.getSignCode());
		editableSignInfo.add(signInfoProperty);
		setDirtyFlag();
	}

	public void addTagCategory(String tag, boolean inUserPart) {
		getOrCreateTagInfo(tag, inUserPart);
	}

	private TagInfo getOrCreateTagInfo(String tag, boolean inUserPart) {
		if (!tags.containsKey(tag)) {
			tags.put(tag, new TagInfo(tag, inUserPart));
			setDirtyFlag();
			notifyListeners(new TagEvent(this));
		}
		return (TagInfo) tags.get(tag);
	}

	/**
	 * Add a new tagCategory information.
	 * 
	 * @param property :
	 *            a tag, having tagCategory as class.
	 */

	public void addTagLabel(XMLInfoProperty property) {
		String tag = property.get("tag");
		TagInfo translations = getOrCreateTagInfo(tag, property
				.isUserDefinition());
		translations.labels.add(property);
		property.setParent(propertyChangeListener);
		setDirtyFlag();
	}

	public void addTagLabel(String tag, String lang, String label,
			boolean inUserPart) {

		XMLInfoProperty xmlInfoProperty = new XMLInfoProperty("tagCategory",
				inUserPart);
		xmlInfoProperty.setAttribute("tag", tag);
		xmlInfoProperty.setAttribute("lang", lang);
		xmlInfoProperty.setAttribute("label", label);
		addTagLabel(xmlInfoProperty);
	}

	/**
	 * Returns the available translations for a tag.
	 * 
	 * @param tag
	 * @return
	 */
	public List getLabelListFor(String tag) {
		return ((TagInfo) tags.get(tag)).labels;
	}

	/**
	 * Discover if a given tag was created by the user.
	 * 
	 * @param tag
	 * @return
	 */
	public boolean isUserTag(String tag) {
		return ((TagInfo) tags.get(tag)).userDefined;
	}

	/**
	 * Returns a list of Strings representing the available tags.
	 * 
	 * @return
	 */
	public List getTags() {
		return new ArrayList(tags.keySet());
	}

	public TreeSet getCodes() {
		return codes;
	}

	/**
	 * Returns the first known code, or fail with an exception.
	 * 
	 * @return the first code (in Gardiner font order).
	 * @throws NoSuchElementException
	 */
	public String getFirstCode() {
		return (String) codes.first();
	}

	/**
	 * Returns the code after a given code, or null.
	 * 
	 * @param currentCode
	 * @return the next code after <code>currentCode</code>, or null if none
	 *         exists.
	 */
	public String getCodeAfter(String currentCode) {
		String result = null;
		SortedSet tailSet = codes.tailSet(currentCode);
		// Iterates in the set until a value different from currentCode is
		// found.
		// normally takes only two iterations.
		Iterator it = tailSet.iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (!currentCode.equals(code)) {
				result = code;
				break;
			}
		}
		return result;
	}

	public String getCodeBefore(String currentCode) {
		String result = null;
		SortedSet s = codes.headSet(currentCode);
		if (!s.isEmpty()) {
			result = (String) s.last();
		}
		return result;
	}

	public String getLastCode() {
		return (String) codes.last();
	}

	public EditableSignInfo getSign(String code) {
		EditableSignInfo result = (EditableSignInfo) signInfoDataList.get(code);
		return result;
	}

	/**
	 * Returns the tags used for a given sign family
	 * 
	 * @param family
	 *            a family of signs, as per Gardiner (A, B...)
	 * @return a set of strings corresponding to the available tags.
	 */
	public SortedSet getTagsForFamily(String family) {
		TreeSet set = new TreeSet();
		// Let's be stupid.
		SortedSet s = codes.tailSet(family + "1");
		Iterator it = s.iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			// Stop if we have finished looking at the sign's family.
			if (!family.equals(GardinerCode.createGardinerCode(code)
					.getFamily()))
				break;
			List usedTags = getSign(code).getPropertyList(
					SignDescriptionConstants.HAS_TAG);
			for (int i = 0; i < usedTags.size(); i++) {
				SignInfoProperty prop = (SignInfoProperty) usedTags.get(i);
				set.add(prop.get(SignDescriptionConstants.TAG));
			}
		}
		return set;
	}
	
	public SortedSet getUsedTags() {
		TreeSet set = new TreeSet();
		// Let's be stupid.
		SortedSet s = codes;
		Iterator it = s.iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			List usedTags = getSign(code).getPropertyList(
					SignDescriptionConstants.HAS_TAG);
			for (int i = 0; i < usedTags.size(); i++) {
				SignInfoProperty prop = (SignInfoProperty) usedTags.get(i);
				set.add(prop.get(SignDescriptionConstants.TAG));
			}
		}
		return set;
	}

	/**
	 * Returns a list of EditableSignInfo
	 * 
	 * @return a list of EditableSignInfo
	 * @see EditableSignInfo
	 */
	public List getSignInfoList() {
		return new ArrayList(signInfoDataList.values());
	}

	/**
	 * States that a given sign is a basic one.
	 * 
	 * @param code
	 * @param inUserPart
	 *            is this a userPart declaration ?
	 */
	public void setSignAlwaysDisplay(String code, boolean inUserPart) {
		EditableSignInfo sign = getOrCreateSign(code);
		sign.setAlwaysDisplay(true);
		sign.setAlwaysDisplayProvidedByUser(inUserPart);
		setDirtyFlag();
	}

	private EditableSignInfo getOrCreateSign(String code) {
		if (!signInfoDataList.containsKey(code)) {
			EditableSignInfo editableSignInfo = new EditableSignInfo(code);
			editableSignInfo.setParent(propertyChangeListener);
			signInfoDataList.put(code, editableSignInfo);
			setDirtyFlag();
		}
		return getSign(code);
	}

	private static class TagInfo {
		String tagName;
		boolean userDefined;
		ArrayList labels = new ArrayList();

		public TagInfo(String tagName, boolean userDefined) {
			this.userDefined = userDefined;
			this.tagName = tagName;
		}
	}

	/**
	 * Return the label properties for a given tag.
	 * 
	 * @param currentTag
	 * @return a List of XMLInfoProperty
	 * @see XMLInfoProperty
	 */
	public List getLabelsForTag(String currentTag) {
		return new ArrayList(((TagInfo) tags.get(currentTag)).labels);
	}

	public void removeLabel(XMLInfoProperty prop) {
		if (SignDescriptionConstants.TAG_LABEL.equals(prop.getName())) {
			TagInfo tagInfo = (TagInfo) tags.get(prop
					.get(SignDescriptionConstants.TAG));
			tagInfo.labels.remove(prop);
			prop.setParent(null);
			setDirtyFlag();
		} else
			throw new RuntimeException("Bad XML property. Should be a tag "
					+ prop.toString());
	}

	/**
	 * Is a tag called <code>tag</code> defined here ? 
	 * @param tag
	 * @return
	 */
	public boolean definesTag(String tag) {
		return tags.containsKey(tag);
	}

	/**
	 * Removes a tag from the list.
	 * 
	 * @param tag
	 * @return true if removal succeeded 
	 */
	public boolean removeTag(String tag) {
		boolean result= true;
		// Check if the tag is used somewhere !
		if (getUsedTags().contains(tag)) {
			result= false;
		} else {
			setDirtyFlag();
			tags.remove(tag);
			notifyListeners(new TagEvent(this));
		}
		return result;
	}

	private void notifyListeners(SignInfoModelEvent event) {
		for (int i= 0; i< listeners.size(); i++)
		{
			SignInfoModelEventListener l= (SignInfoModelEventListener) listeners.get(i);
			l.signInfoModelChanged(event);
		}
	}

	public void addSignInfoModelEventListener(SignInfoModelEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeSignInfoModelEventListener(SignInfoModelEventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Has the model been changed since the last save ?
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	public void clearDirtyFlag() {
		dirty= false;
	}
	
	public void setDirtyFlag() {
		dirty= true;
	}
	
	private class PropertyChangeListener implements ChildListener {
		public void childChanged() {
			setDirtyFlag();
		}	
	}
}
