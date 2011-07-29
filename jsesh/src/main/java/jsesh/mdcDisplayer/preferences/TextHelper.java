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
package jsesh.mdcDisplayer.preferences;

/**
 * Helper class for processing text transformation.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class TextHelper {
	/**
	 * Transform a mdc String into a String which can be displayed.
	 * @param mdcCode
	 * @param drawingSpecification the drawing specifications to use
	 * @return
	 */
	public static String getActualTransliterationString(String mdcCode, DrawingSpecification drawingSpecification) {
		if (drawingSpecification.isTranslitUnicode()) {
			StringBuilder builder= new StringBuilder();
			boolean nextIsUpper= false;
			for (int i= 0; i < mdcCode.length(); i++) {
				char c= mdcCode.charAt(i);			
				if (c == '^')
					nextIsUpper= true;
				else {
					String cDown, cUp;
					switch (c) {
					case 'A':
						//cUp= "\u0722";
						//cDown= "\u0723";
						cUp= "Ꜣ";
						cDown= "ꜣ";
						break;
					case 'i':
						if (drawingSpecification.getYodChoice() == YODChoice.U0313) {
							cUp= "I\u0313";
							cDown= "i\u0313";
						} else {
							cUp= "I\u0486";
							cDown= "i\u0486";
						}
						break;
					case 'a':
						//cUp="\u0724";
						//cDown= "\u0725";
						cUp="Ꜥ";
						cDown= "ꜥ";				
						break;
					case 'H':
						cUp="\u1e24";
						cDown= "\u1e25";						
						break;
					case 'x':
						cUp="\u1e2a";
						cDown= "\u1e2b";						
						break;
					case 'X':
						cUp="H\u0331";
						cDown= "\u1e96";
						break;
					case 'S':
						cUp="\u0160";
						cDown= "\u0161";
						break;
					case 'q':					
						if (drawingSpecification.isGardinerQofUsed()) {
							cUp= "\u1e32";
							cDown= "\u1e33";
						} else {
							cUp= "Q";
							cDown= "q";
						}
						break;
					case 'T':
						cUp= "\u1e6e";
						cDown= "\u1e6f";
						break;
					case 'D':
						cUp= "\u1e0e";
						cDown= "\u1e0f";
						break;
					default:
						cUp= ""+Character.toUpperCase(c);
						cDown= ""+c;
						break;
					}
					if (nextIsUpper) 
						builder.append(cUp);
					else
						builder.append(cDown);
					nextIsUpper= false;
				}
			}
			return builder.toString();
		} else {
			// We don't do capitals in our mdc font.
			return mdcCode.replace("^", "");
		}
	}
}
