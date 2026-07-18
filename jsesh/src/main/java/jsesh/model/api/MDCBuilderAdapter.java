/*
 * Created on 21 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.model.api;

import jsesh.model.constants.ToggleType;

/**
 * A utility implementation of MDCBuilder.
 * <p>This class can be used as a base for writing simple
 * builders ; however you should take note that its methods basically
 * do nothing. In most cases, you should completely write the builder,
 * in order to forget no construct. The only case where this class is useful is when
 * you are only interested in one construct or two.  
 * @author rosmord
 */
abstract public class MDCBuilderAdapter implements MDCBuilder {

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addCadratToBasicItemList(jsesh.model.api.BasicItemListInterface, jsesh.model.api.CadratInterface, int)
	 */
	public void addCadratToBasicItemList(BasicItemListInterface l, CadratInterface c, int shading) {
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addCadratToTopItemList(jsesh.model.api.TopItemListInterface, jsesh.model.api.CadratInterface, int)
	 */
	public void addCadratToTopItemList(TopItemListInterface l, CadratInterface e, int shading) {
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addCartoucheToTopItemList(jsesh.model.api.TopItemListInterface, jsesh.model.api.CartoucheInterface)
	 */
	public void addCartoucheToTopItemList(TopItemListInterface l, CartoucheInterface c) {
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addHRuleToTopItemList(jsesh.model.api.TopItemListInterface, char, int, int)
	 */
	public void addHRuleToTopItemList(TopItemListInterface l, char lineType, int startPos, int endPos) {
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addLineBreakToTopItemList(jsesh.model.api.TopItemListInterface, int)
	 */
	public void addLineBreakToTopItemList(TopItemListInterface l, int skip) {
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addModifierToModifierList(jsesh.model.api.ModifierListInterface, java.lang.String, java.lang.Integer)
	 */
	public void addModifierToModifierList(ModifierListInterface mods, String name, Integer value) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addPageBreakToTopItemList(jsesh.model.api.TopItemListInterface)
	 */
	public void addPageBreakToTopItemList(TopItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addStartHieroglyphicTextToTopItemList(jsesh.model.api.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addStartHieroglyphicTextToBasicItemList(jsesh.model.api.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addTabStopToTopItemList(jsesh.model.api.TopItemListInterface, int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addTextSuperscriptToTopItemList(jsesh.model.api.TopItemListInterface, java.lang.String)
	 */
	public void addTextSuperscriptToTopItemList(TopItemListInterface l, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addTextToBasicItemList(jsesh.model.api.BasicItemListInterface, char, java.lang.String)
	 */
	public void addTextToBasicItemList(BasicItemListInterface l, char scriptCode, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addTextToTopItemList(jsesh.model.api.TopItemListInterface, char, java.lang.String)
	 */
	public void addTextToTopItemList(TopItemListInterface l, char scriptCode, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToggleToBasicItemList(jsesh.model.api.BasicItemListInterface, jsesh.model.constants.ToggleType)
	 */
	public void addToggleToBasicItemList(BasicItemListInterface l, ToggleType toggleCode) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToggleToTopItemList(jsesh.model.api.TopItemListInterface, jsesh.model.constants.ToggleType)
	 */
	public void addToggleToTopItemList(TopItemListInterface l, ToggleType toggle) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToHorizontalList(jsesh.model.api.HBoxInterface, jsesh.model.api.InnerGroupInterface)
	 */
	public void addToHorizontalList(HBoxInterface h, HorizontalListElementInterface elt) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToLigature(jsesh.model.api.LigatureInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public void addToLigature(LigatureInterface i, HieroglyphInterface h) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToVerticalList(jsesh.model.api.VBoxInterface, jsesh.model.api.HBoxInterface)
	 */
	public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildBasicItemList()
	 */
	public BasicItemListInterface buildBasicItemList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildCadrat(jsesh.model.api.VBoxInterface)
	 */
	public CadratInterface buildCadrat(VBoxInterface e) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildCartouche(int, int, jsesh.model.api.BasicItemListInterface, int)
	 */
	public CartoucheInterface buildCartouche(int type, int leftPart, BasicItemListInterface e, int rightPart) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildHBox()
	 */
	public HBoxInterface buildHBox() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildHieroglyph(boolean, int, java.lang.String, jsesh.model.api.ModifierListInterface, int)
	 */
	public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, ModifierListInterface m, int isEnd) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildLigature()
	 */
	public LigatureInterface buildLigature() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildMDCFileInterface(jsesh.model.api.TopItemListInterface)
	 */
	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildModifierList()
	 */
	public ModifierListInterface buildModifierList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildOverwrite(jsesh.model.api.HieroglyphInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public OverwriteInterface buildOverwrite(HieroglyphInterface e1, HieroglyphInterface e2) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildPhilology(int, jsesh.model.api.BasicItemListInterface, int)
	 */
	public PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildSubCadrat(jsesh.model.api.BasicItemListInterface)
	 */
	public SubCadratInterface buildSubCadrat(BasicItemListInterface e) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildTopItemList()
	 */
	public TopItemListInterface buildTopItemList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildVBox()
	 */
	public VBoxInterface buildVBox() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#completeLigature(jsesh.model.api.LigatureInterface)
	 */
	public void completeLigature(LigatureInterface i) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#reset()
	 */
	public void reset() {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#setHieroglyphPosition(jsesh.model.api.HieroglyphInterface, int, int, int)
	 */
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y, int scale) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addToAbsoluteGroup(jsesh.model.api.AbsoluteGroupInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface RESULT, HieroglyphInterface e) {
		// Auto-generated method stub
		
	}
	

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addOption(jsesh.model.api.OptionListInterface, java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {
		// NO-OP

	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addOption(jsesh.model.api.OptionListInterface, java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {
		//NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addOption(jsesh.model.api.OptionListInterface, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {
		//NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#addZoneStartToTopItemList(jsesh.model.api.TopItemListInterface, jsesh.model.api.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1,
			ZoneStartInterface e2) {
		// NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {
		// NO OP
		return null;
	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {
		// NO OP
		return null;
	}
	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#setOptionList(jsesh.model.api.ZoneStartInterface, jsesh.model.api.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
		// NO OP

	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#setOptionList(jsesh.model.api.CadratInterface, jsesh.model.api.OptionListInterface)
	 */
	public void setOptionList(CadratInterface result, OptionListInterface e1) {
		// NO OP
	}

	/* (non-Javadoc)
	 * @see jsesh.model.api.MDCBuilder#buildComplexLigature(jsesh.model.api.InnerGroupInterface, jsesh.model.api.HieroglyphInterface, jsesh.model.api.InnerGroupInterface)
	 */
	public ComplexLigatureInterface buildComplexLigature(InnerGroupInterface e1, HieroglyphInterface e2, InnerGroupInterface e3) {
		// NO OP
		return null;
	}
	
	public void addTabbingClearToTopItemList(TopItemListInterface e1) {
		// NO OP
	}
	
	public void addTabbingToTopItemList(TopItemListInterface e1,
			OptionListInterface e3) {
		// NO OP
	}
	
	
}
