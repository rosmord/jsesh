package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.splash.SplashScreen;

import com.apple.eawt.Application;

/**
 * "Dirty" application. 
 * We create it to explore the technical problem,
 * then we will refactor and generalize it.
 * 
 * Goals : a multi-platform application.
 * On mac: an hidden window will allow one to create new files.
 * On Pc/Linux: the application will quit if no window is opened.
 *
 * Version of the Main for the macintosh.
 * @author rosmord
 *
 */
public class MainMac {

    private SplashScreen splashScreen = null;

    public void start() {
        try {
            splashScreen = new SplashScreen("/org/qenherkhopeshef/guiFrameWork/demo/nicePicture.png");
            splashScreen.display();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    buildApp();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void buildApp() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");

        TextEditorApplicationController textEditorApplicationController = new TextEditorApplicationController();

        Application application = new Application();
        application.addApplicationListener(new MainMacApplicationAdapter(textEditorApplicationController));
        textEditorApplicationController.getMacEmptyWindow().addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                splashScreen.closeSplash();
            }
        });

        textEditorApplicationController.getMacEmptyWindow().setVisible(true);
    }

    public static void main(String[] args) {
        MainMac mainMac = new MainMac();
        mainMac.start();
    }
}
