package jsesh.mdc.model;

import jsesh.mdc.constants.Dialect;
import jsesh.mdc.constants.TabbingJustification;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.constants.ToggleType;
import jsesh.mdc.constants.WordEndingCode;
import jsesh.mdc.interfaces.AbsoluteGroupInterface;
import jsesh.mdc.interfaces.BasicItemListInterface;
import jsesh.mdc.interfaces.CadratInterface;
import jsesh.mdc.interfaces.CartoucheInterface;
import jsesh.mdc.interfaces.ComplexLigatureInterface;
import jsesh.mdc.interfaces.HBoxInterface;
import jsesh.mdc.interfaces.HieroglyphInterface;
import jsesh.mdc.interfaces.HorizontalListElementInterface;
import jsesh.mdc.interfaces.InnerGroupInterface;
import jsesh.mdc.interfaces.LigatureInterface;
import jsesh.mdc.interfaces.MDCBuilder;
import jsesh.mdc.interfaces.MDCFileInterface;
import jsesh.mdc.interfaces.ModifierListInterface;
import jsesh.mdc.interfaces.OptionListInterface;
import jsesh.mdc.interfaces.OverwriteInterface;
import jsesh.mdc.interfaces.PhilologyInterface;
import jsesh.mdc.interfaces.SubCadratInterface;
import jsesh.mdc.interfaces.TopItemListInterface;
import jsesh.mdc.interfaces.VBoxInterface;
import jsesh.mdc.interfaces.ZoneStartInterface;

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
	 * jsesh.mdc.interfaces.MDCBuilder#addCadratToBasicItemList(jsesh.mdc.interfaces
	 * .BasicItemListInterface, jsesh.mdc.interfaces.CadratInterface, int)
	 */
	public void addCadratToBasicItemList(BasicItemListInterface l,
			CadratInterface c, int shading) {
		Cadrat cadrat = (Cadrat) c;
		cadrat.setShading(shading);
		((BasicItemList) l).addBasicItem(cadrat);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addCadratToTopItemList(jsesh.mdc.interfaces
	 * .TopItemListInterface, jsesh.mdc.interfaces.CadratInterface, int)
	 */
	public void addCadratToTopItemList(TopItemListInterface l,
			CadratInterface e, int shading) {
		Cadrat c = (Cadrat) e;
		c.setShading(shading);
		addToTop(l, c);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addCartoucheToTopItemList(jsesh.mdc.
	 * interfaces.TopItemListInterface, jsesh.mdc.interfaces.CartoucheInterface)
	 */
	public void addCartoucheToTopItemList(TopItemListInterface l,
			CartoucheInterface c) {
		addToTop(l, (TopItem) c);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addHRuleToTopItemList(jsesh.mdc.interfaces
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
	 * jsesh.mdc.interfaces.MDCBuilder#addToModifierList(jsesh.mdc.interfaces
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
	 * jsesh.mdc.interfaces.MDCBuilder#addTabStopToTopItemList(jsesh.mdc.interfaces
	 * .TopItemListInterface, int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
		addToTop(l, new TabStop(stopWidth));
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addTextSuperscriptToTopItemList(jsesh
	 * .mdc.interfaces.TopItemListInterface, java.lang.String)
	 */
	public void addTextSuperscriptToTopItemList(TopItemListInterface l,
			String text) {
		// Remove protection characters... (only in front of "\" and "-")
		text= text.replaceAll("\\\\(\\\\|-)", "$1");
		System.out.println(text);
		Superscript t = new Superscript(text);
		addToTop(l, t);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addTextToBasicItemList(jsesh.mdc.interfaces
	 * .BasicItemListInterface, char code, java.lang.String)
	 */
	public void addTextToBasicItemList(BasicItemListInterface l,
			char scriptCode, String text) {
		((BasicItemList) l).addBasicItem(new AlphabeticText(scriptCode, text));
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addTextToTopItemList(jsesh.mdc.interfaces
	 * .TopItemListInterface, char scriptCode, java.lang.String)
	 */
	public void addTextToTopItemList(TopItemListInterface l, char scriptCode,
			String text) {
		AlphabeticText t = new AlphabeticText(scriptCode, text);
		addToTop(l, t);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addToggleToBasicItemList(jsesh.mdc.interfaces
	 * .BasicItemListInterface, int)
	 */
	public void addToggleToBasicItemList(BasicItemListInterface l,
			ToggleType toggleCode) {
		addToggle(toggleCode);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addToggleToTopItemList(jsesh.mdc.interfaces
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
	 * jsesh.mdc.interfaces.MDCBuilder#addToHorizontalList(jsesh.mdc.interfaces
	 * .HBoxInterface, jsesh.mdc.interfaces.HorizontalListElementInterface)
	 */
	public void addToHorizontalList(HBoxInterface h,
			HorizontalListElementInterface elt) {
		((HBox) h).addHorizontalListElement((HorizontalListElement) elt);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#addToLigature(jsesh.mdc.interfaces.
	 * LigatureInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public void addToLigature(LigatureInterface i, HieroglyphInterface h) {
		Ligature l = ((Ligature) i);
		l.addHieroglyph((Hieroglyph) h);
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addToVerticalList(jsesh.mdc.interfaces
	 * .VBoxInterface, jsesh.mdc.interfaces.HBoxInterface)
	 */
	public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
		((Cadrat) l).addHBox((HBox) h);
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildBasicItemList()
	 */
	public BasicItemListInterface buildBasicItemList() {
		return new BasicItemList();
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildCadrat(jsesh.mdc.interfaces.
	 * VBoxInterface)
	 */
	public CadratInterface buildCadrat(VBoxInterface e) {
		return (CadratInterface) e;
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildCartouche(int, int,
	 * jsesh.mdc.interfaces.BasicItemListInterface, int)
	 */
	public CartoucheInterface buildCartouche(int type, int leftPart,
			BasicItemListInterface e, int rightPart) {
		Cartouche c;
		c = new Cartouche(type, leftPart, rightPart, (BasicItemList) e);
		return c;
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildHBox()
	 */
	public HBoxInterface buildHBox() {
		return new HBox();
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildHieroglyph(java.lang.Boolean,
	 * int, java.lang.String, jsesh.mdc.interfaces.ModifierListInterface,
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
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildLigature()
	 */
	public LigatureInterface buildLigature() {
		return new Ligature();
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#buildMDCFileInterface(jsesh.mdc.interfaces
	 * .TopItemListInterface)
	 */
	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
		result = (TopItemList) l;
		return (MDCFileInterface) l;
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildModifierList()
	 */
	public ModifierListInterface buildModifierList() {
		return new ModifiersList();
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildOverwrite(jsesh.mdc.interfaces.
	 * HieroglyphInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public OverwriteInterface buildOverwrite(HieroglyphInterface e1,
			HieroglyphInterface e2) {
		Overwrite result;
		result = new Overwrite((Hieroglyph) e1, (Hieroglyph) e2);
		return result;
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildPhilology(int,
	 * jsesh.mdc.interfaces.BasicItemListInterface, int) remark : normally
	 * e2=e1.
	 */

	public PhilologyInterface buildPhilology(int code1,
			BasicItemListInterface e, int code2) {
		return new Philology(code1, (BasicItemList) e);
	}

	/*
	 * @seejsesh.mdc.interfaces.MDCBuilder#buildSubCadrat(jsesh.mdc.interfaces.
	 * BasicItemListInterface)
	 */
	public SubCadratInterface buildSubCadrat(BasicItemListInterface e) {
		SubCadrat result;
		result = new SubCadrat((BasicItemList) e);
		return result;
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildTopItemList()
	 */
	public TopItemListInterface buildTopItemList() {
		return new TopItemList();
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildVBox()
	 */
	public VBoxInterface buildVBox() {
		return new Cadrat();
	}

	/*
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#completeLigature(jsesh.mdc.interfaces
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
	 * jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToTopItemList
	 * (jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToBasicItemList
	 * (jsesh.mdc.interfaces.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
	}

	/*
	 * @see jsesh.mdc.interfaces.MDCBuilder#reset()
	 */
	public void reset() {
		result = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#setHieroglyphPosition(jsesh.mdc.interfaces
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
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		return new AbsoluteGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.interfaces.MDCBuilder#addToAbsoluteGroup(jsesh.mdc.interfaces
	 * .AbsoluteGroupInterface, jsesh.mdc.interfaces.HieroglyphInterface)
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
	 * interfaces.TopItemListInterface, jsesh.mdc.interfaces.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1,
			ZoneStartInterface e2) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.
	 * OptionListInterface, java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {
		OptionsMap optionsMap = (OptionsMap) e1;
		optionsMap.setOption(optName, val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.
	 * OptionListInterface, java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {
		OptionsMap optionsMap = (OptionsMap) e1;
		optionsMap.setOption(optName, val);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.
	 * OptionListInterface, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {
		OptionsMap o = (OptionsMap) e1;
		o.setOption(optName, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {
		return new OptionsMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {
		return new ZoneStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.
	 * ZoneStartInterface, jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.
	 * CadratInterface, jsesh.mdc.interfaces.OptionListInterface)
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
				.valueOf(justificationCode.toUpperCase());
		Tabbing tabbing= new Tabbing(id, justification, orientation);
		l.addTopItem(tabbing);

	}

}
