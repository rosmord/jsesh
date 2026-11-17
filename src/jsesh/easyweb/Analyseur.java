/*
 * Created on 15 d�c. 2003
 *
 */
package jsesh.easyweb;


import java.io.StringReader;
import java.util.LinkedList;

import jsesh.mdc.MDCParserFacade;
import jsesh.mdc.interfaces.CadratInterface;
import jsesh.mdc.interfaces.HieroglyphInterface;
import jsesh.mdc.interfaces.MDCBuilderAdapter;
import jsesh.mdc.interfaces.ModifierInterface;
import jsesh.mdc.interfaces.ModifierListInterface;
import jsesh.mdc.interfaces.OptionListInterface;

/**
 * Analyses a MDC text and produces a list of signs codes.
 */
public class Analyseur {
	private MDCParserFacade parser;
	private SignListBuilder builder;
	
	
	/**
	 * Cr�e un analyseur.
	 */
	public Analyseur() {
		builder= new SignListBuilder();
		parser= new MDCParserFacade(builder);
	}
	
	/**
	 * Retourne la liste des signes contenus dans un texte au format du manuel de codage.
	 * @param texte
	 * @return la liste des signes
	 * @throws Exception
	 */
	public Signe [] analyser(String texte) throws Exception
	{
		parser.parse(new StringReader(texte));
		return (Signe [])builder.result.toArray(new Signe[builder.result.size()]);
		
	}
	
	private class SimpleModifier implements ModifierListInterface, ModifierInterface {
		 public boolean isDeterminative= false;
	}
	
	private class SignListBuilder extends MDCBuilderAdapter {
		
		public LinkedList result;

		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilder#reset()
		 */
		public void reset() {
			result= new LinkedList();
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilderAdapter#buildModifierList()
		 */
		public ModifierListInterface buildModifierList() {
			return new SimpleModifier();
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilderAdapter#addModifierToModifierList(jsesh.mdc.interfaces.ModifierListInterface, java.lang.String, java.lang.Integer)
		 */
		public void addModifierToModifierList(
			ModifierListInterface mods,
			String name,
			Integer value) {
			SimpleModifier m= (SimpleModifier) mods;
			if ("d".equals(name))
				m.isDeterminative= true;
		}

		
		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilder#buildHieroglyph(boolean, int, java.lang.String, jsesh.mdc.interfaces.ListInterface, int)
		 */
		public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, ModifierListInterface m, int isEnd) {
			Signe s= new Signe(code);
			if (((SimpleModifier)m).isDeterminative)
				s.setDeterminative(true); 
			result.add(s);
			return null;
		}

        /* (non-Javadoc)
         * @see jsesh.mdc.interfaces.MDCBuilder#setOptionList(jsesh.mdc.interfaces.CadratInterface, jsesh.mdc.interfaces.OptionListInterface)
         */
        public void setOptionList(CadratInterface result, OptionListInterface e1) {
         
        }

	}
	
	public static void main(String[] args) throws Exception {
		Analyseur analyseur= new Analyseur();
		
		Signe s[]= analyseur.analyser("p\\r*t\\:pt\\d:n-<-ra-mn:n-xpr->-sw-t:n-G7\\d");
		for (int i=0; i < s.length; i++)
			{
				System.out.print(s[i].toString()+ " ");
			}
		System.out.println();
	}
}
