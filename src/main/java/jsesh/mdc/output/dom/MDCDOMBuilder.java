/*
* Created on 18 mai 2004
*
* To change the template for this generated file go to
* Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
*/
package jsesh.mdc.output.dom;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementIterator;
import jsesh.mdc.model.ModelElementVisitor;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdc.model.OptionsMap;
import jsesh.mdc.model.Overwrite;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.Tabbing;
import jsesh.mdc.model.TabbingClear;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.ZoneStart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * @author rosmord
 */
public class MDCDOMBuilder {
	static public final String ALPHABETIC_TEXT = "alphabeticText";
	static public final String FILL_X = "fillX";
	static public final String FILL_Y = "fillY";
	static public final String ORIENTATION = "orientation";
	static public final String BASIC_ITEM_LIST = "basicItemList";
	static public final String CADRAT = "cadrat";
	static public final String SHADING = "shading";
	static public final String CARTOUCHE = "cartouche";
	static public final String START_PART = "startPart";
	static public final String END_PART = "endPart";
	static public final String TYPE = "type";
	static public final String TOGGLE = "toggle";
	static public final String TABSTOP = "tabStop";
	static public final String VALUE = "value";
	static public final String HBOX = "hbox";
	static public final String HRULE = "hrule";
	static public final String START_POS = "startPos";
	static public final String END_POS = "endPos";
	static public final String LIGATURE = "ligature";
	static public final String OVERWRITE = "overwrite";
	static public final String PHILOLOGY = "philology";
	static public final String SUBCADRAT = "subcadrat";
	static public final String SUPERSCRIPT = "superscript";
	static public final String TOP_ITEM_LIST = "topItemList";
	static public final String LINE_BREAK = "lineBreak";
	static public final String SKIP = "skip";
	static public final String PAGE_BREAK = "pageBreak";
	static public final String HIEROGLYPH = "hieroglyph";
	static public final String ENDINGCODE = "endingCode";
	static public final String GRAMMAR = "grammar";
	static public final String ANGLE = "angle";
	static public final String SCALE = "scale";
	static public final String REVERSED = "reversed";
	static public final String UNSURE = "unsure";
	static public final String ABSOLUTE = "absolute";
	static public final String COMPLEXLIGATURE= "complexLigature";
	
	private Document theDocument;
	MDCDOMBuilderAux visitor;

	public MDCDOMBuilder() {
		visitor = new MDCDOMBuilderAux();
	}

	public Document buildMDCDOM(TopItemList l) {
		theDocument = null;
		visitor.init();
		l.accept(visitor);
		return theDocument;
	}

	private class MDCDOMBuilderAux implements ModelElementVisitor {
		DocumentBuilder documentBuilder;
		Node parent;

		public MDCDOMBuilderAux() {
			try {
				DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();
				documentBuilder = factory.newDocumentBuilder();
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}

		public void init() {
			theDocument = documentBuilder.newDocument();
			parent = theDocument;
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			Element node = createModelElement(ALPHABETIC_TEXT, t);
			setText(node, t.getText().toString());
			parent.appendChild(node);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			createContainerElement(BASIC_ITEM_LIST, l);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			Element e = createContainerElement(CADRAT, c);
			e.setAttribute(SHADING, "" + c.getShading());
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
		 */
		public void visitCartouche(Cartouche c) {
			Element node = createContainerElement(CARTOUCHE, c);

			node.setAttribute(START_PART, "" + c.getStartPart());
			node.setAttribute(END_PART, "" + c.getEndPart());
			node.setAttribute(TYPE, "" + c.getType());
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
		 */
		public void visitHBox(HBox b) {
			Element node = createContainerElement(HBOX, b);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 */
		public void visitHieroglyph(Hieroglyph h) {
			Node oldParent = parent;
			Element node = createModelElement(HIEROGLYPH, h);
			node.setAttribute(ENDINGCODE, "" + h.getEndingCode());
			node.setAttribute(GRAMMAR, "" + h.isGrammar());
			node.setAttribute(TYPE, "" + h.getType());
			parent = node;
			h.getModifiers().accept(this);
			setText(node, h.getCode());
			parent = oldParent;
		}

		public void visitHRule(HRule h) {
			Element node = createModelElement(HRULE, h);
			node.setAttribute(START_POS, "" + h.getStartPos());
			node.setAttribute(END_POS, "" + h.getEndPos());
			node.setAttribute(TYPE, "" + h.getType());
		}

		public void visitLigature(Ligature l) {
			Element e = createContainerElement(LIGATURE, l);
		}

		public void visitAbsoluteGroup(AbsoluteGroup g) {
			Element e = createContainerElement(ABSOLUTE, g);
		}

		public void visitModifier(Modifier mod) {
			// The parent for a modifier is always a hieroglyph Element. So we cast:
			Element e = (Element) parent;
			// Modifiers's names are supposed to be strings, except for "?".
			if ("?".equals(mod.getName())) {
				e.setAttribute(UNSURE, "" + true);
			} else {
				if (mod.getValue() == null)
					e.setAttribute(mod.getName(), "" + true);
				else
					e.setAttribute(
						mod.getName(),
						"" + mod.getValue().toString());
			}
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
		 */
		public void visitModifierList(ModifiersList l) {
			//			The parent for a modifier is always a hieroglyph Element. So we cast:
			Element e = (Element) parent;
			if (l.getAngle() != 0)
				e.setAttribute(ANGLE, "" + l.getAngle());
			if (l.isReversed())
				e.setAttribute(REVERSED, "" + true);
			if (l.getScale() != 100)
				e.setAttribute(SCALE, "" + l.getScale());
			ModelElementIterator i = l.getModelElementIterator();
			while (i.hasNext()) {
				ModelElement subElement = i.next();
				subElement.accept(this);
			}

		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
		 */
		public void visitOverwrite(Overwrite o) {
			Element node = createContainerElement(OVERWRITE, o);

		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
		 */
		public void visitPhilology(Philology p) {
			Element e = createContainerElement(PHILOLOGY, p);
			e.setAttribute(TYPE, "" + p.getType());
		}

		public void visitComplexLigature(ComplexLigature ligature) {
			Node oldParent = parent;
			Element e= createModelElement(COMPLEXLIGATURE,ligature); 
				//createContainerElement(COMPLEXLIGATURE, ligature);
			if (ligature.getBeforeGroup() != null) {
				Element subNode = theDocument.createElement("beforeGroup");
				e.appendChild(subNode);
				parent= subNode;
				ligature.getBeforeGroup().accept(this);
			}
			
			Element subNode = theDocument.createElement("mainSign");
			e.appendChild(subNode);
			parent= subNode;
			ligature.getHieroglyph().accept(this);
			
			if (ligature.getAfterGroup() != null) {
				Element subNode1 = theDocument.createElement("afterGroup");
				e.appendChild(subNode1);
				parent= subNode1;
				ligature.getAfterGroup().accept(this);	
			}
			parent= oldParent;
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
		 */
		public void visitSubCadrat(SubCadrat c) {
			Element e = createContainerElement(SUBCADRAT, c);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
		 */
		public void visitSuperScript(Superscript s) {
			Element node = createModelElement(SUPERSCRIPT, s);
			setText(node, s.getText().toString());
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
		 */
		public void visitTabStop(TabStop t) {
			Element node = createModelElement(TABSTOP, t);
			// or setValue ???
			// Yet tab stops are non printing elements.
			node.setAttribute(VALUE, "" + t.getStopPos());
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitTopItemList(jsesh.mdc.model.TopItemList)
		 */
		public void visitTopItemList(TopItemList t) {
			Element e = createContainerElement(TOP_ITEM_LIST, t);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
		 */
		public void visitLineBreak(LineBreak b) {
			Element node = createModelElement(LINE_BREAK, b);
			node.setAttribute(SKIP, "" + b.getSpacing());

		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitPageBreak(jsesh.mdc.model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			Element node = createModelElement(PAGE_BREAK, b);
		}

		public Element createContainerElement(String title, ModelElement e) {
			// As all model element have an iterator, we simplify the task for all
			// those by using it.
			Node oldParent = parent;
			Element node = createModelElement(title, e);
			parent = node;
			ModelElementIterator i = e.getModelElementIterator();
			while (i.hasNext()) {
				ModelElement subElement = i.next();
				subElement.accept(this);
			}
			parent = oldParent;
			return node;
		}
//Fix the attributes that are common to all model elements
		/**
		 * Create an element.
		 * @param elt
		 * @param name
		 * @return the element created.
		 */

		private Element createModelElement(String name, ModelElement elt) {
			Element node = theDocument.createElement(name);
			//node.setAttribute(FILL_X, "" + elt.isFillX());
			//node.setAttribute(FILL_Y, "" + elt.isFillY());
			parent.appendChild(node);
			return node;
		}

		private void setText(Node n, String t) {
			Text e = theDocument.createTextNode(t);
			n.appendChild(e);
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitZoneStart(jsesh.mdc.model.ZoneStart)
		 */
		public void visitZoneStart(ZoneStart start) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitOptionList(jsesh.mdc.model.OptionsMap)
		 */
		public void visitOptionList(OptionsMap list) {
			// TODO Auto-generated method stub
			
		}
		
		public void visitTabbing(Tabbing tabbing) {
			// TODO Auto-generated method stub
			
		}
		
		public void visitTabbingClear(TabbingClear tabbingClear) {
			// TODO Auto-generated method stub
			
		}

	}

}
