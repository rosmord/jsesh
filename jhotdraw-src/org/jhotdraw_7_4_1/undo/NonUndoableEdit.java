/*
 * @(#)NonUndoableEdit.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw_7_4_1.undo;

import javax.swing.undo.*;
/**
 * NonUndoableEdit.
 *
 * @author  Werner Randelshofer
 * @version $Id: NonUndoableEdit.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class NonUndoableEdit extends AbstractUndoableEdit {
    
    /** Creates a new instance. */
    public NonUndoableEdit() {
    }
    
    public boolean canUndo() {
        return false;
    }
    public boolean canRedo() {
        return false;
    }
}
