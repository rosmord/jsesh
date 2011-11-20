package jsesh.swing.shadingMenuBuilder;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import jsesh.mdc.model.ShadingCode;


public abstract class ShadingMenuBuilder {

	public Action[] buildShadeActions() {
		ArrayList<Action> shadeActions= new ArrayList<Action>();
		shadeActions.add(buildAction(ShadingCode.NONE, ".."));
		shadeActions.add(buildAction(ShadingCode.TOP_START, "..#1"));
		shadeActions.add(buildAction(ShadingCode.TOP_END, "..#2"));
		shadeActions.add(buildAction(ShadingCode.TOP_END | ShadingCode.TOP_START,
				"..#12"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_START, "..#3"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_START
				| ShadingCode.TOP_START, "..#13"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_START | ShadingCode.TOP_END,
				"..#23"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_START | ShadingCode.TOP_END
				| ShadingCode.TOP_START, "..#123"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END, "..#4"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_START,
				"..#14"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_END,
				"..#24"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_END
				| ShadingCode.TOP_START, "..#124"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START, "..#34"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_START, "..#134"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_END, "..#234"));
		shadeActions.add(buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_END | ShadingCode.TOP_START,
				"..#1234"));
		return shadeActions.toArray(new Action[shadeActions.size()]);
	}

	/**
	 * Build a shading action
	 * @param shadingCode : information about the shading,expressed as combination of ShadingCodes
	 * @param string
	 * @see ShadingCode
	 * @return
	 */
	protected abstract Action buildAction(int shadingCode, String string);

	public JPopupMenu buildPopup() {
		JPopupMenu popup= new JPopupMenu();
		
		
	
		popup.setLayout(new GridLayout(0, 4));
		Action[] actions = buildShadeActions();
		for (int i= 0; i < actions.length; i++) {
			popup.add(actions[i]);
		}
		return popup;
	}
}
