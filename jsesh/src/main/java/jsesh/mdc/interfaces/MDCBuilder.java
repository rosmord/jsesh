package jsesh.mdc.interfaces;

import jsesh.mdc.constants.ToggleType;

/**
 * An abstract builder used to build the components of a MDC Result.
 *
 * <p>To implement a MDCBuilder, you need a) to write all method and b) you obviously need 
 * to implement some of the interfaces. It doesn't mean that the final result of your work should
 * have a structure that reflect these interfaces : you can simply use them as wrappers, if you like so.
 * That's for example what we do in jsesh.mdc.implementation.PrintMDCBuilder, where the final result is a string.</p>
 *
 * <p> The clean OO way for parsing is to build an abstract parse
 * tree. However, it means that if you need another structure, you
 * build a memory representation for your data twice. There are cases
 * where you prefer a single pass compile. The problem is that then
 * you need to make some casts, which is both unclean and
 * cumbersome. If someone has a better solution, I'd be glad to hear from it.
 * </p>
 *
 * This builder interface is admitedly pretty complex.</p>
 *
 * <p> In a first version, only "build" methods where here, and the rest
 * (add, setLeft) were in the interfaces.  getting everything in the
 * MDCBuilder has an advantage : if you don't want to implement one of
 * the interface, you can simply return null. Now, if you are not
 * interested in modifiers, for instance, having an add method in
 * ModifierListInterface meant that you had to instanciate a ModifierListInterface class.</p> 
 * <p>
 * An important note on the parsing process : the parsing order is not always easy to guess.
 * It may sometimes be interesting to build temporary data. 
 * 
 * */

public interface MDCBuilder {
    
    /**
     * @param l an <code>BasicItemListInterface</code> value
     * @param c a <code>CadratInterface</code> value
     * @param shading an <code>int</code> value with the following meaning :
     * It's the sum of the following codes :
     * <ul>
     * <li>1 : means the top left part is shaded</li>
     * <li>2 : means the top right part is shaded</li>
     * <li>4 : means the bottom left part is shaded</li>
     * <li>8 : means the bottom right part is shaded</li>
     * </ul>
     * So a full shading is 15
     */
    public void addCadratToBasicItemList(BasicItemListInterface l, CadratInterface c, int shading); 


    /**
     * Describe <code>addCadratToTopItemListInterface</code> method here.
     *
     * @param l an <code>ItemListInterface</code> value
     * @param e a <code>CadratInterface</code> value
     * @param shading an <code>int</code> value with the following meaning :
     * It's the sum of the following codes :
     * <ul>
     * <li>1 : means the top left part is shaded</li>
     * <li>2 : means the top right part is shaded</li>
     * <li>4 : means the bottom left part is shaded</li>
     * <li>8 : means the bottom right part is shaded</li>
     * </ul>
     * So a full shading is 15
     */
    
    public void addCadratToTopItemList(TopItemListInterface l, CadratInterface e, int shading);
  
    public void addCartoucheToTopItemList(TopItemListInterface l, CartoucheInterface c);


    /**
     * add an horizontal rule to the text.
     *
     * The "default" scale was more or less 200 units for the width of a cadrat.
     * Currently, it is fixed in drawing specifications (and can be changed). 
     *
     * @param l a <code>TopItemListInterface</code> value
     * @param lineType type of line. 'l' for single, 'L' for double.
     * @param startPos start of the rule, as an absolute integer position from left edge of the page (?). 
     * 	(in "tab units")
     * @param endPos an absolute integer position from left edge of the page (?). 
     * 	(in "tab units")
     * @see DrawingSpecification#getTabUnitWidth()
     */

    public void addHRuleToTopItemList(TopItemListInterface l, char lineType, int startPos, int endPos);

    /**
     * add a line break or a page break to a TopItemList.
     *
     * @param l an <code>TopItemListInterface</code> value
     * @param skip : vertical skip, as percentage of line height. 100 is normal skip.
     *               less will result int line overlapping.
     */
    public void addLineBreakToTopItemList(TopItemListInterface l, int skip);
    
		
    /**
     * Adds a modifier to a list of modifier.
     * In the Manuel, modifiers are more or less of the form
     * "\XXX000"
     * where XXX is a string (usually reduced to only one letter)
     * and 000 is an optionnal Integer value.
     * 
     * XXX is made of letters or "?"
     * 000 is a positive integer.
     * both are optionnal.
     * if XXX is empty, it's the empty string.
     * if 000 is not here, value is null
     *
     * @param mods a <code>ModifierListInterface</code> value
     * @param name a <code>String</code> value. 
     * @param value an <code>Integer</code> value. null if the modifier has no integer part.
     */
    public void addModifierToModifierList(ModifierListInterface mods, String name, Integer value);

    public void addPageBreakToTopItemList(TopItemListInterface l);

	/**
	 * This method is called when the Manuel de codage construct "+s" is met.
	 * <P> +s is used to toggle between latin text and hieroglyphic text.
	 * In most cases, you won't need to do anything about it, 
	 * and most builders will ignore this construct (well, implement it as a
	 * no-operation method).
	 * <p> The method is provided simply in order to allow the programmer to 
	 * have access to a complete and faithfull representation of the original text. 
	 * @param l
	 */
	public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l);

	public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l);
	
    public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth);

    public void addTextSuperscriptToTopItemList(TopItemListInterface l, String text);

	/**
	 * Adds a text in latin characters to a basic item list.
	 * codes are 
	 * <ul>
	 * <li> 'l' : latin
	 * <li> 'b' : bold
	 * <li> 'i' : italic
	 * <li> 't' : transliteration
	 * <li> 'c' : coptic
	 * <li> 'g' : greek
	 * <li> 'h' : hebrew
	 * <li> 'r' : cyrillic
	 * </ul>
	 * @param l : the list to modify.
	 * @param scriptCode : the code for the script. 
	 * @param text : the text.
	 */
    public void addTextToBasicItemList(BasicItemListInterface l, char scriptCode, String text); 
    
    /**
     * Adds a text in latin characters to top item list.
     * see addTextToBasicItemList for details.
     * @param l
     * @param scriptCode
     * @param text
     * @see MDCBuilder#addTextToBasicItemList(BasicItemListInterface, char, String)
     */
    public void addTextToTopItemList(TopItemListInterface l, char scriptCode, String text);
    
    public void addToggleToBasicItemList(BasicItemListInterface l, ToggleType toggleCode); 
  
    public void addToggleToTopItemList(TopItemListInterface l, ToggleType toggle);
    
    public void addToHorizontalList(HBoxInterface h, HorizontalListElementInterface elt);
  
    /**
     * adds a hieroglyph to a ligature
     * @param i the ligature
     * @param h the hieroglyph to add.
     */
    
    public void addToLigature(LigatureInterface i, HieroglyphInterface h);

    public void addToVerticalList(VBoxInterface l, HBoxInterface h);
    
    public BasicItemListInterface buildBasicItemList();
    public CadratInterface buildCadrat(VBoxInterface e);
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
    public CartoucheInterface buildCartouche(int type, int leftPart, BasicItemListInterface e, int rightPart);

    public HBoxInterface buildHBox();
    
    /**
     * Build a sign in context from a glyph and all corresponding comments.
     * 
     *
     * @param isGrammar is true if the sign is part of a grammatical ending
     * @param type is a constant which states which kind of sign we have : a true hieroglyph (MDCCODE)
     *             or another symbol like a FULLSHADE or a REDPOINT. The values for these codes are
     * 			   in the interface jsesh.mdc.lex.SymbolCodes
     * @see jsesh.mdc.constants.SymbolCodes for values.
     * @param code the string with the manuel de codage code for this sign.

     * @param m a <code>ModifierListInterface</code> which corresponds to the list of modifiers for this sign.
     * @param isEnd 1 if the sign is a word's end, 2 if it's a sentence end, else 0.
     * @return a <code>HieroglyphInterface</code> value 
     */

    public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, 
					       ModifierListInterface m, int isEnd);

    /**
     * Starts Building a ligature element.
     * @return the ligatureInterface just built.
     *
     */
    public LigatureInterface buildLigature();

    // BUILDERS :

    public MDCFileInterface buildMDCFileInterface(TopItemListInterface l);

    public ModifierListInterface buildModifierList();
    
    public OverwriteInterface buildOverwrite(HieroglyphInterface e1, HieroglyphInterface e2);
    
    public PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2);

    public SubCadratInterface buildSubCadrat(BasicItemListInterface e);
    
    /**
     * build a list of Top items
     * @return the TopItemListInterface built.
     */

    public TopItemListInterface buildTopItemList();
    
    public VBoxInterface buildVBox();

    /**
     * called when a ligature is complete.
     * <p> It might be a good time, for instance, to look out in a ligature table.
     * @param i the complete ligature.
     */

    public void completeLigature(LigatureInterface i);

    /**
     * This method is called before each parse. It's the place for cleaning
     * the builder, building objects needed for parsing, etc.
     * often it will do nothing.
     */
    
    public void reset();


	/**
	 * Sets the hieroglyph position for absolute placement.
	 * <p> x,y is relative to the top left corner of the cadrat.
	 * the unit used for them is 1/1000 of the height of the A1 sign.
	 * <p> scale is given as a percentage of the natural size of the sign.
	 * <p> This is mostly meaningfull in "absolute groups", written with "&amp;&amp;".
	 * in other groups, it might be used for fine tuning of the sign's placement,
	 * but the semantics are not really clear.
	 * @param h
	 * @param x abscisse
	 * @param y ordinate
	 * @param scale scale in percentage
	 */
	
	public void setHieroglyphPosition(HieroglyphInterface h, int x, int y, int scale);


	/**
	 * build a group for absolute placement.
	 * @return the group.
	 */
	public AbsoluteGroupInterface buildAbsoluteGroup();


	/**
	 * Add a hieroglyph to an "absolute group."
	 * @param group
	 * @param e
	 */
	public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface group, HieroglyphInterface e);


	/**
	 * @param e1
	 * @param e2
	 */
	public void addZoneStartToTopItemList(TopItemListInterface e1, ZoneStartInterface e2);


	/**
	 * add an option with integer value to a list of options.
	 * @param e1
	 * @param optName
	 * @param val
	 */
	public void addOption(OptionListInterface e1, String optName, int val);


	/**
	 * add an option with string value to a list of options.
	 * @param e1
	 * @param optName
	 * @param val
	 */
	public void addOption(OptionListInterface e1, String optName, String val);


	/**
	 * Add a non valued (boolean) option to a list.
	 * @param e1
	 * @param optName
	 */
	public void addOption(OptionListInterface e1, String optName);


	/**
	 * @return an empty list of options.
	 */
	public OptionListInterface buildOptionList();


	/**
	 * @return a zone start.
	 * TODO : change completely the whole zone business.
	 */
	public ZoneStartInterface buildZone();


	/**
	 * @param result
	 * @param e1
	 */
	public void setOptionList(ZoneStartInterface result, OptionListInterface e1);


    /**
     * @param result
     * @param e1
     */
    public void setOptionList(CadratInterface result, OptionListInterface e1);


    /**
     * Build a complex ligature (inspired from macscribe). 
     * <p> These ligature combine a glyph with one or two "insert zones", and two possibly complex construct, like a subcadrat.
     * 
     * Complete example : t^^w&amp;&amp;t ; ns&&(mSa*Z3)
     * 
     * @param e1 the group which should go in the first zone (may be null)
     * @param e2 the main sign.
     * @param e3 the group which should go in the second zone (may be null)
     * @return
     */

	public ComplexLigatureInterface buildComplexLigature(InnerGroupInterface e1, HieroglyphInterface e2, InnerGroupInterface e3);


	public void addTabbingToTopItemList(TopItemListInterface e1,
			OptionListInterface e3);


	public void addTabbingClearToTopItemList(TopItemListInterface e1);

	
}


