package jsesh.mdcDisplayer.swing.application;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jsesh.Version;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.swing.application.actions.AboutAction;
import jsesh.mdcDisplayer.swing.application.actions.DisplayJavaPropertiesAction;
import jsesh.mdcDisplayer.swing.application.actions.DocumentationAction;
import jsesh.mdcDisplayer.swing.application.actions.EditPreferencesAction;
import jsesh.mdcDisplayer.swing.application.actions.ExitAction;
import jsesh.mdcDisplayer.swing.application.actions.NewTextAction;
import jsesh.mdcDisplayer.swing.application.actions.OpenTextAction;
import jsesh.mdcDisplayer.swing.application.actions.SaveTextAction;
import jsesh.mdcDisplayer.swing.application.actions.SelectCopyPasteConfigurationAction;
import jsesh.mdcDisplayer.swing.application.actions.generic.ForwardedAction;
import jsesh.mdcDisplayer.swing.application.actions.generic.WorkflowMethodAction;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;
import jsesh.mdcDisplayer.swing.editor.MDCEditorKeyManager;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener;
import jsesh.mdcDisplayer.swing.splash.SplashScreen;
import jsesh.resources.ResourcesManager;
import jsesh.utils.PlatformDetection;

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

	SplashScreen splash;

	/**
	 * Constructor for MDCDisplayerAppli.
	 */

	public MDCDisplayerAppli() {
		super();
		data = new HieroglyphicTextModel();
		data.setPhilologyIsSign(true);

		workflow = new MDCDisplayerAppliWorkflow(data);
		String title = "JSesh MDC Editor v. " + Version.getVersion();
		frame = new MDCDisplayerAppliFrame(title, workflow);
		// TODO IMPORTANT clean up the relations between these two.
		workflow.setFrame(frame);

		
		menubar = buildMenuBar();
		frame.setJMenuBar(menubar);
		frame.addWindowListener(new WindowAdapter() {

			
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				workflow.exit();
			}

		});
	}

	private void loadDemo() {
		try {
			Reader r = ResourcesManager.getInstance().getDemoData();
			// Reader r= new
			// FileReader("/home/rosmord/HieroFinal/inBase/amenemope.hie");
			 //r= new StringReader("r:a-ra-m-!-t:A-p*t:pt-n\\200:(x:t)*U30\\200-xAst-!-s-r:D-A1");
			//r= new StringReader("<h-m-p*t:pt->");
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

		menu.setMnemonic(KeyEvent.VK_F);

		addWithMnemonics(menu,new NewTextAction("New", workflow), new Integer('N'));
		addWithMnemonics(menu, new OpenTextAction("Open", workflow), new Integer('O'));
		addWithMnemonics(menu, new SaveTextAction("Save",workflow), new Integer('S'));
		
		JMenu exportMenu= new JMenu("Export as");
		menu.add(exportMenu);
		menu.addSeparator();
		addWithMnemonics(menu, new ExitAction("Exit", workflow), new Integer('X'));
		menubar.add(menu);

		exportMenu.add(new WorkflowMethodAction("Export as Bitmap", "exportAsBitmap", workflow));
		exportMenu.add(new WorkflowMethodAction("Export as Wmf", "exportAsWMF", workflow,true));
		exportMenu.add(new WorkflowMethodAction("Export as SVG", "exportAsSVG", workflow,true));
		exportMenu.add(new WorkflowMethodAction("Export as Html", "exportAsHtml", workflow));
		exportMenu.add(new WorkflowMethodAction("Export as Pdf", "exportAsPdf", workflow));
		exportMenu.add(new WorkflowMethodAction("Export as Rtf", "exportAsRtf", workflow));
				
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
		addWithShortCut(edit,workflow.getSetLinePageNumberModeAction(), KeyEvent.VK_EXCLAMATION_MARK);
		edit.addSeparator();
		//
		// Selection of the copy and paste configuration.
		ButtonGroup group= new ButtonGroup();
		JRadioButtonMenuItem copyAndPaste1= new JRadioButtonMenuItem(new SelectCopyPasteConfigurationAction("copy : large size",workflow,0));
		JRadioButtonMenuItem copyAndPaste2= new JRadioButtonMenuItem(new SelectCopyPasteConfigurationAction("copy : small size",workflow,1));
		group.add(copyAndPaste1);
		group.add(copyAndPaste2);
		edit.add(copyAndPaste1);
		edit.add(copyAndPaste2);
		copyAndPaste1.setSelected(true);
		//
		menubar.add(edit);

		JMenu manipulate = new JMenu("Text Manipulation");
		manipulate.setMnemonic(KeyEvent.VK_T);
		addWithShortCut(manipulate, workflow.getGroupHorizontallyAction(),
				KeyEvent.VK_H);
		addWithShortCut(manipulate, workflow.getGroupVerticallyAction(),
				KeyEvent.VK_G);
		addWithShortCut(manipulate, workflow.getLigatureAction(), KeyEvent.VK_L);
		addWithShortCut(manipulate, workflow.getLigatureBeforeAction(), KeyEvent.VK_K);
		addWithShortCut(manipulate, workflow.getLigatureAfterAction(), KeyEvent.VK_M);
		addWithShortCut(manipulate, workflow.getExplodeGroupAction(),
				KeyEvent.VK_E);
		manipulate.addSeparator();
		addWithShortCut(manipulate, workflow.getInsertSpaceAction(),
				KeyEvent.VK_P);
		addWithShortCut(manipulate, workflow.getInsertHalfSpaceAction(),
				KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK);
		addWithShortCut(manipulate, workflow.getInsertPageBreakAction(),
				KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
		addWithShortCut(manipulate, workflow.getInsertRedPointAction(),
				KeyEvent.VK_O);
		addWithShortCut(manipulate, workflow.getInsertBlackPointAction(),
				KeyEvent.VK_O, InputEvent.SHIFT_DOWN_MASK);
		manipulate.addSeparator();
		addWithShortCut(manipulate, workflow.getShadeZoneAction(),
				KeyEvent.VK_Z);
		addWithShortCut(manipulate, workflow.getUnShadeZoneAction(),
				KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK);
		// manipulate.add(workflow.getInsertPageBreakAction());
		addWithShortCut(manipulate, workflow.getPaintRedAction(), KeyEvent.VK_R);
		addWithShortCut(manipulate, workflow.getPaintBlackAction(),
				KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK);
		addWithShortCut(manipulate, workflow.getEditGroupAction(),
				KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK);
		manipulate.addSeparator();

		// Sign manipulation menu.
		JMenu sign = new JMenu("Sign");
		sign.setMnemonic(KeyEvent.VK_S);
		sign.add(workflow.getReverseSignAction());

		JMenu size = new JMenu("Size");
		size.setMnemonic(KeyEvent.VK_S);
		// Sizes : 200 144 120 100 70 50 35 25
		int sizes[] = { 240, 200, 144, 120, 100, 70, 50, 35, 25 ,1};
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

		// grammar and stuff.
		sign.addSeparator();
		sign.add(workflow.getToggleGrammarAction());
		sign.add(workflow.getSignIsInWordAction());
		sign.add(workflow.getSignIsWordEndAction());
		sign.add(workflow.getSignIsSentenceEndAction());
		sign.add(workflow.getSignIsInWordAction());
		sign.add(workflow.getToggleIgnoredSignAction());

		// manipulate.add(sign);
		buildShadingMenu(manipulate);
		buildCartoucheMenu(manipulate);
		buildPhilologicalMenu(manipulate);

		menubar.add(manipulate);
		menubar.add(sign);

		JMenu view = new JMenu("View");
		view.setMnemonic(KeyEvent.VK_V);
		view.add(workflow.getHalfZoomAction());
		view.add(workflow.getDoubleZoomAction());
		view.add(workflow.getResetZoomAction());
		view.addSeparator();
		// FIXME : design a cleaner system for actions (xml definition file ?)
		JMenu orientationMenu= new JMenu("Orientation");
		JMenu directionMenu= new JMenu("Direction");
		view.add(orientationMenu);
		view.add(directionMenu);		
		view.addSeparator();
		// Small signs on base line ?
		view
				.add(new JCheckBoxMenuItem(workflow
						.getSelectCenteredSignsAction()));
		menubar.add(view);

		ButtonGroup orientationGroup= new ButtonGroup();
		JRadioButtonMenuItem horizontalButton = new JRadioButtonMenuItem(new ForwardedAction(workflow, "Horizontal", MDCEditorKeyManager.SELECT_HORIZONTAL_ORIENTATION));
		JRadioButtonMenuItem verticalButton= new JRadioButtonMenuItem(new ForwardedAction(workflow, "Vertical", MDCEditorKeyManager.SELECT_VERTICAL_ORIENTATION));
		orientationGroup.add(horizontalButton);
		orientationGroup.add(verticalButton);
		orientationMenu.add(horizontalButton);
		orientationMenu.add(verticalButton);
		
		ButtonGroup directionGroup= new ButtonGroup();
		JRadioButtonMenuItem leftToRightButton=new JRadioButtonMenuItem(new ForwardedAction(workflow, "Left to Right", MDCEditorKeyManager.SELECT_L2R_DIRECTION));
		JRadioButtonMenuItem rightToLeftButton = new JRadioButtonMenuItem(new ForwardedAction(workflow, "Right to Left", MDCEditorKeyManager.SELECT_R2L_DIRECTION));
		directionGroup.add(rightToLeftButton);
		directionGroup.add(leftToRightButton);
		directionMenu.add(leftToRightButton);
		directionMenu.add(rightToLeftButton);
		
		buildHieroglyphicMenus(menubar);

		buildToolsMenus(menubar);

		menubar.add(Box.createHorizontalGlue());
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_L);
		help.add(new AboutAction("About JSesh",frame));
		help.add(new DocumentationAction("JSesh user documentation"));
		help.add(new DisplayJavaPropertiesAction("Display java properties", frame));
		menubar.add(help);
		return menubar;
	}

	
	/**
	 * Add a menu item with a specific mnemonic
	 * @param menu
	 * @param action
	 * @param keyCode
	 */
	private void addWithMnemonics(JMenu menu, Action action, Integer keyCode) {
		action.putValue(Action.MNEMONIC_KEY, keyCode);
		menu.add(action);
	}

	private void buildToolsMenus(JMenuBar menubar2) {
		JMenu tools = new JMenu("Tools");
		tools.add(workflow.getAddNewSignsAction());

		tools.addSeparator();
		// JMenuItem prefs= new JMenuItem("Edit preferences");
		tools.add(new EditPreferencesAction("Edit preferences",workflow));
		menubar2.add(tools);
	}

	/**
	 * @param menubar
	 */
	private void buildPhilologicalMenu(JMenu menubar) {
		JMenu philology = new JMenu("Philological Markup");
		philology.setMnemonic(KeyEvent.VK_P);
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
	 * @param menubar
	 */
	private void buildCartoucheMenu(JMenu menubar) {
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
	private void buildShadingMenu(JMenu menubar) {
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
	 * Build a menu.
	 * 
	 * @param menu
	 * @param action
	 * @param vk
	 * @param mask
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
	 * @param menubar
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
		hieroglyphs.getPopupMenu().setLayout(new GridLayout(14, 2));
		for (int i = 0; i < families.length; i++) {

			HieroglyphicMenu fmenu = new HieroglyphicMenu(titles[i],
					families[i], 6);
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
	
	/**
	 * @param f
	 */
	public void setInitialFile(File f) {
		try {
			Reader r = new FileReader(f);
			workflow.loadFile(f);
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Utilitary class to start the application in a thread correct way.
	 * 
	 * @author rosmord
	 * 
	 */

	private static class StartMe implements Runnable {
		MDCDisplayerAppliFrame startme;

		/**
		 * @param startme
		 */
		public StartMe(MDCDisplayerAppliFrame startme) {
			this.startme = startme;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			startme.setVisible(true);
		}

	}

	/**
	 * This listener closes the splash window when the main window has appeared.
	 * 
	 * @author rosmord
	 * 
	 */
	private static class SplashCloser extends WindowAdapter {
		private SplashScreen splash;

		/**
		 * @param splash
		 */
		public SplashCloser(SplashScreen splash) {
			super();
			this.splash = splash;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
		 */
		public void windowOpened(WindowEvent e) {
			splash.setVisible(false);
			splash.dispose();
		}
	}
	
	

	public static void main(String[] args) {
	
		// We should have a more flexible look and feel system.
		// However, for Linux, I like liquid.
		try {
			if (PlatformDetection.getPlatform() == PlatformDetection.UNIX) {
	
				// UIManager.setLookAndFeel(new KunststoffLookAndFeel());
				// UIManager.setLookAndFeel(new LiquidLookAndFeel());
				// UIManager.setLookAndFeel(new MetouiaLookAndFeel());
				//UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
				
				//UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
				//OyoahaLookAndFeel lnf = new OyoahaLookAndFeel();
				//UIManager.setLookAndFeel(lnf);
				// For fun, napkin is real nice :-)
				//UIManager.setLookAndFeel("net.sourceforge.napkinlaf.NapkinLookAndFeel");
			} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS) {
	
				UIManager
						.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
				//UIManager.setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel;");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SplashScreen splash = new SplashScreen();
			splash.display();
			UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
			
			MDCDisplayerAppli a = new MDCDisplayerAppli();
			boolean noTextWasLoaded= true;
			
			if (args.length > 0) {
				File f= new File(args[0]);
				if (f.exists()) {
					a.setInitialFile(f);
					noTextWasLoaded= false;
				}
			}
			
			if (noTextWasLoaded) {
				a.loadDemo();
			}
			// Swing commands should be performed in the graphical thread.
			// Hence we create a runnable to actually start the program.
	
			a.frame.addWindowListener(new SplashCloser(splash));
	
			StartMe run = new StartMe(a.frame);
			SwingUtilities.invokeLater(run);
			// SwingUtilities.invokeLater(run);
			// In order to have a nice splash screen, we should do more work
			// * move splash screen creation out of MDCDisplayerAppli
			// constructor
			// * probably move some of MDCDisplayerAppli's creation work out of
			// the graphical thread.
			// run.run();
		} catch (Exception e) {
			String message = "An unexpected error occurred when starting jsesh.\n"
					+ "can you please note the message and send a mail to rosmord@iut.univ-paris8.fr about it?\n"
					+ e.getMessage();
			StringWriter string = new StringWriter();
			PrintWriter out = new PrintWriter(string);
			e.printStackTrace(out);
			JTextArea textArea = new JTextArea(e.getMessage() + "\n"
					+ string.toString());
			JScrollPane pane = new JScrollPane(textArea);
			pane.setPreferredSize(new Dimension(640, 400));
			JOptionPane.showMessageDialog(null, pane, "Problem",
					JOptionPane.ERROR_MESSAGE);
		}
	}


}