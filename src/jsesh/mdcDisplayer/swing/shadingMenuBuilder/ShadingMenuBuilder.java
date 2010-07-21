package jsesh.mdcDisplayer.swing.shadingMenuBuilder;

import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import jsesh.mdc.model.ShadingCode;


public abstract class ShadingMenuBuilder {

	public Action[] buildShadeActions() {
		Action []shadeActions = new Action[16];
		shadeActions[0] = buildAction(ShadingCode.NONE, "..");
		shadeActions[1] = buildAction(ShadingCode.TOP_START, "..#1");
		shadeActions[2] = buildAction(ShadingCode.TOP_END, "..#2");
		shadeActions[3] = buildAction(ShadingCode.TOP_END | ShadingCode.TOP_START,
				"..#12");
		shadeActions[4] = buildAction(ShadingCode.BOTTOM_START, "..#3");
		shadeActions[5] = buildAction(ShadingCode.BOTTOM_START
				| ShadingCode.TOP_START, "..#13");
		shadeActions[6] = buildAction(ShadingCode.BOTTOM_START | ShadingCode.TOP_END,
				"..#23");
		shadeActions[7] = buildAction(ShadingCode.BOTTOM_START | ShadingCode.TOP_END
				| ShadingCode.TOP_START, "..#123");
		shadeActions[8] = buildAction(ShadingCode.BOTTOM_END, "..#4");
		shadeActions[9] = buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_START,
				"..#14");
		shadeActions[10] = buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_END,
				"..#24");
		shadeActions[11] = buildAction(ShadingCode.BOTTOM_END | ShadingCode.TOP_END
				| ShadingCode.TOP_START, "..#124");
		shadeActions[12] = buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START, "..#34");
		shadeActions[13] = buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_START, "..#134");
		shadeActions[14] = buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_END, "..#234");
		shadeActions[15] = buildAction(ShadingCode.BOTTOM_END
				| ShadingCode.BOTTOM_START | ShadingCode.TOP_END | ShadingCode.TOP_START,
				"..#1234");
		return shadeActions;
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
