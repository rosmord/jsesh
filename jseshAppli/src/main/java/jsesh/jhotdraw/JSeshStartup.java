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
import jsesh.hieroglyphs.data.coremdc.ManuelDeCodage;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.preferences.application.model.ApplicationUIPreferences;
import jsesh.resources.ResourcesManager;
import jsesh.swing.utils.MDCIconFactory;
import net.miginfocom.layout.PlatformDefaults;

/**
 * Code for starting up the JSesh application.
 * <p> Loads resources, display a splash screen, and start the application.
 * <p> Starting up the application means :
 * <ul>
 * <li> creating this object
 * <li> calling the main method, which will call the run method of this class
 * </ul>
 */
public class JSeshStartup extends AppStartup<JSeshApplicationStartingData> {
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

        MDCIconFactory mdcIconFactory = new MDCIconFactory(applicationDefaults.hieroglyphShapeRepository());
        mdcIconFactory.setCadratHeight(applicationPreferences.getIconHeight());


        JSeshApplicationStartingData data = new JSeshApplicationStartingData(applicationDefaults, mdcIconFactory);        
        preloadHieroglyphicIcons(data); 

        return data;
    }

    private void preloadHieroglyphicIcons(JSeshApplicationStartingData data) { 
        HieroglyphDatabaseInterface database = data.applicationDefaults().hieroglyphDatabase();
        MDCIconFactory mdcIconFactory = data.mdcIconFactory();
        List<HieroglyphFamily> families = database.getFamilies();
        for (int i = 0; i < families.size(); i++) {
            HieroglyphFamily family = families.get(i);
            for (String code : ManuelDeCodage.getInstance()
                    .getBasicGardinerCodesForFamily(family.getCode())) {
                mdcIconFactory.buildGlyphImage(code);
            }
        }
        EditorCartoucheAction.preloadCartoucheIcons(mdcIconFactory);
        EditorShadeAction.preloadIcons(mdcIconFactory);
    }

    @Override
    public void startApplication(JSeshApplicationStartingData data) {
        // Force creation of the hieroglyphic font manager and loading of the
        // fonts (do it elsewhere).

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

    public void main(String[] args) throws InterruptedException,
            InvocationTargetException,
            ClassNotFoundException {
         ((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap())
                .addUnencodedNativeForFlavor(new DataFlavor("application/pdf"), "PDF");
        this.args = args;
        setSplashPicture("/jseshResources/images/splash.png");
        SplashMessageText message = new SplashMessageText(50, 172, "Version "
                + Version.getVersion());
        message.setColor(Color.WHITE);
        addSplashMessage(message);
        setTickTimer();
        this.run();
        
    }
}
