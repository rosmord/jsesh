package jsesh.jhotdraw.utils;

import jsesh.jhotdraw.documentview.JSeshViewCore;

/**
 * A class which has access to the active view.
 * 
 */
public interface ActiveViewProvider {
   
        /**
        * Do we have an active view ?
        * @return 
        */
        public boolean hasActiveView();

        /**
         * Get the active view.
         * @return the active view or null.
         */
        JSeshViewCore activeView();
}
