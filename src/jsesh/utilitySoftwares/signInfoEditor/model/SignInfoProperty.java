package jsesh.utilitySoftwares.signInfoEditor.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



/**
 * An entry related to a sign in SignInfo.
 * @see SignInfoModel
 * @author rosmord
 *
 */
public class SignInfoProperty extends XMLInfoProperty {
	/**
	 * Create a new SignInfo Data.
	 * @param name the kind of information to supply.
	 * @param content the actual information.
	 * @param userDefinition is the information supplied by a user or by the standard software ?
	 */
	public SignInfoProperty(String name, String content, boolean userDefinition) {
		super(name,content, userDefinition);
	}

	
	public SignInfoProperty(String name, boolean userDefinition) {
		super(name, userDefinition);
	}


	public SignInfoProperty(SignInfoProperty signInfoProperty) {
		this(signInfoProperty.getName(), signInfoProperty.getContent(), signInfoProperty.isUserDefinition());
		Iterator it= signInfoProperty.getAttributes().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry= (Entry) it.next();
			setAttribute((String)entry.getKey(),(String)entry.getValue());
		}
	}


	public String getSignCode() {
		return get("sign");
	}

}
