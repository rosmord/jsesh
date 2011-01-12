/*
 * Created on 21 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.interfaces;

import jsesh.mdc.constants.ToggleType;

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
	 * @see jsesh.mdc.interfaces.MDCBuilder#addCadratToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface, jsesh.mdc.interfaces.CadratInterface, int)
	 */
	public void addCadratToBasicItemList(BasicItemListInterface l, CadratInterface c, int shading) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addCadratToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.interfaces.CadratInterface, int)
	 */
	public void addCadratToTopItemList(TopItemListInterface l, CadratInterface e, int shading) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addCartoucheToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.interfaces.CartoucheInterface)
	 */
	public void addCartoucheToTopItemList(TopItemListInterface l, CartoucheInterface c) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addHRuleToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, char, int, int)
	 */
	public void addHRuleToTopItemList(TopItemListInterface l, char lineType, int startPos, int endPos) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addLineBreakToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, int)
	 */
	public void addLineBreakToTopItemList(TopItemListInterface l, int skip) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addModifierToModifierList(jsesh.mdc.interfaces.ModifierListInterface, java.lang.String, java.lang.Integer)
	 */
	public void addModifierToModifierList(ModifierListInterface mods, String name, Integer value) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addPageBreakToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addPageBreakToTopItemList(TopItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTabStopToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTextSuperscriptToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, java.lang.String)
	 */
	public void addTextSuperscriptToTopItemList(TopItemListInterface l, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTextToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface, char, java.lang.String)
	 */
	public void addTextToBasicItemList(BasicItemListInterface l, char scriptCode, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTextToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, char, java.lang.String)
	 */
	public void addTextToTopItemList(TopItemListInterface l, char scriptCode, String text) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToggleToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface, jsesh.mdc.constants.ToggleType)
	 */
	public void addToggleToBasicItemList(BasicItemListInterface l, ToggleType toggleCode) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToggleToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.constants.ToggleType)
	 */
	public void addToggleToTopItemList(TopItemListInterface l, ToggleType toggle) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToHorizontalList(jsesh.mdc.interfaces.HBoxInterface, jsesh.mdc.interfaces.InnerGroupInterface)
	 */
	public void addToHorizontalList(HBoxInterface h, HorizontalListElementInterface elt) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToLigature(jsesh.mdc.interfaces.LigatureInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public void addToLigature(LigatureInterface i, HieroglyphInterface h) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToVerticalList(jsesh.mdc.interfaces.VBoxInterface, jsesh.mdc.interfaces.HBoxInterface)
	 */
	public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildBasicItemList()
	 */
	public BasicItemListInterface buildBasicItemList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildCadrat(jsesh.mdc.interfaces.VBoxInterface)
	 */
	public CadratInterface buildCadrat(VBoxInterface e) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildCartouche(int, int, jsesh.mdc.interfaces.BasicItemListInterface, int)
	 */
	public CartoucheInterface buildCartouche(int type, int leftPart, BasicItemListInterface e, int rightPart) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildHBox()
	 */
	public HBoxInterface buildHBox() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildHieroglyph(boolean, int, java.lang.String, jsesh.mdc.interfaces.ModifierListInterface, int)
	 */
	public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, ModifierListInterface m, int isEnd) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildLigature()
	 */
	public LigatureInterface buildLigature() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildMDCFileInterface(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildModifierList()
	 */
	public ModifierListInterface buildModifierList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildOverwrite(jsesh.mdc.interfaces.HieroglyphInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public OverwriteInterface buildOverwrite(HieroglyphInterface e1, HieroglyphInterface e2) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildPhilology(int, jsesh.mdc.interfaces.BasicItemListInterface, int)
	 */
	public PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildSubCadrat(jsesh.mdc.interfaces.BasicItemListInterface)
	 */
	public SubCadratInterface buildSubCadrat(BasicItemListInterface e) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildTopItemList()
	 */
	public TopItemListInterface buildTopItemList() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildVBox()
	 */
	public VBoxInterface buildVBox() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#completeLigature(jsesh.mdc.interfaces.LigatureInterface)
	 */
	public void completeLigature(LigatureInterface i) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#reset()
	 */
	public void reset() {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#setHieroglyphPosition(jsesh.mdc.interfaces.HieroglyphInterface, int, int, int)
	 */
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y, int scale) {
		// Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToAbsoluteGroup(jsesh.mdc.interfaces.AbsoluteGroupInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface RESULT, HieroglyphInterface e) {
		// Auto-generated method stub
		
	}
	

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {
		// NO-OP

	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {
		//NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {
		//NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addZoneStartToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.interfaces.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1,
			ZoneStartInterface e2) {
		// NO OP

	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {
		// NO OP
		return null;
	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {
		// NO OP
		return null;
	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.ZoneStartInterface, jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
		// NO OP

	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.CadratInterface, jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(CadratInterface result, OptionListInterface e1) {
		// NO OP
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildComplexLigature(jsesh.mdc.interfaces.InnerGroupInterface, jsesh.mdc.interfaces.HieroglyphInterface, jsesh.mdc.interfaces.InnerGroupInterface)
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
