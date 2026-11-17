package jsesh.tmp.simpleDemo;

import java.util.LinkedList;

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
 * HieroglyphicTextBuilder builds a hieroglyphic text from an MDC
 * input.  This is a very basic simple example. The real builder will
 * be cleaner and more complex.
 *
 * Created: Fri Jun  7 16:26:11 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 */

public class HieroglyphicTextBuilder implements MDCBuilder
{

	private HieroglyphicText text;
	private LinkedList currentLine;

	// Utility classes to wrap the partially built result :

	private static class InternalLine implements BasicItemListInterface, CartoucheInterface
	{
		LinkedList rep;
		InternalLine()
		{
			rep= new LinkedList();
		}

		void add(InternalCadrat a)
		{
			rep.add(((InternalCadrat) a).rep);
		}
	}

	private static class InternalCadrat implements CadratInterface, VBoxInterface
	{
		LinkedList rep;
		InternalCadrat()
		{
			rep= new LinkedList();
		}

		void add(HBoxInterface b)
		{
			rep.add(((InternalHBox) b).rep);
		}
	}

	private static class InternalHBox implements HBoxInterface
	{
		LinkedList rep;
		InternalHBox()
		{
			rep= new LinkedList();
		}
		void add(InternalHiero h)
		{
			rep.add(h.rep);
		}
	}

	private static class InternalHiero
		implements HieroglyphInterface, LigatureInterface, OverwriteInterface, HorizontalListElementInterface
	{
		InternalHiero(String rep)
		{
			this.rep= rep;
		}
		String rep;
	}

	public HieroglyphicTextBuilder()
	{

	}

	public HieroglyphicText getText()
	{
		return text;
	}

	public void reset()
	{
		text= new HieroglyphicText();
		currentLine= new LinkedList();
	}

	public void addCadratToTopItemList(TopItemListInterface l, CadratInterface e, int shading)
	{
		if (e != null)
			currentLine.add(((InternalCadrat) e).rep);
	}

	public void addCartoucheToTopItemList(TopItemListInterface l, CartoucheInterface c)
	{
		if (c != null)
			currentLine.addAll(((InternalLine) c).rep);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToggleToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.constants.ToggleType)
	 */
	public void addToggleToTopItemList(TopItemListInterface l, ToggleType toggle)
	{
	}

	public void addTextToTopItemList(TopItemListInterface l, char code, String text)
	{
	}

	public void addTextSuperscriptToTopItemList(TopItemListInterface l, String text)
	{
	}

	public void addToHorizontalList(HBoxInterface l, HorizontalListElementInterface e)
	{
		if (e != null)
			 ((InternalHBox) l).rep.add(((InternalHiero) e).rep);
	}

	public void addToVerticalList(VBoxInterface l, HBoxInterface h)
	{
		if (l != null)
			 ((InternalCadrat) l).add(h);
	}

	public void addCadratToBasicItemList(BasicItemListInterface l, CadratInterface c, int shading)
	{
		((InternalLine) l).add((InternalCadrat) c);
	}

	public void addTextToBasicItemList(BasicItemListInterface l, char code, String text)
	{
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToggleToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface, jsesh.mdc.constants.ToggleType)
	 */
	public void addToggleToBasicItemList(BasicItemListInterface l, ToggleType toggleCode)
	{
	}

	public LigatureInterface buildLigature()
	{
		return new InternalHiero("");
	}

	public void addToLigature(LigatureInterface h, HieroglyphInterface elt)
	{
		((InternalHiero) h).rep= ((InternalHiero) elt).rep;
	}

	public void completeLigature(LigatureInterface h)
	{
	}

	public void addModifierToModifierList(ModifierListInterface mods, String name, Integer val)
	{
	}

	public BasicItemListInterface buildBasicItemList()
	{
		return new InternalLine();
	}

	public CadratInterface buildCadrat(VBoxInterface e)
	{
		return (InternalCadrat) e;
	}

	/**
	 * builds a Cartouche.
	 *
	 * @param type an <code>int</code> code, which describes the kind of Cartouche built.
	 *  codes are 'c', 'h', 's', 'f' :
	 * <ul>     
	 * 	<li> c : cartouche</li>
	 * 	<li> s : serekh</li>
	 * 	<li> h : hout-sign</li>
	 * 	<li> f : castle</li>
	 * </ul>
	 * @param leftPart an <code>int</code> value which
	 * indicates how the first extremity of the cartouche should be drawn.
	 * <ul>
	 * 	<li>0 : do not draw this extremity.</li>
	 * </ul>
	 * for cartouches and serekh 1 : begin 2 : end
	 *		(2 being normally the decorated part)
	 * <p> for Hout signs :
	 * <ul>
	 *		<li> 1 : no square</li>
	 *		<li> 2 : square in the lower part.</li>
	 *		<li> 3 : square in the upper part.</li>
	 * </ul>     
	 * @param e a <code>BasicItemListInterface</code> value, the cartouche's contents
	 * @param rightPart an <code>int</code> value, which indicates how
	 * the second extremity of the cartouche should be drawn. Codes have the same meaning as for leftPart.
	 * @return a <code>CartoucheInterface</code> value 
	 */
	public CartoucheInterface buildCartouche(
		int type,
		int leftPart,
		BasicItemListInterface e,
		int rightPart)
	{
		return (CartoucheInterface) e;
	}

	//  Build a sign in context,
	//  from a glyph and all corresponding comments
	public HieroglyphInterface buildHieroglyph(
		boolean isGrammar,
		int type,
		String code,
		ModifierListInterface m,
		int isEnd)
	{
		return new InternalHiero(code);
	}

	public TopItemListInterface buildTopItemList()
	{
		return null;
	}

	public LigatureInterface buildLigature(java.util.Vector v)
	{
		return null;
	}

	public MDCFileInterface buildMDCFileInterface(TopItemListInterface l)
	{
		// l is normally null
		text.add(currentLine);
		return null;
	}

	public ModifierListInterface buildModifierList()
	{
		return null;
	}

	public PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2)
	{
		return null;
	}

	public OverwriteInterface buildOverwrite(HieroglyphInterface e1, HieroglyphInterface e2)
	{
		return (OverwriteInterface) e1;
	}

	public HBoxInterface buildHBox()
	{
		return new InternalHBox();
	}

	public VBoxInterface buildVBox()
	{
		return new InternalCadrat();
	}

	public SubCadratInterface buildSubCadrat(BasicItemListInterface e)
	{
		return null;
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addHRuleToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, char, int, int)
	 */
	public void addHRuleToTopItemList(
		TopItemListInterface l,
		char lineType,
		int startPos,
		int endPos)
	{
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addLineBreakToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, int)
	 */
	public void addLineBreakToTopItemList(TopItemListInterface l, int skip)
	{
		text.add(currentLine);
		currentLine= new LinkedList();
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addPageBreakToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addPageBreakToTopItemList(TopItemListInterface l)
	{
		text.add(currentLine);
		currentLine= new LinkedList();
	}

	/**
	 * @see jsesh.mdc.interfaces.MDCBuilder#addTabStopToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, int)
	 */
	public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth)
	{
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToTopItemList(jsesh.mdc.interfaces.TopItemListInterface)
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l)
	{
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addStartHieroglyphicTextToBasicItemList(jsesh.mdc.interfaces.BasicItemListInterface)
	 */
	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l)
	{
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#setHieroglyphPosition(jsesh.mdc.interfaces.HieroglyphInterface, int, int, int)
	 */
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y, int scale) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildAbsoluteGroup()
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup() {
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addToAbsoluteGroup(jsesh.mdc.interfaces.AbsoluteGroupInterface, jsesh.mdc.interfaces.HieroglyphInterface)
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface RESULT, HieroglyphInterface e) {
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addZoneStartToTopItemList(jsesh.mdc.interfaces.TopItemListInterface, jsesh.mdc.interfaces.ZoneStartInterface)
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1, ZoneStartInterface e2) {
		
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String, int)
	 */
	public void addOption(OptionListInterface e1, String optName, int val) {
		
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName, String val) {
		
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#addOption(jsesh.mdc.interfaces.OptionListInterface, java.lang.String)
	 */
	public void addOption(OptionListInterface e1, String optName) {
		
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildOptionList()
	 */
	public OptionListInterface buildOptionList() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#buildZone()
	 */
	public ZoneStartInterface buildZone() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.ZoneStartInterface, jsesh.mdc.interfaces.OptionListInterface)
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
		
		
	}

    /* (non-Javadoc)
     * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.CadratInterface, jsesh.mdc.interfaces.OptionListInterface)
     */
    public void setOptionList(CadratInterface result, OptionListInterface e1) {
        // TODO Auto-generated method stub
        
    }

	public ComplexLigatureInterface buildComplexLigature(InnerGroupInterface e1, HieroglyphInterface e2, InnerGroupInterface e3) {
		return null;
	}
    


} // HieroglyphicTextBuilder
