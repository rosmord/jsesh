package org.qenherkhopeshef.guiFramework.demo.simple2;

/**
 * A new demo software for our framework.
 * 
 * <h3> Principles :</h3>
 * <p> we have a number of layers. First, the application. Its purpose is to deal with all shared resources, to provide stuff like shared palette, and to create 
 * document editors.
 * 
 *  <p> Then, there is the document editor itself (which we will call the editing session). In principle, we might have multiple views for a session. 
 *  
 *  <p> The gui philosophy will be taken from the mac (mostly). That is, we will have multiple independant windows (and, on the mac, a mock window to display when no document is 
 *   opened).
 * 
 *  <p> Each window has its menu bar, and <b> all menu bars are identical ; </b>
 *  as a result, we need to keep a menu generator somewhere. Sharing a number of menu resources (graphics) is desirable.
 *  
 *  <p> Actions might exist at the application level (create a new document, quit...) or at the window level. 
 * @author rosmord
 *
 */
public class SimpleDemo2 {

}
