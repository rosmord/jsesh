/*
 * @(#)URIUtil.java
 * 
 * Copyright (c) 2009-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 * 
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_6.net;

import java.io.File;
import java.net.URI;

/**
 * URIUtil.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-12-16 Created.
 */
public class URIUtil {

    /** Prevent instance creation. */
    private void URIUtil() {
    }

    /** Returns the name of an URI for display in the title bar of a window. */
    public static String getName(URI uri) {
        if (uri.getScheme()!=null&&uri.getScheme().equals("file")) {
            return new File(uri).getName();
        }
        return uri.toString();
    }
}
