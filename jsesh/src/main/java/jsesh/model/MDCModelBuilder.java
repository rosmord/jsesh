package jsesh.model;

import java.util.Locale;

import jsesh.model.constants.Dialect;
import jsesh.model.constants.TabbingJustification;
import jsesh.model.constants.TextOrientation;
import jsesh.model.constants.ToggleType;
import jsesh.model.constants.WordEndingCode;
import jsesh.model.api.AbsoluteGroupInterface;
import jsesh.model.api.BasicItemListInterface;
import jsesh.model.api.CadratInterface;
import jsesh.model.api.CartoucheInterface;
import jsesh.model.api.ComplexLigatureInterface;
import jsesh.model.api.HBoxInterface;
import jsesh.model.api.HieroglyphInterface;
import jsesh.model.api.HorizontalListElementInterface;
import jsesh.model.api.InnerGroupInterface;
import jsesh.model.api.LigatureInterface;
import jsesh.model.api.MDCBuilder;
import jsesh.model.api.MDCFileInterface;
import jsesh.model.api.ModifierListInterface;
import jsesh.model.api.OptionListInterface;
import jsesh.model.api.OverwriteInterface;
import jsesh.model.api.PhilologyInterface;
import jsesh.model.api.SubCadratInterface;
import jsesh.model.api.TopItemListInterface;
import jsesh.model.api.VBoxInterface;
import jsesh.model.api.ZoneStartInterface;

/**
 * @author rosmord
 * 
 */

public class MDCModelBuilder implements MDCBuilder {

	private TopItemList result;
	private TopItemState currentState;
	private Dialect dialect = Dialect.OTHER;

	/**
	 * Constructor for MDCModelBuilder.
	 */
	public MDCModelBuilder() {
		this(Dialect.OTHER);
	}

	/**
	 * Create a builder for manuel de codage data.
	 * 
	 * @param dialect
	 *            a dialect for the Manuel de codage
	 * @see Dialect
	 */
	public MDCModelBuilder(Dialect dialect) {
		result = null;
		currentState = new TopItemState();
		this.dialect = dialect;
	}

	private void addToTop(TopItemListInterface l, TopItem elt) {
		elt.setState(currentState.duplicate());
		((TopItemList) l).addTopItem(elt);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addCadratToBasicItemList(jsesh.model.api
	 * .BasicItemListInterface, jsesh.model.api.CadratInterface, int)
	 */
	public void addCadratToBasicItemList(BasicItemListInterface l,
			CadratInterface c, int shading) {
		Cadrat cadrat = (Cadrat) c;
		cadrat.setShading(shading);
		((BasicItemList) l).addBasicItem(cadrat);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addCadratToTopItemList(jsesh.model.api
	 * .TopItemListInterface, jsesh.model.api.CadratInterface, int)
	 */
	public void addCadratToTopItemList(TopItemListInterface l,
			CadratInterface e, int shading) {
		Cadrat c = (Cadrat) e;
		c.setShading(shading);
		addToTop(l, c);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addCartoucheToTopItemList(jsesh.mdc.
	 * interfaces.TopItemListInterface, jsesh.model.api.CartoucheInterface)
	 */
	public void addCartoucheToTopItemList(TopItemListInterface l,
			CartoucheInterface c) {
		addToTop(l, (TopItem) c);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addHRuleToTopItemList(jsesh.model.api
	 * .TopItemListInterface, char, int, int)
	 */
	public void addHRuleToTopItemList(TopItemListInterface l, char lineType,
			int startPos, int endPos) {
		addToTop(l, new HRule(lineType, startPos, endPos));
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addLineBreakToTopItemList(jsesh.mdc.
	 * interfaces.TopItemListInterface, int)
	 */
	public void addLineBreakToTopItemList(TopItemListInterface l, int skip) {
		LineBreak t;
		t = new LineBreak(skip);
		addToTop(l, t);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addToModifierList(jsesh.model.api
	 * .ModifierListInterface, java.lang.String, java.lang.Integer)
	 */
	public void addModifierToModifierList(ModifierListInterface mods,
			String name, Integer value) {
		String actualName = name;
		Integer actualValue = value;
		// Deal with incompatibility between Winglyph and MacScribe
		if ("r".equals(name) && dialect == Dialect.MACSCRIBE) {
			// Then, r is the same as "R".
			actualName = "R";
		}
		((ModifiersList) mods).includeModifier(new Modifier(actualName,
				actualValue));
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addPageBreakToTopItemList(jsesh.mdc.
	 * interfaces.TopItemListInterface)
	 */
	public void addPageBreakToTopItemList(TopItemListInterface l) {
		addToTop(l, new PageBreak());
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addTabStopToTopItemList(jsesh.model.api
	 * .TopItemListInterface, int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
		addToTop(l, new TabStop(stopWidth));
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addTextSuperscriptToTopItemList(jsesh
	 * .mdc.interfaces.TopItemListInterface, java.lang.String)
	 */
	public void addTextSuperscriptToTopItemList(TopItemListInterface l,
			String text) {
		// Remove protection characters... (only in front of "\" and "-")
		text= text.replaceAll("\\\\(\\\\|-)", "$1");
		Superscript t = new Superscript(text);
		addToTop(l, t);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addTextToBasicItemList(jsesh.model.api
	 * .BasicItemListInterface, char code, java.lang.String)
	 */
	public void addTextToBasicItemList(BasicItemListInterface l,
			char scriptCode, String text) {
		((BasicItemList) l).addBasicItem(new AlphabeticText(scriptCode, text));
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addTextToTopItemList(jsesh.model.api
	 * .TopItemListInterface, char scriptCode, java.lang.String)
	 */
	public void addTextToTopItemList(TopItemListInterface l, char scriptCode,
			String text) {
		AlphabeticText t = new AlphabeticText(scriptCode, text);
		addToTop(l, t);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addToggleToBasicItemList(jsesh.model.api
	 * .BasicItemListInterface, int)
	 */
	public void addToggleToBasicItemList(BasicItemListInterface l,
			ToggleType toggleCode) {
		addToggle(toggleCode);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addToggleToTopItemList(jsesh.model.api
	 * .TopItemListInterface, int)
	 */
	public void addToggleToTopItemList(TopItemListInterface l,
			ToggleType toggleCode) {
		addToggle(toggleCode);
	}

	private void addToggle(ToggleType toggle) {
		if (toggle == ToggleType.BLACK) {
			currentState.setRed(false);
		} else if (toggle == ToggleType.BLACKRED) {
			currentState.setRed(!currentState.isRed());
		} else if (toggle == ToggleType.LACUNA) {
		} else if (toggle == ToggleType.LINELACUNA) {
			// Should lineLACUNA be a toggle ???
			// TO DO : IMPLEMENT LACUNAS
		} else if (toggle == ToggleType.OMMIT) {
			// IMPLEMENT OMMIT
		} else if (toggle == ToggleType.RED) {
			currentState.setRed(true);
		} else if (toggle == ToggleType.SHADINGOFF) {
			currentState.setShaded(false);
		} else if (toggle == ToggleType.SHADINGON) {
			currentState.setShaded(true);
		} else if (toggle == ToggleType.SHADINGTOGGLE) {
			currentState.setShaded(!currentState.isShaded());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.api.MDCBuilder#addToHorizontalList(jsesh.model.api
	 * .HBoxInterface, jsesh.model.api.HorizontalListElementInterface)
	 */
	public void addToHorizontalList(HBoxInterface h,
			HorizontalListElementInterface elt) {
		((HBox) h).addHorizontalListElement((HorizontalListElement) elt);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addToLigature(jsesh.model.api.
	 * LigatureInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public void addToLigature(LigatureInterface i, HieroglyphInterface h) {
		Ligature l = ((Ligature) i);
		l.addHieroglyph((Hieroglyph) h);
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#addToVerticalList(jsesh.model.api
	 * .VBoxInterface, jsesh.model.api.HBoxInterface)
	 */
	public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
		((Cadrat) l).addHBox((HBox) h);
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildBasicItemList()
	 */
	public BasicItemListInterface buildBasicItemList() {
		return new BasicItemList();
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildCadrat(jsesh.model.api.
	 * VBoxInterface)
	 */
	public CadratInterface buildCadrat(VBoxInterface e) {
		return (CadratInterface) e;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildCartouche(int, int,
	 * jsesh.model.api.BasicItemListInterface, int)
	 */
	public CartoucheInterface buildCartouche(int type, int leftPart,
			BasicItemListInterface e, int rightPart) {
		Cartouche c;
		c = new Cartouche(type, leftPart, rightPart, (BasicItemList) e);
		return c;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildHBox()
	 */
	public HBoxInterface buildHBox() {
		return new HBox();
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildHieroglyph(java.lang.Boolean,
	 * int, java.lang.String, jsesh.model.api.ModifierListInterface,
	 * java.lang.Boolean)
	 */
	public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type,
			String code, ModifierListInterface m, int isEnd) {
		Hieroglyph result;
		result = new Hieroglyph(code);

		result.setGrammar(isGrammar);
		result.setType(type);
		switch (isEnd) {
		case 0:
			result.setEndingCode(WordEndingCode.NONE);
			break;
		case 1:
			result.setEndingCode(WordEndingCode.WORD_END);
			break;
		case 2:
			result.setEndingCode(WordEndingCode.SENTENCE_END);
			break;
		}
		result.setModifiers((ModifiersList) m);
		return result;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildLigature()
	 */
	public LigatureInterface buildLigature() {
		return new Ligature();
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#buildMDCFileInterface(jsesh.model.api
	 * .TopItemListInterface)
	 */
	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
		result = (TopItemList) l;
		return (MDCFileInterface) l;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildModifierList()
	 */
	public ModifierListInterface buildModifierList() {
		return new ModifiersList();
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildOverwrite(jsesh.model.api.
	 * HieroglyphInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public OverwriteInterface buildOverwrite(HieroglyphInterface e1,
			HieroglyphInterface e2) {
		Overwrite result;
		result = new Overwrite((Hieroglyph) e1, (Hieroglyph) e2);
		return result;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildPhilology(int,
	 * jsesh.model.api.BasicItemListInterface, int) remark : normally
	 * e2=e1.
	 */

	public PhilologyInterface buildPhilology(int code1,
			BasicItemListInterface e, int code2) {
		return new Philology(code1, (BasicItemList) e);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildSubCadrat(jsesh.model.api.
	 * BasicItemListInterface)
	 */
	public SubCadratInterface buildSubCadrat(BasicItemListInterface e) {
		SubCadrat result;
		result = new SubCadrat((BasicItemList) e);
		return result;
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildTopItemList()
	 */
	public TopItemListInterface buildTopItemList() {
		return new TopItemList();
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#buildVBox()
	 */
	public VBoxInterface buildVBox() {
		return new Cadrat();
	}

	/*
	 * @see
	 * jsesh.model.api.MDCBuilder#completeLigature(jsesh.model.api
	 * .LigatureInterface)
	 */
	public void completeLigature(LigatureInterface i) {
		// THIS METHOD INTENTIONNALY LEFT BLANK :-)
	}

	/*
	 * Returns the result.
	 * 
	 * @return TopItemList
	 */
	public TopItemList getResult() {
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.api.MDCBuilder#addStartHieroglyphicTextToTopItemList
	 * (jsesh.model.api.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.api.MDCBuilder#addStartHieroglyphicTextToBasicItemList
	 * (jsesh.model.api.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
	}

	/*
	 * @see jsesh.model.api.MDCBuilder#reset()
	 */
	public void reset() {
		result = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.api.MDCBuilder#setHieroglyphPosition(jsesh.model.api
	 * .HieroglyphInterface, int, int, int)
	 */
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y,
			int scale) {
		Hieroglyph hiero = (Hieroglyph) h;
		hiero.setExplicitPosition(x, y, scale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.api.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		return new AbsoluteGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.api.MDCBuilder#addToAbsoluteGroup(jsesh.model.api
	 * .AbsoluteGroupInterface, jsesh.model.api.HieroglyphInterface)
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface group,
			HieroglyphInterface e) {
		AbsoluteGroup grp = (AbsoluteGroup) group;
		grp.addHieroglyph((Hieroglyph) e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addZoneStartToTopItemList(jsesh.mdc.
	 * interfaces.TopItemListInterface, jsesh.model.api.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1,
			ZoneStartInterface e2) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.model.api.
	 * OptionListInterface, java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {
		OptionsMap optionsMap = (OptionsMap) e1;
		optionsMap.setOption(optName, val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.model.api.
	 * OptionListInterface, java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {
		OptionsMap optionsMap = (OptionsMap) e1;
		optionsMap.setOption(optName, val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.model.api.
	 * OptionListInterface, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {
		OptionsMap o = (OptionsMap) e1;
		o.setOption(optName, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.api.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {
		return new OptionsMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.api.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {
		return new ZoneStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.model.api.
	 * ZoneStartInterface, jsesh.model.api.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.model.api.
	 * CadratInterface, jsesh.model.api.OptionListInterface)
	 */
	public void setOptionList(CadratInterface result, OptionListInterface e1) {
		// TODO Auto-generated method stub

	}

	public ComplexLigatureInterface buildComplexLigature(
			InnerGroupInterface e1, HieroglyphInterface e2,
			InnerGroupInterface e3) {
		return new ComplexLigature((InnerGroup) e1, (Hieroglyph) e2,
				(InnerGroup) e3);
	}

	public void addTabbingClearToTopItemList(TopItemListInterface e1) {
		TopItemList l = (TopItemList) e1;
		l.addTopItem(new TabbingClear());
	}

	// TODO : improve error handling here.
	public void addTabbingToTopItemList(TopItemListInterface e1,
			OptionListInterface e3) {
		TopItemList l = (TopItemList) e1;
		OptionsMap options = (OptionsMap) e3;
		int id = options.getInt("id");
		String orientationCode = options.getString("orientation", "horizontal");
		String justificationCode = options.getString("justification", "left");
		TextOrientation orientation = TextOrientation.HORIZONTAL;
		if (orientationCode.equalsIgnoreCase(TextOrientation.VERTICAL
				.toString())) {
			orientation = TextOrientation.VERTICAL;
		}
		TabbingJustification justification = TabbingJustification
				.valueOf(justificationCode.toUpperCase(Locale.ENGLISH));
		Tabbing tabbing= new Tabbing(id, justification, orientation);
		l.addTopItem(tabbing);

	}

}
