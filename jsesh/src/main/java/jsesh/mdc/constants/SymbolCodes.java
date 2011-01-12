package jsesh.mdc.constants;

public interface SymbolCodes {
   
    int HALFSPACE= 1;
    int FULLSPACE= 2;
    int REDPOINT= 3;
    int BLACKPOINT= 4;
    int FULLSHADE= 5;
    int VERTICALSHADE= 6;
    int HORIZONTALSHADE= 7;
    int QUATERSHADE= 8;
    /**
     * This code means that the symbol is a hieroglyph or similar sign
     * described as a manuel de codage code.
     */
    int MDCCODE= 9;
    
    /**
     * Text inserted as a sign. 
     */
    int SMALLTEXT= 10;

    
    // Codes for philological marks
    // Depending on the parser configuration, codes like [[ and ]]
    // can be interpreted either as SYMBOLS or as PHILOLOGICAL COMMENTS

    // Codes to use if the philological comments are understood as parenthesis.

    int ERASEDSIGNS= 50;
    int EDITORADDITION= 51;
    int EDITORSUPERFLUOUS= 52;
    int PREVIOUSLYREADABLE= 53;
    int SCRIBEADDITION= 54;
    int MINORADDITION= 55;
    int DUBIOUS= 56;
	
    // Code to use if philological comments are used as plain symbols.
    // To get these codes, simply multiply the previous ones by two.
    // The end code is : x * 2 + 1
    int BEGINERASE= 100;
    int ENDERASE= 101;
    int BEGINEDITORADDITION= 102;
    int ENDEDITORADDITION= 103;
    int BEGINEDITORSUPERFLUOUS= 104;	// [{ }] => { }
    int ENDEDITORSUPERFLUOUS= 105;	// [{ }] => { }
    int BEGINPREVIOUSLYREADABLE= 106;	// [" "] => [| |]
    int ENDPREVIOUSLYREADABLE= 107;	// [" "] => [| |]
    int BEGINSCRIBEADDITION= 108;	// [' '] => ' '
    int ENDSCRIBEADDITION= 109;	// [' '] => ' '
    int BEGINMINORADDITION= 110; // [( )] => ()
    int ENDMINORADDITION= 111; 
	int BEGINDUBIOUS = 112; // [? ?] => half [ ]
	int ENDDUBIOUS = 113; // [? ?] => half [ ]
	
}
