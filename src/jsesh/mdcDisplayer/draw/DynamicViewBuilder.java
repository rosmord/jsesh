/*
 * Created on 17 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdcDisplayer.draw;

import java.util.Observable;
import java.util.Observer;

/**
 * This class tracks modifications in the model 
 * and creates or deletes views elements accordingly.
 * @author rosmord
 */
public class DynamicViewBuilder implements Observer {

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	/*
	 * We shall receive a message from the modified TopItemList.
	 * This message should be specific enough so that we can act accordingly.
	 */
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
