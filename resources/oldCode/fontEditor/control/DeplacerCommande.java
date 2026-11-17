/*
 * Created on 5 mars 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.fontEditor.control;

import jsesh.fontEditor.model.Forme;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DeplacerCommande implements Command {
	
	private Forme f;
	
	private double dx, dy;

	public DeplacerCommande(Forme f, double dx, double dy) {
		this.f= f;
		this.dx= dx;
		this.dy= dy;
	}
	
	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#doCommande(simpledraw.sol3.GlyphModel)
	 */
	public void doCommande() {
		f.deplacer(dx,dy);	
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#undoCommande(simpledraw.sol3.GlyphModel)
	 */
	public void undoCommande() {
		f.deplacer(-dx, -dy);
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Command#canUndo()
	 */
	public boolean canUndo() {
		return true;
	}

}

