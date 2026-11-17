package jsesh.mdcDisplayer.swing.application;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterJob;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jsesh.Version;
import jsesh.doc.DocDisplayer;
import jsesh.graphicExport.JPGExporter;
import jsesh.graphicExport.PDFExporter;
import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.ViewPrinter;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener;
import jsesh.mdcDisplayer.swing.splash.SplashScreen;
import jsesh.resources.ResourcesManager;

import org.freehep.util.export.ExportDialog;

import com.birosoft.liquid.LiquidLookAndFeel;

/**
 * An application to load, display and edit MDC files This file is free Software
 * (c) Serge Rosmorduc
 * 
 * @author rosmord
 */

public class MDCDisplayerAppli {

    /**
     * The application data
     */
    private HieroglyphicTextModel data;

    /**
     * The Main GUI window
     *  
     */
    private MDCDisplayerAppliFrame frame;

    /**
     * The Application control
     * 
     * @see java.lang.Object#Object()
     *  
     */
    private MDCDisplayerAppliWorkflow workflow;

    /**
     * The application menu
     */

    private JMenuBar menubar;

    /**
     *  
     */
    // The architecture of the application is still a bit unsure (whereas the
    // architecture for the manuel de codage viewer itself is quite sound)
    AbstractAction exportGraphicsAction = new AbstractAction("Export Graphics") {
        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            ExportDialog dialog = new ExportDialog();
            dialog.showExportDialog(frame, "export as", frame.editor, "export");
        }

    };

    AbstractAction exportPDFAction;

    /**
     *  
     */
    JPGExporter exportJPGAction;

    AbstractAction printAction = new AbstractAction("Print") {

        public void actionPerformed(ActionEvent e) {
            // IMPORTANT : we need some central drawer somewhere.
            PrinterJob printJob = PrinterJob.getPrinterJob();
            ViewPrinter printer = new ViewPrinter();

            printJob.setPageable(printer);
            printer.setModel(data.getModel());
            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    };

    SplashScreen splash;

    /**
     * Constructor for MDCDisplayerAppli.
     */
    
    public MDCDisplayerAppli() {
        super();
        splash = new SplashScreen();
        Properties props = System.getProperties();
        props.list(System.out);
        data = new HieroglyphicTextModel();
        data.setPhilologyIsSign(true);

        loadFirstText();
        workflow = new MDCDisplayerAppliWorkflow(data);
        String title = "JSesh MDC Editor v. " + Version.VERSION;
        frame = new MDCDisplayerAppliFrame(title, workflow);
        // IMPORTANT clean up the relations between these two.
        workflow.setFrame(frame);

        exportPDFAction = new PDFExporter(data, frame, "Export PDF");
        exportJPGAction = new JPGExporter(data, frame, "Export jpeg");
        exportJPGAction.setScale(2);

        menubar = buildMenuBar();
        frame.setJMenuBar(menubar);
        frame.addWindowListener(new WindowAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
             */
            public void windowOpened(WindowEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    public void run() {
                        splash.closeSplash();
                    }
                });
                

                //  JOptionPane.showMessageDialog(frame,"opened");
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
             */
            public void windowClosing(WindowEvent e) {
                workflow.exit();
            }

        });
        frame.setVisible(true);
    }

    private void loadFirstText() {
        try {
            Reader r = ResourcesManager.getInstance().getDemoData();
            //Reader r= new
            // FileReader("/home/rosmord/HieroFinal/inBase/amenemope.hie");
            data.readModel(r);
            r.close();
            // Test :
            /*
             * ModelWriter w = new ModelWriter(); OutputStreamWriter outw = new
             * OutputStreamWriter(System.out); w.write(outw, data.getModel());
             * outw.flush();
             */

        } catch (MDCSyntaxError e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JMenuBar buildMenuBar() {
        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item;

        menu.setMnemonic(KeyEvent.VK_F);

        menu.add(workflow.getNewAction());
        menu.add(workflow.getLoadTextAction());
        menu.add(workflow.getSaveTextAction());
        menu.addSeparator();
        // menu.add(exportGraphicsAction);
        menu.add(exportPDFAction);
        menu.add(exportJPGAction);
        menu.add(workflow.getWmfExportAction());
        menu.add(workflow.getHtmlExportAction());
        menu.add(printAction);
        menu.addSeparator();
        menu.add(workflow.getExitAction());
        menubar.add(menu);

        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        addWithShortCut(edit, workflow.getCutAction(), KeyEvent.VK_X);
        addWithShortCut(edit, workflow.getCopyAction(), KeyEvent.VK_C);
        addWithShortCut(edit, workflow.getPasteAction(), KeyEvent.VK_V);
        edit.addSeparator();
        addWithShortCut(edit, workflow.getSelectAllAction(), KeyEvent.VK_A);
        edit.addSeparator();
        addWithShortCut(edit, workflow.getSetHieroglyphicModeAction(),
                KeyEvent.VK_N);
        addWithShortCut(edit, workflow.getSetLatinModeAction(), KeyEvent.VK_D);
        addWithShortCut(edit, workflow.getSetBoldModeAction(), KeyEvent.VK_B);
        addWithShortCut(edit, workflow.getSetItalicModeAction(), KeyEvent.VK_I);
        addWithShortCut(edit, workflow.getSetTransliterationModeAction(),
                KeyEvent.VK_T);
        menubar.add(edit);

        JMenu manipulate = new JMenu("Text Manipulation");
        manipulate.setMnemonic(KeyEvent.VK_T);
        addWithShortCut(manipulate, workflow.getGroupHorizontallyAction(),
                KeyEvent.VK_H);
        addWithShortCut(manipulate, workflow.getGroupVerticallyAction(),
                KeyEvent.VK_G);
        addWithShortCut(manipulate, workflow.getLigatureAction(), KeyEvent.VK_L);
        addWithShortCut(manipulate, workflow.getExplodeGroupAction(),
                KeyEvent.VK_E);
        manipulate.addSeparator();
        addWithShortCut(manipulate, workflow.getInsertSpaceAction(),
                KeyEvent.VK_P);
        addWithShortCut(manipulate, workflow.getInsertHalfSpaceAction(),
                KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK);
        addWithShortCut(manipulate, workflow.getInsertPageBreakAction(),
                KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        addWithShortCut(manipulate, workflow.getInsertRedPointAction(),
                KeyEvent.VK_O);
        addWithShortCut(manipulate, workflow.getInsertBlackPointAction(),
                KeyEvent.VK_O, KeyEvent.SHIFT_DOWN_MASK);
        manipulate.addSeparator();
        addWithShortCut(manipulate, workflow.getShadeZoneAction(),
                KeyEvent.VK_Z);
        addWithShortCut(manipulate, workflow.getUnShadeZoneAction(),
                KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK);
        //manipulate.add(workflow.getInsertPageBreakAction());
        addWithShortCut(manipulate, workflow.getPaintRedAction(), KeyEvent.VK_R);
        addWithShortCut(manipulate, workflow.getPaintBlackAction(),
                KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK);
        addWithShortCut(manipulate, workflow.getEditGroupAction(),
                KeyEvent.VK_G, KeyEvent.SHIFT_DOWN_MASK);
        menubar.add(manipulate);

        // Sign manipulation menu.
        JMenu sign = new JMenu("Sign");
        sign.setMnemonic(KeyEvent.VK_S);
        sign.add(workflow.getReverseSignAction());

        JMenu size = new JMenu("Size");
        size.setMnemonic(KeyEvent.VK_S);
        // TODO : move this table to the workflow.
        // Sizes : 200 144 120 100 70 50 35 25
        int sizes[] = { 200, 144, 120, 100, 70, 50, 35, 25 };
        for (int i = 0; i < sizes.length; i++) {
            size.add(workflow.buildSizeAction(sizes[i]));
        }
        sign.add(size);

        JMenu rotation = new JMenu("Rotation");
        rotation.setMnemonic(KeyEvent.VK_R);
        for (int i = 0; i < workflow.getRotations().length; i++)
            rotation.add(workflow.getRotations()[i]);
        sign.add(rotation);
        sign.add(workflow.getToggleRedSignAction());
        sign.add(workflow.getToggleWideSignAction());

        //	 grammar and stuff.
        sign.addSeparator();
        sign.add(workflow.getToggleGrammarAction());
        sign.add(workflow.getSignIsInWordAction());
        sign.add(workflow.getSignIsWordEndAction());
        sign.add(workflow.getSignIsSentenceEndAction());
        sign.add(workflow.getSignIsInWordAction());
        sign.add(workflow.getToggleIgnoredSignAction());

        menubar.add(sign);

        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.add(workflow.getHalfZoomAction());
        view.add(workflow.getDoubleZoomAction());
        view.add(workflow.getResetZoomAction());
        menubar.add(view);

        buildShadingMenu(menubar);
        buildCartoucheMenu(menubar);
        buildPhilologicalMenu(menubar);

        buildHieroglyphicMenus(menubar);

        menubar.add(Box.createHorizontalGlue());
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_L);
        help.add(new AbstractAction("JSesh user documentation") {

            public void actionPerformed(ActionEvent e) {
                DocDisplayer.getInstance().show();

            }

        });
        menubar.add(help);
        return menubar;
    }

    /**
     * @param menubar2
     */
    private void buildPhilologicalMenu(JMenuBar menubar) {
        JMenu philology = new JMenu("Philological Markup");
        philology.setMnemonic(KeyEvent.VK_LEFT_PARENTHESIS);
        JPopupMenu pm = philology.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        String tips[] = { "Erased text", "Editor addition",
                "Superfluous signs", "Previously readable signs",
                "Scribe addition" };
        for (int i = 0; i < workflow.getPhilologyActions().length; i++) {
            JMenuItem sub = new JMenuItem(workflow.getPhilologyActions()[i]);
            if (i < tips.length)
                sub.setToolTipText(tips[i]);
            philology.add(sub);
        }
        menubar.add(philology);

    }

    /**
     * @param menubar2
     */
    private void buildCartoucheMenu(JMenuBar menubar) {
        JMenu cartouche = new JMenu("Cartouche");
        cartouche.setMnemonic(KeyEvent.VK_C);
        JPopupMenu pm = cartouche.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (int i = 0; i < workflow.getCartoucheActions().length; i++) {
            cartouche.add(workflow.getCartoucheActions()[i]);
        }
        menubar.add(cartouche);
    }

    /**
     * @param menubar
     */
    private void buildShadingMenu(JMenuBar menubar) {
        JMenu shading = new JMenu("Shading");
        shading.setMnemonic(KeyEvent.VK_H);
        JPopupMenu pm = shading.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (int i = 0; i < workflow.getShadeActions().length; i++) {
            shading.add(workflow.getShadeActions()[i]);
        }
        menubar.add(shading);
    }

    /**
     * @param manipulate
     * @param groupHorizontallyAction
     * @param vk_e
     * @param mask :
     *            additional mask
     */
    private void addWithShortCut(JMenu menu, AbstractAction action, int vk,
            int mask) {
        JMenuItem item = new JMenuItem(action);
        item.setAccelerator(KeyStroke
                .getKeyStroke(vk, mask
                        | java.awt.Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask()));
        menu.add(item);

    }

    private void addWithShortCut(JMenu menu, AbstractAction action, int vk) {
        JMenuItem item = new JMenuItem(action);

        // item.setAccelerator(KeyStroke.getKeyStroke(vk,
        // KeyEvent.CTRL_DOWN_MASK));
        // Respect the usual shortcuts for this system.
        item.setAccelerator(KeyStroke.getKeyStroke(vk, java.awt.Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(item);

    }

    /**
     * @param menubar2
     */
    private void buildHieroglyphicMenus(JMenuBar menubar) {
        String families[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                "X", "Y", "Z", "Aa", "Ff" };
        String titles[] = { "A. Man and his occupations",
                "B. Woman and her occupations", "C. Anthropomorphic Deities",
                "D. Parts of the human body", "E. Mammals",
                "F. Parts of Mammals", "G. Birds", "H. Parts of Birds",
                "I. Amphibious animals, reptiles, etc.",
                "K. Fishes and parts of fishes",
                "L. Invertebrata and lesser animals", "M. Trees and plants",
                "N. Sky, earth, water",
                "O. Buildings, parts of buildings, etc.",
                "P. Ships and parts of ships",
                "Q. Domestic and funerary furniture",
                "R. Temple furniture and sacred emblems",
                "S. Crowns, Dress, Staves, etc.",
                "T. Warfare, hunting, butchery",
                "U. Agriculture, crafts, and professions",
                "V. Rope, Fibre, baskets, bags, etc.",
                "W. Vessels of stone and earthenware", "X. Loaves and cakes",
                "Y. Writings, games, music", "Z. Strokes",
                "Aa. Unclassified (J)",
                "Ff. Hieratic signs, Gardiner, JEA 15 (&)" };

        JMenu hieroglyphs = new JMenu("Hieroglyphs");
        for (int i = 0; i < families.length; i++) {
            String codes[] = HieroglyphsManager.getInstance()
                    .getCodesForFamily(families[i]);
            HieroglyphicMenu fmenu = new HieroglyphicMenu(titles[i], codes, 6);
            fmenu.setHieroglyphicMenuListener(new HieroglyphicMenuMediator());
            if (i < 25)
                fmenu.setMnemonic(families[i].toUpperCase().charAt(0));
            else if (i == 25) {
                fmenu.setMnemonic(KeyEvent.VK_J);
            } else if (i == 26)
                fmenu.setMnemonic(KeyEvent.VK_AMPERSAND);
            hieroglyphs.add(fmenu);
        }
        hieroglyphs.setMnemonic(KeyEvent.VK_H);
        menubar.add(hieroglyphs);

    }

    private class HieroglyphicMenuMediator implements HieroglyphicMenuListener {

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener#codeSelected(java.lang.String)
         */
        public void codeSelected(String code) {
            workflow.insert(code);

        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener#enter(java.lang.String)
         */
        public void enter(String code) {
            workflow.displaySignInfo(code);
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener#exit(java.lang.String)
         */
        public void exit(String code) {
            workflow.setMessage("");
        }

    }

    public static void main(String[] args) {
    	
    	// We should have a more flexible look and feel system.
    	// However, for Linux, I like liquid.
        if ("Linux".equals(System.getProperty("os.name")))
        try {
            //UIManager.setLookAndFeel(new KunststoffLookAndFeel());
            UIManager.setLookAndFeel(new LiquidLookAndFeel());
            //UIManager.setLookAndFeel(new MetouiaLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MDCDisplayerAppli a = new MDCDisplayerAppli();
    }
}