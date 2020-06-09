/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.data.SimpleHieroglyphDatabase;
import jsesh.hieroglyphs.data.ManuelDeCodage;
import jsesh.mdc.constants.Dialect;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.WordEndingCode;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementVisitor;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdc.model.OptionsMap;
import jsesh.mdc.model.Overwrite;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.ShadingCode;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.Tabbing;
import jsesh.mdc.model.TabbingClear;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.TopItemState;
import jsesh.mdc.model.ZoneStart;

/**
 * A MdCModelWriter knows how to write a MDCModel to a file, in the manuel de
 * codage encoding.
 * 
 * @author rosmord
 */

public class MdCModelWriter {
	private Writer out;
	private int startIndex;
	private int endIndex;
	private Dialect inDialect= Dialect.JSESH1;
	private Dialect outDialect= Dialect.JSESH1;
	private boolean normalized= false;

	public MdCModelWriter() {
		out = null;
	}

	public void write(String fileName, TopItemList top) throws IOException {
		write(new File(fileName), top);
	}

	public void write(File f, TopItemList top) throws IOException {
		if (f.getName().endsWith(".hie"))
			setOutDialect(Dialect.TKSESH);
		else if (f.getName().endsWith(".gly"))
			setOutDialect(Dialect.JSESH1);
		Writer w = new FileWriter(f);
		write(w, top);
		w.close();
	}
	
	/**
	 * Write the model to the writer.
	 * <p>
	 * Note that this method doesn't close the writer ! (thus one can write
	 * more than one model to a stream). You should then call close
	 * explicitely. An easier alternative is to call write(File, top) or
	 * write(String, top).
	 * 
	 * @param out
	 * @param top
	 */
	public void write(Writer out, TopItemList top) {
		checkDialectCompatibility();
		write(out, top, 0, top.getNumberOfChildren());
	}
	
	private void checkDialectCompatibility() {
		if (! inDialect.equals(outDialect)) {
			if (outDialect.equals(Dialect.TKSESH)) {
				throw new MdcDialectConversionException("Can't convert from "+ inDialect + " into " + outDialect);
			}
		}
	}
	
	class ModelWriterAux implements ModelElementVisitor {
		
		/**
		 * avoid writing two minus signs after |....-
		 */
		boolean wantsMinus= true;

		private void write(String s) {
			try {
				out.write(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void write(char s) {
			try {
				out.write(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void visitElementList(ModelElement elt, String separator) {
			for (int i = 0; i < elt.getNumberOfChildren(); i++) {
				ModelElement sub = elt.getChildAt(i);
				if (i > 0)
					write(separator);
				sub.accept(this);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			write("+" + t.getScriptCode());
			String text = t.getText().toString();
			text = text.replaceAll("\\\\", "\\\\");
			text = text.replaceAll("\\+", "\\\\+");
			write(text);
			write("+s");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			visitElementList(l, "-");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			visitElementList(c, ":");
			write(ShadingCode.toString("#", c.getShading()));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
		 */
		public void visitCartouche(Cartouche c) {
			String cartoucheCode = "";
			String start;
			String end;
			if (c.getType() != 'c')
				cartoucheCode = "" + (char) c.getType();
			if (c.getStartPart() == 1 && c.getEndPart() == 2) {
				start = "<" + cartoucheCode.toUpperCase() + "-";
				end = "->";
			} else {
				start = "<" + cartoucheCode + c.getStartPart() + "-";
				end = "-" + cartoucheCode+ c.getEndPart() + ">";
			}
			write(start);
			c.getBasicItemList().accept(this);
			write(end);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
		 */
		public void visitHBox(HBox b) {
			visitElementList(b, "*");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 */
		public void visitHieroglyph(Hieroglyph h) {
			boolean explicitPosition = false;
			if (h.isGrammar())
				write("=");
			//		grammar
			// Well I would write something for SMALLTEXT, but it already works !
			String code;
			if (normalized)
				code=	HieroglyphDatabaseRepository.getHieroglyphDatabase().getCanonicalCode(h.getCode());
			else
				code= h.getCode();
 
			write(code);

			ModifiersList l = h.getModifiers();
			//		Inversion
			if (l.isReversed())
				write("\\");

			//		scaling
			// scaling will be written with the explicit positioning if
			// specified.
			if (h.getX() != 0 || h.getY() != 0)
				explicitPosition = true;
			if (!explicitPosition && l.getScale() != 100)
				write("\\" + l.getScale());
			//		Rotation
			if (l.getAngle() != 0)
				write("\\R" + l.getAngle());
			visitModifierList(l);

			if (explicitPosition) {
				write("{{");
				write("" + h.getX() + "," + h.getY() + "," + l.getScale());
				write("}}");
			}
			// ending
			if (h.getEndingCode().equals(WordEndingCode.WORD_END)) {
				write("_");
			} else if (h.getEndingCode().equals(WordEndingCode.SENTENCE_END)) {
				write("__");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
		 */
		public void visitHRule(HRule h) {
			write("{");
			write(h.getType());
			write("" + h.getStartPos());
			write("," + h.getEndPos() + "}");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
		 */
		public void visitLigature(Ligature l) {
			visitElementList(l, "&");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
		 */
		public void visitModifier(Modifier mod) {
			write('\\');
			if (mod.getName() != null)
				write(mod.getName());
			if (mod.getValue() != null)
				write(mod.getValue().toString());

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
		 */
		public void visitModifierList(ModifiersList l) {
			for (int i = 0; i < l.getNumberOfChildren(); i++) {
				visitModifier(l.getModifierAt(i));
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
		 */
		public void visitOverwrite(Overwrite o) {
			o.getFirst().accept(this);
			write("##");
			o.getSecond().accept(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
		 */
		public void visitPhilology(Philology p) {
			String open = LexicalSymbolsUtils.getOpenCodeForPhilology(p
					.getType());
			String close = LexicalSymbolsUtils.getCloseCodeForPhilology(p
					.getType());
			if (outDialect.isEditorialMarksAsSign()) {
				write('(');
			}
			write(open);
			write('-');
			visitElementList(p, "-");
			write('-');
			write(close);
			if (outDialect.isEditorialMarksAsSign()) {
				write(')');
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
		 */
		public void visitSubCadrat(SubCadrat c) {
			write("(");
			visitElementList(c, "-");
			write(")");

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
		 */
		public void visitSuperScript(Superscript s) {
			String txt= s.getText();
			// protect txt :
			txt= txt.replaceAll("\\\\", "\\\\\\\\").replaceAll("-", "\\\\-");
			// done...
			write("|" + txt + "-");
			wantsMinus= false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
		 */
		public void visitTabStop(TabStop t) {
			write("?" + t.getStopPos());
		}

		public void visitTabbing(Tabbing tabbing) {
			write("%[");
			write("id="+ tabbing.getId()+",");
			write("orientation="+ tabbing.getOrientation().name().toLowerCase()+",");
			write("justification="+ tabbing.getTabbingJustification().name().toLowerCase()+"]");
		}
		
		public void visitTabbingClear(TabbingClear tabbingClear) {
			write("%clear");
		}
		/**
		 * Writes the top item to the output. ABout toggles : HieroTeX won't
		 * like the way we deal with them, but then a) HieroTeX user know how
		 * to correct syntax errors ; and b) I will write a java port of
		 * hierotex anyway.
		 * @param t
		 */
		public void visitTopItemList(TopItemList t) {
			TopItemState state = new TopItemState();
			int virtualpos = 0;
			// Was the last opening red toggle closer than the last opening gray
			// toggle ?
			boolean redCloser = false;

			for (int i = startIndex; i < endIndex; i++) {
				TopItem sub = t.getTopItemAt(i);
				TopItemState newState = sub.getState();

				if (i > startIndex && wantsMinus)
					write('-');
				wantsMinus= true;
				// For reasons of compatibility with HieroTeX, we close all
				// toggles at line or page breaks :
				if (sub instanceof LineBreak || sub instanceof PageBreak)
					newState = new TopItemState();

				redCloser = fixToggles(state, newState, redCloser);

				sub.accept(this);
				state = newState;
			}
			fixToggles(state, new TopItemState(), redCloser);
		}

		/**
		 * Avoid toggle crossing.
		 * 
		 * @param state
		 * @param newState
		 * @param redCloser
		 * @return true if a red toggle was closer than a gray toggle.
		 * TODO : perhaps move the whole "closer" business to fields of this class.
		 */
		private boolean fixToggles(TopItemState state, TopItemState newState,
				boolean redCloser) {
			// see if a toggle is needed.
			// we don't want closing toggles to cross.

			if (newState.isRed() != state.isRed()
					|| newState.isShaded() != state.isShaded()) {
				boolean closeGray = !newState.isShaded() && state.isShaded();
				boolean openGray = newState.isShaded() && !state.isShaded();
				boolean closeRed = !newState.isRed() && state.isRed();
				boolean openRed = newState.isRed() && !state.isRed();

				// Close before opening :
				if (closeGray && closeRed) {
					if (redCloser) {
						write("$b-#e-");

					} else {
						write("#e-$b-");
					}
				} else if (closeGray) {
					// Avoid crossing :
					if (state.isRed() && redCloser) {
						write("$b-#e-$r-");
						redCloser = true;
					} else {
						write("#e-");
					}
				} else if (closeRed) {
					if (state.isShaded() && !redCloser) {
						write("#e-$b-#b-");
						redCloser = false;
					} else {
						write("$b-");
					}
				}

				// Note that we won't write #b or $r twice,
				// as $r, for instance, is only written when state.isRed(),
				// which means openRed is false !
				if (openGray) {
					write("#b-");
					redCloser = false;
				}

				if (openRed) {
					write("$r-");
					redCloser = true;
				}
			}
			return redCloser;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
		 */
		public void visitLineBreak(LineBreak b) {
			write("!");
			if (b.getSpacing() != 100)
				write("=" + b.getSpacing() + "%");
			write("\n");
			wantsMinus= false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitPageBreak(jsesh.mdc.model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			write("!!\n");
			wantsMinus= false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementVisitor#visitAbsoluteGroup(jsesh.mdc.model.AbsoluteGroup)
		 */
		public void visitAbsoluteGroup(AbsoluteGroup g) {
			// TODO : generate "&&" if the correct option is selected.
			//visitElementList(g, "&&");
			visitElementList(g, "**");
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitZoneStart(jsesh.mdc.model.ZoneStart)
		 */
		public void visitZoneStart(ZoneStart start) {
			// TODO : finish this !!!
			write("zone");
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementVisitor#visitOptionList(jsesh.mdc.model.OptionsMap)
		 */
		public void visitOptionList(OptionsMap list) {
			// TODO Auto-generated method stub
			
		}

		public void visitComplexLigature(ComplexLigature ligature) {
			// TODO finish this and propose "&&" and "^^" as operators if possible (according to some switch)
			// TODO CHANGE THE GRAMMAR TO AVOID PROBLEMS WITH ASSOCIATIVITY AND PARENTHESIS.
			// LIGATURES SHOULD NOT BE INNERGROUPS... OR AT LEAST THEIR SECOND PART SHOULD NOT BE...
			InnerGroup before= ligature.getBeforeGroup();
			InnerGroup after= ligature.getAfterGroup();
			
			if (before != null) {
				before.accept(this);
				write("^^^");
			} 
			ligature.getHieroglyph().accept(this);
			if (after != null) {
				write("&&&");
				after.accept(this);
			}
		}
	}

	/**
	 * write the elements between indexes a and b.
	 * @param w
	 * @param top
	 * @param a
	 * @param b
	 */
	public void write(Writer w, TopItemList top, int a, int b) {
		this.out = w;
		ModelWriterAux aux= new ModelWriterAux();
		startIndex= a;
		endIndex= b;
		top.accept(aux);
	}
	
		
	public void setInDialect(Dialect inDialect) {
		this.inDialect = inDialect;
	}
	
	public void setOutDialect(Dialect outDialect) {
		this.outDialect = outDialect;
	}
	
	/**
	 * Should we normalize the hieroglyph codes to their Gardiner form.
	 * @param normalized
	 */
	public void setNormalized(boolean normalized) {
		this.normalized = normalized;
	}

	/**
	 * Should we normalize the hieroglyph codes to their Gardiner form.
	 * @return true if signs are normalized. 
	  */
	public boolean isNormalized() {
		return normalized;
	}
}