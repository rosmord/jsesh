package jsesh.mdcDisplayer.swing.editor;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Observable;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.Dialect;
import jsesh.mdc.model.ModelElementObserver;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;

/**
 * The model of a hieroglyphic text.
 * 
 * This is a wrapper above a TopItemList, which is the actual model. 
 * The advantages of using this class is that the text can be easily loaded, cleared, and so on.
 * 
 * TODO : express this class in terms of patterns, and modify it accordingly.
 * 
 * FIXME : currently, the Observable pattern used is misleading.
 * <p> For instance, the workflow and the Carets are all understood as Observers.
 * Now, the workflow uses the carets, and hence should be warned <em>after</em>
 * them, because some of them may be out of date at the time they are used.
 * 
 * <p> In a few cases, these caused a problem with the code of getCurrentMDCLine, but it's now fixed.
 * 
 * <p> The carets would probably deserve a better treatment (and we might use a more precise class than observer).
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 */

public class HieroglyphicTextModel extends Observable implements ModelElementObserver {

	private TopItemList model;

	// private HieroglyphicFontManager signsManager;
	private boolean philologyIsSign;
	
	private boolean debug;

	/**
	 * Is the model clean (i.e. has it been modified since last loaded or saved ?).
	 */
	
	private boolean clean;
	
	public HieroglyphicTextModel() {
		// TODO : we create an empty TopItemList. Is that right ?
		setTopItemList(new TopItemList());
		//signsManager = new HieroglyphicFontManager();
		philologyIsSign = false;
		debug = false;
		clean= true;
	}

	/**
	 * Returns the model.
	 * @return TopItemList
	 */
	public TopItemList getModel() {
		return model;
	}

	/**
	 * Sets the model.
	 * @param model The model to set
	 * 
	 */
	public void setTopItemList(TopItemList model) {
		// Detach the ancient model.
		if (this.model != null) {
			this.model.deleteObserver(this);
		}
		this.model = model;
		if (model != null) {
			model.addObserver(this);
		}
		setChanged();
		notifyObservers();
	}

	public void clear() {
		setTopItemList(new TopItemList());
	}

	public void readTopItemList(Reader in) throws MDCSyntaxError {
		
		// long start= System.currentTimeMillis();
		TopItemList l= createGenerator(Dialect.OTHERS).parse(in); 
		// System.err.println("model created in "+ (System.currentTimeMillis() - start));
		setTopItemList(l);
	}

	/**
	 * read data into the model.
	 * @param in
	 * @param dialect a MDC dialect identifier from Dialect 
	 * @throws MDCSyntaxError
	 * @see Dialect
	 */
	public void readTopItemList(Reader in, int dialect) throws MDCSyntaxError {
		
		// long start= System.currentTimeMillis();
		TopItemList l= createGenerator(dialect).parse(in); 
		// System.err.println("model created in "+ (System.currentTimeMillis() - start));
		setTopItemList(l);
	}
	

	public void setMDCCode(String text) throws MDCSyntaxError {
		StringReader r= new StringReader(text);
		readTopItemList(r);
	}
	
	
	private MDCParserModelGenerator createGenerator(int dialect) {
		MDCParserModelGenerator generator = new MDCParserModelGenerator(dialect);
		generator.setPhilologyAsSigns(philologyIsSign);
		generator.setDebug(debug);
		return generator;
	}
	/**
	 * Returns the debug.
	 * @return boolean
	 * 
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Returns the philologyIsSign.
	 * @return boolean
	 * 
	 */
	public boolean isPhilologyIsSign() {
		return philologyIsSign;
	}

	/**
	 * Sets the debug.
	 * @param debug The debug to set
	 * 
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Sets the philologyIsSign.
	 * @param philologyIsSign The philologyIsSign to set
	 * 
	 */
	public void setPhilologyIsSign(boolean philologyIsSign) {
		this.philologyIsSign = philologyIsSign;
	}


	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElementObserver#observedElementChanged(jsesh.mdc.model.operations.ModelOperation)
	 */
	public void observedElementChanged(ModelOperation operation) {
		setChanged();
		notifyObservers(operation);	
	}
	
	/**
	 * Build a list of topitems from a String.
	 * @param text
	 * @return the corresponding list of items.
	 * @throws MDCSyntaxError
	 */
	
	public List buildItems(String text) throws MDCSyntaxError {
		MDCParserModelGenerator generator= createGenerator(Dialect.OTHERS);
		StringReader r= new StringReader(text);
		TopItemList t= generator.parse(r);
		// We take advantage of the return value of removeTopItems here :
		return t.removeTopItems(0, t.getNumberOfChildren());
	}

	/**
	 * A simple method for adding text at a given place.
	 * Note that this method is really not the most efficient one. 
	 * It's quite error prone. Yet it allows a to write simple stuff fast.
	 * @param index
	 * @param mdcText
	 * @throws MDCSyntaxError
	 */
	public void insertMDCText(int index, String mdcText) throws MDCSyntaxError {
		List items= buildItems(mdcText);
		getModel().addAllAt(index, items);
	}

	/**
	 * Is the model clean (i.e. has it been modified since last loaded or saved ?).
	 */
	
	public boolean isClean() {
		return clean;
	}
	
	/**
	 * Sets the "clean" status of the text.
	 * @param clean
	 */
	public void setClean(boolean clean) {
		this.clean = clean;
	}

}
