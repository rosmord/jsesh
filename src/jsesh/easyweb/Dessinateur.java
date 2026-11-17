/*
 * Created on 15 déc. 2003
 *
 */
package jsesh.easyweb;

import java.io.StringReader;

import java.awt.Graphics2D;

import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.mdc.MDCParserFacade;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.tmp.simpleDemo.HieroglyphicDrawer;
import jsesh.tmp.simpleDemo.HieroglyphicText;
import jsesh.tmp.simpleDemo.HieroglyphicTextBuilder;

/**
 * @author rosmord
 *
 * Classe en charge du dessin d'un texte hieroglyphique.
 *
 **/

public class Dessinateur {
	
	private String text;
	private HieroglyphicText hieroglyphicText;
	private HieroglyphicDrawer drawer;
	
	private MDCParserFacade parser;
	private HieroglyphicTextBuilder builder;
			
	public Dessinateur() {
		drawer= new HieroglyphicDrawer(DefaultHieroglyphicFontManager.getInstance());
		builder= new HieroglyphicTextBuilder();
		parser= new MDCParserFacade(builder);
	}

	public void setText(String txt) throws MDCSyntaxError {
		text= txt;
		parser.parse(new StringReader(text));
		hieroglyphicText= builder.getText();
	}
	
	/**
	 * Fixe la hauteur maximale d'un signe, en pixels.
	 * @param h
	 */
	public void setSignHeight(int h) {
		drawer.setSigneHeight(h);
	}
	
	
	public  int getWidth() {
		return (int)drawer.getTextWidth(hieroglyphicText);	
	}
	
	public int getHeight() {
		return (int)drawer.getTextHeight(hieroglyphicText); 
	}
	
	public void draw(Graphics2D g) {
		drawer.draw(g,hieroglyphicText);
	}
	/**
	 * @return the text.
	 */
	public String getText() {
		return text;
	}

}
