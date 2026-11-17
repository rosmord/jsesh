package jsesh.mdc.lex;
import jsesh.mdc.utils.*;
import jsesh.mdc.constants.*;
import java_cup.runtime.Symbol;


class MDCLexAux implements MDCSymbols, SymbolCodes, java_cup.runtime.Scanner, ParserErrorManager {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 65536;
	private final int YY_EOF = 65537;
 
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
    public MDCSyntaxError buildError() {
        return buildError("error");
    }
    public MDCSyntaxError buildError(String msg) {
            String res= msg + " line " + yyline + " char " + yychar + " at token '" + yytext()+ "'";
            return new MDCSyntaxError(res, yyline, yychar, yytext());
    }
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	MDCLexAux (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	MDCLexAux (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private MDCLexAux () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int YYINITIAL = 0;
	private final int PROPERTIES = 1;
	private final int yy_state_dtrans[] = {
		0,
		118
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NO_ANCHOR,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NO_ANCHOR,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NO_ANCHOR,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NO_ANCHOR,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NO_ANCHOR,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NO_ANCHOR,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NO_ANCHOR,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NO_ANCHOR,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NOT_ACCEPT,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NOT_ACCEPT,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NOT_ACCEPT,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NOT_ACCEPT,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NOT_ACCEPT,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NO_ANCHOR,
		/* 110 */ YY_NOT_ACCEPT,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NOT_ACCEPT,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NOT_ACCEPT,
		/* 117 */ YY_NO_ANCHOR,
		/* 118 */ YY_NOT_ACCEPT,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NOT_ACCEPT,
		/* 122 */ YY_NOT_ACCEPT,
		/* 123 */ YY_NO_ANCHOR,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NOT_ACCEPT,
		/* 126 */ YY_NOT_ACCEPT,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,65538,
"16:9,58,27,16:2,27,16:18,30,1,47,29,32,5,49,48,51,52,39,12,9,2,59,60,45,34," +
"35,36,37,4:5,38,16,41,3,46,6,57,54:5,42,54,42,54:3,11,54:2,56,54:2,53,42,54" +
":7,40,14,50,33,28,55,22,31,21,23,20,44,13,62,13:3,8,43,19,18,13:2,24,15,25," +
"13,61,13:3,17,7,26,10,16:65410,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,130,
"0,1,2,3,1,4,1,5,6,4,7,8,9,10,11,1,12,13,14,1,15,1:3,4,16,17,18,19,20,21,1:3" +
",22,1:2,23,1,24,1:4,25,1:2,26,1:7,27,1:2,28,1:2,29,1:12,4,1:2,4,1,30,31,1:4" +
",32,33,34,35,36,37,38,39,40,41,42,43,44,22,45,46,47,48,1,49,50,51,8,52,53,5" +
"4,55,56,57,1,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72")[0];

	private int yy_nxt[][] = unpackFromString(73,63,
"1,2,3,4,5,6,7,88,5,6,96,5,102,5,8,87,6,124,9,5:2,129,5:4,10,11:2,12,11,5,13" +
",14,95:2,5:2,15,16,17,18,5:2,87,95,19,106,109,20,112,21,22,5:2,23,24,101,11" +
",25,26,105,108,-1:64,27,86,85,-1:23,86:2,-1,86,-1:27,86,-1:6,28,-1:24,28:2," +
"29,28,-1:27,28,-1:8,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-1:5,5,-1:2,5:4,-1:4,5" +
":4,-1:7,5:2,-1,5:2,-1:3,5:2,-1:4,30,-1,31,-1:27,30:4,-1:7,30,-1:21,89,-1,36" +
",-1,97,-1:2,97,-1,97,-1,97,-1,97:9,-1:5,97,-1:2,89:4,-1:4,97:3,89,-1:7,37,9" +
"7,-1,97,-1:4,97:2,-1,10,-1,10:60,-1:27,39:2,-1,39,-1:27,39,-1:6,40,-1:17,41" +
",-1:8,42,92,43,-1:2,44,93,99,103,-1:49,45,-1:6,46,-1:64,47,-1:68,49,-1:30,5" +
"0,-1:32,51,-1:6,52,53,54,-1:28,55,-1:4,56,-1:10,56,-1:2,57:2,-1:6,58,56,55," +
"57,-1:16,55,-1:49,61,62,-1:71,64,-1:63,65,-1:4,27,-1:24,27:2,-1,27,-1:27,27" +
",-1:6,28,-1:24,28:2,-1,28,-1:27,28,-1:6,68,-1:17,41,-1:10,43,-1:35,30,-1:29" +
",30:4,-1:7,30,-1:18,34:11,104,34,90,34:48,-1:2,91,-1,91,-1:3,97,-1:2,97,-1," +
"97,-1,97,-1,97:9,-1:5,97,-1:2,91:4,-1:4,97:3,91,-1:7,97:2,-1,97,-1:4,97:2,-" +
"1:27,107:2,-1,107,-1:27,107,-1:39,93,99,103,-1:58,70,-1:63,71:3,-1:8,71,-1:" +
"37,72,-1:10,72,-1:11,72,-1:68,73,-1:17,79,-1:29,79:4,-1:7,79,-1:21,80,-1:3," +
"80,-1:2,80,-1,80,-1,80,-1,80:9,-1:2,80,-1:2,80,-1:2,80:4,-1:4,80:4,-1:7,80:" +
"2,-1,80,-1:4,80:2,-1:4,94,-1:29,94:4,-1:7,94,-1:19,86,-1:24,86:2,-1,86,-1:2" +
"7,86,-1:8,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-1:5,5,-1:2,111:3,5,-1:4,5:3,111" +
",38,-1:6,5:2,-1,5:2,-1:3,5:2,-1:7,32,121,-1:2,126,-1:55,89,-1:29,89:4,-1:7," +
"89,-1:18,34:11,98,34,90,34:48,-1:4,91,-1:29,91:4,-1:7,91,-1:47,92,-1:68,99," +
"103,-1:29,94,86,-1:28,94:4,-1:7,94,-1:21,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-" +
"1:5,5,-1:2,5:4,-1:4,5:4,48,-1:6,5:2,-1,5:2,-1:3,5:2,-1:50,33,-1:16,89,-1:3," +
"97,-1:2,97,-1,97,-1,97,-1,97:9,-1:5,97,-1:2,89:4,-1:4,97:3,89,-1:7,97:2,-1," +
"97,-1:4,97:2,-1:37,103,-1:29,100,-1:4,110,-1:24,100:4,-1:7,100,-1:18,114:3," +
"5,114:3,5,114:2,5,114,5,114,5,114,5:9,114,-1,114:3,5,114:2,5:4,114:4,5:4,11" +
"4:7,5:2,114,5:2,114:3,5:2,-1:8,34,-1:3,34:2,-1,35,-1,34:9,-1:5,34,-1:11,34:" +
"2,-1:16,34:2,-1,34:7,-1,34:3,-1:2,34,-1,34,-1:9,34:5,-1,34:11,-1:2,34:16,-1" +
":6,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:" +
"2,-1:2,66,5:2,-1:50,59,-1:16,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-1:5,5,-1:2,1" +
"11:3,5,-1:4,5:3,111,38,-1:6,5:2,-1,5:2,-1:2,67,5:2,-1:50,60,-1:16,113,-1:29" +
",113:4,-1:7,113,-1:21,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:9,-1:5,5,-1:2,5:4,-1:4" +
",5:4,69,-1:6,5:2,-1,5:2,-1:3,5:2,-1:50,63,-1:16,113,-1:5,75,-1:23,113:4,-1:" +
"7,113,-1:27,84,-1:56,116,-1:5,76,-1:23,116:4,-1:7,116,-1:21,5,-1:3,5,-1:2,5" +
",-1,5,-1,5,-1,5:3,74,5:5,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:2,-1:3,5:2," +
"1,6:2,78,79,6:3,80,81,115,80,6,80,6,80,6,80:9,6,82,80,6,82,80,6:2,79:4,6:4," +
"80:3,79,6:4,83,6:2,80:2,6,80,6,82,6:2,80:2,-1:4,5,-1:3,5,-1:2,5,-1,5,-1,5,-" +
"1,5:8,77,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:2,-1:3,5:2,-1:4,5,-1:3,5,-1" +
":2,5,-1,5,-1,5,-1,5:2,117,5:6,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:2,-1:3" +
",5:2,-1:4,100,-1:29,100:4,-1:7,100,-1:21,116,-1:29,116:4,-1:7,116,-1:21,5,-" +
"1:3,5,-1:2,5,-1,5,-1,5,-1,5:5,119,5:3,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1," +
"5:2,-1:3,5:2,-1:4,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5,120,5:7,-1:5,5,-1:2,5:4,-1" +
":4,5:4,-1:7,5:2,-1,5:2,-1:3,5:2,-1:4,125,-1:4,122,-1:24,125:4,-1:7,125,-1:2" +
"1,125,-1:29,125:4,-1:7,125,-1:21,5,-1:3,5,-1:2,5,-1,5,-1,5,-1,5:7,123,5,-1:" +
"5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:2,-1:3,5:2,-1:4,5,-1:3,5,-1:2,5,-1,5,-1" +
",5,-1,5:6,127,5:2,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1,5:2,-1:3,5:2,-1:4,5," +
"-1:3,5,-1:2,5,-1,5,-1,5,-1,5:5,128,5:3,-1:5,5,-1:2,5:4,-1:4,5:4,-1:7,5:2,-1" +
",5:2,-1:3,5:2");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

    printDebug(EOF);
    return buildMDCSymbol(EOF);
    // In state YYINITIAL, the system is waiting for hieroglyphs
	// state PROPERTIES is used when hieroglyphic codes are not expected (i.e. between {{ and }} ; between [] for the Revised Encoding ...
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{ 
    	  // The skip is expressed as percentage of normal skip.
    	  // So 100% <=> normal skip (default), 200% = skip one line, etc.
    	  return buildMDCIntValuedSymbol(LINEEND, extractIntFromLineSkip());    
	}
					case -3:
						break;
					case 3:
						{return buildMDCSymbol(SEPARATOR);}
					case -4:
						break;
					case 4:
						{return buildMDCSymbol(GRAMMAR);}
					case -5:
						break;
					case 5:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -6:
						break;
					case 6:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -7:
						break;
					case 7:
						{return buildMDCToggle(ToggleType.LACUNA);}
					case -8:
						break;
					case 8:
						{return buildMDCModifier(yytext());}
					case -9:
						break;
					case 9:
						{return buildMDCSign(REDPOINT, "o");}
					case -10:
						break;
					case 10:
						{
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -11:
						break;
					case 11:
						{
	justAfterSign= false;
	if (expectSpace)
		return buildMDCSymbol(WORDEND);
}
					case -12:
						break;
					case 12:
						{
	if (justAfterSign)	// Potential problem here : A #x could really mean "A (end word) overwrite x" !!!
		return buildMDCSymbol(OVERWRITE);
	else
		return buildMDCToggle(ToggleType.SHADINGTOGGLE);
}
					case -13:
						break;
					case 13:
						{return buildMDCToggle(ToggleType.BLACKRED);}
					case -14:
						break;
					case 14:
						{return buildMDCToggle(ToggleType.OMMIT);}
					case -15:
						break;
					case 15:
						{return buildMDCSymbol(COLON);}
					case -16:
						break;
					case 16:
						{return buildMDCSymbol(STAR);}
					case -17:
						break;
					case 17:
						{yybegin(PROPERTIES); return buildMDCSymbol(OPENBRACE);}
					case -18:
						break;
					case 18:
						{return buildStartOldCartouche('c', 'a');}
					case -19:
						break;
					case 19:
						{return buildEndCartouche('c', '2');}
					case -20:
						break;
					case 20:
						{return buildMDCSymbol(AMP);}
					case -21:
						break;
					case 21:
						{return buildMDCSymbol(BPAR);}
					case -22:
						break;
					case 22:
						{return buildMDCSymbol(EPAR);}
					case -23:
						break;
					case 23:
						{
	 return buildMDCSign(MDCCODE,yytext());
	}
					case -24:
						break;
					case 24:
						{return buildMDCSign(BLACKPOINT, "O");}
					case -25:
						break;
					case 25:
						{return buildMDCSign(HALFSPACE, ".");}
					case -26:
						break;
					case 26:
						{return buildMDCSign(QUATERSHADE, "/");}
					case -27:
						break;
					case 27:
						{return buildMDCSymbol(PAGEEND);}
					case -28:
						break;
					case 28:
						{// Non standard rule. Used to fix small errors.
	  return buildMDCSymbol(SEPARATOR);}
					case -29:
						break;
					case 29:
						{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
					case -30:
						break;
					case 30:
						{  // Positions are in "glyph units". the "n" sign is about 200 glyph units wide
    	   // More, positionning is absolute
	   return buildMDCIntValuedSymbol(TABSTOP, Integer.parseInt(yytext().substring(1)));
	}
					case -31:
						break;
					case 31:
						{return buildMDCToggle(ToggleType.LINELACUNA); }
					case -32:
						break;
					case 32:
						{yybegin(PROPERTIES); return buildMDCSymbol(DOUBLELEFTCURLY);}
					case -33:
						break;
					case 33:
						{return handlePhilology(ENDPHIL, EDITORSUPERFLUOUS);}
					case -34:
						break;
					case 34:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -35:
						break;
					case 35:
						{
	  return buildMDCSymbol(STARTHIEROGLYPHS);
	}
					case -36:
						break;
					case 36:
						{return buildMDCModifier(yytext());
	 // NOTE the three different forms for modifiers :
	 //  This one for the ? modifier
	 //  The next one, which is the general form
	 //  and a specific for for \R,
	 //  because of the possibility of having an negative
	 // value after it, 
	 //  which shouldn't be possible in the generic way.
	 //  a modifier list system would be a plus
	}
					case -37:
						break;
					case 37:
						{return buildMDCModifier(yytext());}
					case -38:
						break;
					case 38:
						{return buildEndCartouche(yytext().charAt(0), 
	                         '2');}
					case -39:
						break;
					case 39:
						{
		justAfterSign= false;
    	if (expectSpace)
	    	return buildMDCSymbol(SENTENCEEND);
}
					case -40:
						break;
					case 40:
						{
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
					case -41:
						break;
					case 41:
						{return buildMDCToggle(ToggleType.SHADINGOFF); }
					case -42:
						break;
					case 42:
						{return buildMDCSymbol(OVERWRITE);}
					case -43:
						break;
					case 43:
						{return buildMDCToggle(ToggleType.SHADINGON); }
					case -44:
						break;
					case 44:
						{return buildMDCShading(yytext());}
					case -45:
						break;
					case 45:
						{return buildMDCToggle(ToggleType.RED); }
					case -46:
						break;
					case 46:
						{return buildMDCToggle(ToggleType.BLACK); }
					case -47:
						break;
					case 47:
						{return buildMDCSymbol(LIGBEFORE);}
					case -48:
						break;
					case 48:
						{return buildEndCartouche('c', 
	                         yytext().charAt(0));}
					case -49:
						break;
					case 49:
						{return buildMDCSymbol(DOUBLEAMP);}
					case -50:
						break;
					case 50:
						{return handlePhilology(BEGINPHIL, EDITORSUPERFLUOUS);}
					case -51:
						break;
					case 51:
						{return handlePhilology(BEGINPHIL, ERASEDSIGNS);}
					case -52:
						break;
					case 52:
						{return handlePhilology(BEGINPHIL, PREVIOUSLYREADABLE);}
					case -53:
						break;
					case 53:
						{return handlePhilology(BEGINPHIL, SCRIBEADDITION);}
					case -54:
						break;
					case 54:
						{return handlePhilology(BEGINPHIL, EDITORADDITION);}
					case -55:
						break;
					case 55:
						{return buildBeginCartouche(yytext().charAt(1), '1');}
					case -56:
						break;
					case 56:
						{return buildStartOldCartouche('c', yytext().charAt(1));}
					case -57:
						break;
					case 57:
						{return buildBeginCartouche('c', yytext().charAt(1));}
					case -58:
						break;
					case 58:
						{return buildStartOldCartouche(yytext().charAt(1), 'a');}
					case -59:
						break;
					case 59:
						{return handlePhilology(ENDPHIL, PREVIOUSLYREADABLE);}
					case -60:
						break;
					case 60:
						{return handlePhilology(ENDPHIL, SCRIBEADDITION);}
					case -61:
						break;
					case 61:
						{return buildMDCSymbol(DOUBLEAMP);}
					case -62:
						break;
					case 62:
						{return handlePhilology(ENDPHIL, EDITORADDITION);}
					case -63:
						break;
					case 63:
						{return handlePhilology(ENDPHIL, ERASEDSIGNS);}
					case -64:
						break;
					case 64:
						{return buildMDCSign(FULLSPACE, "..");}
					case -65:
						break;
					case 65:
						{return buildMDCSign(FULLSHADE, "//");}
					case -66:
						break;
					case 66:
						{return buildMDCSign(VERTICALSHADE, "v/");}
					case -67:
						break;
					case 67:
						{return buildMDCSign(HORIZONTALSHADE, "h/");}
					case -68:
						break;
					case 68:
						{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
					case -69:
						break;
					case 69:
						{return buildEndCartouche(yytext().charAt(0), 
	                         yytext().charAt(1));}
					case -70:
						break;
					case 70:
						{return buildMDCSymbol(LIGBEFORE);}
					case -71:
						break;
					case 71:
						{return 
	     buildBeginCartouche(yytext().charAt(1),
	                    yytext().charAt(2));}
					case -72:
						break;
					case 72:
						{return buildStartOldCartouche(
	                                 yytext().charAt(1),
	                                 yytext().charAt(2));}
					case -73:
						break;
					case 73:
						{return buildMDCSymbol(LIGAFTER);}
					case -74:
						break;
					case 74:
						{
		return buildMDCSymbol(ZONE);
	}
					case -75:
						break;
					case 75:
						{ // Simple horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('l');
	}
					case -76:
						break;
					case 76:
						{ // wide horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('L');
	}
					case -77:
						break;
					case 77:
						{
		return buildMDCSymbol(CADRAT);
	}
					case -78:
						break;
					case 78:
						{return buildMDCSymbol(EQUAL);}
					case -79:
						break;
					case 79:
						{return buildMDCIntValuedSymbol(INTEGER,Integer.parseInt(yytext()));}
					case -80:
						break;
					case 80:
						{return buildMDCString(IDENTIFIER, yytext());}
					case -81:
						break;
					case 81:
						{return buildMDCSymbol(COMMA);}
					case -82:
						break;
					case 82:
						{}
					case -83:
						break;
					case 83:
						{yybegin(YYINITIAL); return buildMDCSymbol(CLOSEBRACE);}
					case -84:
						break;
					case 84:
						{yybegin(YYINITIAL); return buildMDCSymbol(DOUBLERIGHTCURLY);}
					case -85:
						break;
					case 86:
						{ 
    	  // The skip is expressed as percentage of normal skip.
    	  // So 100% <=> normal skip (default), 200% = skip one line, etc.
    	  return buildMDCIntValuedSymbol(LINEEND, extractIntFromLineSkip());    
	}
					case -86:
						break;
					case 87:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -87:
						break;
					case 88:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -88:
						break;
					case 89:
						{return buildMDCModifier(yytext());}
					case -89:
						break;
					case 90:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -90:
						break;
					case 91:
						{return buildMDCModifier(yytext());}
					case -91:
						break;
					case 92:
						{
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
					case -92:
						break;
					case 93:
						{return buildMDCShading(yytext());}
					case -93:
						break;
					case 95:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -94:
						break;
					case 96:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -95:
						break;
					case 97:
						{return buildMDCModifier(yytext());}
					case -96:
						break;
					case 98:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -97:
						break;
					case 99:
						{return buildMDCShading(yytext());}
					case -98:
						break;
					case 101:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -99:
						break;
					case 102:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -100:
						break;
					case 103:
						{return buildMDCShading(yytext());}
					case -101:
						break;
					case 105:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -102:
						break;
					case 106:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -103:
						break;
					case 108:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -104:
						break;
					case 109:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -105:
						break;
					case 111:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -106:
						break;
					case 112:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -107:
						break;
					case 114:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -108:
						break;
					case 115:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -109:
						break;
					case 117:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -110:
						break;
					case 119:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -111:
						break;
					case 120:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -112:
						break;
					case 123:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -113:
						break;
					case 124:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -114:
						break;
					case 127:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -115:
						break;
					case 128:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -116:
						break;
					case 129:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -117:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
