package jsesh.mdc.lex;

public class MDCShading implements MDCSymbols {
    int b;

    public MDCShading() {
	b= 0;
    }

    public int getShading() {
	return b;
    }

    public MDCShading(String yytext) {
	this();
	int i;
	for(i=0; i< yytext.length(); i++) {
	    switch (yytext.charAt(i)) {
	    case '1':
		b |= 1;
		break;
	    case '2':
		b |= 2;
		break;
	    case '3':
		b |= 4;
		break;
	    case '4':
		b |= 8;
		break;
	    default:
	    }
	}
    }
}


