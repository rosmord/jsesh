/*
 * Created on 30 nov. 2004 by S. Rosmorduc
 * This file is distributed under the LGPL licence.
 */
package jsesh.swing.groupEditor;


/**
 * @author Serge Rosmorduc
 *
 * This class is distributed under the LGPL.
 */
public interface GroupEditorListener  {

    public void mouseClicked(GroupEditorEvent e) ;
    
    public void mouseEntered(GroupEditorEvent e);

    public void mouseExited(GroupEditorEvent e);

    public void mousePressed(GroupEditorEvent e) ;

    public void mouseReleased(GroupEditorEvent e) ;
   
    public void mouseDragged(GroupEditorEvent e) ;

    public void mouseMoved(GroupEditorEvent e) ;
}
