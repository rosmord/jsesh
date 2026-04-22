/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

import org.jhotdraw_7_6.app.Application;
import org.qenherkhopeshef.guiFramework.AppStartup;
import org.qenherkhopeshef.guiFramework.splash.SplashMessageText;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXApplication;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXLikeApplication;

import jsesh.JSeshUserSignLibraryConfiguration;
import jsesh.Version;
import jsesh.editor.actions.text.EditorCartoucheAction;
import jsesh.editor.actions.text.EditorShadeAction;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.HieroglyphFamily;
import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.jhotdraw.applicationPreferences.model.ApplicationUIPreferences;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.resources.ResourcesManager;
import jsesh.swing.utils.ImageIconFactory;
import net.miginfocom.layout.PlatformDefaults;

/**
 * JSeshMain class.
 *
 * @author Serge Rosmorduc.
 * @version $Id: JSeshMain.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class Main extends AppStartup<JSeshApplicationStartingData> {

    public final static String NAME = "JSesh";
    public final static String COPYRIGHT = "JSesh is CeCiLL Software (GPL-compatible) written by S. Rosmorduc";

    private String args[];

    /**
     * Preloading of data before starting the actual application.
     */
    @Override
    public JSeshApplicationStartingData initApplicationData() {
        // Prepare icon factory...
        ApplicationUIPreferences applicationPreferences = ApplicationUIPreferences.getFromPreferences();

        
        JSeshUserSignLibraryConfiguration applicationDefaults = new JSeshUserSignLibraryConfiguration();
        // Dirty architecture.
        // Pre-load a number of objects so that they are ready when graphic
        // stuff starts.
        ResourcesManager.getInstance();
        // Preload icons. Bad architecture.
        preloadHieroglyphicIcons(applicationDefaults); 

        JSeshApplicationStartingData data = new JSeshApplicationStartingData(applicationPreferences.getIconHeight(), applicationDefaults);        
        return data;
    }

    private void preloadHieroglyphicIcons(JSeshUserSignLibraryConfiguration applicationDefaults) { 
        HieroglyphDatabaseInterface database = applicationDefaults.hieroglyphDatabase();
        List<HieroglyphFamily> families = database.getFamilies();
        for (int i = 0; i < families.size(); i++) {
            HieroglyphFamily family = families.get(i);
            for (String code : ManuelDeCodage.getInstance()
                    .getBasicGardinerCodesForFamily(family.getCode())) {
                ImageIconFactory.getInstance().buildGlyphImage(code);
            }
        }
        EditorCartoucheAction.preloadCartoucheIcons();
        EditorShadeAction.preloadIcons();
    }

    @Override
    public void startApplication(JSeshApplicationStartingData data) {
        // Force creation of the hieroglyphic font manager and loading of the
        // fonts (do it elsewhere).

        // Move this to startApplication (using data from JSeshApplicationStartingData)
        ImageIconFactory.getInstance().setCadratHeight(data.iconHeight());

        JSeshApplicationModel applicationModel = new JSeshApplicationModel(data.applicationDefaults());
        applicationModel.setCopyright(COPYRIGHT);
        applicationModel.setName(NAME);
        applicationModel.setVersion(Version.getVersion());

        // We use setViewClass method instead of setViewClassName
        // Because we will probably move the code...
        applicationModel.setViewClass(JSeshView.class);

        Application app;
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("mac os x")) {            
            app = new QenherOSXApplication();
        } else if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
                .startsWith("win")) {
            // app = new QenherOSXApplication();
            app = new QenherOSXLikeApplication();
        } else {
            app = new QenherOSXLikeApplication();
            // app= new QenherOSXApplication();
        }
        System.err.println("getDefaultToolkit().getScreenResolution() :"+ 
                Toolkit.getDefaultToolkit().getScreenResolution());
        System.err.println("getDefaultToolkit().getScreenSize() : "+ java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        System.err.println("PlatformDefaults.getHorizontalScaleFactor(): "
        +PlatformDefaults.getHorizontalScaleFactor());
        System.err.println("PlatformDefaults.getVerticalScaleFactor(): "+
                 PlatformDefaults.getVerticalScaleFactor());
        System.err.println("PlatformDefaults.getDefaultDPI "+ PlatformDefaults.getDefaultDPI());       
        app.setModel(applicationModel);
        app.launch(args);
    }

    public static void main(String[] args) throws InterruptedException,
            InvocationTargetException,
            ClassNotFoundException {
        ((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap())
                .addUnencodedNativeForFlavor(new DataFlavor("application/pdf"), "PDF");
        Main jseshMain = new Main();
        jseshMain.args = args;
        jseshMain.setSplashPicture("/jseshResources/images/splash.png");
        SplashMessageText message = new SplashMessageText(50, 172, "Version "
                + Version.getVersion());
        message.setColor(Color.WHITE);
        jseshMain.addSplashMessage(message);
        jseshMain.setTickTimer();
        jseshMain.run();
    }

}
