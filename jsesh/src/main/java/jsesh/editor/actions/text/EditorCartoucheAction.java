/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.swing.utils.ImageIconFactory;

/**
 * Action for adding a cartouche to a JSesh editor.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class EditorCartoucheAction extends EditorAction {

	public static final String ID = "text.addCartouche_";
	private int end;

	private int start;

	private int type;

	public EditorCartoucheAction(JMDCEditor editor, int type, int start,
			int end, String mdcText) {
		super(editor);
		this.type = type;
		this.start = start;
		this.end = end;
		putValue(Action.SMALL_ICON, 
                        ImageIconFactory.getInstance().buildImage(mdcText));
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().addCartouche(type, start, end);
	}

	/**
	 * 
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 *
	 */
	private static class CartoucheParameters {
		char c;
		int start;
		int end;
		String sample;

		public CartoucheParameters(char c, int start, int end, String sample) {
			super();
			this.c = c;
			this.start = start;
			this.end = end;
			this.sample = sample;
		}
	}

	private static CartoucheParameters[] allCartouches = {
			new CartoucheParameters('c', 1, 2, "<-ra-mn:n-xpr\\R270->"),
			new CartoucheParameters('c', 1, 1, "<1-ra-mn:n-xpr\\R270-1>"),
			new CartoucheParameters('c', 2, 1, "<2-ra-mn:n-xpr\\R270-1>"),
			new CartoucheParameters('c', 2, 1, "<2-ra-mn:n-xpr\\R270-1>"),
			new CartoucheParameters('c', 0, 1, "<0-ra-mn:n-xpr\\R270-1>"),
			new CartoucheParameters('c', 1, 0, "<1-ra-mn:n-xpr\\R270-0>"),
			new CartoucheParameters('c', 2, 0, "<2-ra-mn:n-xpr\\R270-0>"),
			new CartoucheParameters('c', 0, 2, "<0-ra-mn:n-xpr\\R270-2>"),
			new CartoucheParameters('s', 1, 2, "<s-E1:D40-xa:M-R19->"),
			new CartoucheParameters('s', 2, 1, "<s2-E1:D40-xa:M-R19-s1>"),
			new CartoucheParameters('h', 1, 2, "<h1-ra-xa-f-h2>"),
			new CartoucheParameters('h', 1, 3, "<h1-ra-xa-f-h3>"),
			new CartoucheParameters('h', 1, 1, "<h1-ra-xa-f-h1>"),
			new CartoucheParameters('h', 1, 0, "<h1-ra-xa-f-h0>"),
			new CartoucheParameters('h', 2, 1, "<h2-ra-xa-f-h1>"),
			new CartoucheParameters('h', 2, 0, "<h2-ra-xa-f-h0>"),
			new CartoucheParameters('h', 3, 1, "<h3-ra-xa-f-h1>"),
			new CartoucheParameters('h', 3, 0, "<h3-ra-xa-f-h0>"),
			new CartoucheParameters('h', 0, 2, "<h0-ra-xa-f-h2>"),
			new CartoucheParameters('h', 0, 3, "<h0-ra-xa-f-h3>"),
			new CartoucheParameters('h', 0, 1, "<h0-ra-xa-f-h1>"),
			new CartoucheParameters('h', 0, 0, "<h0-ra-xa-f-h0>"),
			new CartoucheParameters('F', 1, 2, "<F-ra-xa-f->") };

	public final static String[] actionNames = new String[allCartouches.length];

	static {
		for (int i = 0; i < allCartouches.length; i++) {
			CartoucheParameters c = allCartouches[i];
			actionNames[i] = ID + c.c + "_" + c.start + "_" + c.end;
		}
	}

	/**
	 * create a action map for a given editor, containing all cartouche-related actions.
	 * @param editor
	 * @return an action map
	 */
	public static Map<String, Action> generateActionMap(JMDCEditor editor) {
		TreeMap<String, Action> map = new TreeMap<String, Action>();
		for (int i = 0; i < allCartouches.length; i++) {
			CartoucheParameters c = allCartouches[i];
			map.put(actionNames[i], new EditorCartoucheAction(editor, c.c,
					c.start, c.end, c.sample));
		}
		return map;
	}

	/**
	 * Can be called to preload cartouches pictures before the program is
	 * started.
	 * <p> 
	 */
	public static void preloadCartoucheIcons() {
		for (int i = 0; i < allCartouches.length; i++) {
			CartoucheParameters c = allCartouches[i];
			ImageIconFactory.getInstance().buildImage(c.sample);
		}
	}
}