/*
 * @(#)PrintableView.java
 *
 * Copyright (c) 2007 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw_7_4_1.app;

import java.awt.print.*;

/**
 * The interface of a {@link View} which can print its document.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p><em>Framework</em><br>
 * The interfaces and classes listed below define together the contracts
 * of a smaller framework inside of the JHotDraw framework for document oriented
 * applications.<br>
 * Contract: {@link PrintableView}, {@link org.jhotdraw_7_4_1.app.action.file.PrintFileAction}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id: PrintableView.java 594 2009-12-21 23:32:52Z rawcoder $
 */
public interface PrintableView extends View {
public Pageable createPageable();   
}
