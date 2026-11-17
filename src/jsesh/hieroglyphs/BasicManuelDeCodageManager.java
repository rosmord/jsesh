/*
 * BasicManuelDeCodageManager.java
 *
 * Created on 27 sept. 2007, 17:36:34
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsesh.hieroglyphs;

import java.util.HashMap;

public class BasicManuelDeCodageManager {

    private HashMap canonical;

    public BasicManuelDeCodageManager() {
        canonical = new HashMap();
        fillMap();
    }

    /**
     * fill data.
     */
    private void fillMap() {
        putCanon("mSa", "A12");
        putCanon("xr", "A15");
        putCanon("Xrd", "A17");
        putCanon("sr", "A21");
        putCanon("mniw", "A33");
        putCanon("qiz", "A38");
        putCanon("iry", "A47");
        putCanon("Sps", "A50");
        putCanon("Spsi", "A51");
        putCanon("x", "Aa1");
        putCanon("Hp", "Aa5");
        putCanon("qn", "Aa8");
        putCanon("mAa", "Aa11");
        putCanon("M", "Aa13");
        putCanon("im", "Aa13");
        putCanon("gs", "Aa13");
        putCanon("sA", "Aa17");
        putCanon("apr", "Aa20");
        putCanon("wDa", "Aa21");
        putCanon("nD", "Aa27");
        putCanon("qd", "Aa28");
        putCanon("Xkr", "Aa30");
        putCanon("msi", "B3");
        putCanon("DHwty", "C3");
        putCanon("Xnmw", "C4");
        putCanon("inpw", "C6");
        putCanon("stX", "C7");
        putCanon("mnw", "C8");
        putCanon("mAat", "C10");
        putCanon("HH", "C11");
        putCanon("tp", "D1");
        putCanon("Hr", "D2");
        putCanon("Sny", "D3");
        putCanon("ir", "D4");
        putCanon("rmi", "D9");
        putCanon("wDAt", "D10");
        putCanon("fnD", "D19");
        putCanon("r", "D21");
        putCanon("rA", "D21");
        putCanon("spt", "D24");
        putCanon("spty", "D25");
        putCanon("mnD", "D27");
        putCanon("kA", "D28");
        putCanon("aHA", "D34");
        putCanon("a", "D36");
        putCanon("Dsr", "D45");
        putCanon("d", "D46");
        putCanon("Dba", "D50");
        putCanon("mt", "D52");
        putCanon("rd", "D56");
        putCanon("sbq", "D56");
        putCanon("gH", "D56");
        putCanon("gHs", "D56");
        putCanon("b", "D58");
        putCanon("ab", "D59");
        putCanon("wab", "D60");
        putCanon("sAH", "D61");
        putCanon("zzmt", "E6");
        putCanon("zAb", "E17");
        putCanon("mAi", "E22");
        putCanon("rw", "E23");
        putCanon("l", "E23");
        putCanon("Aby", "E24");
        putCanon("wn", "E34");
        putCanon("HAt", "F4");
        putCanon("SsA", "F5");
        putCanon("wsr", "F12");
        putCanon("wp", "F13");
        putCanon("db", "F16");
        putCanon("Hw", "F18");
        putCanon("bH", "F18");
        putCanon("ns", "F20");
        putCanon("idn", "F21");
        putCanon("msDr", "F21");
        putCanon("sDm", "F21");
        putCanon("DrD", "F21");
        putCanon("pH", "F22");
        putCanon("kfA", "F22");
        putCanon("xpS", "F23");
        putCanon("wHm", "F25");
        putCanon("Xn", "F26");
        putCanon("sti", "F29");
        putCanon("Sd", "F30");
        putCanon("ms", "F31");
        putCanon("X", "F32");
        putCanon("sd", "F33");
        putCanon("ib", "F34");
        putCanon("nfr", "F35");
        putCanon("zmA", "F36");
        putCanon("imAx", "F39");
        putCanon("Aw", "F40");
        putCanon("spr", "F42");
        putCanon("iwa", "F44");
        putCanon("isw", "F44");
        putCanon("pXr", "F46");
        putCanon("qAb", "F46");
        putCanon("A", "G1");
        putCanon("AA", "G2");
        putCanon("tyw", "G4");
        putCanon("mwt", "G14");
        putCanon("nbty", "G16");
        putCanon("m", "G17");
        putCanon("mm", "G18");
        putCanon("nH", "G21");
        putCanon("Db", "G22");
        putCanon("rxyt", "G23");
        putCanon("Ax", "G25");
        putCanon("dSr", "G27");
        putCanon("gm", "G28");
        putCanon("bA", "G29");
        putCanon("baHi", "G32");
        putCanon("aq", "G35");
        putCanon("wr", "G36");
        putCanon("gb", "G38");
        putCanon("zA", "G39");
        putCanon("pA", "G40");
        putCanon("xn", "G41");
        putCanon("wSA", "G42");
        putCanon("w", "G43");
        putCanon("ww", "G44");
        putCanon("mAw", "G46");
        putCanon("TA", "G47");
        putCanon("snD", "G54");
        putCanon("wSm", "H2");
        putCanon("pAq", "H3");
        putCanon("Sw", "H6");
        putCanon("aSA", "I1");
        putCanon("Styw", "I2");
        putCanon("mzH", "I3");
        putCanon("sbk", "I4");
        putCanon("sAq", "I5");
        putCanon("km", "I6");
        putCanon("Hfn", "I8");
        putCanon("f", "I9");
        putCanon("D", "I10");
        putCanon("DD", "I11");
        putCanon("in", "K1");
        putCanon("ad", "K3");
        putCanon("XA", "K4");
        putCanon("bz", "K5");
        putCanon("nSmt", "K6");
        putCanon("xpr", "L1");
        putCanon("bit", "L2");
        putCanon("srqt", "L7");
        putCanon("iAm", "M1");
        putCanon("Hn", "M2");
        putCanon("xt", "M3");
        putCanon("rnp", "M4");
        putCanon("tr", "M6");
        putCanon("SA", "M8");
        putCanon("zSn", "M9");
        putCanon("wdn", "M11");
        putCanon("xA", "M12");
        putCanon("wAD", "M13");
        putCanon("HA", "M16");
        putCanon("i", "M17");
        putCanon("ii", "M18");
        putCanon("sxt", "M20");
        putCanon("sm", "M21");
        putCanon("sw", "M23");
        putCanon("rsw", "M24");
        putCanon("Sma", "M26");
        putCanon("nDm", "M29");
        putCanon("bnr", "M30");
        putCanon("bdt", "M34");
        putCanon("Dr", "M36");
        putCanon("iz", "M40");
        putCanon("pt", "N1");
        putCanon("iAdt", "N4");
        putCanon("idt", "N4");
        putCanon("ra", "N5");
        putCanon("zw", "N5");
        putCanon("hrw", "N5");
        putCanon("Hnmmt", "N8");
        putCanon("pzD", "N9");
        putCanon("Abd", "N11");
        putCanon("iaH", "N11");
        putCanon("dwA", "N14");
        putCanon("sbA", "N14");
        putCanon("dwAt", "N15");
        putCanon("tA", "N16");
        putCanon("iw", "N18");
        putCanon("wDb", "N20");
        putCanon("spAt", "N24");
        putCanon("xAst", "N25");
        putCanon("Dw", "N26");
        putCanon("Axt", "N27");
        putCanon("xa", "N28");
        putCanon("q", "N29");
        putCanon("iAt", "N30");
        putCanon("n", "N35");
        putCanon("mw", "N35A");
        putCanon("S", "N37");
        putCanon("Sm", "N40");
        putCanon("id", "N42");
        putCanon("pr", "O1");
        putCanon("h", "O4");
        putCanon("Hwt", "O6");
        putCanon("aH", "O11");
        putCanon("wsxt", "O15");
        putCanon("kAr", "O18");
        putCanon("zH", "O22");
        putCanon("txn", "O25");
        putCanon("iwn", "O28");
        putCanon("aA", "O29");
        putCanon("zxnt", "O30");
        putCanon("z", "O34");
        putCanon("zb", "O35");
        putCanon("inb", "O36");
        putCanon("Szp", "O42");
        putCanon("ipt", "O45");
        putCanon("nxn", "O47");
        putCanon("niwt", "O49");
        putCanon("zp", "O50");
        putCanon("Snwt", "O51");
        putCanon("aAv", "O29v");
        putCanon("wHa", "P4");
        putCanon("TAw", "P5");
        putCanon("nfw", "P5");
        putCanon("aHa", "P6");
        putCanon("xrw", "P8");
        putCanon("st", "Q1");
        putCanon("wz", "Q2");
        putCanon("p", "Q3");
        putCanon("qrsw", "Q6");
        putCanon("qrs", "Q6");
        putCanon("xAwt", "R1");
        putCanon("xAt", "R1");
        putCanon("Htp", "R4");
        putCanon("kAp", "R5");
        putCanon("kp", "R5");
        putCanon("snTr", "R7");
        putCanon("nTr", "R8");
        putCanon("bd", "R9");
        putCanon("dd", "R11");
        putCanon("Dd", "R11");
        putCanon("imnt", "R14");
        putCanon("iAb", "R15");
        putCanon("wx", "R16");
        putCanon("xm", "R22");
        putCanon("HDt", "S1");
        putCanon("N", "S3");
        putCanon("dSrt", "S3");
        putCanon("sxmty", "S6");
        putCanon("xprS", "S7");
        putCanon("Atf", "S8");
        putCanon("Swty", "S9");
        putCanon("mDH", "S10");
        putCanon("wsx", "S11");
        putCanon("nbw", "S12");
        putCanon("tHn", "S15");
        putCanon("THn", "S15");
        putCanon("mnit", "S18");
        putCanon("sDAw", "S19");
        putCanon("xtm", "S20");
        putCanon("sT", "S22");
        putCanon("dmD", "S23");
        putCanon("Tz", "S24");
        putCanon("Sndyt", "S26");
        putCanon("mnxt", "S27");
        putCanon("s", "S29");
        putCanon("sf", "S30");
        putCanon("siA", "S32");
        putCanon("Tb", "S33");
        putCanon("anx", "S34");
        putCanon("Swt", "S35");
        putCanon("xw", "S37");
        putCanon("HqA", "S38");
        putCanon("awt", "S39");
        putCanon("wAs", "S40");
        putCanon("Dam", "S41");
        putCanon("abA", "S42");
        putCanon("sxm", "S42");
        putCanon("xrp", "S42");
        putCanon("md", "S43");
        putCanon("Ams", "S44");
        putCanon("nxxw", "S45");
        putCanon("HD", "T3");
        putCanon("HDD", "T6");
        putCanon("pd", "T9");
        putCanon("pD", "T10");
        putCanon("zin", "T11");
        putCanon("zwn", "T11");
        putCanon("sXr", "T11");
        putCanon("Ai", "T12");
        putCanon("Ar", "T12");
        putCanon("rwd", "T12");
        putCanon("rwD", "T12");
        putCanon("rs", "T13");
        putCanon("qmA", "T14");
        putCanon("wrrt", "T17");
        putCanon("Sms", "T18");
        putCanon("qs", "T19");
        putCanon("wa", "T21");
        putCanon("sn", "T22");
        putCanon("iH", "T24");
        putCanon("DbA", "T25");
        putCanon("Xr", "T28");
        putCanon("nmt", "T29");
        putCanon("sSm", "T31");
        putCanon("nm", "T34");
        putCanon("mA", "U1");
        putCanon("mr", "U6");
        putCanon("it", "U10");
        putCanon("HqAt", "U11");
        putCanon("hb", "U13");
        putCanon("Sna", "U13");
        putCanon("tm", "U15");
        putCanon("biA", "U16");
        putCanon("grg", "U17");
        putCanon("stp", "U21");
        putCanon("mnx", "U22");
        putCanon("Ab", "U23");
        putCanon("Hmt", "U24");
        putCanon("wbA", "U26");
        putCanon("DA", "U28");
        putCanon("rtH", "U31");
        putCanon("zmn", "U32");
        putCanon("ti", "U33");
        putCanon("xsf", "U34");
        putCanon("Hm", "U36");
        putCanon("mxAt", "U38");
        putCanon("St", "V1");
        putCanon("Snt", "V1");
        putCanon("100", "V1");
        putCanon("sTA", "V2");
        putCanon("sTAw", "V3");
        putCanon("wA", "V4");
        putCanon("snT", "V5");
        putCanon("Sn", "V7");
        putCanon("arq", "V12");
        putCanon("T", "V13");
        putCanon("iTi", "V15");
        putCanon("mDt", "V19");
        putCanon("XAr", "V19");
        putCanon("TmA", "V19");
        putCanon("10", "V20");
        putCanon("mD", "V20");
        putCanon("mH", "V22");
        putCanon("wD", "V24");
        putCanon("aD", "V26");
        putCanon("H", "V28");
        putCanon("wAH", "V29");
        putCanon("sk", "V29");
        putCanon("nb", "V30");
        putCanon("k", "V31");
        putCanon("msn", "V32");
        putCanon("sSr", "V33");
        putCanon("idr", "V37");
        putCanon("bAs", "W2");
        putCanon("Hb", "W3");
        putCanon("Xnm", "W9");
        putCanon("iab", "W10");
        putCanon("g", "W11");
        putCanon("nst", "W11");
        putCanon("Hz", "W14");
        putCanon("xnt", "W17");
        putCanon("mi", "W19");
        putCanon("Hnqt", "W22");
        putCanon("nw", "W24");
        putCanon("ini", "W25");
        putCanon("t", "X1");
        putCanon("rdi", "X8");
        putCanon("di", "X8");
        putCanon("mDAt", "Y1");
        putCanon("zS", "Y3");
        putCanon("mnhd", "Y3");
        putCanon("mn", "Y5");
        putCanon("ibA", "Y6");
        putCanon("zSSt", "Y8");
        putCanon("1", "Z1");
        putCanon("3", "Z2");
        putCanon("y", "Z4");
        putCanon("W", "Z7");
        putCanon("imi", "Z11");
        putCanon("wnm", "Z11");
        putCanon("`", "Ff1");
        putCanon("nr", "H4");
        putCanon("R", "D153");
        putCanon("K", "S56");

        // putCanon("Z98A", "Ff1");
    }

    /**
     * save the association between the code and the canonicalcode.
     *
     * @param code
     * @param canonicalCode
     */
    private void putCanon(String code, String canonicalCode) {
        canonical.put(code, canonicalCode);
    }

    /**
     * Returns the canonical code for a sign.
     *
     * @param code
     * @return the canonical code for the sign code, or the code if no canonical code is known.
     */
    public String getCanonicalCode(String code) {
        String result = (String) canonical.get(code);
        if (result == null) {
            result = code;
        }
        return result;
    }

    /**
     * Returns true if the sign is either a sign in Gardiner's grammar list, or
     * if the sign code corresponds to a standard translitteration.
     *
     * @param code
     * @return
     */
    public boolean isKnownCode(String code) {
        return canonical.containsKey(code);
    }
}