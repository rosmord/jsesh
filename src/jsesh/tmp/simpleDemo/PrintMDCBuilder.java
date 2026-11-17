package jsesh.tmp.simpleDemo;

/**
 * PrintMDCBuilder.java
 *
 *
 * Created: Thu Apr 18 18:35:25 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

import jsesh.mdc.constants.ToggleType;
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
 * Returns a String representation of a parsed MDC text. 
 * <p>currently for testing purporses, and demonstration of MDCBuilder. The class is incomplete.
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * @version 1.0
 */

public class PrintMDCBuilder implements MDCBuilder {

	int hieroNbr = 0; // Used while building ligatures.

	int left; // for cartouches

	int right; // for cartouches

	int type; // for cartouches

	private class TmpString implements BasicItemListInterface, CadratInterface,
			CartoucheInterface, HieroglyphInterface, TopItemListInterface,
			LigatureInterface, ModifierListInterface, PhilologyInterface,
			InnerGroupInterface, HBoxInterface, VBoxInterface,
			SubCadratInterface, MDCFileInterface, OverwriteInterface, ComplexLigatureInterface {
		private String rep;

		public TmpString() {
			rep = "";
		}

		public TmpString(String s) {
			rep = s;
		}

		public void set(String s) {
			rep = s;
		}

		public String toString() {
			return rep;
		}

		public void add(String b) {
			rep = rep + b;
		}

		public void add(TmpString b) {
			rep = rep + b;
		}
	}

	private String result;

	public PrintMDCBuilder() {
		reset();
	}

	public void reset() {
		result = "";
	}

	public String getResult() {
		return result;
	}

	// implementation of jsesh.MDCBuilder interface

	public void addCadratToTopItemList(TopItemListInterface l,
			CadratInterface e, int shading) {
		((TmpString) l).add("<topCadrat shading=" + shading + ">" + e
				+ "</topCadrat>");
	}

	public void addCartoucheToTopItemList(TopItemListInterface l,
			CartoucheInterface c) {
		((TmpString) l).add((TmpString) c);
	}

	public void addToggleToTopItemList(TopItemListInterface l,
			ToggleType toggleCode) {
		((TmpString) l).add("<toggle code=\"" + toggleCode + "\"/>");
	}

	public void addTextToTopItemList(TopItemListInterface l, char code,
			String text) {
		((TmpString) l)
				.add("<text script=\"" + code + "\">" + text + "</text>");
	}

	public void addTextSuperscriptToTopItemList(TopItemListInterface l,
			String text) {
		((TmpString) l).add("<text type=\"super\">" + text + "</text>");
	}

	public void addLineBreakToTopItemList(TopItemListInterface l, int skip) {
		((TmpString) l).add("<line");
		if (skip != 100) {
			((TmpString) l).add(" skip=\"" + skip + '"');
		}
		((TmpString) l).add(">");
	}

	/*
	 * Deprecated ? public void addToVerticalList(HBoxInterface l,
	 * InnerGroupInterface e) { ((TmpString)l).add((TmpString)e); }
	 */
	public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
		((TmpString) l).add("<h>" + h + "</h>\n");
	}

	public void addToHorizontalList(HBoxInterface h, InnerGroupInterface elt) {
		((TmpString) h).add((TmpString) elt);
	}

	public void addCadratToBasicItemList(BasicItemListInterface l,
			CadratInterface c, int shading) {
		((TmpString) l).add((TmpString) c);
	}

	public void addTextToBasicItemList(BasicItemListInterface l, char code,
			String text) {
		((TmpString) l)
				.add("<text script=\"" + code + "\">" + text + "</text>");
	}

	public void addToggleToBasicItemList(BasicItemListInterface l,
			ToggleType toggleCode) {
		((TmpString) l).add("<toggle code=\"" + toggleCode + "\"/>");
	}

	public void addModifierToModifierList(ModifierListInterface mods,
			String mod, Integer i) {
		((TmpString) mods).add(mod + "=\"" + i + "\"");
	}

	// returns the final result

	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
		return (MDCFileInterface) l;
	}

	/**
	 * 
	 * @return <description>
	 */
	public BasicItemListInterface buildBasicItemList() {
		return new TmpString();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @return <description>
	 */
	public CadratInterface buildCadrat(VBoxInterface param1) {
		return new TmpString("<Cadrat>" + param1 + "</Cadrat>" + "\n");
	}

	/**
	 * build a Cartouche.
	 * 
	 * @param type
	 *            an <code>int</code> code, which describes the kind of
	 *            Cartouche built. codes are 'c', 'h', 's', 'f' :
	 *            <ul>
	 *            <li> c : cartouche</li>
	 *            <li> s : serekh</li>
	 *            <li> h : hout-sign</li>
	 *            <li> f : castle</li>
	 *            </ul>
	 * @param leftPart
	 *            an <code>int</code> value which indicates how the first
	 *            extremity of the cartouche should be drawn.
	 *            <ul>
	 *            <li>0 : do not draw this extremity.</li>
	 *            </ul>
	 *            for cartouches and serekh 1 : begin 2 : end (2 being normally
	 *            the decorated part)
	 *            <p>
	 *            for Hout signs :
	 *            <ul>
	 *            <li> 1 : no square</li>
	 *            <li> 2 : square in the lower part.</li>
	 *            <li> 3 : square in the upper part.</li>
	 *            </ul>
	 * @param e
	 *            a <code>BasicItemListInterface</code> value, the cartouche's
	 *            contents
	 * @param rightPart
	 *            an <code>int</code> value, which indicates how the second
	 *            extremity of the cartouche should be drawn. Codes have the
	 *            same meaning as for leftPart.
	 * @return a <code>CartoucheInterface</code> value
	 */
	public CartoucheInterface buildCartouche(int type, int leftPart,
			BasicItemListInterface e, int rightPart) {
		return new TmpString("<Cartouche>" + e + "</Cartouche>" + "\n");
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param type
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 * @param param4
	 *            <description>
	 * @return <description>
	 */
	public HieroglyphInterface buildHieroglyph(boolean param1, int type,
			String param2, ModifierListInterface param3, int param4) {
		String attrs = "";
		if (param1)
			attrs = " isGrammar=\"true\"";
		if (param4 == 1)
			attrs += " isWordEnd=\"true\"";
		if (param3.toString().length() != 0)
			if (attrs.length() != 0)
				attrs += " " + param3;
			else
				attrs = param3.toString();
		return new TmpString("<Hieroglyph" + attrs + ">" + param2
				+ "</Hieroglyph>" + "\n");
	}

	/**
	 * 
	 * @return <description>
	 */
	public TopItemListInterface buildTopItemList() {
		return new TmpString();
	}

	/**
	 * 
	 * @return <description>
	 */
	public LigatureInterface buildLigature() {
		return new TmpString("");
	}

	public void completeLigature(LigatureInterface l) {
		((TmpString) l).set("<ligature>" + l.toString() + "</ligature>");
	}

	public void addToLigature(LigatureInterface lig, HieroglyphInterface h) {
		((TmpString) lig).add(h.toString());
	}

	/**
	 * 
	 * @return <description>
	 */
	public ModifierListInterface buildModifierList() {
		return new TmpString();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 * @return <description>
	 */
	public PhilologyInterface buildPhilology(int param1,
			BasicItemListInterface param2, int param3) {
		return new TmpString("<philology c=" + param1 + "c=" + param3 + ">"
				+ param2 + "</philology>");
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @return <description>
	 */
	public OverwriteInterface buildOverwrite(HieroglyphInterface param1,
			HieroglyphInterface param2) {
		return new TmpString("<over>" + param1 + param2 + "</over>");
	}

	/**
	 * 
	 * @return <description>
	 */
	public HBoxInterface buildHBox() {
		return new TmpString();
	}

	/**
	 * 
	 * @return <description>
	 */
	public VBoxInterface buildVBox() {
		return new TmpString();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @return <description>
	 */
	public SubCadratInterface buildSubCadrat(BasicItemListInterface param1) {
		return new TmpString("<subGroup>" + param1 + "</subGroup>");
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addHRuleToTopItemList(jsesh.mdc.interfaces.TopItemListInterface,
	 *      char, int, int)
	 */
	public void addHRuleToTopItemList(TopItemListInterface l, char lineType,
			int startPos, int endPos) {
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addPageBreakToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addPageBreakToTopItemList(TopItemListInterface l) {
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTabStopToTopItemList(jsesh.mdc.interfaces.TopItemListInterface,
	 *      int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#setHieroglyphPosition(jsesh.mdc.interfaces.HieroglyphInterface,
	 *      int, int, int)
	 */
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y,
			int scale) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToAbsoluteGroup(jsesh.mdc.interfaces.AbsoluteGroupInterface,
	 *      jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface RESULT,
			HieroglyphInterface e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addZoneStartToTopItemList(jsesh.mdc.interfaces.TopItemListInterface,
	 *      jsesh.mdc.interfaces.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1,
			ZoneStartInterface e2) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface,
	 *      java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface,
	 *      java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface,
	 *      java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.ZoneStartInterface,
	 *      jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.CadratInterface,
	 *      jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(CadratInterface result, OptionListInterface e1) {

	}

	public void addToHorizontalList(HBoxInterface h, HorizontalListElementInterface elt) {
		// TODO Auto-generated method stub
		
	}

	public ComplexLigatureInterface buildComplexLigature(InnerGroupInterface e1, HieroglyphInterface e2, InnerGroupInterface e3) {
		// TODO Auto-generated method stub
		return null;
	}

	

} // PrintMDCBuilder
