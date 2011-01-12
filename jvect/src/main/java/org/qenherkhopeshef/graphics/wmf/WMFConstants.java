package org.qenherkhopeshef.graphics.wmf;

public interface WMFConstants {
    /* Map modes */
	
	/**
	 * Text: downward oriented, 1 pixel = 1 unit.
	 */
    int MM_TEXT = 1;
    
    int MM_LOMETRIC	  = 2;
    int MM_HIMETRIC	  = 3;
    
    int MM_LOENGLISH	  = 4;
    int MM_HIENGLISH	  = 5;
    /**
     * one TWIPS is 1/20 of a point, or 1/1440 of an inch.
     */
    int MM_TWIPS = 6;
    /**
     * explicit scale.
     */
    int MM_ISOTROPIC = 7;
    /**
     * explicit scale.
     */
    int MM_ANISOTROPIC = 8;

  /* Background modes */

    int TRANSPARENT = 1;
    int OPAQUE = 2;
    int BKMODE_LAST = 2;

    /* Raster operations */
    /* for WMFSetROP2 */
    int R2_BLACK = 1;
    int R2_NOTMERGEPEN = 2;
    int R2_MASKNOTPEN = 3;
    int R2_NOTCOPYPEN = 4;
    int R2_MASKPENNOT = 5;
    int R2_NOT = 6;
    int R2_XORPEN = 7;
    int R2_NOTMASKPEN = 8;
    int R2_MASKPEN = 9;
    int R2_NOTXORPEN = 10;
    int R2_NOP = 11;
    int R2_MERGENOTPEN = 12;
    int R2_COPYPEN = 13;
    int R2_MERGEPENNOT = 14;
    int R2_MERGEPEN = 15;
    int R2_WHITE = 16;

/*
 * Pen Styles
 */
    int PS_SOLID = 0x00000000;
    int PS_DASH = 0x00000001;
    int PS_DOT = 0x00000002;
    int PS_DASHDOT = 0x00000003;
    int PS_DASHDOTDOT = 0x00000004;
    int PS_NULL = 0x00000005;
    int PS_INSIDEFRAME = 0x00000006;
    int PS_USERSTYLE = 0x00000007;
    int PS_ALTERNATE = 0x00000008;
    int PS_STYLE_MASK = 0x0000000f;

    int PS_ENDCAP_ROUND = 0x00000000;
    int PS_ENDCAP_SQUARE = 0x00000100;
    int PS_ENDCAP_FLAT = 0x00000200;
    int PS_ENDCAP_MASK = 0x00000f00;

    int PS_JOIN_ROUND = 0x00000000;
    int PS_JOIN_BEVEL = 0x00001000;
    int PS_JOIN_MITER = 0x00002000;
    int PS_JOIN_MASK = 0x0000f000;

    int PS_COSMETIC = 0x00000000;
    int PS_GEOMETRIC = 0x00010000;
    int PS_TYPE_MASK = 0x000f0000;

  /* Brush styles */
    int BS_SOLID = 0;
    int BS_NULL = 1;
    int BS_HOLLOW = 1;
    int BS_HATCHED = 2;
    int BS_PATTERN = 3;
    int BS_INDEXED = 4;
    int	BS_DIBPATTERN = 5;
    int	BS_DIBPATTERNPT = 6;
    int BS_PATTERN8X8 = 7;
    int	BS_DIBPATTERN8X8 = 8;
    int BS_MONOPATTERN = 9;

    /* Hatch styles */
    short HS_HORIZONTAL = 0;
    short HS_VERTICAL = 1;
    short HS_FDIAGONAL = 2;
    short HS_BDIAGONAL = 3;
    short HS_CROSS = 4;
    short HS_DIAGCROSS = 5;


    /* from wingdi : lfCharSet values */
    int ANSI_CHARSET	       = 0;;
    int DEFAULT_CHARSET        = 1;
    int SYMBOL_CHARSET	       = 2;
    int SHIFTJIS_CHARSET       = 128 ;
    int HANGEUL_CHARSET        = 129;
    int HANGUL_CHARSET = HANGEUL_CHARSET;
    int GB2312_CHARSET         = 134;
    int CHINESEBIG5_CHARSET    = 136;
    int GREEK_CHARSET          = 161;
    int TURKISH_CHARSET        = 162;
    int HEBREW_CHARSET         = 177;
    int ARABIC_CHARSET         = 178;
    int BALTIC_CHARSET         = 186;
    int RUSSIAN_CHARSET        = 204;
    int EE_CHARSET	       = 238;
    int EASTEUROPE_CHARSET = EE_CHARSET;
    int THAI_CHARSET	       = 222;
    int JOHAB_CHARSET          = 130;
    int MAC_CHARSET            = 77;
    int OEM_CHARSET	       = 255;

    /* lfPitchAndFamily pitch values */
    int DEFAULT_PITCH = 0x00;
    int FIXED_PITCH = 0x01;
    int VARIABLE_PITCH = 0x02;
    int MONO_FONT = 0x08;

    int FF_DONTCARE = 0x00;
    int FF_ROMAN = 0x10;
    int FF_SWISS = 0x20;
    int FF_MODERN = 0x30;
    int FF_SCRIPT = 0x40;
    int FF_DECORATIVE = 0x50;

    /* Polygon modes */
    short ALTERNATE = 1;
    short WINDING = 2;
    short POLYFILL_LAST = 2;
}
