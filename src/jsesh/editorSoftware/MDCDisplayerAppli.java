package jsesh.editorSoftware;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jsesh.Version;
import jsesh.editor.MDCEditorKeyManager;
import jsesh.editorSoftware.actions.AboutAction;
import jsesh.editorSoftware.actions.DisplayJavaPropertiesAction;
import jsesh.editorSoftware.actions.DocumentationAction;
import jsesh.editorSoftware.actions.EditPreferencesAction;
import jsesh.editorSoftware.actions.ExitAction;
import jsesh.editorSoftware.actions.NewTextAction;
import jsesh.editorSoftware.actions.OpenTextAction;
import jsesh.editorSoftware.actions.SaveTextAsAction;
import jsesh.editorSoftware.actions.SelectCopyPasteConfigurationAction;
import jsesh.editorSoftware.actions.generic.ForwardedAction;
import jsesh.editorSoftware.actions.generic.WorkflowMethodAction;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.HieroglyphFamily;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener;
import jsesh.mdcDisplayer.swing.splash.SplashScreen;
import jsesh.resources.ResourcesManager;
import jsesh.swingUtils.MenuUtils;

import org.qenherkhopeshef.utils.PlatformDetection;

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
	 * Constructor for MDCDisplayerAppli.
	 */

	public MDCDisplayerAppli() {
		super();

		workflow = new MDCDisplayerAppliWorkflow();
		String title = "JSesh MDC Editor v. " + Version.getVersion();
		frame = new MDCDisplayerAppliFrame(title, workflow);
		// TODO IMPORTANT clean up the relations between these two.
		workflow.setFrame(frame);

		menubar = buildMenuBar();
		frame.setJMenuBar(menubar);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent
			 * )
			 */
			public void windowClosing(WindowEvent e) {
				workflow.exit();
			}

		});
	}

	private void loadDemo() {
		Reader r = ResourcesManager.getInstance().getDemoData();
		workflow.loadData(r);
		// Reader r= new
		// FileReader("/home/rosmord/HieroFinal/inBase/amenemope.hie");
		// r= new
		// StringReader("r:a-ra-m-!-t:A-p*t:pt-n\\200:(x:t)*U30\\200-xAst-!-s-r:D-A1");
		// r= new StringReader("<h-m-p*t:pt->");

		// Test :
		/*
		 * MdCModelWriter w = new MdCModelWriter(); OutputStreamWriter outw =
		 * new OutputStreamWriter(System.out); w.write(outw, data.getModel());
		 * outw.flush();
		 */
	}

	public JMenuBar buildMenuBar() {
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");

		fileMenu.setMnemonic(KeyEvent.VK_F);

		MenuUtils.addWithShortCut(fileMenu, new NewTextAction("New", workflow),
				KeyEvent.VK_N);
		
		MenuUtils.addWithShortCut(fileMenu, new OpenTextAction("Open", workflow),
				KeyEvent.VK_O);
		
		MenuUtils.addWithShortCut(fileMenu, new WorkflowMethodAction(workflow, "Save", "saveText"),
				KeyEvent.VK_S);
		
		MenuUtils.addWithShortCut(fileMenu, new SaveTextAsAction("Save As", workflow),
				KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK);

		JMenu importMenu = new JMenu("Import");
		importMenu.add(new WorkflowMethodAction(workflow,
				"Import from pasted PDF", "importPastedPDF"));
		importMenu.add(new WorkflowMethodAction(workflow,
				"Import from pasted RTF", "importPastedRTF"));

		fileMenu.add(importMenu);

		JMenu exportMenu = new JMenu("Export as");
		fileMenu.add(exportMenu);
		fileMenu.addSeparator();
		MenuUtils.addWithMnemonics(fileMenu, new ExitAction("Exit", workflow),
				new Integer('X'));
		menubar.add(fileMenu);

		MenuUtils.addWithShortCut(exportMenu, new WorkflowMethodAction(workflow,
				"Export as JPEG or PNG", "exportAsBitmap"), KeyEvent.VK_B,
				InputEvent.SHIFT_DOWN_MASK);
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Wmf",
				"exportAsWMF", true));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Emf",
				"exportAsEMF", true));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as EPS",
				"exportAsEPS", true));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Mac Pict",
				"exportAsMacPict", true));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as SVG",
				"exportAsSVG", true));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Html",
				"exportAsHtml"));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Pdf",
				"exportAsPdf"));
		exportMenu.add(new WorkflowMethodAction(workflow, "Export as Rtf",
				"exportAsRtf"));

		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);

		ForwardedAction undoAction = new ForwardedAction(workflow, "Undo",
				MDCEditorKeyManager.UNDO);
		MenuUtils.addWithShortCut(edit, undoAction, KeyEvent.VK_Z);

		Action redoAction = new ForwardedAction(workflow, "Redo",
				MDCEditorKeyManager.REDO);
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			MenuUtils.addWithShortCut(edit, redoAction, KeyEvent.VK_Z,
					InputEvent.SHIFT_DOWN_MASK);

		} else {
			MenuUtils.addWithShortCut(edit, redoAction, KeyEvent.VK_Y);
		}
		workflow.registerUndoRedoActions(undoAction, redoAction);

		edit.addSeparator();
		MenuUtils.addWithShortCut(edit, new ForwardedAction(workflow, "Cut",
				MDCEditorKeyManager.CUT, true), KeyEvent.VK_X);
		MenuUtils.addWithShortCut(edit, new ForwardedAction(workflow, "Copy",
				MDCEditorKeyManager.COPY, true), KeyEvent.VK_C);
		MenuUtils.addWithShortCut(edit, new ForwardedAction(workflow, "Paste",
				MDCEditorKeyManager.PASTE), KeyEvent.VK_V);

		JMenu copyAs = new JMenu("Copy as...");
		copyAs.add(new ForwardedAction(workflow, "Copy as PDF", "COPY_AS_PDF"));
		copyAs.add(new ForwardedAction(workflow, "Copy as RTF", "COPY_AS_RTF"));
		copyAs.add(new ForwardedAction(workflow,
				"Copy as BITMAP (JPEG,PNG...)", "COPY_AS_BITMAP"));

		edit.add(copyAs);

		edit.addSeparator();
		MenuUtils.addWithShortCut(edit, workflow.getSelectAllAction(), KeyEvent.VK_A);
		edit.addSeparator();
		MenuUtils.addWithShortCut(edit, workflow.getSetHieroglyphicModeAction(),
				KeyEvent.VK_N);
		MenuUtils.addWithShortCut(edit, workflow.getSetLatinModeAction(), KeyEvent.VK_D);
		MenuUtils.addWithShortCut(edit, workflow.getSetBoldModeAction(), KeyEvent.VK_B);
		MenuUtils.addWithShortCut(edit, workflow.getSetItalicModeAction(), KeyEvent.VK_I);
		MenuUtils.addWithShortCut(edit, workflow.getSetTransliterationModeAction(),
				KeyEvent.VK_T);
		MenuUtils.addWithShortCut(edit, workflow.getSetLinePageNumberModeAction(),
				KeyEvent.VK_EXCLAMATION_MARK);
		edit.add(new WorkflowMethodAction(workflow, "Add Short Text",
				"addShortText"));
		edit.addSeparator();
		//
		// Selection of the copy and paste configuration.
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem copyAndPaste1 = new JRadioButtonMenuItem(
				new SelectCopyPasteConfigurationAction("copy : large size",
						workflow, 0));
		JRadioButtonMenuItem copyAndPaste2 = new JRadioButtonMenuItem(
				new SelectCopyPasteConfigurationAction("copy : small size",
						workflow, 1));
		JRadioButtonMenuItem copyAndPaste3 = new JRadioButtonMenuItem(
				new SelectCopyPasteConfigurationAction("copy : wysiwyg",
						workflow, 2));
		group.add(copyAndPaste1);
		group.add(copyAndPaste2);
		group.add(copyAndPaste3);
		edit.add(copyAndPaste1);
		edit.add(copyAndPaste2);
		edit.add(copyAndPaste3);
		copyAndPaste1.setSelected(true);
		//
		menubar.add(edit);

		JMenu manipulate = new JMenu("Text Manipulation");
		manipulate.setMnemonic(KeyEvent.VK_T);
		// For mac, command-H is usually used by the system (hides windows).

		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			MenuUtils.addWithShortCut(manipulate, workflow.getGroupHorizontallyAction(),
					KeyEvent.VK_J);
		} else {
			MenuUtils.addWithShortCut(manipulate, workflow.getGroupHorizontallyAction(),
					KeyEvent.VK_H);
		}
		MenuUtils.addWithShortCut(manipulate, workflow.getGroupVerticallyAction(),
				KeyEvent.VK_G);
		MenuUtils.addWithShortCut(manipulate, workflow.getLigatureAction(), KeyEvent.VK_L);
		MenuUtils.addWithShortCut(manipulate, workflow.getLigatureBeforeAction(),
				KeyEvent.VK_K);
		MenuUtils.addWithShortCut(manipulate, workflow.getLigatureAfterAction(),
				KeyEvent.VK_M);
		MenuUtils.addWithShortCut(manipulate, workflow.getExplodeGroupAction(),
				KeyEvent.VK_E);
		manipulate.addSeparator();

		MenuUtils.addWithShortCut(manipulate, workflow.getInsertSpaceAction(),
				KeyEvent.VK_P);
		MenuUtils.addWithShortCut(manipulate, workflow.getInsertHalfSpaceAction(),
				KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK);
		MenuUtils.addWithShortCut(manipulate, workflow.getInsertPageBreakAction(),
				KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);

		manipulate.add(workflow.getInsertRedPointAction());
		manipulate.add(workflow.getInsertBlackPointAction());

		// addWithShortCut(manipulate, workflow.getInsertRedPointAction(),
		// KeyEvent.VK_O);
		// addWithShortCut(manipulate, workflow.getInsertBlackPointAction(),
		// KeyEvent.VK_O, InputEvent.SHIFT_DOWN_MASK);

		JMenu shadingSymbols = new JMenu("Shading symbols");
		shadingSymbols
				.setToolTipText("Shading shapes used as symbols (not combined with signs)");
		shadingSymbols.add(workflow.getInsertFullShadingAction());
		shadingSymbols.add(workflow.getInsertVerticalShadingAction());
		shadingSymbols.add(workflow.getInsertHorizontalShadingAction());
		shadingSymbols.add(workflow.getInsertQuarterShadingAction());
		manipulate.add(shadingSymbols);

		manipulate.addSeparator();
		// addWithShortCut(manipulate, workflow.getShadeZoneAction(),
		// KeyEvent.VK_O); // doesn't work on mac
		manipulate.add(workflow.getShadeZoneAction());
		// addWithShortCut(manipulate, workflow.getUnShadeZoneAction(),
		// KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK);
		manipulate.add(workflow.getUnShadeZoneAction());
		// manipulate.add(workflow.getInsertPageBreakAction());
		MenuUtils.addWithShortCut(manipulate, workflow.getPaintRedAction(), KeyEvent.VK_R);
		MenuUtils.addWithShortCut(manipulate, workflow.getPaintBlackAction(),
				KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK);
		MenuUtils.addWithShortCut(manipulate, workflow.getEditGroupAction(),
				KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK);
		manipulate.addSeparator();
		// manipulate.add(sign);
		buildShadingMenu(manipulate);
		buildCartoucheMenu(manipulate);
		buildPhilologicalMenu(manipulate);

		menubar.add(manipulate);
		// Sign manipulation menu.
		JMenu sign = new JMenu("Sign");
		sign.setMnemonic(KeyEvent.VK_S);
		sign.add(workflow.getReverseSignAction());

		JMenu size = new JMenu("Size");
		size.setMnemonic(KeyEvent.VK_S);
		// Sizes : 200 144 120 100 70 50 35 25
		int sizes[] = { 240, 200, 144, 120, 100, 70, 50, 35, 25, 1 };
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
		// grammar and stuff.
		sign.addSeparator();
		buildShadeSignMenu(sign);

		menubar.add(sign);

		JMenu view = new JMenu("View");
		view.setMnemonic(KeyEvent.VK_V);
		view.add(workflow.getHalfZoomAction());
		view.add(workflow.getDoubleZoomAction());
		view.add(workflow.getResetZoomAction());
		view.addSeparator();
		// FIXME : design a cleaner system for actions (xml definition file ?)
		JMenu orientationMenu = new JMenu("Orientation");
		JMenu directionMenu = new JMenu("Direction");
		view.add(orientationMenu);
		view.add(directionMenu);
		view.addSeparator();
		// Small signs on base line ?
		view
				.add(new JCheckBoxMenuItem(workflow
						.getSelectCenteredSignsAction()));
		menubar.add(view);

		ButtonGroup orientationGroup = new ButtonGroup();
		JRadioButtonMenuItem horizontalButton = new JRadioButtonMenuItem(
				new ForwardedAction(workflow, "Horizontal",
						MDCEditorKeyManager.SELECT_HORIZONTAL_ORIENTATION));
		JRadioButtonMenuItem verticalButton = new JRadioButtonMenuItem(
				new ForwardedAction(workflow, "Vertical",
						MDCEditorKeyManager.SELECT_VERTICAL_ORIENTATION));
		orientationGroup.add(horizontalButton);
		orientationGroup.add(verticalButton);
		orientationMenu.add(horizontalButton);
		orientationMenu.add(verticalButton);

		ButtonGroup directionGroup = new ButtonGroup();
		JRadioButtonMenuItem leftToRightButton = new JRadioButtonMenuItem(
				new ForwardedAction(workflow, "Left to Right",
						MDCEditorKeyManager.SELECT_L2R_DIRECTION));
		JRadioButtonMenuItem rightToLeftButton = new JRadioButtonMenuItem(
				new ForwardedAction(workflow, "Right to Left",
						MDCEditorKeyManager.SELECT_R2L_DIRECTION));
		directionGroup.add(rightToLeftButton);
		directionGroup.add(leftToRightButton);
		directionMenu.add(leftToRightButton);
		directionMenu.add(rightToLeftButton);

		buildHieroglyphicMenus(menubar);

		buildToolsMenus(menubar);

		menubar.add(Box.createHorizontalGlue());
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_L);
		help.add(new AboutAction("About JSesh", frame));
		help.add(new DocumentationAction("JSesh user documentation"));
		help.add(new DisplayJavaPropertiesAction("Display java properties",
				frame));
		help.add(new AbstractAction("Garbage collector") {

			public void actionPerformed(ActionEvent e) {
				System.gc();
			}
		});

		menubar.add(help);
		return menubar;
	}

	private void buildToolsMenus(JMenuBar menubar2) {
		JMenu tools = new JMenu("Tools");
		tools.add(new WorkflowMethodAction(workflow,
				"Hide/Show hieroglyph palette", "toggleHieroglyphPalette"));

		tools.add(workflow.getAddNewSignsAction());

		tools.addSeparator();
		// JMenuItem prefs= new JMenuItem("Edit preferences");
		tools.add(new EditPreferencesAction("Edit preferences", workflow));
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
		String tips[] = { "Editor addition", "Erased text",
				"Previously readable signs", "Scribe addition",
				"Superfluous signs", "Explanatory addition", "Dubious reading" };
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

	private void buildShadeSignMenu(JMenu parent) {
		JMenu signShading = new JMenu("Sign Shading");
		JPopupMenu pm = signShading.getPopupMenu();
		pm.setLayout(new GridLayout(0, 4));
		for (int i = 0; i < workflow.getShadeSignActions().length; i++) {
			signShading.add(workflow.getShadeSignActions()[i]);
		}
		parent.add(signShading);
	}

	/**
	 * @param menubar
	 */
	private void buildHieroglyphicMenus(JMenuBar menubar) {
		List families = CompositeHieroglyphsManager.getInstance().getFamilies();

		JMenu hieroglyphs = new JMenu("Basic Hieroglyphs");

		hieroglyphs.getPopupMenu().setLayout(new GridLayout(14, 2));
		for (int i = 0; i < families.size(); i++) {
			HieroglyphFamily family = (HieroglyphFamily) families.get(i);

			HieroglyphicMenu fmenu = new HieroglyphicMenu(family.getCode()
					+ ". " + family.getDescription(), family.getCode(), 6);

			fmenu.setHieroglyphicMenuListener(new HieroglyphicMenuMediator());
			if (i < 25)
				fmenu.setMnemonic(family.getCode().toUpperCase().charAt(0));
			else if (i == 25) {
				fmenu.setMnemonic(KeyEvent.VK_J);
			} else if (i == 26)
				fmenu.setMnemonic(KeyEvent.VK_AMPERSAND);
			hieroglyphs.add(fmenu);
		}
		hieroglyphs.add(new HieroglyphicMenu("Tall Narrow Signs",
				HieroglyphicMenu.TALL_NARROW, 6));
		hieroglyphs.add(new HieroglyphicMenu("Low Broad Signs",
				HieroglyphicMenu.LOW_BROAD, 6));
		hieroglyphs.add(new HieroglyphicMenu("Low Narrow Signs",
				HieroglyphicMenu.LOW_NARROW, 6));

		hieroglyphs.setMnemonic(KeyEvent.VK_H);

		menubar.add(hieroglyphs);

	}

	private class HieroglyphicMenuMediator implements HieroglyphicMenuListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener
		 * #codeSelected(java.lang.String)
		 */
		public void codeSelected(String code) {
			workflow.insert(code);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener
		 * #enter(java.lang.String)
		 */
		public void enter(String code) {
			workflow.displaySignInfo(code);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdcDisplayer.swing.hieroglyphicMenu.HieroglyphicMenuListener
		 * #exit(java.lang.String)
		 */
		public void exit(String code) {
			workflow.setMessage("");
		}
	}

	/**
	 * @param f
	 */
	public void openFile(File f) {
		workflow.loadFile(f);
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
		 * @see
		 * java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
		 */
		public void windowOpened(WindowEvent e) {
			splash.setVisible(false);
			splash.dispose();
		}
	}

	public static void prepareApplication() {
		// We should have a more flexible look and feel system.
		// However, for Linux, I like liquid.
		try {
			if (PlatformDetection.getPlatform() == PlatformDetection.UNIX) {

				// UIManager.setLookAndFeel(new KunststoffLookAndFeel());
				// UIManager.setLookAndFeel(new LiquidLookAndFeel());
				// UIManager.setLookAndFeel(new MetouiaLookAndFeel());
				// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");

				// UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
				// OyoahaLookAndFeel lnf = new OyoahaLookAndFeel();
				// UIManager.setLookAndFeel(lnf);
				// For fun, napkin is real nice :-)
				// UIManager.setLookAndFeel("net.sourceforge.napkinlaf.NapkinLookAndFeel");
			} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS) {

				UIManager
						.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
				// UIManager.setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel;");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			System.setProperty("apple.awt.fractionalmetrics", "on");
		}
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

	}

	public static MDCDisplayerAppli startAppli(String args[]) {

		MDCDisplayerAppli a = new MDCDisplayerAppli();

		boolean noTextWasLoaded = true;

		if (args.length > 0) {
			File f = new File(args[0]);
			if (f.exists()) {
				a.openFile(f);
				noTextWasLoaded = false;
			}
		}

		if (noTextWasLoaded) {
			a.loadDemo();
		}

		if (!CompositeHieroglyphsManager.getInstance().isUserFileCorrect()) {
			JOptionPane
					.showMessageDialog(a.frame,
							"I had difficulties reading the signs definitions\n"
									+ "Maybe it's because of the file "
									+ CompositeHieroglyphsManager
											.getUserSignDefinitionFile()
									+ "\n"
									+ "Error message was : \""
									+ CompositeHieroglyphsManager.getInstance()
											.getUserFileMessage() + "\"",
							"Error loading signs definition",
							JOptionPane.ERROR_MESSAGE);
		}
		StartMe run = new StartMe(a.frame);
		SwingUtilities.invokeLater(run);
		return a;
	}

	/**
	 * The application should be built more carefully regarding threads.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final SplashScreen splash = new SplashScreen();
		final String[] arguments = args;
		splash.display();
		// Quick hack.
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// Swing commands should be performed in the graphical
					// thread.
					// Hence we create a runnable to actually start the program.

					MDCDisplayerAppli a = startAppli(arguments);
					a.setSplash(splash);

					// SwingUtilities.invokeLater(run);
					// In order to have a nice splash screen, we should do more
					// work
					// * move splash screen creation out of MDCDisplayerAppli
					// constructor
					// * probably move some of MDCDisplayerAppli's creation work
					// out of
					// the graphical thread.
					// run.run();
				} catch (Exception e) {
					displayErrorMessage(e);
				}
			}
		});
	}

	private static void displayErrorMessage(Exception e) {
		final String message = "An unexpected error occurred when starting jsesh.\n"
				+ "can you please note the message and send a mail to serge.rosmorduc AT qenherkhopeshef.org about it?\n"
				+ e.getMessage();
		StringWriter string = new StringWriter();
		PrintWriter out = new PrintWriter(string);
		e.printStackTrace(out);
		final String stackTrace = string.toString();
		e.printStackTrace();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JTextArea textArea = new JTextArea(message + "\n" + stackTrace);
				JScrollPane pane = new JScrollPane(textArea);
				pane.setPreferredSize(new Dimension(640, 400));
				JOptionPane.showMessageDialog(null, pane, "Problem",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	public void quit() {
		workflow.exit();
	}

	public void setVisible(boolean b) {
		frame.setVisible(true);
	}

	public void setSplash(SplashScreen splash) {
		frame.addWindowListener(new SplashCloser(splash));
	}

}