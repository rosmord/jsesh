/*
 * @(#)JFileURIChooser.java
 * 
 * Copyright (c) 2009-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.gui;

import java.io.File;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * JFileURIChooser.
 *
 * @author Werner Randelshofer
 * @version $Id: JFileURIChooser.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class JFileURIChooser extends JFileChooser implements URIChooser {

    
    public void setSelectedURI(URI uri) {
        setSelectedFile(new File(uri));
    }

    
    public URI getSelectedURI() {
        return getSelectedFile() == null ? null : getSelectedFile().toURI();
    }

    
    public JComponent getComponent() {
        return this;
    }
}
