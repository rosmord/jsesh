/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package org.qenherkhopeshef.graphics.rtfBasicWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to create simple RTF files. This is far less ambitious than IText.
 * However, we are writing it because we want a firm handling of WMF file
 * inclusion (with which we have had problems in IText).
 */
public class SimpleRTFWriter {

    /**
     * Map String -> RTFFontData
     */
    private HashMap<String, RTFFontData> fontNamesToFontData = new HashMap<String, RTFFontData>();
    /**
     * Array of RTFFontData
     */
    private ArrayList<RTFFontData> fontIdToFontData = new ArrayList<RTFFontData>();
    private BufferedWriter writer;
    private int maxFontNum = -1;

    public SimpleRTFWriter(OutputStream outputStream) {
        try {
            this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, "US-ASCII"));
            //this.writer= new OutputStreamWriter(outputStream, "MacRoman");
        } catch (UnsupportedEncodingException e) {
            // Normally impossible. ASCII is universally supported by java.
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a Mac Pict picture.
     *
     * @param data the picture binary data
     * @param width the width of the picture, in points
     * @param height the height of the picture, in points.
     * @throws IOException
     */
    public void writeMacPictPicture(byte[] data, double width, double height) throws IOException {
        //writer.write("{\\*\\shppict{\\pict"); // We can even use old version of word.
        writer.write("{{\\pict");

        // Unnecessary stuff: picture scale (100 is default)
        // picture crop (0 is default)
        // \picscalex100\picscaley100
        // \piccropl0\piccropr0\piccropt0\piccropb0
        int integerWidth = (int) Math.ceil(width);
        int integerHeight = (int) Math.ceil(height);

        // The "inner" width and height of the picture.
        writer.write("\\picw" + integerWidth);
        writer.write("\\pich" + integerHeight);
        // The targeted dimensions (in TWIPS)

        //picwgoal83\pichgoal359
        int widthInTwips = (int) Math.ceil(20 * width);
        int heightInTwips = (int) Math.ceil(20 * height);
        writer.write("\\picwgoal" + widthInTwips);
        writer.write("\\pichgoal" + heightInTwips);

        writer.write("\\macpict");
        // \blipupi : units per inch. Not needed.

        writer.newLine();
        for (int i = 0; i < data.length; i++) {
            if (i % 64 == 0) {
                writer.newLine();
            }
            int v = (data[i] + 0x100) % 0x100;
            //System.out.println(v+ " => " + Integer.toHexString(v));
            String hex = Integer.toHexString(v);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            writer.write(hex);
        }
        writer.write("}}");
        writer.newLine();
    }

    public void writeHeader() throws IOException {
        //String header="{\\rtf1\\ansi";
        //header= "{\\rtf1\\ansi\\uc0\\deff0\\stshfdbch0\\stshfloch0\\stshfhich0\\stshfbi0\\deflang1036\\deflangfe1036{\\upr{\\fonttbl{\\f0\\fnil\\fcharset256\\fprq2 Times New Roman;}}}\\pard\\plain";
        //header= "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\froman\\fcharset0 Times New Roman;}}";
        StringBuffer header = new StringBuffer();
        header.append("{\\rtf1\\ansi");
        header.append("{\\fonttbl");
        for (int i = 0; i <= maxFontNum; i++) {
            // \f0\fswiss\fcharset77 Helvetica;
            // \f2\fnil\fcharset77 MDCTranslitLC;
            RTFFontData data = fontIdToFontData.get(i);
            header.append("\\f" + i + "\\fnil" + "\\fcharset77 " + data.fontName);
            header.append(";");
        }
        header.append("}");
        writer.write(header.toString());
        // Non Unicode reader will ignore Unicode chars.
        //writer.write("\\uc0");
    }

    public void writeTail() throws IOException {
        writer.write("}");
        //writer.write("}"); // ???? word seems to write an extraneous "}". So do we.

        //writer.write(0); // For mac. Does it work elsewhere ?
        writer.close();
    }

    public void setItalic(boolean italic) throws IOException {
        writer.write("\\i");
        if (!italic) {
            writer.write("0");
        }
        writer.write(" ");
    }

    public void setBold(boolean bold) throws IOException {
        writer.write("\\b");
        if (!bold) {
            writer.write("0");
        }
        writer.write(" ");
    }

    public void writeString(String text) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 128) {
                writer.write(c);
            } else {
                writer.write("\\u");
                writer.write(Integer.toString(c));
                // "equivalent character in ascii... well, we only do unicode, so we publish a "?" here:
                writer.write(" ?");

            }
        }
    }

    public void newParagraph() throws IOException {
        writer.write("\\par ");
    }

    public void startBlock() throws IOException {
        writer.write("{");
    }

    public void endBlock() throws IOException {
        writer.write("}");
    }

    public void declareFont(String fontName, RTFFontFamily fontFamily) {
        RTFFontData fontData = new RTFFontData(++maxFontNum, fontName, fontFamily);
        fontNamesToFontData.put(fontName, fontData);
        fontIdToFontData.add(null);
        fontIdToFontData.set(maxFontNum, fontData);
    }

    public void useFont(String fontName) throws IOException {
        RTFFontData data = fontNamesToFontData.get(fontName);
        writer.write("\\f" + data.fontNum + " ");
    }

    /**
     * @throws IOException
     *
     */
    public void newPage() throws IOException {
        writer.write("");
    }

    private static class RTFFontData {

        int fontNum;
        String fontName;
        RTFFontFamily fontFamily;

        public RTFFontData(int fontNum, String fontName, RTFFontFamily fontFamily) {
            super();
            this.fontNum = fontNum;
            this.fontName = fontName;
            this.fontFamily = fontFamily;
        }

    }

    /**
     * Write an emf picture
     *
     * @param data
     * @param width picture width, in points
     * @param height picture height, in points
     * @throws IOException
     */
    public void writeEmfPicture(byte[] data, double width, double height) throws IOException {

        // TODO : add \picwgoal and \pichgoal ?
        double picScale = 100.0 / (72 / 25.4); // Number of 1/100mm in one inch.

        int scaleWidth = (int) Math.ceil(width * picScale);
        int scaledHeight = (int) Math.ceil(height * picScale);

        writer.write("{{\\pict");
        writer.write("\\emfblip");

        // Width and height are expressed in 1/100 of a mm here.
        writer.write("\\picw" + scaleWidth);
        writer.write("\\pich" + scaledHeight);

        writer.write("\\picscalex100");
        writer.write("\\picscaley100");
        
        // TODO ? add \bin - no, I use SDATA. ?

        for (int i = 0; i < data.length; i++) {
            if (i % 20 == 0) {
                writer.newLine();
            }
            int v = (data[i] + 0x100) % 0x100;
            //System.out.println(v+ " => " + Integer.toHexString(v));
            String hex = Integer.toHexString(v);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            writer.write(hex);
        }

//		FileOutputStream out= new FileOutputStream("/tmp/truc.emf");
//		out.write(data);
//		out.close();
        writer.write("}}");
    }

    /**
     * Write an emf picture
     *
     * @param data
     * @param width picture width, in points
     * @param height picture height, in points
     * @throws IOException
     */
    public void writeWmfPicture(byte[] data, double width, double height) throws IOException {
        double picScale = 100.0 / (72 / 25.4); // Number of 1/100mm in one inch.

        int scaleWidth = (int) Math.ceil(width * picScale);
        int scaledHeight = (int) Math.ceil(height * picScale);

        writer.write("{\\*\\shppict{\\pict");
        writer.write("\\wmetafile8");

        // Width and height are expressed in 1/100 of a mm here.
        writer.write("\\picw" + scaleWidth);
        writer.write("\\pich" + scaledHeight);

        writer.write("\\picscalex100");
        writer.write("\\picscaley100");

        for (int i = 0; i < data.length; i++) {
            if (i % 64 == 0) {
                writer.newLine();
            }
            int v = (data[i] + 0x100) % 0x100;
            //System.out.println(v+ " => " + Integer.toHexString(v));
            String hex = Integer.toHexString(v);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            writer.write(hex);
        }

        writer.write("}}");

    }
}
