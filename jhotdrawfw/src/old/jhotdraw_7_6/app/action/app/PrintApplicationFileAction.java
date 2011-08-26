/*
 * @(#)OSXOpenFileAction.java
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
package org.jhotdraw_7_6.app.action.app;

import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.PrintableView;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.file.PrintFileAction;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.util.*;

/**
 * Prints a file for which a print request was sent to the application.
 * <p>
 * The file name is passed in the action command of the action event.
 * <p>
 * This action is called when {@code DefaultOSXApplication} receives a print
 * request from another application. The file name is passed in the action
 * command of the action event.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}. The views created by
 * {@code ApplicationModel} must implement the {@link PrintableView} interface.
 * <p>
 * You should also create a {@link PrintFileAction} when you create this action.
 *
 * @author  Werner Randelshofer
 * @version $Id: PrintApplicationFileAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class PrintApplicationFileAction extends PrintFileAction {

    public final static String ID = "application.printFile";
    private JFileChooser fileChooser;
    private int entries;

    /** Creates a new instance. */
    public PrintApplicationFileAction(Application app) {
        super(app, null);
        putValue(Action.NAME, "OSX Print File");
    }

    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        final String filename = evt.getActionCommand();
        View v = app.createView();
        if (!(v instanceof PrintableView)) {
            return;
        }
        final PrintableView p = (PrintableView) v;
        p.setEnabled(false);
        app.add(p);
//            app.show(p);
        p.execute(new Worker() {

            @Override
            public Object construct() throws IOException {
                p.read(new File(filename).toURI(), null);
                return null;
            }

            @Override
            protected void done(Object value) {
                p.setURI(new File(filename).toURI());
                p.setEnabled(false);
                if (System.getProperty("apple.awt.graphics.UseQuartz", "false").equals("true")) {
                    printQuartz(p);
                } else {
                    printJava2D(p);
                }
                p.setEnabled(true);
                app.dispose(p);
            }

            @Override
            protected void failed(Throwable value) {
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                app.dispose(p);
                JOptionPane.showMessageDialog(
                        null,
                        "<html>" + UIManager.getString("OptionPane.css")
                        + "<b>" + labels.getFormatted("file.open.couldntOpen.message", new File(filename).getName()) + "</b><p>"
                        + value,
                        "",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
