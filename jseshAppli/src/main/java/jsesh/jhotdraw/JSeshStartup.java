package jsesh.jhotdraw;

import org.qenherkhopeshef.swingUtils.PortableToolKit;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import javax.swing.UIManager;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.jhotdraw_7_6.app.Application;
import org.qenherkhopeshef.guiFramework.AppStartup;
import org.qenherkhopeshef.guiFramework.splash.SplashMessageText;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXApplication;
import org.qenherkhopeshef.jhotdrawChanges.QenherOSXLikeApplication;
import org.qenherkhopeshef.utils.PlatformDetection;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.UIScale;

import jsesh.Version;
import jsesh.defaults.HieroglyphResources;
import jsesh.defaults.HieroglyphResourcesBuilder;
import jsesh.defaults.UserFontDirectoryManager;
import jsesh.glossary.GlossaryManager;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.HieroglyphFamily;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.preferences.application.model.ApplicationUIPreferences;
import jsesh.render.draw.MDCIconFactory;
import jsesh.signcodes.ManuelDeCodage;
import jsesh.ui.editor.actions.text.EditorCartoucheAction;
import jsesh.ui.editor.actions.text.EditorShadeAction;
import jsesh.utils.resources.ResourcesManager;
import net.miginfocom.layout.PlatformDefaults;

/**
 * Code for starting up the JSesh application.
 * <p>
 * Loads resources, display a splash screen, and start the application.
 * <p>
 * Starting up the application means :
 * <ul>
 * <li>creating this object
 * <li>calling the main method, which will call the run method of this class
 * </ul>
 */
public class JSeshStartup extends AppStartup<JSeshApplicationStartingData> {
    // Logging
    private static final Logger LOGGER = Logger.getLogger(JSeshStartup.class.getName());

    /// Height of the icons of the actions (toolbar...), at 100 % interface scale.
    public static final int ACTION_ICON_HEIGHT = 30;

    public final static String NAME = "JSesh";
    public final static String COPYRIGHT = "JSesh is CeCiLL Software (GPL-compatible) written by S. Rosmorduc";

    private String args[];

    @Override
    protected void initLookAndFeel() {
        // The interface scale (flatlaf.uiScale) is set earlier, in Main,
        // by ApplicationUIPreferences.applyStoredUiScale().

        // CSS options for option panel - maybe not needed and a mere patch 
        // done long time ago.
        // if (UIManager.getString("OptionPane.css") == null) {
        //     UIManager.put("OptionPane.css", "<head>"
        //             + "<style type=\"text/css\">"
        //             + "b { font: 13pt \"Dialog\" }"
        //             + "p { font: 11pt \"Dialog\"; margin-top: 8px }"
        //             + "</style>" + "</head>");
        // }

        // Several jhotdrawfw dialogs prepend UIManager.getString("OptionPane.css")
        // to their message. FlatLaf doesn't define it, which printed "null".
        if (UIManager.getString("OptionPane.css") == null) {
            UIManager.put("OptionPane.css", "");
        }

        if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
            FlatMacLightLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        // FlatDarculaLaf.setup(); // used to test if L&F is set

        // MigLayout sizes without unit are "logical pixels": make them follow an
        // explicit interface scale, as FlatLaf does for fonts and margins.
        // In automatic mode, MigLayout keeps its own computation.
        if (ApplicationUIPreferences.getFromPreferences().getUiScale() > 0) {
            float scale = UIScale.getUserScaleFactor();
            PlatformDefaults.setHorizontalScaleFactor(scale);
            PlatformDefaults.setVerticalScaleFactor(scale);
        }
    }

    /**
     * Preloading of data before starting the actual application.
     */
    @Override
    public JSeshApplicationStartingData initApplicationData() {
        // Prepare icon factory...
        UserFontDirectoryManager userFontDirectoryManager = UserFontDirectoryManager.buildUserFontManager();

        GlossaryManager glossaryManager = new GlossaryManager();
        try {
            glossaryManager.read();
        } catch (Exception e) {
            LOGGER.severe("Could not read glossary : " + e.getMessage());
        }

        HieroglyphResources hieroglyphResources = HieroglyphResourcesBuilder.buildFull(
                userFontDirectoryManager.getUserFontReference(),
                glossaryManager.getGlossary());

        // Dirty architecture.
        // Pre-load a number of objects so that they are ready when graphic
        // stuff starts.
        ResourcesManager.getInstance();

        MDCIconFactory mdcIconFactory = new MDCIconFactory(hieroglyphResources.hieroglyphShapeRepository());
        // The icon height is given at 100 %: follow the interface scale.
        mdcIconFactory.setCadratHeight(UIScale.scale(ACTION_ICON_HEIGHT));

        JSeshApplicationStartingData data = new JSeshApplicationStartingData(hieroglyphResources,
                userFontDirectoryManager, mdcIconFactory, glossaryManager);
        preloadHieroglyphicIcons(data);

        return data;
    }

    private void preloadHieroglyphicIcons(JSeshApplicationStartingData data) {
        HieroglyphDatabase database = data.hieroglyphResources().database();
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

        JSeshApplicationModel applicationModel = new JSeshApplicationModel(data.hieroglyphResources(),
                data.userFontDirectoryManager(), data.glossaryManager(), data.mdcIconFactory());
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
        LOGGER.info(() -> "getDefaultToolkit().getScreenResolution() :" +
                Toolkit.getDefaultToolkit().getScreenResolution());
        LOGGER.info(
                () -> "getDefaultToolkit().getScreenSize() : " + PortableToolKit.createToolkit().getScreenSize());
        LOGGER.info(() -> "PlatformDefaults.getHorizontalScaleFactor(): "
                + PlatformDefaults.getHorizontalScaleFactor());
        LOGGER.info(() -> "PlatformDefaults.getVerticalScaleFactor(): " +
                PlatformDefaults.getVerticalScaleFactor());
        LOGGER.info(() -> "PlatformDefaults.getDefaultDPI " + PlatformDefaults.getDefaultDPI());
        app.setModel(applicationModel);
        app.launch(args);
    }

    public void main(String[] args) throws InterruptedException,
            InvocationTargetException,
            ClassNotFoundException {
        ((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap())
                .addUnencodedNativeForFlavor(new DataFlavor("application/pdf"), "PDF");
        this.args = args;
        // Print the current path (just to be sure we can find the logging.properties
        // file).
        setSplashPicture("/jseshResources/images/splash.png");
        SplashMessageText message = new SplashMessageText(50, 172, "Version "
                + Version.getVersion());
        message.setColor(Color.WHITE);
        addSplashMessage(message);
        setTickTimer();
        this.run();

    }
}
