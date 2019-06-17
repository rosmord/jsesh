/*
 * @(#)OSXAdapter.java
 *
 * Copyright (c) 2009-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 *
 * This class has been derived from class OSXAdapter 2.0 by Apple Inc.
 * http://developer.apple.com/mac/library/samplecode/OSXAdapter/listing3.html
 *
 * Original disclaimer:
 *
 * File: OSXAdapter.java
 *
 * Abstract: Hooks existing preferences/about/quit functionality from an
 * existing Java app into handlers for the Mac OS X application menu.
 * Uses a Proxy object to dynamically implement the
 * com.apple.eawt.ApplicationListener interface and register it with the
 * com.apple.eawt.Application object.  This allows the complete project
 * to be both built and run on any platform without any stubs or
 * placeholders. Useful for developers looking to implement Mac OS X
 * features while supporting multiple platforms with minimal impact.
 *
 * Version: 2.0
 *
 * Disclaimer: IMPORTANT:  This Apple software is supplied to you by
 * Apple Inc. ("Apple") in consideration of your agreement to the
 * following terms, and your use, installation, modification or
 * redistribution of this Apple software constitutes acceptance of these
 * terms.  If you do not agree with these terms, please do not use,
 * install, modify or redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, and
 * subject to these terms, Apple grants you a personal, non-exclusive
 * license, under Apple's copyrights in this original Apple software (the
 * "Apple Software"), to use, reproduce, modify and redistribute the Apple
 * Software, with or without modifications, in source and/or binary forms;
 * provided that if you redistribute the Apple Software in its entirety and
 * without modifications, you must retain this notice and the following
 * text and disclaimers in all such redistributions of the Apple Software.
 * Neither the name, trademarks, service marks or logos of Apple Inc.
 * may be used to endorse or promote products derived from the Apple
 * Software without specific prior written permission from Apple.  Except
 * as expressly stated in this notice, no other rights or licenses, express
 * or implied, are granted by Apple herein, including but not limited to
 * any patent rights that may be infringed by your derivative works or by
 * other works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND
 * OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 * MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED
 * AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 * STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright © 2003-2007 Apple, Inc., All Rights Reserved
 */
package org.jhotdraw_7_6.app.osx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Rewrite of OSXAdapter to use java.awt.desktop - will lead to a Windows port later.
 * TODO : move this code - it makes less sense now.
 * Revised by S. Rosmorduc
 * @author Werner Randelshofer
 * @version $Id: OSXAdapter.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class OSXAdapter {

    static Object macOSXApplication;

    /**
     * The action listener will be called when the application receives an Open
     * Application event from the Finder or another application. Usually this
     * will come from the Finder when a user double-clicks your application
     * icon. If there is any special code that you want to run when you user
     * launches your application from the Finder or by sending an Open
     * Application event from another application, include that code as part of
     * this handler. The Open Application event is sent after AWT has been loaded.
     */
    @Deprecated
    public static void setOpenApplicationHandler(ActionListener openHandler) {
        // NOP ?
        //setHandler(new OSXAdapter("handleOpenApplication", openHandler));
    }
    /**
     * Called when the application receives a Reopen Application event from the
     * Finder or another application. Usually this will come when a user clicks
     * on your application icon in the Dock. If there is any special code that
     * needs to run when your user clicks on your application icon in the Dock
     * or when a Reopen Application event is sent from another application,
     * include that code as part of this handler.
     */
    @Deprecated
    public static void setReOpenApplicationHandler(ActionListener reopenHandler) {
        //setHandler(new OSXAdapter("handleReOpenApplication", reopenHandler));
    }
    /**
     * The action listener will be called when the Quit menu item is selected
     * from the application menu.
     */
    public static void setQuitHandler(ActionListener quitHandler) {
        Desktop.getDesktop().setQuitHandler((e, response) -> quitHandler.actionPerformed(
                new ActionEvent(Desktop.getDesktop(), ActionEvent.ACTION_PERFORMED, "quit")       
        ));
    }
           
    /**
     * The action listener will be called when the user selects the About item
     * in the application menu.
     */
    public static void setAboutHandler(ActionListener aboutHandler) {
//        boolean enableAboutMenu = (aboutHandler != null);
//        if (enableAboutMenu) {
//            setHandler(new OSXAdapter("handleAbout", aboutHandler));
//        }
//        // If we're setting a handler, enable the About menu item by calling
//        // com.apple.eawt.Application reflectively
//        try {
//            Method enableAboutMethod = macOSXApplication.getClass().getDeclaredMethod("setEnabledAboutMenu", new Class[]{boolean.class});
//            enableAboutMethod.invoke(macOSXApplication, new Object[]{Boolean.valueOf(enableAboutMenu)});
//        } catch (Exception ex) {
//            System.err.println("OSXAdapter could not access the About Menu");
//            ex.printStackTrace();
//        }
    }

    /**
     * Pass this method an {@code ActionListener} equipped to
     * display application options.
     * They will be called when the Preferences menu item is selected from the
     * application menu.
     */
    public static void setPreferencesHandler(ActionListener prefsHandler) {
        Desktop.getDesktop().setPreferencesHandler(e -> {
            if (prefsHandler != null) {
                prefsHandler.actionPerformed(
                        new ActionEvent(macOSXApplication, ActionEvent.ACTION_PERFORMED, "")
                );
            }
        });
//        boolean enablePrefsMenu = (prefsHandler != null);
//        if (enablePrefsMenu) {
//            setHandler(new OSXAdapter("handlePreferences", prefsHandler));
//        }
//        // If we're setting a handler, enable the Preferences menu item by calling
//        // com.apple.eawt.Application reflectively
//        try {
//            Method enablePrefsMethod = macOSXApplication.getClass().getDeclaredMethod("setEnabledPreferencesMenu", new Class[]{boolean.class});
//            enablePrefsMethod.invoke(macOSXApplication, new Object[]{Boolean.valueOf(enablePrefsMenu)});
//        } catch (Exception ex) {
//            System.err.println("OSXAdapter could not access the Preferences Menu");
//            ex.printStackTrace();
//        }
    }

    /**
     * Pass this method an {@code ActionListener} equipped to
     * handle document events from the Finder.
     * Documents are registered with the Finder via the
     * CFBundleDocumentTypes dictionary in the application bundle's Info.plist.
     * <p>
     * The filename is passed as the {@code actionCommand}.
     */
    public static void setOpenFileHandler(ActionListener fileHandler) {
        System.err.println("ICI");
        Desktop.getDesktop().setOpenFileHandler(
                e -> JOptionPane.showMessageDialog(null,"Open files "+ e.getFiles())
        );
        Desktop.getDesktop().setOpenURIHandler(
                e -> JOptionPane.showMessageDialog(null,"Open URI "+ e.getURI())
        );
    }
    /**
     * Pass this method an {@code ActionListener} equipped to
     * handle document events from the Finder.
     * Documents are registered with the Finder via the
     * CFBundleDocumentTypes dictionary in the application bundle's Info.plist.
     * <p>
     * The filename is passed as the {@code actionCommand}.
     */
    public static void setPrintFileHandler(ActionListener fileHandler) {
//        setHandler(new OSXAdapter("handlePrintFile", fileHandler) {
//            // Override OSXAdapter.callTarget to send information on the
//            // file to be opened
//
//
//            public boolean callTarget(Object appleEvent) {
//                if (appleEvent != null) {
//                    try {
//                        Method getFilenameMethod = appleEvent.getClass().getDeclaredMethod("getFilename", (Class[]) null);
//                        String filename = (String) getFilenameMethod.invoke(appleEvent, (Object[]) null);
//                        targetAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, filename));
//                    } catch (Exception ex) {
//                    }
//                }
//                return true;
//            }
//        });
    }

}
