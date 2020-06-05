package jsesh.utilitySoftwares.signInfoEditor.helpers;

import jsesh.hieroglyphs.data.io.SignDescriptionBuilder;
import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoModel;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;

/**
 * Builder for creating memory models of XML sign descriptions.
 * 
 * @author rosmord
 * 
 */
public class SignInfoModelBuilder implements SignDescriptionBuilder {

	/**
	 * Are we reading data from the user ?
	 */
	public boolean inUserPart = false;
	private SignInfoModel signInfoModel;

	public SignInfoModelBuilder(SignInfoModel signInfoModel) {
		this.signInfoModel = signInfoModel;
	}

	public void addDeterminative(String category, String lang, String label) {
		// Determinative categories are not used yet.
	}

	public void addDeterminativeValue(String currentSign, String category) {
		// Not used yet.
	}

	public void addPartOf(String currentSign, String baseSign) {
		SignInfoProperty contains = new SignInfoProperty(
				SignDescriptionConstants.CONTAINS, inUserPart);
		setSignCode(contains, baseSign);
		contains.setAttribute(SignDescriptionConstants.PART_CODE, currentSign);
		signInfoModel.add(contains);
	}

	/**
	 * Fix the data which corresponds to the current sign code.
	 * 
	 * @param prop
	 * @param baseSign
	 */
	private void setSignCode(XMLInfoProperty prop, String baseSign) {
		prop.setAttribute(SignDescriptionConstants.SIGN, baseSign);
	}

	public void addPhantom(String currentSign, String baseSign, String existsIn) {
		// TODO Auto-generated method stub

	}

	public void addSignDescription(String currentSign, String text,
			String currentLang) {
		SignInfoProperty data = new SignInfoProperty(
				SignDescriptionConstants.SIGN_DESCRIPTION, text, isInUserPart());
		setSignCode(data, currentSign);
		data.setAttribute(SignDescriptionConstants.LANG, currentLang);
		signInfoModel.add(data);
	}

	public void addSimilarTo(String currentSign, String baseSign) {
		SignInfoProperty data = new SignInfoProperty(
				SignDescriptionConstants.IS_SIMILAR, "", isInUserPart());
		setSignCode(data, currentSign);
		signInfoModel.add(data);
	}

	public void addTagLabel(String tag, String lang, String label) {
		signInfoModel.addTagLabel(tag, lang, label, isInUserPart());
	}

	public void addTagCategory(String tag) {
		signInfoModel.addTagCategory(tag,isInUserPart());
	}
	
	public void addTagToSign(String currentSign, String tag) {
		SignInfoProperty data = new SignInfoProperty(
				SignDescriptionConstants.HAS_TAG, "", isInUserPart());
		setSignCode(data, currentSign);
		data.setAttribute(SignDescriptionConstants.TAG, tag);
		signInfoModel.add(data);
	}

	public void addTransliteration(String currentSign, String transliteration,
			String use, String type) {
		SignInfoProperty data = new SignInfoProperty(
				SignDescriptionConstants.HAS_TRANSLITERATION, "",
				isInUserPart());
		setSignCode(data, currentSign);
		data.setAttribute(SignDescriptionConstants.TRANSLITERATION,
				transliteration);
		data.setAttribute(SignDescriptionConstants.USE, use);
		data.setAttribute(SignDescriptionConstants.TYPE, type);
		signInfoModel.add(data);
	}

	public void addVariant(String currentSign, String baseSign,
			String isSimilar, String degree) {
		SignInfoProperty data = new SignInfoProperty(
				SignDescriptionConstants.VARIANT_OF, "", inUserPart);
		setSignCode(data, currentSign);
		data.setAttribute(SignDescriptionConstants.BASE_SIGN, baseSign);
		data.setAttribute(SignDescriptionConstants.IS_SIMILAR, isSimilar);
		data.setAttribute(SignDescriptionConstants.LINGUISTIC, degree);
		signInfoModel.add(data);
	}

	/**
	 * States that we are now loading a user-defined file.
	 * 
	 * @param inUserPart
	 */
	public void setInUserPart(boolean inUserPart) {
		this.inUserPart = inUserPart;
	}

	public boolean isInUserPart() {
		return inUserPart;
	}

	public void setSignAlwaysDisplay(String currentSign) {
		signInfoModel.setSignAlwaysDisplay(currentSign,inUserPart);
	}
}
