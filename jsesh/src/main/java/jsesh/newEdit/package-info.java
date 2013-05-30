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
/**
 * Temporary package to test the use of the "new" view toolkit library.
 * <p> The goal is to have very loose coupling between model and view,
 * and in particular :
 * <ul>
 * <li> to be able to have more than one view object for a particular model item
 * (think of a latin text item, whose content might correspond to multiple views)
 * <li> to have model elements without views (currently, we use empty views for comments, for instance).
 * <li> to be able to have view elements without corresponding model elements (I would probably need to think about it).
 * <li> to be able to explicitely manage selection areas. The idea is that we double the view with selector zones, which are 
 * really the active areas. Then, interaction with the page is interaction with the selector zones. (later ??)
 * </ul>
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
package jsesh.newEdit;