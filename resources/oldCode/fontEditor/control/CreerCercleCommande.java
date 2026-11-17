/*
 * Created on 5 mars 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.fontEditor.control;

import jsesh.fontEditor.model.Cercle;
import jsesh.fontEditor.model.GlyphModel;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CreerCercleCommande implements Command {

	private Cercle c;

	private GlyphModel d;

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#doCommande(simpledraw.sol3.GlyphModel)
	 */
	 
	public CreerCercleCommande(GlyphModel d, Cercle c) {
		this.c= c;
		this.d= d;
	}
	
	public void doCommande() {
		d.add(c);	
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#undoCommande(simpledraw.sol3.GlyphModel)
	 */
	public void undoCommande() {
		d.remove(c);
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#canUndo()
	 */
	public boolean canUndo() {
		return true;
	}
}

