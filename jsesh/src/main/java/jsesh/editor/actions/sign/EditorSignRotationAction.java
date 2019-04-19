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
package jsesh.editor.actions.sign;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.swing.utils.ImageIconFactory;

/**
 * Action for rotating a sign.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class EditorSignRotationAction extends EditorAction {

	public static final String ID = "sign.signRotate_";

	private int angle;

	public EditorSignRotationAction(JMDCEditor editor, int angle) {
		super(editor);
		putValue(NAME, angle + "°");
		String mdcText= "A\\R" + angle;
		putValue(Action.SMALL_ICON, ImageIconFactory.getInstance().buildImage(mdcText));
		this.angle= angle;
	}

	
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().setAngle(angle);
	}

	
	private static class RotationInfo implements Comparable<RotationInfo>{
		int angle;
		public String id;
		public RotationInfo(int angle) {
			super();
			this.angle = angle;
			this.id=  id+ angle;
		}
		
		public int compareTo(RotationInfo o) {
			return this.angle - o.angle;
		}		
	}
	
	private static final RotationInfo[] rotationInfos= prepareRotationInfo();
	
	private static RotationInfo[] prepareRotationInfo() {
		TreeSet<Integer> angles= new TreeSet<Integer>();
		for (int a= 0; a < 360; a= a+30) {
			angles.add(a);
		}
		for (int a= 0; a < 360; a= a+45) {
			angles.add(a);
		}
		RotationInfo [] result= new RotationInfo[angles.size()];
		int i= 0;
		for (int a: angles) {
			result[i++]= new RotationInfo(a);
		}
		return result;
	}
	
	public static Map<String, Action> generateActionMap(JMDCEditor editor) {
		TreeMap<String, Action> map = new TreeMap<String, Action>();
		for (RotationInfo info: rotationInfos) {
			map.put(info.id,		new EditorSignRotationAction(editor, info.angle));
		}
		return map;
	}
	
	public static List<String> getActionNames() {
		ArrayList<String> actionNames= new ArrayList<String>();
		for (RotationInfo rotationInfo: rotationInfos) {
			actionNames.add(rotationInfo.id);
		}
		return actionNames;
	}
}