// Cup specification for a manuel de codage parser.

/****************************************************************************
Entry system for JSesh according to the Manuel de Codage's specifications.
				(c) 1993-2005 Serge Rosmorduc

	 This code is free software under the LGPL (Lesser GNU Public Licence)
	 
****************************************************************************/
 
package jsesh.mdc.parser;

//import java_cup.runtime.*;
//import java.util.*;
import jsesh.mdc.lex.*;
import jsesh.mdc.constants.*;
import jsesh.mdc.*;
import jsesh.mdc.interfaces.*;

/**
 * Uses The Builder design pattern
 */

parser code {:
		// The object used to build the result.
		private MDCBuilder builder;
		

		/**
		 * Object used for error messages
		 */
		private ParserErrorManager errorManager;
		
		/**
		 * Get the value of builder (The object used to build the result).
		 * @return Value of builder.
		 */
		public MDCBuilder getBuilder() {return builder;}
		
		/**
		 * Set the value of builder (The object used to build the result).
		 * @param v  Value to assign to builder.
		 */
		public void setBuilder( MDCBuilder  v) {this.builder = v;}
		
		public void setErrorManager(ParserErrorManager m) {errorManager= m;}
		public ParserErrorManager getErrorManager() {return errorManager;}
		
    :};

init with {:
	getBuilder().reset();
	:}

action code {:
		final static int defaultPos[]= {0,0,100};
		public MDCBuilder getBuilder() {return parser.getBuilder();}
		private ParserErrorManager getErrorManager() {return parser.getErrorManager();}
		public void doError(String message) throws MDCSyntaxError {
				throw getErrorManager().buildError(message);
		}
		:};

/* Non terminals */

// An Item is any group in the system.

non terminal MDCFileInterface mdcfile;

non terminal TopItemListInterface topitems;

// CartoucheInterface EXTENDS ItemInterface
non terminal CartoucheInterface cartouche;

// CadratInterface EXTENDS BasicItemInterface
non terminal CadratInterface cadrat;

non terminal ZoneStartInterface zoneStart;
non terminal OptionListInterface optionList;
non terminal OptionListInterface options;
// non terminal OptionInterface option;


non terminal VBoxInterface verticalStack;

//// an horizontal list is made of simple groups.

non terminal HBoxInterface horizontalList;

non terminal HorizontalListElementInterface horizontalListElement;

//  InnerGroupInterface EXTENDS HorizontalListElementInterface
non terminal InnerGroupInterface innerGroup;

//  ComplexLigatureInterface EXTENDS HorizontalListElementInterface

non terminal ComplexLigatureInterface complexLigature;

non terminal InnerGroupInterface overwrite;

// PhilologyInterface EXTENDS InnerGroupInterface
non terminal PhilologyInterface philology;

// SubCadratInterface EXTENDS InnerGroupInterface
non terminal SubCadratInterface subgroup;

// HieroglyphInterface EXTENDS InnerGroupInterface

non terminal HieroglyphInterface hieroglyph;

// LigatureInterface EXTENDS InnerGroupInterface

non terminal LigatureInterface ligature;

non terminal LigatureInterface ligAux;

// BasicItemInterface EXTENDS ItemInterface

// non terminal BasicItemInterface basicitem;
non terminal BasicItemListInterface basicitems;

non terminal ModifierListInterface modifiers;

non terminal Boolean optGrammar;
non terminal optSeparator;
non terminal Integer optShading;
non terminal Integer optWordEnd;
non terminal int[] optPos;
non terminal AbsoluteGroupInterface absoluteGroup;
non terminal AbsoluteGroupInterface absoluteGroupAux;
// non terminal optStar;

/* Terminals (tokens returned by the scanner). */

terminal AMP;
terminal MDCCartouche BEGINCARTOUCHE;
terminal MDCStartOldCartouche BEGINOLDCARTOUCHE;
terminal MDCCartouche ENDCARTOUCHE;
terminal MDCSubType BEGINPHIL;
terminal MDCSubType ENDPHIL;

terminal BPAR;
terminal COLON;
terminal EPAR;
terminal GRAMMAR;
terminal MDCSign HIEROGLYPH;
terminal Integer LINEEND;
terminal MDCModifier MODIFIER;
terminal OVERWRITE;
terminal PAGEEND;
terminal STARTHIEROGLYPHS;
terminal Integer TABSTOP;
terminal TABBING;
terminal TABBINGCLEAR;
terminal MDCHRule HRULE;
terminal SEPARATOR;
terminal MDCShading SHADING;
terminal STAR;
terminal MDCAlphabeticText TEXT;
terminal String TEXTSUPER;
terminal ToggleType TOGGLE;
terminal WORDEND;
terminal SENTENCEEND;
terminal OPENBRACE;
terminal CLOSEBRACE;
terminal Integer INTEGER;
terminal COMMA;
terminal DOUBLEAMP;
terminal LIGBEFORE;
terminal LIGAFTER;
terminal DOUBLELEFTCURLY;
terminal DOUBLERIGHTCURLY;
terminal UNKNOWN;

terminal String IDENTIFIER;
terminal EQUAL;
terminal ZONE;
terminal CADRAT;

// non terminal            expr_list, expr_part;

/* Precedences */

//precedence left LIGBEFORE;
//precedence right LIGAFTER;
//precedence nonassoc LIGBEFORE;
//precedence nonassoc LIGAFTER;

/* The grammar */


// mdcfile ::= [WORDEND] ([SEPARATOR] topitem)*

mdcfile ::= topitems:l optSeparator
{:
  // BUILDER MDCFileInterface buildMDCFileInterface(TopItemListInterface l)
 RESULT= getBuilder().buildMDCFileInterface(l); :}
;

topitems ::=
{: RESULT= getBuilder().buildTopItemList(); :}
;

topitems ::= topitems:e1 optSeparator cadrat:e2 optShading:s
{: 
 RESULT= e1; 
 getBuilder().addCadratToTopItemList(e1,e2,s.intValue());
 :};

// The optionnal shading after Cartouches is incorrect, but we parse it.
// Cartouches are now "innerItems". They can appear anywhere.
//topitems ::= topitems:e1 optSeparator cartouche:e2 optShading
//{: 
// RESULT= e1; 
// getBuilder().addCartoucheToTopItemList(e1,e2);
// :};

topitems ::= topitems:e1 optSeparator TOGGLE:e2
{: 
 RESULT= e1; 
 getBuilder().addToggleToTopItemList(e1,e2);
 :};

topitems ::= topitems:e1 optSeparator STARTHIEROGLYPHS
{: 
 RESULT= e1; 
 getBuilder().addStartHieroglyphicTextToTopItemList(e1);
 :};

topitems ::= topitems:e1 optSeparator TEXT:e2
{: 
 RESULT= e1; 
 getBuilder().addTextToTopItemList(e1,e2.getScriptCode(), e2.getText());
:};

topitems ::= topitems:e1 optSeparator TEXTSUPER:e2
{: 
 RESULT= e1; 
 getBuilder().addTextSuperscriptToTopItemList(e1, e2);
 :};

topitems ::= topitems:e1 optSeparator LINEEND:e2
{: 
 RESULT= e1; 
 getBuilder().addLineBreakToTopItemList(e1,e2.intValue());
 :};

topitems ::= topitems:e1 optSeparator PAGEEND:e2
{: 
 RESULT= e1; 
 getBuilder().addPageBreakToTopItemList(e1);
 :};

topitems ::= topitems:e1 optSeparator TABSTOP:e2
{: 
 RESULT= e1;
 getBuilder().addTabStopToTopItemList(e1,e2.intValue());
 :};

topitems ::= topitems:e1 optSeparator TABBING optionList:e3
{: 
 RESULT= e1;
 getBuilder().addTabbingToTopItemList(e1,e3);
 :};

topitems ::= topitems:e1 optSeparator TABBINGCLEAR
{: 
 RESULT= e1;
 getBuilder().addTabbingClearToTopItemList(e1);
 :};


topitems ::= topitems:e1 optSeparator HRULE:e2
{: 
 RESULT= e1;
 getBuilder().addHRuleToTopItemList(e1,e2.getLineType(), e2.getStartPos(), e2.getEndPos());
 :};

topitems ::= topitems:e1 optSeparator zoneStart:e2
{: 
 RESULT= e1;
 getBuilder().addZoneStartToTopItemList(e1,e2);
 :};


//topitems ::= topitems:e1 error SEPARATOR
//{:
// doError("unexpected or unknown item.");
// RESULT= e1;
// :};

optShading ::=
{:
 RESULT= new Integer(0);
:};

optShading ::= SHADING:e
{:
 RESULT= new Integer(e.getShading());
:};


cadrat ::= error
{:
 doError("unexpected or unknown item.");
 RESULT= null;
:};
 
cadrat ::= verticalStack:e
{:
 // BUILDER CadratInterface buildCadrat(VBoxInterface e)
 RESULT= getBuilder().buildCadrat(e);
:}
;

cadrat ::= CADRAT BPAR verticalStack:e EPAR 
{:
 // BUILDER CadratInterface buildCadrat(VBoxInterface e)
 RESULT= getBuilder().buildCadrat(e);
:}
;

cadrat ::= CADRAT BPAR verticalStack:e EPAR optionList:e1
{:
 // BUILDER CadratInterface buildCadrat(VBoxInterface e)
 RESULT= getBuilder().buildCadrat(e);
 getBuilder().setOptionList(RESULT, e1);
:}
;


verticalStack ::= horizontalList:e
{:
 RESULT= getBuilder().buildVBox();
 getBuilder().addToVerticalList(RESULT,e);
 // RESULT.add(e);
:};

verticalStack ::= verticalStack:e1 COLON horizontalList:e2
{:
 RESULT= e1;
 // BUILDER void addToVerticalList(VBoxInterface l, HBoxInterface h)
 getBuilder().addToVerticalList(e1,e2); 
 // e1.add(e2);
:};


horizontalList ::= horizontalListElement:e
{:
 RESULT= getBuilder().buildHBox();
 // RESULT.add(e);
 // BUILDER void addToHorizontalList(HBoxInterface h, InnerGroupInterface elt)
 getBuilder().addToHorizontalList(RESULT,e); 
 :};

horizontalList ::= horizontalList:e1 STAR horizontalListElement:e2
{:
 RESULT= e1;
 // RESULT.add(e2);
 getBuilder().addToHorizontalList(e1,e2); 

:};

horizontalListElement ::= complexLigature:e
{:
  RESULT= e;
:};

horizontalListElement ::= innerGroup:e
{:
  RESULT= e;
:};

// Cartouches are now "innerItems". They can appear anywhere.

horizontalListElement ::= cartouche:e  
{:
  RESULT= e;
:};

// Ligatures of the form t^^w&&t
complexLigature ::= innerGroup:e1 LIGBEFORE hieroglyph:e2 LIGAFTER innerGroup:e3
{:
	RESULT= getBuilder().buildComplexLigature(e1,e2,e3);
:};

complexLigature ::= innerGroup:e1 LIGBEFORE hieroglyph:e2
{:
	RESULT= getBuilder().buildComplexLigature(e1,e2,null);
:};

complexLigature ::=  hieroglyph:e1 LIGAFTER innerGroup:e2
{:
	RESULT= getBuilder().buildComplexLigature(null,e1,e2);
:};


innerGroup ::= ligature:e
{:
  RESULT= e;
:};

innerGroup ::= hieroglyph:e
{:
 RESULT= e;
:};

innerGroup ::= overwrite:e
{:
 RESULT= e;
:};

innerGroup ::= philology:e
{:
  RESULT= e;
:};

innerGroup ::= subgroup:e
{:
  RESULT= e;
:};

innerGroup ::= absoluteGroup:e
{:
  RESULT= e;
:};

philology ::= BEGINPHIL:p1  basicitems:e  optSeparator ENDPHIL:p2
{:
 // BUILDER PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2)
 RESULT= getBuilder().buildPhilology(p1.getSubType(), e, p2.getSubType());
:};

subgroup ::= BPAR basicitems:e optSeparator EPAR
{:
 // BUILDER SubCadratInterface buildSubCadrat(BasicItemListInterface e)
 RESULT= getBuilder().buildSubCadrat(e);
 :};

basicitems ::= 
{:
 RESULT= getBuilder().buildBasicItemList();
 :};

basicitems ::= basicitems:e1 optSeparator cadrat:e2 optShading:s
{:
 RESULT= e1;
 // e1.add(e2);
 getBuilder().addCadratToBasicItemList(e1,e2,s.intValue()); 
 :};

basicitems ::= basicitems:e1 optSeparator TEXT:e2
{:
 RESULT= e1;
 // e1.add(e2);
 getBuilder().addTextToBasicItemList(e1,e2.getScriptCode(), e2.getText()); 
 :};

basicitems ::= basicitems:e1 optSeparator STARTHIEROGLYPHS
{:
 RESULT= e1;
 // e1.add(e2);
 getBuilder().addStartHieroglyphicTextToBasicItemList(e1);
 :};

basicitems ::= basicitems:e1 optSeparator TOGGLE:e2
{:
 RESULT= e1;
 // e1.add(e2);
 getBuilder().addToggleToBasicItemList(e1,e2); 
 :};


optSeparator ::=
{:
:};

optSeparator ::= SEPARATOR
{:
:};

overwrite ::= hieroglyph:e1 OVERWRITE hieroglyph:e2
{:
 // BUILDER InnerGroupInterface buildOverwrite(InnerGroupInterface e1, InnerGroupInterface e2)
 RESULT= getBuilder().buildOverwrite(e1,e2);
:};

ligature ::= ligAux:e1 AMP hieroglyph:e2
{:
 RESULT= e1;
 // BUILDER void addToLigature(LigatureInterface h, HieroglyphInterface elt)
 getBuilder().addToLigature(e1, e2);
 getBuilder().completeLigature(e1);
:};

ligAux ::= ligAux:e1 AMP hieroglyph:e2
{:
 RESULT= e1;
 getBuilder().addToLigature(e1, e2);
 :};

ligAux ::= hieroglyph:e
{:
 RESULT= getBuilder().buildLigature();
 getBuilder().addToLigature(RESULT, e);
 :};

absoluteGroup ::= absoluteGroupAux:e1 DOUBLEAMP hieroglyph:e2
{:
	RESULT= e1;
 	getBuilder().addHieroglyphToAbsoluteGroup(e1, e2);
:};

absoluteGroupAux ::= absoluteGroupAux:e1 DOUBLEAMP hieroglyph:e2
{:
	RESULT= e1;
 	getBuilder().addHieroglyphToAbsoluteGroup(e1, e2);
:};

absoluteGroupAux ::= hieroglyph:e
{:
	RESULT= getBuilder().buildAbsoluteGroup();
 	getBuilder().addHieroglyphToAbsoluteGroup(RESULT, e);
:};

hieroglyph ::= optGrammar:g HIEROGLYPH:e modifiers:m optPos:pos optWordEnd:f
{:
 //// Build a sign in context,
 //// from a glyph and all corresponding comments
 // BUILDER HieroglyphInterface buildHieroglyph(Boolean isGrammar, String code, ModifierListInterface m, Boolean isEnd)
 HieroglyphInterface h= getBuilder().buildHieroglyph(g.booleanValue(),e.getType(), e.getString(),m,f.intValue());
 if (h != null && pos != defaultPos) {
 	getBuilder().setHieroglyphPosition(h, pos[0], pos[1], pos[2]);
 }
 RESULT= h;
:};

optWordEnd ::=
{:
 RESULT= new Integer(0);
 :};

optWordEnd ::= WORDEND
{:
 RESULT= new Integer(1);
 :};


optWordEnd ::= SENTENCEEND
{:
 RESULT= new Integer(2);
 :};

optPos ::= 
{:
	RESULT= defaultPos; 
:};

optPos ::= DOUBLELEFTCURLY INTEGER:i1 COMMA INTEGER:i2 COMMA INTEGER:i3 DOUBLERIGHTCURLY
{:
	// absolute positionning of a sign, similar to Macscribe. 
	// three values are passed. They are x,y,scale. scale is a percentage ; 
	// x,y are given in a unit that is 1/1000 of the height of a A1 sign.
	int t[]= new int[3];
	t[0]= i1.intValue();
	t[1]= i2.intValue();
	t[2]= i3.intValue();
	RESULT= t;
:};

optGrammar ::=
{:
 RESULT= new Boolean(false);
 :};

optGrammar ::= GRAMMAR
{:
 RESULT= new Boolean(true);
 :};

modifiers ::=
{:
 RESULT= getBuilder().buildModifierList();
 :};

modifiers ::= modifiers:e1 MODIFIER:e2
{:
 RESULT= e1;
 // e1.add(e2);
 // Cut the modifier 
 // BUILDER void addToModifierList(ModifierListInterface mods, )
 getBuilder().addModifierToModifierList(e1,e2.getName(),e2.getIntValue());

 :};

zoneStart ::= ZONE 
{:
	 RESULT = getBuilder().buildZone();
:}
| ZONE optionList:e1
{:
  RESULT = getBuilder().buildZone();
	getBuilder().setOptionList(RESULT, e1);
:};

optionList ::= OPENBRACE options:e1 CLOSEBRACE
{:
	 RESULT = e1;
:}
;

options ::= IDENTIFIER:e1
{:
	RESULT= getBuilder().buildOptionList();
	getBuilder().addOption(RESULT, e1);
:};

options ::= IDENTIFIER:e1 EQUAL IDENTIFIER:e2 
	{:
  RESULT= getBuilder().buildOptionList();
	getBuilder().addOption(RESULT, e1,e2);
	 :};


options ::= IDENTIFIER:e1 EQUAL INTEGER:e2	{:
	RESULT= getBuilder().buildOptionList();
	getBuilder().addOption(RESULT, e1,e2.intValue());
	 :};

options ::= options:e1 COMMA IDENTIFIER:e2 	
	{:
	 RESULT= e1;
	 getBuilder().addOption(e1,e2);
	 :};

options ::= options:e1 COMMA IDENTIFIER:e2 EQUAL IDENTIFIER:e3	
	{:
	 RESULT= e1;
	 getBuilder().addOption(e1, e2, e3);
	 :};

options ::= options:e1 COMMA IDENTIFIER:e2 EQUAL INTEGER:e3 	{:
	 RESULT= e1;
	 getBuilder().addOption(e1, e2, e3.intValue());																															 
	 :};

cartouche ::= BEGINCARTOUCHE:c1 basicitems:e optSeparator ENDCARTOUCHE:c2 modifiers:m
{:
 // BUILDER CartoucheInterface buildCartouche(BasicItemListInterface e)
 //// Indicates how the first extremity of the cartouche should be drawn.
 //// <ul>
 //// 	<li>0 : do not draw this extremity.</li>
 //// </ul>
 //// for cartouches and serekh 1 : begin 2 : end
 ////		(2 being normally the decorated part)
 //// <p> for Hout signs :
 //// <ul>
 ////		<li> 1 : no square</li>
 ////		<li> 2 : square in the lower part.</li>
 ////		<li> 3 : square in the upper part.</li>
 //// </ul>
 // BUILDER void setLeft(CartoucheInterface c,int part)
 // BUILDER void void setRight(CartoucheInterface e, int part)
 //// Sets the type of a cartouche. Codes are 'c', 'h', 's', 'f'.
 //// 	c : cartouche
 //// 	s : serekh
 //// 	h : hout-sign
 //// 	f : castle
 // BUILDER void setType(CartoucheInterface e, int type)

 RESULT= getBuilder().buildCartouche(c1.getCartoucheType(), c1.getPart(), e, c2.getPart());
 :};

cartouche ::= BEGINOLDCARTOUCHE:c1 basicitems:e optSeparator ENDCARTOUCHE:c2
{:
 int leftPart;
 int rightPart;
 int type;
 type= c1.getCartoucheType();
 // Now, the parts for Left and Right depend on the value associated with
 // c1
 switch (c1.getPart()) {
 case 'b':
		 leftPart= 1;
		 rightPart= 0;
		 break;
 case 'm':
		 leftPart= 0;
		 rightPart= 0;
		 break;
 case 'e':
		 leftPart= 0;
		 rightPart= 2;
		 break;
 case 'a':
 default:
		 leftPart= 1;
		 rightPart= 2;
 }
 RESULT= getBuilder().buildCartouche(type, leftPart, e, rightPart);
 :};


/*
 * Local Variables:
 * tab-width: 2
 * End:
 */
