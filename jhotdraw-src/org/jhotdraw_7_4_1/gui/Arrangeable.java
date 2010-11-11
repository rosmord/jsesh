/*
 * @(#)Arrangeable.java
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

package org.jhotdraw_7_4_1.gui;

import java.beans.*;
/**
 * Arrangeable.
 * 
 * 
 * @author Werner Randelshofer
 * @version $Id: Arrangeable.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public interface Arrangeable {
    enum Arrangement { VERTICAL, HORIZONTAL, CASCADE };
    
    public void setArrangement(Arrangement newValue);
    public Arrangement getArrangement();
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);
}
