package jsesh.mdc.lex;
import jsesh.mdc.*;
import jsesh.mdc.constants.*;
import java_cup.runtime.Symbol;

 
%%
%class MDCLexAux
%cup

%{ 
	// Changes made to accomodate a JLex version 1.2.6
	// We shall change to JFLex one day. However, it won't be possible
	// to distribute JFLex, as it's GPL and we want LGPL.

    /**
     * Are philological symbols to be treated as plain signs ?
     */
    boolean philologyAsSigns;
    
    // Here code local to the MDCLex class

    boolean expectSpace= false;
    
    // Used to see if '#' corresponds to shading or to overwrite.
    
    boolean justAfterSign= false;
    
    boolean ignoreStars= false;

    boolean debug;

    public void setDebug(boolean d)
    {
        debug= d;
    }

    public boolean getDebug()
    {
        return debug;
    }

    /**
	 * choose if phililogical parenthesis will be considered as simple signs.
     * @param p
     */
    
    public void setPhilologyAsSigns(boolean p)
    {
        philologyAsSigns= p;
    }

    public boolean getPhilologyAsSigns()
    {
        return philologyAsSigns;
    }


    /**
     * put in "space is meaningful" mode if spaces are meaningful after the current symbol.
	 * @param s
     */

    public void fixExpect(Symbol s) {
        expectSpace= false;
        justAfterSign= false;
        switch (s.sym) {
        case HIEROGLYPH:
        case MODIFIER:
        case DOUBLERIGHTCURLY:
            expectSpace= true;
        }
        switch (s.sym) {
        case HIEROGLYPH:
            justAfterSign= true;
        }
    }

    public void reset() {
    	ignoreStars= false;
        expectSpace= false;
        justAfterSign= false;
        yybegin(YYINITIAL);
    }

    /**
     * Handle philological parenthesis, choosing between simple signs and
     * complex contructs, depending on user's choice.
	 * @param type
	 * @param sub
	 * @return corresponding token Symbol.
     */
    private Symbol handlePhilology(int type, int sub)
    {
        if (getPhilologyAsSigns())
            {
                if (type ==  BEGINPHIL)
                    return buildMDCSign(sub*2, yytext());
                else
                    return buildMDCSign(sub*2+1, yytext());
            }
        else 
            return buildMDCSubType(type, sub);
    }

    private void printDebug(int code)
    {
        if (debug)
            System.err.println("token : " + code + " " + yytext());

    }
    
    private Symbol buildMDCSymbol(int type) {
        printDebug(type);
        return new Symbol(type);
    }

    private Symbol buildMDCSubType(int type, int subType) {
        printDebug(type);
        return new Symbol(type, new MDCSubType(subType));
    }
    
    private Symbol buildMDCIntValuedSymbol(int type, int value) {
        printDebug(type);
        return new Symbol(type, new Integer(value));
    }

    private Symbol buildMDCString(int type, String s) {
        printDebug(type);
        return new Symbol(type, s);
    }

    private Symbol buildMDCSign(int subtype, String s) {
        printDebug(subtype);
        return new Symbol(HIEROGLYPH, new MDCSign(subtype, s));
    }

    private Symbol buildMDCModifier(String s) {
        printDebug(MODIFIER);
        return new Symbol(MODIFIER, MDCModifier.buildMDCModifierFromString(s));
    }
    
    private Symbol buildMDCToggle(ToggleType v) {
	    printDebug(TOGGLE);
        return new Symbol(TOGGLE, v);
    }
    
    
    private Symbol buildMDCShading(String t) {
        printDebug(SHADING);
        return new Symbol(SHADING, new MDCShading(t));
    }

    private Symbol buildStartOldCartouche(char code, char part) {
        printDebug(BEGINOLDCARTOUCHE);
        return new Symbol(BEGINOLDCARTOUCHE, new MDCStartOldCartouche(code, part));
    }

    private Symbol buildBeginCartouche(char type, char part) {
        printDebug(BEGINCARTOUCHE);
        return new Symbol(BEGINCARTOUCHE, new MDCCartouche(type, part- '0'));
    }

    private Symbol buildEndCartouche(char type, char part) {
        printDebug(ENDCARTOUCHE);
        return new Symbol(ENDCARTOUCHE, new MDCCartouche(type, part - '0'));
    }

    private Symbol buildMDCAlphabetictext(char code, String txt) {
        printDebug(TEXT);
        return new Symbol(TEXT,new MDCAlphabeticText(code, txt)); 
    }

    private Symbol buildHRule(char type) {
        int startPos, endPos;
        printDebug(type);
        
        // We extract startPos and endPos
        String s= yytext();
        int commaIndex= s.indexOf(',');
        startPos= Integer.parseInt(s.substring(2, commaIndex));
        endPos= Integer.parseInt(s.substring(commaIndex + 1, s.indexOf('}')));
        return new Symbol(HRULE, new MDCHRule(type, startPos, endPos));
    }

    private int extractIntFromLineSkip() {
        int skip = 100;		// default value.
        String s= yytext();
        int pos= s.indexOf('=');
        if (pos != -1)
            {
                skip= Integer.parseInt(s.substring(pos+1, s.indexOf('%')));
            }
        return skip;
    }

   

    public MDCSyntaxError buildError(String msg, String token) {
            String res= msg + " line " + yyline + " char " + yychar + " at token '" + yytext()+ "'";
            return new MDCSyntaxError(res, yyline, yychar, token);
    }

%}

%eofval{
    printDebug(EOF);
    return buildMDCSymbol(EOF);
    // In state YYINITIAL, the system is waiting for hieroglyphs
	// state PROPERTIES is used when hieroglyphic codes are not expected (i.e. between {{ and }} ; between [] for the Revised Encoding ...
%eofval}

%state PROPERTIES

%line

%char

%implements MDCSymbols, SymbolCodes, java_cup.runtime.Scanner

%unicode

INTEGER=([0-9]+)
ESPSO=([ \t\n\015_-]*)
TRUESPACE=([ \t\n\015_])

%%
<YYINITIAL>"!!"{ESPSO}	{return buildMDCSymbol(PAGEEND);}

<YYINITIAL>"!"("="{INTEGER}"%")?{ESPSO}	{ 
    	  // The skip is expressed as percentage of normal skip.
    	  // So 100% <=> normal skip (default), 200% = skip one line, etc.
    	  return buildMDCIntValuedSymbol(LINEEND, extractIntFromLineSkip());    
	}

<YYINITIAL>"?"{INTEGER} 	{  // Positions are in "glyph units". the "n" sign is about 200 glyph units wide
    	   // More, positionning is absolute
	   return buildMDCIntValuedSymbol(TABSTOP, Integer.parseInt(yytext().substring(1)));
	}
	
<YYINITIAL>"%" 	{ return buildMDCSymbol(TABBING);}

<YYINITIAL>"%clear" 	{return buildMDCSymbol(TABBINGCLEAR);}

<YYINITIAL>"{l"{INTEGER},{INTEGER}"}"	{ // Simple horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('l');
	}
	
<YYINITIAL>"{L"{INTEGER},{INTEGER}"}"	{ // wide horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('L');
	}
<YYINITIAL>"+"[a-rt-z+](\\\+|[^+]|"+"[^a-z+])*	{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
	
<YYINITIAL>"zone" {
	
		return buildMDCSymbol(ZONE);
	}
<YYINITIAL>"quadrant" {
	
		return buildMDCSymbol(CADRAT);
	}
	
<YYINITIAL>"+s"	{
	  return buildMDCSymbol(STARTHIEROGLYPHS);
	}

<YYINITIAL>"|"([^-]|"\-"|'\\')*	{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}

<YYINITIAL>({TRUESPACE}|"_")	{
	justAfterSign= false;
	if (expectSpace)
		return buildMDCSymbol(WORDEND);
}

<YYINITIAL>({TRUESPACE}{TRUESPACE}|"__")+	{
		justAfterSign= false;
    	if (expectSpace)
	    	return buildMDCSymbol(SENTENCEEND);
}

<YYINITIAL>"-"	{return buildMDCSymbol(SEPARATOR);}
<YYINITIAL>"-"({TRUESPACE}|"_"|"-")* {// Non standard rule. Used to fix small errors.
	  return buildMDCSymbol(SEPARATOR);}

<YYINITIAL>"-#-"	{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
<YYINITIAL>"#-"|("#"\ +)	{
	// ?!??!!!!! winglyph has introduced way too many meanings for '#', and 
	// some of them are really not consistent.
	// As I want to keep a maximal compatibility, I will deal with them.
	// #- can be either SHADINGTOGGLE or full cadrat shading (normally #// or #1234).
		System.out.println("affiche "+ justAfterSign);
		if (justAfterSign)
			return buildMDCShading("#1234");
		else
			return buildMDCToggle(ToggleType.SHADINGTOGGLE);
	}
	
<YYINITIAL>"-#"	{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
 
<YYINITIAL>("#")	{
	if (justAfterSign)	// Potential problem here : A #x could really mean "A (end word) overwrite x" !!!
		return buildMDCSymbol(OVERWRITE);
	else
		return buildMDCToggle(ToggleType.SHADINGTOGGLE);
}

<YYINITIAL>-?"#b"	{return buildMDCToggle(ToggleType.SHADINGON); }
<YYINITIAL>-?"#e"	{return buildMDCToggle(ToggleType.SHADINGOFF); }
<YYINITIAL>"$r"	{return buildMDCToggle(ToggleType.RED); }
<YYINITIAL>"$b"	{return buildMDCToggle(ToggleType.BLACK); }
<YYINITIAL>"?"	{return buildMDCToggle(ToggleType.LACUNA);}
<YYINITIAL>"??"	{return buildMDCToggle(ToggleType.LINELACUNA); }
<YYINITIAL>"^"	{return buildMDCToggle(ToggleType.OMMIT);}
<YYINITIAL>"$"	{return buildMDCToggle(ToggleType.BLACKRED);}
<YYINITIAL>"#""1"?"2"?"3"?"4"?	{return buildMDCShading(yytext());}
<YYINITIAL>":"	{return buildMDCSymbol(COLON);}
<YYINITIAL>"*"	{return buildMDCSymbol(STAR);}
<YYINITIAL>"["	{yybegin(PROPERTIES); return buildMDCSymbol(OPENBRACE);}
<YYINITIAL>"<"	{return buildStartOldCartouche('c', 'a');}
<YYINITIAL>"<"[SFH]([bme])	{return buildStartOldCartouche(
	                                 yytext().charAt(1),
	                                 yytext().charAt(2));}
<YYINITIAL>"<"[SFH]	{return buildStartOldCartouche(yytext().charAt(1), 'a');}
<YYINITIAL>"<"[bme]	{return buildStartOldCartouche('c', yytext().charAt(1));}
<YYINITIAL>"<"[sfh][0123]	{return 
	     buildBeginCartouche(yytext().charAt(1),
	                    yytext().charAt(2));}
<YYINITIAL>"<"[sfh]	{return buildBeginCartouche(yytext().charAt(1), '1');}
<YYINITIAL>"<"[012]	{return buildBeginCartouche('c', yytext().charAt(1));}
<YYINITIAL>">"	{return buildEndCartouche('c', '2');}
<YYINITIAL>[sfh][0123]">"	{return buildEndCartouche(yytext().charAt(0), 
	                         yytext().charAt(1));}
<YYINITIAL>[sfh]">"	{return buildEndCartouche(yytext().charAt(0), 
	                         '2');}
<YYINITIAL>[012]">"	{return buildEndCartouche('c', 
	                         yytext().charAt(0));}

<YYINITIAL>"[["	{return handlePhilology(BEGINPHIL, ERASEDSIGNS);}
<YYINITIAL>"[{" {return handlePhilology(BEGINPHIL, EDITORSUPERFLUOUS);}

<YYINITIAL>"[\""	{return handlePhilology(BEGINPHIL, PREVIOUSLYREADABLE);}
<YYINITIAL>"['"	{return handlePhilology(BEGINPHIL, SCRIBEADDITION);}

<YYINITIAL>"[&"	{return handlePhilology(BEGINPHIL, EDITORADDITION);}
<YYINITIAL>"]]"	{return handlePhilology(ENDPHIL, ERASEDSIGNS);}

<YYINITIAL>"}]"	{return handlePhilology(ENDPHIL, EDITORSUPERFLUOUS);}
<YYINITIAL>"\"]"	{return handlePhilology(ENDPHIL, PREVIOUSLYREADABLE);}

<YYINITIAL>"']"	{return handlePhilology(ENDPHIL, SCRIBEADDITION);}
<YYINITIAL>"&]"	{return handlePhilology(ENDPHIL, EDITORADDITION);}
<YYINITIAL>")]"	{return handlePhilology(ENDPHIL, MINORADDITION);}
<YYINITIAL>"[("	{return handlePhilology(BEGINPHIL, MINORADDITION);}
<YYINITIAL>"?]"	{return handlePhilology(ENDPHIL, DUBIOUS);}
<YYINITIAL>"[?"	{return handlePhilology(BEGINPHIL, DUBIOUS);}


<YYINITIAL>"("	{return buildMDCSymbol(BPAR);}
<YYINITIAL>")"	{return buildMDCSymbol(EPAR);}
<YYINITIAL>("##")	{return buildMDCSymbol(OVERWRITE);}
<YYINITIAL>"&"	{return buildMDCSymbol(AMP);}
<YYINITIAL>"="	{return buildMDCSymbol(GRAMMAR);}
<YYINITIAL>\\"?"	{return buildMDCModifier(yytext());
	 // NOTE the three different forms for modifiers :
	 //  This one for the ? modifier
	 //  The next one, which is the general form
	 //  and a specific for for \R,
	 //  because of the possibility of having an negative
	 // value after it, 
	 //  which shouldn't be possible in the generic way.
	 //  a modifier list system would be a plus
	}
<YYINITIAL>\\R"-"?[0-9]*)	{return buildMDCModifier(yytext());}
<YYINITIAL>\\([a-zA-Z]*[0-9]*)      {return buildMDCModifier(yytext());}
<YYINITIAL>"`"	{
	 return buildMDCSign(MDCCODE,yytext());
	}
<YYINITIAL>"o"	{return buildMDCSign(REDPOINT, "o");}
<YYINITIAL>"O"	{return buildMDCSign(BLACKPOINT, "O");}
<YYINITIAL>\"([^\"\\\]]|\\.)*\"	{
	// Note about this construct: it comes from macscribe "..." construct
	// it's potentially ambiguous with '"]' (previously readable)
	// Hence : "]" (and for good measure and consistency, [) should be escaped
	// with "\" when they appear here.
		return buildMDCSign(SMALLTEXT, yytext());
}		
<YYINITIAL>(([A-Z]|"Aa")[0-9]+A*|[@a-zA-Z0-9]+|"@".)	{
                // MANUEL DE CODAGE CODE FOR A SIGN
    	            
	            return buildMDCSign(MDCCODE,yytext());
}
<YYINITIAL>"."	{return buildMDCSign(HALFSPACE, ".");}
<YYINITIAL>".."	{return buildMDCSign(FULLSPACE, "..");}
<YYINITIAL>"//"	{return buildMDCSign(FULLSHADE, "//");}
<YYINITIAL>"v/"	{return buildMDCSign(VERTICALSHADE, "v/");}
<YYINITIAL>"/"	{return buildMDCSign(QUATERSHADE, "/");}
<YYINITIAL>"h/"	{return buildMDCSign(HORIZONTALSHADE, "h/");}
<YYINITIAL>"&&"	{return buildMDCSymbol(DOUBLEAMP);}
<YYINITIAL>"**"	{return buildMDCSymbol(DOUBLEAMP);}
<YYINITIAL>"&&&"	{return buildMDCSymbol(LIGAFTER);}
<YYINITIAL>"^^^"	{return buildMDCSymbol(LIGBEFORE);}
<YYINITIAL>"^^"	{return buildMDCSymbol(LIGBEFORE);}

<YYINITIAL>"{{"	{yybegin(PROPERTIES); return buildMDCSymbol(DOUBLELEFTCURLY);}

<PROPERTIES>"}}" {yybegin(YYINITIAL); return buildMDCSymbol(DOUBLERIGHTCURLY);}
<PROPERTIES>"]" {yybegin(YYINITIAL); return buildMDCSymbol(CLOSEBRACE);}

<PROPERTIES>"," {return buildMDCSymbol(COMMA);}
<PROPERTIES>"=" {return buildMDCSymbol(EQUAL);}
<PROPERTIES>{INTEGER} {return buildMDCIntValuedSymbol(INTEGER,Integer.parseInt(yytext()));}
<PROPERTIES>[a-zA-Z_][a-zA-Z0-9_]* {return buildMDCString(IDENTIFIER, yytext());}
<PROPERTIES>[ \t\n\015] {}

.	{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}

