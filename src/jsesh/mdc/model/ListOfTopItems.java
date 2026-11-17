/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple class for lists of top items.
 * This is not part of the model. However, we often need to transfer parts of TopItemLists,
 * and this class allows us to do it more easily than if we had to build mock-up TopItemLists
 * (which, besides, behave badly with serialization, as their graph is too complex).
 * @author rosmord
 *
 */
public class ListOfTopItems extends ArrayList implements Serializable {
	
}
