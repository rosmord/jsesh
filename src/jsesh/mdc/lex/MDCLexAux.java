package jsesh.mdc.lex;
import jsesh.mdc.*;
import jsesh.mdc.constants.*;
import java_cup.runtime.Symbol;


class MDCLexAux implements MDCSymbols, SymbolCodes, java_cup.runtime.Scanner {
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
    public MDCSyntaxError buildError(String msg, String token) {
            String res= msg + " line " + yyline + " char " + yychar + " at token '" + yytext()+ "'";
            return new MDCSyntaxError(res, yyline, yychar, token);
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
		135
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
	static private int[][] unpackFromString(int size1, int size2, String st) {
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
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NOT_ACCEPT,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NOT_ACCEPT,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NO_ANCHOR,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NOT_ACCEPT,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NO_ANCHOR,
		/* 114 */ YY_NOT_ACCEPT,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NOT_ACCEPT,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NOT_ACCEPT,
		/* 121 */ YY_NO_ANCHOR,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NOT_ACCEPT,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NO_ANCHOR,
		/* 126 */ YY_NOT_ACCEPT,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NOT_ACCEPT,
		/* 129 */ YY_NO_ANCHOR,
		/* 130 */ YY_NOT_ACCEPT,
		/* 131 */ YY_NOT_ACCEPT,
		/* 132 */ YY_NOT_ACCEPT,
		/* 133 */ YY_NOT_ACCEPT,
		/* 134 */ YY_NOT_ACCEPT,
		/* 135 */ YY_NOT_ACCEPT,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NO_ANCHOR,
		/* 138 */ YY_NOT_ACCEPT,
		/* 139 */ YY_NOT_ACCEPT,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NO_ANCHOR,
		/* 142 */ YY_NOT_ACCEPT,
		/* 143 */ YY_NOT_ACCEPT,
		/* 144 */ YY_NO_ANCHOR,
		/* 145 */ YY_NO_ANCHOR,
		/* 146 */ YY_NO_ANCHOR,
		/* 147 */ YY_NO_ANCHOR,
		/* 148 */ YY_NO_ANCHOR
	};
	static private int yy_cmap[] = unpackFromString(1,65538,
"20:9,59,29,20:2,29,20:18,33,1,50,32,35,5,51,30,54,53,42,16,13,2,61,62,48,37" +
",38,39,40,4:5,41,20,44,3,49,6,60,56:5,45,56,45,56:3,15,56:2,58,56:2,55,45,5" +
"6:7,43,18,52,36,31,57,10,34,7,26,9,47,17,64,17:3,8,46,23,22,17,24,11,19,27," +
"25,63,17:3,21,12,28,14,20:65410,0:2")[0];

	static private int yy_rmap[] = unpackFromString(1,149,
"0,1,2,3,1,4,5,6,7,8,4,9,10,11,12,13,1,14,15,16,1,17,18,1:2,4,19,20,21,22,23" +
",24,1:4,25,1:2,26,1,27,1:5,28,1:2,29,1:10,30,1,31,1:2,32,1:13,4,1:3,4,1,33," +
"34,1:4,35,36,37,1,38,39,40,41,42,43,44,45,46,47,48,25,49,50,51,52,53,1,54,5" +
"5,56,57,58,59,60,61,62,63,1,64,65,66,10,67,68,69,70,71,72,73,74,48,75,76,77" +
",78,79,80,81,82,83,84,85")[0];

	static private int yy_nxt[][] = unpackFromString(86,65,
"1,2,3,4,5,6,7,5:5,8,95,104,5,111,5,9,94,95,141,10,5,148,5:3,11,12,116,12,13" +
",12,5,14,15,103:2,5:2,16,17,18,19,5:2,94,103,20,119,21,122,22,23,5:2,24,25," +
"12,110,26,27,115,118,-1:66,28,93,92,-1:25,93,-1,93,-1,93,-1:25,93,-1:7,29,-" +
"1:26,29,-1,29,30,29,-1:25,29,-1:9,5,-1:2,5:5,-1:3,5,-1,5,-1,5,-1,5:7,-1:6,5" +
",-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:7,102,-1:61,31,-1,32,-1:3" +
"0,31:4,-1:7,31,-1:3,33,-1:20,138,-1:3,34,-1:2,143,-1:53,96,-1,38,105:5,-1:3" +
",105,-1,105,-1,105,-1,105:7,-1:6,105,-1:2,96:4,-1:4,105:3,96,-1:6,39,105,-1" +
",105,-1:4,105:2,-1,11,-1,11:15,97,11:11,106,11:34,-1:29,41,-1,41,-1,41,-1:2" +
"5,41,-1:7,43,-1:6,44,-1:22,45,100,46,-1:2,47,101,108,113,-1:35,48,-1:22,49," +
"-1:66,50,-1:70,52,-1:28,53,-1:5,54,-1:17,55,-1:12,56,-1:6,57,58,-1:2,59,-1:" +
"19,60,-1:9,61,-1:14,60,-1:2,62:2,-1:6,63,60,61,62,-1:15,61,-1:51,66,67,-1:6" +
"4,69,-1:73,70,-1:65,71,-1:4,28,-1:26,28,-1,28,-1,28,-1:25,28,-1:7,29,-1:26," +
"29,-1,29,-1,29,-1:25,29,-1:7,74,-1:6,44,-1:24,46,-1:34,31,-1:32,31:4,-1:7,3" +
"1,-1:17,36:15,126,36,98,36:46,-1:2,99,-1,99,-1:2,105:5,-1:3,105,-1,105,-1,1" +
"05,-1,105:7,-1:6,105,-1:2,99:4,-1:4,105:3,99,-1:6,105:2,-1,105,-1:4,105:2,-" +
"1:29,128,-1,128,-1,128,-1:25,128,-1:43,101,108,113,-1:60,76,-1:65,77:3,-1:8" +
",77,-1:25,78,-1:24,78,-1:11,78,-1:69,79,-1:17,86,-1:32,86:4,-1:7,86,-1:20,8" +
"7,-1:2,87:5,-1:3,87,-1,87,-1,87,-1,87:7,-1:3,87,-1:2,87,-1:2,87:4,-1:4,87:4" +
",-1:6,87:2,-1,87,-1:4,87:2,-1:4,117,-1:32,117:4,-1:7,117,-1:18,93,-1:26,93," +
"-1,93,-1,93,-1:25,93,-1:9,5,-1:2,5:5,-1:3,5,-1,5,-1,5,-1,5:7,-1:6,5,-1:2,12" +
"1:3,5,-1:4,5:3,121,40,-1:5,5:2,-1,5,-1,5,-1:2,5:2,-1:4,96,-1:32,96:4,-1:7,9" +
"6,-1:17,11:17,97,11:11,106,11:34,-1,36:15,107,36,98,36:46,-1:4,99,-1:32,99:" +
"4,-1:7,99,-1:49,100,-1:70,108,113,-1:32,120,-1:60,5,-1:2,5:5,-1:3,5,-1,5,-1" +
",5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,51,-1:5,5:2,-1,5,-1,5,-1:2,5:2,-1:52,35," +
"-1:16,96,-1:2,105:5,-1:3,105,-1,105,-1,105,-1,105:7,-1:6,105,-1:2,96:4,-1:4" +
",105:3,96,-1:6,105:2,-1,105,-1:4,105:2,-1,11,-1,11:15,112,11:11,106,11:34,-" +
"1:40,113,-1:25,109:17,114,109:31,64,109,-1,109:12,-1,124:3,5,124:2,5:5,124:" +
"3,5,124,5,124,5,124,5:7,124,-1,124:4,5,124:2,5:4,124:4,5:4,124:6,5:2,124,5," +
"124,5,124:2,5:2,-1:7,36:5,-1:4,36:2,-1,37,-1,36:7,-1:6,36,-1:11,36:2,-1:15," +
"36:2,-1,11:17,97,11:11,137,11:34,-1,109:28,-1,109:35,-1:4,5,-1:2,5:5,-1:3,5" +
",-1,5,-1,5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1,72,5:2,-1:" +
"52,42,-1:16,117,93,-1:31,117:4,-1:7,117,-1:20,5,-1:2,5:5,-1:3,5,-1,5,-1,5,-" +
"1,5:7,-1:6,5,-1:2,121:3,5,-1:4,5:3,121,40,-1:5,5:2,-1,5,-1,5,-1,73,5:2,-1,1" +
"09:17,114,109:31,64,109,65,109:12,-1:9,130,-1:59,5,-1:2,5:5,-1:3,5,-1,5,-1," +
"5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,75,-1:5,5:2,-1,5,-1,5,-1:2,5:2,-1:52,68,-" +
"1:16,123,-1:8,131,-1:23,123:4,-1:7,123,-1:30,91,-1:51,36:6,-1:5,36:4,-1:2,3" +
"6,-1,36,-1:7,36:6,-1,36:11,-1:2,36:15,-1:6,5,-1:2,5:2,80,5:2,-1:3,5,-1,5,-1" +
",5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:4,5,-1:2," +
"5:5,-1:3,5,-1,5,-1,5,-1,5:6,84,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5," +
"-1:2,5:2,-1:10,132,-1:58,133,-1:32,133:4,-1:7,133,-1:27,81,-1:57,133,-1:9,8" +
"2,-1:22,133:4,-1:7,133,-1:20,134,-1:9,83,-1:22,134:4,-1:7,134,-1:16,1,95:2," +
"85,86,95:2,87:5,95,88,125,87,95,87,95,87,95,87:7,95,89,95,87,95,89,87,95:2," +
"86:4,95:4,87:3,86,95:3,90,95:2,87:2,95,87,89,95:3,87:2,-1:4,5,-1:2,5:5,-1:3" +
",5,-1,5,-1,5,-1,5:2,127,5:4,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:" +
"2,5:2,-1:4,123,-1:32,123:4,-1:7,123,-1:20,134,-1:32,134:4,-1:7,134,-1:20,5," +
"-1:2,5:5,-1:3,5,-1,5,-1,5,-1,5:2,129,5:4,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2," +
"-1,5,-1,5,-1:2,5:2,-1:4,5,-1:2,5:5,-1:3,5,-1,5,-1,5,-1,5,136,5:5,-1:6,5,-1:" +
"2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:4,142,-1:8,139,-1:23,142:4,-1" +
":7,142,-1:20,142,-1:32,142:4,-1:7,142,-1:20,5,-1:2,5:3,140,5,-1:3,5,-1,5,-1" +
",5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:4,5,-1:2," +
"5:4,144,-1:3,5,-1,5,-1,5,-1,5:7,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5" +
",-1:2,5:2,-1:4,5,-1:2,5:5,-1:3,5,-1,5,-1,5,-1,5:5,145,5,-1:6,5,-1:2,5:4,-1:" +
"4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:4,5,-1:2,5:3,146,5,-1:3,5,-1,5,-1,5,-1" +
",5:7,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5,-1:2,5:2,-1:4,5,-1:2,5:5,-" +
"1:3,5,-1,5,-1,5,-1,5:4,147,5:2,-1:6,5,-1:2,5:4,-1:4,5:4,-1:6,5:2,-1,5,-1,5," +
"-1:2,5:2");

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
						{ return buildMDCSymbol(TABBING);}
					case -7:
						break;
					case 7:
						{return buildMDCToggle(ToggleType.LACUNA);}
					case -8:
						break;
					case 8:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -9:
						break;
					case 9:
						{return buildMDCModifier(yytext());}
					case -10:
						break;
					case 10:
						{return buildMDCSign(REDPOINT, "o");}
					case -11:
						break;
					case 11:
						{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -12:
						break;
					case 12:
						{
	justAfterSign= false;
	if (expectSpace)
		return buildMDCSymbol(WORDEND);
}
					case -13:
						break;
					case 13:
						{
	if (justAfterSign)	// Potential problem here : A #x could really mean "A (end word) overwrite x" !!!
		return buildMDCSymbol(OVERWRITE);
	else
		return buildMDCToggle(ToggleType.SHADINGTOGGLE);
}
					case -14:
						break;
					case 14:
						{return buildMDCToggle(ToggleType.BLACKRED);}
					case -15:
						break;
					case 15:
						{return buildMDCToggle(ToggleType.OMMIT);}
					case -16:
						break;
					case 16:
						{return buildMDCSymbol(COLON);}
					case -17:
						break;
					case 17:
						{return buildMDCSymbol(STAR);}
					case -18:
						break;
					case 18:
						{yybegin(PROPERTIES); return buildMDCSymbol(OPENBRACE);}
					case -19:
						break;
					case 19:
						{return buildStartOldCartouche('c', 'a');}
					case -20:
						break;
					case 20:
						{return buildEndCartouche('c', '2');}
					case -21:
						break;
					case 21:
						{return buildMDCSymbol(AMP);}
					case -22:
						break;
					case 22:
						{return buildMDCSymbol(EPAR);}
					case -23:
						break;
					case 23:
						{return buildMDCSymbol(BPAR);}
					case -24:
						break;
					case 24:
						{
	 return buildMDCSign(MDCCODE,yytext());
	}
					case -25:
						break;
					case 25:
						{return buildMDCSign(BLACKPOINT, "O");}
					case -26:
						break;
					case 26:
						{return buildMDCSign(HALFSPACE, ".");}
					case -27:
						break;
					case 27:
						{return buildMDCSign(QUATERSHADE, "/");}
					case -28:
						break;
					case 28:
						{return buildMDCSymbol(PAGEEND);}
					case -29:
						break;
					case 29:
						{// Non standard rule. Used to fix small errors.
	  return buildMDCSymbol(SEPARATOR);}
					case -30:
						break;
					case 30:
						{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
					case -31:
						break;
					case 31:
						{  // Positions are in "glyph units". the "n" sign is about 200 glyph units wide
    	   // More, positionning is absolute
	   return buildMDCIntValuedSymbol(TABSTOP, Integer.parseInt(yytext().substring(1)));
	}
					case -32:
						break;
					case 32:
						{return buildMDCToggle(ToggleType.LINELACUNA); }
					case -33:
						break;
					case 33:
						{return handlePhilology(ENDPHIL, DUBIOUS);}
					case -34:
						break;
					case 34:
						{yybegin(PROPERTIES); return buildMDCSymbol(DOUBLELEFTCURLY);}
					case -35:
						break;
					case 35:
						{return handlePhilology(ENDPHIL, EDITORSUPERFLUOUS);}
					case -36:
						break;
					case 36:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -37:
						break;
					case 37:
						{
	  return buildMDCSymbol(STARTHIEROGLYPHS);
	}
					case -38:
						break;
					case 38:
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
					case -39:
						break;
					case 39:
						{return buildMDCModifier(yytext());}
					case -40:
						break;
					case 40:
						{return buildEndCartouche(yytext().charAt(0), 
	                         '2');}
					case -41:
						break;
					case 41:
						{
		justAfterSign= false;
    	if (expectSpace)
	    	return buildMDCSymbol(SENTENCEEND);
}
					case -42:
						break;
					case 42:
						{return handlePhilology(ENDPHIL, SCRIBEADDITION);}
					case -43:
						break;
					case 43:
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
					case -44:
						break;
					case 44:
						{return buildMDCToggle(ToggleType.SHADINGOFF); }
					case -45:
						break;
					case 45:
						{return buildMDCSymbol(OVERWRITE);}
					case -46:
						break;
					case 46:
						{return buildMDCToggle(ToggleType.SHADINGON); }
					case -47:
						break;
					case 47:
						{return buildMDCShading(yytext());}
					case -48:
						break;
					case 48:
						{return buildMDCToggle(ToggleType.RED); }
					case -49:
						break;
					case 49:
						{return buildMDCToggle(ToggleType.BLACK); }
					case -50:
						break;
					case 50:
						{return buildMDCSymbol(LIGBEFORE);}
					case -51:
						break;
					case 51:
						{return buildEndCartouche('c', 
	                         yytext().charAt(0));}
					case -52:
						break;
					case 52:
						{return buildMDCSymbol(DOUBLEAMP);}
					case -53:
						break;
					case 53:
						{return handlePhilology(BEGINPHIL, DUBIOUS);}
					case -54:
						break;
					case 54:
						{return handlePhilology(BEGINPHIL, EDITORSUPERFLUOUS);}
					case -55:
						break;
					case 55:
						{return handlePhilology(BEGINPHIL, SCRIBEADDITION);}
					case -56:
						break;
					case 56:
						{return handlePhilology(BEGINPHIL, ERASEDSIGNS);}
					case -57:
						break;
					case 57:
						{return handlePhilology(BEGINPHIL, PREVIOUSLYREADABLE);}
					case -58:
						break;
					case 58:
						{return handlePhilology(BEGINPHIL, EDITORADDITION);}
					case -59:
						break;
					case 59:
						{return handlePhilology(BEGINPHIL, MINORADDITION);}
					case -60:
						break;
					case 60:
						{return buildStartOldCartouche('c', yytext().charAt(1));}
					case -61:
						break;
					case 61:
						{return buildBeginCartouche(yytext().charAt(1), '1');}
					case -62:
						break;
					case 62:
						{return buildBeginCartouche('c', yytext().charAt(1));}
					case -63:
						break;
					case 63:
						{return buildStartOldCartouche(yytext().charAt(1), 'a');}
					case -64:
						break;
					case 64:
						{
	// Note about this construct: it comes from macscribe "..." construct
	// it's potentially ambiguous with '"]' (previously readable)
	// Hence : "]" (and for good measure and consistency, [) should be escaped
	// with "\" when they appear here.
		return buildMDCSign(SMALLTEXT, yytext());
}
					case -65:
						break;
					case 65:
						{return handlePhilology(ENDPHIL, PREVIOUSLYREADABLE);}
					case -66:
						break;
					case 66:
						{return buildMDCSymbol(DOUBLEAMP);}
					case -67:
						break;
					case 67:
						{return handlePhilology(ENDPHIL, EDITORADDITION);}
					case -68:
						break;
					case 68:
						{return handlePhilology(ENDPHIL, ERASEDSIGNS);}
					case -69:
						break;
					case 69:
						{return handlePhilology(ENDPHIL, MINORADDITION);}
					case -70:
						break;
					case 70:
						{return buildMDCSign(FULLSPACE, "..");}
					case -71:
						break;
					case 71:
						{return buildMDCSign(FULLSHADE, "//");}
					case -72:
						break;
					case 72:
						{return buildMDCSign(VERTICALSHADE, "v/");}
					case -73:
						break;
					case 73:
						{return buildMDCSign(HORIZONTALSHADE, "h/");}
					case -74:
						break;
					case 74:
						{return buildMDCToggle(ToggleType.SHADINGTOGGLE);}
					case -75:
						break;
					case 75:
						{return buildEndCartouche(yytext().charAt(0), 
	                         yytext().charAt(1));}
					case -76:
						break;
					case 76:
						{return buildMDCSymbol(LIGBEFORE);}
					case -77:
						break;
					case 77:
						{return 
	     buildBeginCartouche(yytext().charAt(1),
	                    yytext().charAt(2));}
					case -78:
						break;
					case 78:
						{return buildStartOldCartouche(
	                                 yytext().charAt(1),
	                                 yytext().charAt(2));}
					case -79:
						break;
					case 79:
						{return buildMDCSymbol(LIGAFTER);}
					case -80:
						break;
					case 80:
						{
		return buildMDCSymbol(ZONE);
	}
					case -81:
						break;
					case 81:
						{return buildMDCSymbol(TABBINGCLEAR);}
					case -82:
						break;
					case 82:
						{ // Simple horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('l');
	}
					case -83:
						break;
					case 83:
						{ // wide horizontal line from position integer1 to integer2. Absolute positioning
	    return buildHRule('L');
	}
					case -84:
						break;
					case 84:
						{
		return buildMDCSymbol(CADRAT);
	}
					case -85:
						break;
					case 85:
						{return buildMDCSymbol(EQUAL);}
					case -86:
						break;
					case 86:
						{return buildMDCIntValuedSymbol(INTEGER,Integer.parseInt(yytext()));}
					case -87:
						break;
					case 87:
						{return buildMDCString(IDENTIFIER, yytext());}
					case -88:
						break;
					case 88:
						{return buildMDCSymbol(COMMA);}
					case -89:
						break;
					case 89:
						{}
					case -90:
						break;
					case 90:
						{yybegin(YYINITIAL); return buildMDCSymbol(CLOSEBRACE);}
					case -91:
						break;
					case 91:
						{yybegin(YYINITIAL); return buildMDCSymbol(DOUBLERIGHTCURLY);}
					case -92:
						break;
					case 93:
						{ 
    	  // The skip is expressed as percentage of normal skip.
    	  // So 100% <=> normal skip (default), 200% = skip one line, etc.
    	  return buildMDCIntValuedSymbol(LINEEND, extractIntFromLineSkip());    
	}
					case -93:
						break;
					case 94:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -94:
						break;
					case 95:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -95:
						break;
					case 96:
						{return buildMDCModifier(yytext());}
					case -96:
						break;
					case 97:
						{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -97:
						break;
					case 98:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -98:
						break;
					case 99:
						{return buildMDCModifier(yytext());}
					case -99:
						break;
					case 100:
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
					case -100:
						break;
					case 101:
						{return buildMDCShading(yytext());}
					case -101:
						break;
					case 103:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -102:
						break;
					case 104:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -103:
						break;
					case 105:
						{return buildMDCModifier(yytext());}
					case -104:
						break;
					case 106:
						{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -105:
						break;
					case 107:
						{
	  String txt="";
	  if (yytext().length() > 2) txt= yytext().substring(2);
	  return buildMDCAlphabetictext(yytext().charAt(1), txt.replaceAll("\\\\\\+","+").replaceAll("\\\\\\\\","\\\\"));
	}
					case -106:
						break;
					case 108:
						{return buildMDCShading(yytext());}
					case -107:
						break;
					case 110:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -108:
						break;
					case 111:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -109:
						break;
					case 112:
						{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -110:
						break;
					case 113:
						{return buildMDCShading(yytext());}
					case -111:
						break;
					case 115:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -112:
						break;
					case 116:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -113:
						break;
					case 118:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -114:
						break;
					case 119:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -115:
						break;
					case 121:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -116:
						break;
					case 122:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -117:
						break;
					case 124:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -118:
						break;
					case 125:
						{return buildMDCString(UNKNOWN, yytext());
    // Local Variables:
    // mode: java
    // tab-width: 48
    // End: 
}
					case -119:
						break;
					case 127:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -120:
						break;
					case 129:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -121:
						break;
					case 136:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -122:
						break;
					case 137:
						{
	  // Text position
	  return buildMDCString(TEXTSUPER, yytext().substring(1));
	}
					case -123:
						break;
					case 140:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -124:
						break;
					case 141:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -125:
						break;
					case 144:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -126:
						break;
					case 145:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -127:
						break;
					case 146:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -128:
						break;
					case 147:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -129:
						break;
					case 148:
						{
                // MANUEL DE CODAGE CODE FOR A SIGN
	            return buildMDCSign(MDCCODE,yytext());
}
					case -130:
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
