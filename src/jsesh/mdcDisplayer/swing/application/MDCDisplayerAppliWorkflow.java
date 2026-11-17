package jsesh.mdcDisplayer.swing.application;

/**
 * Changelog:
 * 2004/04/28 : modified saveText so that all texts get an extension.
 */

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jsesh.externalFonts.ExternalSignImporter;
import jsesh.graphicExport.BitmapExporter;
import jsesh.graphicExport.CaretBroker;
import jsesh.graphicExport.ExportData;
import jsesh.graphicExport.HTMLExporter;
import jsesh.graphicExport.IPDFExporter;
import jsesh.graphicExport.RTFExportPreferences;
import jsesh.graphicExport.RTFExporterUI;
import jsesh.graphicExport.SVGExporter;
import jsesh.graphicExport.WMFExporter;
import jsesh.graphicExport.RTFExportPreferences.RTFExportGranularity;
import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.ModelWriter;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.secondaryData.MDCCaret;
import jsesh.mdcDisplayer.swing.actionsUtils.PreferenceBoundAction;
import jsesh.mdcDisplayer.swing.actionsUtils.PreferencesChangeListener;
import jsesh.mdcDisplayer.swing.application.actions.AddNewSignAction;
import jsesh.mdcDisplayer.swing.application.actions.AddPhilologyAction;
import jsesh.mdcDisplayer.swing.application.actions.CartoucheAction;
import jsesh.mdcDisplayer.swing.application.actions.EditGroupAction;
import jsesh.mdcDisplayer.swing.application.actions.InsertElementAction;
import jsesh.mdcDisplayer.swing.application.actions.InsertElementIconAction;
import jsesh.mdcDisplayer.swing.application.actions.RotationAction;
import jsesh.mdcDisplayer.swing.application.actions.ShadeAction;
import jsesh.mdcDisplayer.swing.application.actions.SizeAction;
import jsesh.mdcDisplayer.swing.application.actions.generic.ForwardedAction;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;
import jsesh.mdcDisplayer.swing.editor.MDCModelTransferableBroker;
import jsesh.mdcDisplayer.swing.groupEditor.GroupEditorDialog;
import jsesh.mdcDisplayer.swing.preferencesEditor.PreferencesEditor;
import jsesh.mdcDisplayer.swing.preferencesEditor.PreferencesFacade;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.utils.MdcFileDialog;
import jsesh.utils.FileUtils;
import jsesh.utils.SimpleStringModel;
import jsesh.utils.SystemUtils;

/**
 * This class represents the top-level logic of the editor. This file is free
 * Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * Patterns: Mediator
 * 
 * IMPORTANT : this class is WAY TOO LARGE. WE SHOULD CLEAN UP THE
 * INITIALIZATION PROCESS OF ALL DATA.
 * 
 * TODO Separate in a) action catalog b) shared data (in particular, a list of
 * editors)
 * 
 * Actions and menus should be created from an XML file.
 * 
 * (c) Serge Rosmorduc
 * 
 * @author Serge Rosmorduc
 */

public class MDCDisplayerAppliWorkflow implements CaretBroker,
		MDCModelTransferableBroker {

	private PreferenceBoundAction addNewSignsAction = new AddNewSignAction(
			"Add new signs", this);

	private BitmapExporter bitmapExporter = null;

	private CaretActionManager caretActionManager;

	private CartoucheAction[] cartoucheActions;

	private AbstractAction copyAction;

	/**
	 * The file currently being edited.
	 */
	private File currentFile;

	/**
	 * The directory which contains the mdc files
	 */
	private File currentMDCDirectory;

	/**
	 * The current RTF export file.
	 */

	private File currentRTFExportFile = null;

	/**
	 * The current directory for outputing results, such as pdf files, png
	 * files...
	 */

	private File currentOutputDirectory;

	/**
	 * The directory where the source for SVG, TTF fonts and the like are read.
	 */
	private File currentHieroglyphsSource;

	private AbstractAction cutAction;

	private HieroglyphicTextModel data;

	private DrawingSpecifications drawingSpecifications;

	private AbstractAction doubleZoomAction;

	private AbstractAction editGroupAction = new EditGroupAction("Edit group",
			this);

	private AbstractAction explodeGroupAction;

	private MDCDisplayerAppliFrame frame;

	private AbstractAction groupHorizontallyAction;

	private AbstractAction groupVerticallyAction;

	private AbstractAction halfZoomAction;

	// Data for HTML export (created on request).
	private HTMLExporter htmlExporter = null;

	private AbstractAction insertBlackPointAction = new InsertElementIconAction(
			new Hieroglyph(SymbolCodes.BLACKPOINT), "O", this);

	private InsertElementAction insertHalfSpaceAction = new InsertElementAction(
			"Insert Half Space", new Hieroglyph(SymbolCodes.HALFSPACE), this);

	// Data for PDF export using IText (created on request too)

	private AbstractAction insertPageBreakAction;

	private AbstractAction insertRedPointAction = new InsertElementIconAction(
			new Hieroglyph(SymbolCodes.REDPOINT), "o", this);

	private AbstractAction insertSpaceAction = new InsertElementAction(
			"Insert Space", new Hieroglyph(SymbolCodes.FULLSPACE), this);

	private IPDFExporter iPDFExporter = null;

	private AbstractAction ligatureAction = new AbstractAction(
			"Ligature elements") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureElements();
		}

	};

	private AbstractAction ligatureBeforeAction = new AbstractAction(
			"ligature group with hieroglyph") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureBefore();
		}

	};

	private AbstractAction ligatureAfterAction = new AbstractAction(
			"ligature hieroglyph with group") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureAfter();
		}

	};

	/**
	 * Informations to display.
	 * 
	 */
	private SimpleStringModel message;

	private AbstractAction paintBlackAction = new AbstractAction(
			"Paint zone in black") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().redZone(false);
		}

	};

	private AbstractAction paintRedAction = new AbstractAction(
			"Paint zone in Red") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().redZone(true);
		}

	};

	private AbstractAction pasteAction;

	private AbstractAction[] philologicalActions = null;

	private Preferences preferences;

	private AbstractAction resetZoomAction;

	private AbstractAction reverseSignAction = new AbstractAction(
			"Reverse sign") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().reverseSign();
		}
	};

	private AbstractAction[] rotationActions = null;

	private AbstractAction selectAllAction;

	private AbstractAction setBoldModeAction = new AbstractAction(
			"Edit bold text") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('b');
		}

	};

	private AbstractAction setHieroglyphicModeAction = new AbstractAction(
			"Edit Hieroglyphs") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('s');
		}

	};

	private AbstractAction setItalicModeAction = new AbstractAction(
			"Edit italic text") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('i');
		}

	};

	private AbstractAction setLatinModeAction = new AbstractAction(
			"Edit latin text") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('l');
		}

	};

	private AbstractAction setTransliterationModeAction = new AbstractAction(
			"Edit translitteration") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('t');
		}

	};

	private AbstractAction setLinePageNumberModeAction = new AbstractAction(
			"Edit line/page number") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setMode('|');
		}

	};

	private AbstractAction[] shadeActions;

	private AbstractAction shadeZoneAction = new AbstractAction("Shade zone") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().shadeZone(true);
		}

	};

	private AbstractAction signIsInWordAction = new AbstractAction(
			"Sign inside a word") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsInsideWord();
		}
	};

	private AbstractAction signIsSentenceEndAction = new AbstractAction(
			"Sign is at sentence end") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsAtSentenceEnd();
		}
	};

	private AbstractAction signIsWordEndAction = new AbstractAction(
			"Sign is at word end") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsAtWordEnd();
		}
	};

	private AbstractAction toggleGrammarAction = new AbstractAction(
			"Toggle Grammar") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleGrammar();
		}
	};

	private AbstractAction toggleIgnoredSignAction = new AbstractAction(
			"Toggle Ignored Sign") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleIgnoredSign();
		}
	};

	private AbstractAction toggleRedSignAction = new AbstractAction(
			"Toggle sign is red") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleRedSign();
		}
	};

	private AbstractAction toggleWideSignAction = new AbstractAction(
			"Toggle wide sign") {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleWideSign();
		}
	};

	private AbstractAction unShadeZoneAction = new AbstractAction(
			"Unshade zone") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().shadeZone(false);
		}

	};

	private WMFExporter wmfExporter;
	
	private SVGExporter svgExporter;

	private List preferencesListeners;

	private RTFExportPreferences rtfExportPreferences[];

	private LengthUnit preferedUnit;

	/**
	 * Index of rtfExportPreferences which will be used when copying
	 */

	private int rtfExportPreferencesIndex;

	public MDCDisplayerAppliWorkflow(HieroglyphicTextModel data) {
		this.data = data;
		preferencesListeners = new ArrayList();
		preferences = Preferences.userNodeForPackage(this.getClass());

		rtfExportPreferences = new RTFExportPreferences[] {
				new RTFExportPreferences(20,
						RTFExportGranularity.ONE_PICTURE_PER_CADRAT),
				new RTFExportPreferences(14,
						RTFExportGranularity.ONE_PICTURE_PER_CADRAT),
				new RTFExportPreferences(20,
						RTFExportGranularity.ONE_PICTURE_PER_CADRAT) };

		rtfExportPreferencesIndex = 0;

		preferedUnit = LengthUnit.POINT;

		wmfExporter = new WMFExporter();
		svgExporter= new SVGExporter();
		setPhilologySign(true);
		this.frame = null;
		String dir = System.getProperty("user.dir");
		currentMDCDirectory = new File(dir);
		currentOutputDirectory = new File(dir);
		currentHieroglyphsSource = new File(dir);
		message = new SimpleStringModel("");

		buildActions();
		drawingSpecifications = new DrawingSpecifications();
		refreshFromPreferences();
		currentFile = getDefaultFile();

		// FIXME : All actions which are preference listeners should be added at
		// some point.
		// we must do it after creating them and after creating the preferences
		// node.
		// the best way in the final version would probably be through
		// reflection.
		addPreferencesChangeListener(addNewSignsAction);
		firePreferencesChange();
	}

	/**
	 * 
	 */
	public void editGroup() {
		AbsoluteGroup g = getEditor().getWorkflow().buildAbsoluteGroup();
		if (g != null) {
			GroupEditorDialog d = new GroupEditorDialog();
			d.setGroup(g);
			JOptionPane.showConfirmDialog(frame, d, "Group editor",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			getEditor().getWorkflow().fixAbsoluteGroup(g);
		}
	}

	/**
	 * Import new signs in the sign database.
	 */
	public void importNewSign() {
		ExternalSignImporter importer = new ExternalSignImporter();
		importer.setSourceDirectory(currentHieroglyphsSource);
		JOptionPane.showMessageDialog(frame, importer.getPanel(),
				"Import new signs", JOptionPane.PLAIN_MESSAGE);
		currentHieroglyphsSource = importer.getSourceDirectory();

	}

	/**
	 * @param shade
	 */
	public void doShade(int shade) {
		getEditor().getWorkflow().doShade(shade);
	}

	/**
	 * @param code
	 */
	public void addPhilologicalMarkup(int code) {
		getEditor().getWorkflow().addPhilologicalMarkup(code);
	}

	/**
	 * @param element
	 */
	public void insertElement(ModelElement element) {
		getEditor().getWorkflow().insertElement(element);
	}

	/**
	 * @param type
	 * @param start
	 * @param end
	 */
	public void addCartouche(int type, int start, int end) {
		getEditor().getWorkflow().addCartouche(type, start, end);
	}

	private File getDefaultFile() {
		return new File(currentMDCDirectory, "default.gly");
	}

	public void addPreferencesChangeListener(PreferencesChangeListener l) {
		preferencesListeners.add(l);
	}

	public void firePreferencesChange() {
		for (int i = 0; i < preferencesListeners.size(); i++)
			((PreferencesChangeListener) preferencesListeners.get(i))
					.preferencesChanged();
	}

	private void buildActions() {

		halfZoomAction = new AbstractAction("unzoom") {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				getEditor().setScale(getEditor().getScale() / Math.sqrt(2.0));
			}

		};
		doubleZoomAction = new AbstractAction("zoom") {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				getEditor().setScale(getEditor().getScale() * Math.sqrt(2.0));
			}

		};

		resetZoomAction = new AbstractAction("Reset zoom") {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				getEditor().setScale(2.0);
			}

		};

		selectAllAction = new AbstractAction("Select All") {

			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().selectAll();

			}

		};

		insertPageBreakAction = new AbstractAction("Insert Page Break") {

			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().insertPageBreak();

			}

		};

		explodeGroupAction = new AbstractAction("Explode group") {

			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().explodeGroup();

			}

		};

		groupVerticallyAction = new AbstractAction("Group Vertically") {

			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().groupVertical();
			}
		};

		groupHorizontallyAction = new AbstractAction("Group Horizontally") {

			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().groupHorizontal();
			}

		};

		pasteAction = new ForwardedAction(this, "Paste", "PASTE");

		// Create the copy action. Actually, this should be done through a
		// configuration file

		copyAction = new ForwardedAction(this, "Copy", "COPY");

		cutAction = new ForwardedAction(this, "Cut", "CUT");

		buildShadeActions();
		buildCartoucheActions();

		int angles[] = { 0, 30, 45, 60, 90, 120, 135, 150, 180, 210, 225, 240,
				270, 300, 315, 330 };
		rotationActions = new AbstractAction[angles.length];
		for (int i = 0; i < rotationActions.length; i++)
			rotationActions[i] = new RotationAction(angles[i], this);

	}

	/**
	 * Clear the current text and prepare for a new one.
	 */
	public void newText() {
		getEditor().getWorkflow().clear();
		currentFile = getDefaultFile();
	}

	private void buildCartoucheActions() {
		cartoucheActions = new CartoucheAction[23];
		int i = 0;

		cartoucheActions[i++] = new CartoucheAction('c', 1, 2,
				"<-ra-mn:n-xpr\\R270->", this);

		cartoucheActions[i++] = new CartoucheAction('c', 1, 1,
				"<1-ra-mn:n-xpr\\R270-1>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 2, 1,
				"<2-ra-mn:n-xpr\\R270-1>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 2, 1,
				"<2-ra-mn:n-xpr\\R270-2>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 0, 1,
				"<0-ra-mn:n-xpr\\R270-1>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 1, 0,
				"<1-ra-mn:n-xpr\\R270-0>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 2, 0,
				"<2-ra-mn:n-xpr\\R270-0>", this);
		cartoucheActions[i++] = new CartoucheAction('c', 0, 2,
				"<0-ra-mn:n-xpr\\R270-2>", this);

		cartoucheActions[i++] = new CartoucheAction('s', 1, 2,
				"<s-E1:D40-xa:M-R19->", this);
		cartoucheActions[i++] = new CartoucheAction('s', 2, 1,
				"<s2-E1:D40-xa:M-R19-s1>", this);

		cartoucheActions[i++] = new CartoucheAction('h', 1, 2,
				"<h1-ra-xa-f-h2>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 1, 3,
				"<h1-ra-xa-f-h3>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 1, 1,
				"<h1-ra-xa-f-h1>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 1, 0,
				"<h1-ra-xa-f-h0>", this);

		cartoucheActions[i++] = new CartoucheAction('h', 2, 1,
				"<h2-ra-xa-f-h1>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 2, 0,
				"<h2-ra-xa-f-h0>", this);

		cartoucheActions[i++] = new CartoucheAction('h', 3, 1,
				"<h3-ra-xa-f-h1>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 3, 0,
				"<h3-ra-xa-f-h0>", this);

		cartoucheActions[i++] = new CartoucheAction('h', 0, 2,
				"<h0-ra-xa-f-h2>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 0, 3,
				"<h0-ra-xa-f-h3>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 0, 1,
				"<h0-ra-xa-f-h1>", this);
		cartoucheActions[i++] = new CartoucheAction('h', 0, 0,
				"<h0-ra-xa-f-h0>", this);

		cartoucheActions[i++] = new CartoucheAction('F', 1, 2, "<F-ra-xa-f->",
				this);

	}

	/**
	 * Build actions for shading.
	 */
	private void buildShadeActions() {
		shadeActions = new ShadeAction[16];
		shadeActions[0] = new ShadeAction(Cadrat.NONE, "..", this);
		shadeActions[1] = new ShadeAction(Cadrat.TOP_START, "..#1", this);
		shadeActions[2] = new ShadeAction(Cadrat.TOP_END, "..#2", this);
		shadeActions[3] = new ShadeAction(Cadrat.TOP_END | Cadrat.TOP_START,
				"..#12", this);
		shadeActions[4] = new ShadeAction(Cadrat.BOTTOM_START, "..#3", this);
		shadeActions[5] = new ShadeAction(Cadrat.BOTTOM_START
				| Cadrat.TOP_START, "..#13", this);
		shadeActions[6] = new ShadeAction(Cadrat.BOTTOM_START | Cadrat.TOP_END,
				"..#23", this);
		shadeActions[7] = new ShadeAction(Cadrat.BOTTOM_START | Cadrat.TOP_END
				| Cadrat.TOP_START, "..#123", this);
		shadeActions[8] = new ShadeAction(Cadrat.BOTTOM_END, "..#4", this);
		shadeActions[9] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_START,
				"..#14", this);
		shadeActions[10] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_END,
				"..#24", this);
		shadeActions[11] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_END
				| Cadrat.TOP_START, "..#124", this);
		shadeActions[12] = new ShadeAction(Cadrat.BOTTOM_END
				| Cadrat.BOTTOM_START, "..#34", this);
		shadeActions[13] = new ShadeAction(Cadrat.BOTTOM_END
				| Cadrat.BOTTOM_START | Cadrat.TOP_START, "..#134", this);
		shadeActions[14] = new ShadeAction(Cadrat.BOTTOM_END
				| Cadrat.BOTTOM_START | Cadrat.TOP_END, "..#234", this);
		shadeActions[15] = new ShadeAction(Cadrat.BOTTOM_END
				| Cadrat.BOTTOM_START | Cadrat.TOP_END | Cadrat.TOP_START,
				"..#1234", this);
	}

	/**
	 * @param size
	 *            a zoom value, in percentage.
	 * @return a size action for the given size.
	 */
	public AbstractAction buildSizeAction(int size) {
		return new SizeAction(size, this);
	}

	/**
	 * @param code
	 */
	public void displaySignInfo(String code) {
		String info = code + " : ";
		List l = HieroglyphsManager.getInstance().getValuesFor(code);
		if (l != null) {
			Iterator i = l.iterator();
			while (i.hasNext())
				info += i.next().toString() + " ";
		}
		setMessage(info);
	}

	/**
	 * Method called when the program exits normally.
	 */
	public void exit() {
		savePreferences();
		System.exit(0);
	}

	/**
	 * @return Returns the addNewSignsAction.
	 */
	public AbstractAction getAddNewSignsAction() {
		return addNewSignsAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.graphicExport.CaretProvider#getCaret()
	 */
	public MDCCaret getCaret() {
		return frame.editor.getWorkflow().getCaret();
	}

	/**
	 * @return Returns the cartoucheActions.
	 */
	public CartoucheAction[] getCartoucheActions() {
		return cartoucheActions;
	}

	/**
	 * @return a copyAction.
	 */
	public AbstractAction getCopyAction() {
		return copyAction;
	}

	/**
	 * Returns the current directory for this application.
	 * 
	 * @return Returns the currentMDCDirectory.
	 */
	public File getCurrentMDCDirectory() {
		return currentMDCDirectory;
	}

	/**
	 * @return Returns the currentOutputDirectory.
	 */
	public File getCurrentOutputDirectory() {
		return currentOutputDirectory;
	}

	/**
	 * @return a cut Action;
	 */
	public AbstractAction getCutAction() {
		return cutAction;
	}

	public HieroglyphicTextModel getData() {
		return data;
	}

	public AbstractAction getDoubleZoomAction() {
		return doubleZoomAction;
	}

	/**
	 * @return Returns the drawingSpecifications.
	 */
	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}

	public AbstractAction getEditGroupAction() {
		return editGroupAction;
	}

	/**
	 * Returns the current editor. (currently, the only editor !)
	 * 
	 * @return the editor.
	 */
	public JMDCEditor getEditor() {
		return frame.editor;
	}

	/**
	 * @return an explode group Action.
	 */
	public AbstractAction getExplodeGroupAction() {
		return explodeGroupAction;
	}

	/**
	 * @return a GroupHorizontallyAction
	 */
	public AbstractAction getGroupHorizontallyAction() {
		return groupHorizontallyAction;
	}

	/**
	 * @return a GroupVerticallyAction
	 */
	public AbstractAction getGroupVerticallyAction() {
		return groupVerticallyAction;
	}

	public AbstractAction getHalfZoomAction() {
		return halfZoomAction;
	}

	/**
	 * @return Returns the insertBlackPointAction.
	 */
	public AbstractAction getInsertBlackPointAction() {
		return insertBlackPointAction;
	}

	/**
	 * @return InsertHalfSpaceAction
	 */
	public AbstractAction getInsertHalfSpaceAction() {
		return insertHalfSpaceAction;
	}

	/**
	 * @return InsertPageBreakAction
	 */
	public AbstractAction getInsertPageBreakAction() {
		return insertPageBreakAction;
	}

	/**
	 * @return Returns the insertRedPointAction.
	 */
	public AbstractAction getInsertRedPointAction() {
		return insertRedPointAction;
	}

	/**
	 * @return InsertSpaceAction
	 */
	public AbstractAction getInsertSpaceAction() {
		return insertSpaceAction;
	}

	/**
	 * @return a LigatureAction
	 */
	public AbstractAction getLigatureAction() {
		return ligatureAction;
	}

	/**
	 * @return the message
	 * 
	 */
	public SimpleStringModel getMessage() {
		return message;
	}

	/**
	 * @return Returns the paintBlackAction.
	 */
	public AbstractAction getPaintBlackAction() {
		return paintBlackAction;
	}

	/**
	 * @return Returns the paintRedAction.
	 */

	public AbstractAction getPaintRedAction() {
		return paintRedAction;
	}

	/**
	 * @return a paste action.
	 */
	public AbstractAction getPasteAction() {
		return pasteAction;
	}

	/**
	 * @return Returns the philologicalActions.
	 */
	public AbstractAction[] getPhilologicalActions() {
		return philologicalActions;
	}

	public AbstractAction[] getPhilologyActions() {
		if (philologicalActions == null) {

			AbstractAction buildAddedSignsBlock = new AddPhilologyAction(
					"[&-gm-D3-&]", SymbolCodes.EDITORADDITION, this);

			AbstractAction buildErasedSignBlock = new AddPhilologyAction(
					"[[-gm-D3-]]", SymbolCodes.ERASEDSIGNS, this);

			AbstractAction buildPreviouslyReadableBlock = new AddPhilologyAction(
					"[\"-gm-D3-\"]", SymbolCodes.PREVIOUSLYREADABLE, this);

			AbstractAction buildScribeAdditionBlock = new AddPhilologyAction(
					"['-gm-D3-']", SymbolCodes.SCRIBEADDITION, this);

			AbstractAction buildSuperfluousSignsBlock = new AddPhilologyAction(
					"[{-gm-D3-}]", SymbolCodes.EDITORSUPERFLUOUS, this);

			List l = new ArrayList();
			AbstractAction block[] = { buildErasedSignBlock,
					buildAddedSignsBlock, buildSuperfluousSignsBlock,
					buildPreviouslyReadableBlock, buildScribeAdditionBlock };

			l.addAll(Arrays.asList(block));
			l.add(new InsertElementIconAction(SymbolCodes.BEGINERASE, this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDERASE, this));
			l.add(new InsertElementIconAction(SymbolCodes.BEGINEDITORADDITION,
					this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDEDITORADDITION,
					this));
			l.add(new InsertElementIconAction(
					SymbolCodes.BEGINEDITORSUPERFLUOUS, this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDEDITORSUPERFLUOUS,
					this));
			l.add(new InsertElementIconAction(
					SymbolCodes.BEGINPREVIOUSLYREADABLE, this));
			l.add(new InsertElementIconAction(
					SymbolCodes.ENDPREVIOUSLYREADABLE, this));
			l.add(new InsertElementIconAction(SymbolCodes.BEGINSCRIBEADDITION,
					this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDSCRIBEADDITION,
					this));
			philologicalActions = (AbstractAction[]) l
					.toArray(new AbstractAction[l.size()]);
		}
		return philologicalActions;
	}

	/**
	 * @return a reset zoom action.
	 * 
	 */
	public AbstractAction getResetZoomAction() {
		return resetZoomAction;
	}

	/**
	 * @return a reverse sign action.
	 */
	public AbstractAction getReverseSignAction() {
		return reverseSignAction;
	}

	/**
	 * @return rotation actions.
	 */
	public AbstractAction[] getRotations() {
		return rotationActions;
	}

	/**
	 * @return a select all action.
	 */
	public AbstractAction getSelectAllAction() {
		return selectAllAction;
	}

	/**
	 * @return Returns the setBoldModeAction.
	 */
	public AbstractAction getSetBoldModeAction() {
		return setBoldModeAction;
	}

	/**
	 * @return Returns the setHieroglyphicModeAction.
	 */
	public AbstractAction getSetHieroglyphicModeAction() {
		return setHieroglyphicModeAction;
	}

	/**
	 * @return Returns the setItalicModeAction.
	 */
	public AbstractAction getSetItalicModeAction() {
		return setItalicModeAction;
	}

	/**
	 * @return Returns the setLatinModeAction.
	 */
	public AbstractAction getSetLatinModeAction() {
		return setLatinModeAction;
	}

	/**
	 * @return Returns the setTransliterationModeAction.
	 */
	public AbstractAction getSetTransliterationModeAction() {
		return setTransliterationModeAction;
	}

	/**
	 * @return Returns the setLinePageNumberModeAction.
	 */
	public AbstractAction getSetLinePageNumberModeAction() {
		return setLinePageNumberModeAction;
	}

	public AbstractAction[] getShadeActions() {
		return shadeActions;
	}

	/**
	 * @return a shade action
	 */
	public AbstractAction getShadeZoneAction() {
		return shadeZoneAction;
	}

	/**
	 * @return Returns the signIsInWordAction.
	 */
	public AbstractAction getSignIsInWordAction() {
		return signIsInWordAction;
	}

	/**
	 * @return Returns the signIsSentenceEnd.
	 */
	public AbstractAction getSignIsSentenceEndAction() {
		return signIsSentenceEndAction;
	}

	/**
	 * @return Returns the signIsWordEndAction.
	 */
	public AbstractAction getSignIsWordEndAction() {
		return signIsWordEndAction;
	}

	/**
	 * @return a toggle grammar action.
	 */
	public AbstractAction getToggleGrammarAction() {
		return toggleGrammarAction;
	}

	/**
	 * @return a toggle ignored sign action.
	 */
	public AbstractAction getToggleIgnoredSignAction() {
		return toggleIgnoredSignAction;
	}

	/**
	 * @return Returns the toggleRedSign.
	 */
	public AbstractAction getToggleRedSignAction() {
		return toggleRedSignAction;
	}

	/**
	 * @return a toggle wide sign action.
	 */
	public AbstractAction getToggleWideSignAction() {
		return toggleWideSignAction;
	}

	/**
	 * @return Returns the unShadeZoneAction.
	 */
	public AbstractAction getUnShadeZoneAction() {
		return unShadeZoneAction;
	}

	/**
	 * @param code
	 */
	public void insert(String code) {
		getEditor().getWorkflow().addSign(code);
	}

	/**
	 * Returns true if philological markup is considered as a simple sign in the
	 * current text. If false, philological markup is considered as a kind of
	 * parenthesis.
	 * 
	 * @return Returns the philologySign.
	 */
	public boolean isPhilologySign() {
		return data.isPhilologyIsSign();
	}

	/**
	 * Open a file selection dialog, and load the corresponding text.
	 * 
	 */
	public void loadText() {
		MdcFileDialog dialog = new MdcFileDialog(currentMDCDirectory, "choose a manuel de codage text");
		int result = dialog.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			getEditor().clearText();
			currentMDCDirectory = dialog.getCurrentDirectory();
			File file = dialog.getSelectedFile();
			loadFile(file, dialog.getEncoding());
		}
	}

	/**
	 * Open a file.
	 * 
	 * @param file
	 */
	public void loadFile(File file) {
		loadFile(file, SystemUtils.getDefaultEncoding());
	}

	/**
	 * Open a file in a given encoding.
	 * 
	 * @param file
	 * @param encoding
	 */
	public void loadFile(File file, String encoding) {
		StringBuffer buffer = new StringBuffer();
		try {
			// We read the file in memory,
			// because we need to suppress non manuel de codage headers, if
			// any.

			Reader r = new InputStreamReader(new FileInputStream(file),
					encoding);
			int c;
			while ((c = r.read()) != -1) {
				buffer.append((char) c);
			}
			r.close();

			if (buffer.length() != 0 && buffer.charAt(0) == '@') {
				int first = buffer.indexOf("\n");
				if (first == -1)
					first = buffer.length();
				buffer.replace(0, first, "");
			}
			// handle the difference between winglyph and tksesh about
			// philology.
			setPhilologySign(!file.getName().endsWith(".hie"));

			data.readModel(new StringReader(buffer.toString()));
			currentFile = file;
		} catch (MDCSyntaxError e) {
			String msg = "error at line " + e.getLine();
			int infPos, supPos;
			if ((e.getCharPos() - 10) > 0)
				infPos = (e.getCharPos() - 10);
			else
				infPos = 0;
			if (e.getCharPos() + 10 > buffer.length())
				supPos = buffer.length();
			else
				supPos = e.getCharPos() + 10;
			msg += ". Wrong text : " + buffer.substring(infPos, supPos);
			JOptionPane.showMessageDialog(frame, msg, "Syntax Error",
					JOptionPane.ERROR_MESSAGE);
			e.getCharPos();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Switch between a text in which philological signs are a kind of
	 * parenthesis and a text in which it's a simple sign. The current system is
	 * pretty stupid when going from "signs" to parenthesis : it deletes all
	 * philological signs !
	 * 
	 */
	public void philologyToggle() {
		// TODO : write this.
	}

	/**
	 * Change a number of preferences for this program according to the user
	 * preferences.
	 * 
	 */
	public void refreshFromPreferences() {
		// working directory
		String path = preferences.get("currentMDCDirectory",
				currentMDCDirectory.getAbsolutePath());
		currentMDCDirectory = new File(path);
		String outputPath = preferences.get("currentOutputDirectory",
				currentOutputDirectory.getAbsolutePath());
		currentOutputDirectory = new File(outputPath);
		currentRTFExportFile = new File(preferences.get("RTFExportFile",
				new File(currentOutputDirectory, "unnamed.rtf")
						.getAbsolutePath()));

		// wmf export :
		wmfExporter.setExportFile(new File(preferences.get("wmfExportFile",
				new File(currentOutputDirectory, "unnamed.wmf").getPath())));
		// directory looked for loading external hieroglyph fonts
		path = preferences.get("currentHieroglyphsSource",
				currentHieroglyphsSource.getAbsolutePath());
		currentHieroglyphsSource = new File(path);
		// Cut and paste preferences.
		// "Large" set
		rtfExportPreferences[0].setCadratHeight(preferences.getInt(
				"rtf_large_size", 20));
		rtfExportPreferences[0].setExportGranularity(RTFExportGranularity
				.getGranularity(preferences.getInt("rtf_large_mode",
						RTFExportGranularity.ONE_PICTURE_PER_CADRAT.getId())));
		rtfExportPreferences[1].setCadratHeight(preferences.getInt(
				"rtf_small_size", 12));
		rtfExportPreferences[1].setExportGranularity(RTFExportGranularity
				.getGranularity(preferences.getInt("rtf_small_mode",
						RTFExportGranularity.ONE_PICTURE_PER_CADRAT.getId())));
		rtfExportPreferences[2].setCadratHeight(preferences.getInt(
				"rtf_file_size", 12));
		rtfExportPreferences[2].setExportGranularity(RTFExportGranularity
				.getGranularity(preferences.getInt("rtf_file_mode",
						RTFExportGranularity.GROUPED_CADRATS.getId())));

	}

	public void savePreferences() {
		// Also save the creator software version.
		preferences.put("prefversion", "2a10");
		preferences.put("currentMDCDirectory", currentMDCDirectory
				.getAbsolutePath());
		preferences.put("currentOutputDirectory", currentOutputDirectory
				.getAbsolutePath());
		preferences.put("wmfExportFile", wmfExporter.getExportFile()
				.getAbsolutePath());
		preferences.put("currentHieroglyphsSource", currentHieroglyphsSource
				.getAbsolutePath());
		// Save cut and paste preferences
		preferences.putInt("rtf_large_size", rtfExportPreferences[0]
				.getCadratHeight());
		preferences.putInt("rtf_large_mode", rtfExportPreferences[0]
				.getExportGranularity().getId());
		preferences.putInt("rtf_small_size", rtfExportPreferences[1]
				.getCadratHeight());
		preferences.putInt("rtf_small_mode", rtfExportPreferences[1]
				.getExportGranularity().getId());
		preferences.putInt("rtf_file_size", rtfExportPreferences[2]
				.getCadratHeight());
		preferences.putInt("rtf_file_mode", rtfExportPreferences[2]
				.getExportGranularity().getId());
	}

	public void saveText() {
		MdcFileDialog fc= new MdcFileDialog(currentMDCDirectory, "Save to");
				
		if (currentFile != null)
			fc.setSelectedFile(currentFile);
	
		int result = fc.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			if ((!currentFile.equals(fc.getSelectedFile()))
					&& fc.getSelectedFile().exists()) {
				int opt = JOptionPane.showConfirmDialog(frame, ""
						+ fc.getSelectedFile().getName()
						+ " will be replaced. Are you sure ?", "confirm",
						JOptionPane.YES_NO_OPTION);
				if (opt == JOptionPane.NO_OPTION)
					return;
			}
			saveFile(fc.getSelectedFile(), fc.getEncoding());
		}
	}

	public void saveFile(File file, String encoding) {

		String correctExtension = null;

		if (data.isPhilologyIsSign())
			correctExtension = "gly";
		else
			correctExtension = "hie";

		currentFile = FileUtils.buildFileWithExtension(file, correctExtension);
		
		Writer f= null;
		try {
			f= new OutputStreamWriter(
					new FileOutputStream(currentFile), encoding);
			ModelWriter w = new ModelWriter();
			w.write(f, data.getModel());
			f.close();
		} catch (IOException e) {
			String message = "Could not write to "
					+ currentFile.getAbsolutePath();
			if (!currentFile.canWrite())
				message += " (no write permission)";
			JOptionPane.showMessageDialog(frame, message);
		} finally {
			if (f!= null)
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
	 */
	public void setDrawingSpecifications(
			DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

	/**
	 * @param frame
	 * 
	 */
	public void setFrame(MDCDisplayerAppliFrame frame) {
		this.frame = frame;

		// NOW, WE can set the caret Action Manager.
		// TODO IMPORTANT ; the relationship between this workflow and frame is
		// unhealthy to say the least.
		// We should find a good abtraction for the system. See next line for
		// the general direction.
		// TODO IMPORTANT : prepare for the time of multiple editing sessions
		// !!!!
		caretActionManager = new CaretActionManager(getEditor().getWorkflow());
		caretActionManager.addAction(cutAction);
		caretActionManager.addAction(copyAction);
		// TODO : register this action as interested in the caret.
		// caretActionManager.addAction(wmfExportAction);
		caretActionManager.addAction(groupHorizontallyAction);
		caretActionManager.addAction(groupVerticallyAction);
		caretActionManager.addAction(shadeZoneAction);
		caretActionManager.addAction(unShadeZoneAction);
		caretActionManager.addAction(paintRedAction);
		caretActionManager.addAction(paintBlackAction);

	}

	/**
	 * @param code
	 */
	public void setMessage(String code) {
		message.setText(code);
	}

	/**
	 * Choose whether we consider philology markup as parenthesis or as signs in
	 * the current text.
	 * 
	 * @param philologySign
	 *            The philologySign to set.
	 */
	private void setPhilologySign(boolean philologySign) {
		data.setPhilologyIsSign(philologySign);
		for (int i = 5; i < getPhilologyActions().length; i++)
			getPhilologyActions()[i].setEnabled(philologySign);
	}

	/**
	 * Ugly system for editing preferences. A better one shall be used. FIXME :
	 * refactor the system for preferences.
	 */
	public void editPreferences() {
		PreferencesFacade preferencesFacade = new PreferencesFacade(
				getDrawingSpecifications(), preferedUnit, rtfExportPreferences);
		// Maybe facade should be an interface, and the workflow itself would be
		// the facade.
		PreferencesEditor preferencesEditor = new PreferencesEditor(frame,
				preferencesFacade);

		preferencesEditor.initializePreferences(preferencesFacade);
		int ok = preferencesEditor.askForPreferences();
		if (ok == JOptionPane.OK_OPTION) {
			preferencesEditor.updatePreferences(preferencesFacade);
			firePreferencesChange();
		}
		getEditor().invalidateView();
	}

	/**
	 * FIXME : rewrite this poor method. The whole drawing specification
	 * business is wanting.
	 * 
	 * @return
	 */
	public AbstractAction getSelectCenteredSignsAction() {
		return new AbstractAction("Center single signs") {

			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) e
							.getSource();
					getDrawingSpecifications().setSmallSignsCentered(
							checkBox.isSelected());
					getEditor().getDrawingSpecifications()
							.setSmallSignsCentered(checkBox.isSelected());
					getEditor().invalidateView();
					System.out.println("redrawing");
				}

			}

		};
	}

	/**
	 * @return
	 */
	public AbstractAction getLigatureBeforeAction() {
		return ligatureBeforeAction;
	}

	/**
	 * @return
	 */
	public AbstractAction getLigatureAfterAction() {
		return ligatureAfterAction;
	}

	/**
	 * @param configurationNumber
	 */
	public void selectCopyPasteConfiguration(int configurationNumber) {
		rtfExportPreferencesIndex = configurationNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelTransferableBroker#buildTransferable(jsesh.mdc.model.TopItemList)
	 */
	public MDCModelTransferable buildTransferable(TopItemList top) {
		MDCModelTransferable result = new MDCModelTransferable(top);
		result.setDrawingSpecifications(getDrawingSpecifications());
		result
				.setRtfPreferences(rtfExportPreferences[rtfExportPreferencesIndex]);
		return result;
	}

	public void exportAsWMF() {
		if (getCaret().hasSelection()
				&& wmfExporter.askUser() == JOptionPane.OK_OPTION) {
			/*
			 * DrawingSpecifications ds = frame.editor
			 * .getDrawingSpecifications().copy(); ds.setTopMargin(1f);
			 * ds.setLeftMargin(1f); new ExportData(ds,
			 * MDCDisplayerAppliWorkflow.this, data, 2.0f)
			 */
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getData().getModel(), 1f);
			wmfExporter.export(exportData);
		}
	}

	public void exportAsSVG() {
		if (getCaret().hasSelection()
				&& svgExporter.askUser() == JOptionPane.OK_OPTION) {
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getData().getModel(), 1f);
			svgExporter.export(exportData);
		}
	}
	/**
	 * 
	 */
	public void exportAsBitmap() {
		if (bitmapExporter == null) {
			bitmapExporter = new BitmapExporter();

		}

		boolean selectionOnly = getCaret().hasSelection();
		if (bitmapExporter.askUser(selectionOnly) == JOptionPane.OK_OPTION) {
			if (selectionOnly) {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(), getData()
								.getModel(), 2f);
				bitmapExporter.export(exportData);
			} else {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(), getData()
								.getModel(), 2f);
				try {
					bitmapExporter.exportAll(exportData);
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}

		}
	}

	public void exportAsHtml() {
		if (htmlExporter == null) {
			htmlExporter = new HTMLExporter();
			// htmlExporter.setDirectory(new
			// File(currentMDCDirectory,"testhtml"));
			htmlExporter.setDirectory(new File(currentOutputDirectory,
					"testhtml"));
			htmlExporter.setBaseName("test");
		}
		if (htmlExporter.getOptionPanel(frame, "Export as HTML").askAndSet() == JOptionPane.OK_OPTION) {
			htmlExporter.setDrawingSpecifications(getDrawingSpecifications());
			htmlExporter.exportModel(data.getModel());
		}
	}

	public void exportAsPdf() {
		if (iPDFExporter == null) {
			iPDFExporter = new IPDFExporter();
			iPDFExporter
					.setFile(new File(currentOutputDirectory, "default.pdf"));
		}

		iPDFExporter.setDrawingSpecifications(getDrawingSpecifications());
		if (iPDFExporter.getOptionPanel(frame, "Export as PDF").askAndSet() == JOptionPane.OK_OPTION)
			try {
				iPDFExporter.exportModel(data.getModel());
				currentOutputDirectory = iPDFExporter.getFile().getParentFile();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,
						"Error while exporting to pdf " + e1.getMessage(),
						"Problem when exporting", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
	}

	public void exportAsRtf() {
		RTFExporterUI rtfExporterUI;
		rtfExporterUI = new RTFExporterUI(currentRTFExportFile,
				rtfExportPreferences[2]);

		if (rtfExporterUI.getOptionPanel(frame, "Export as RTF").askAndSet() == JOptionPane.OK_OPTION) {
			rtfExporterUI.exportModel(drawingSpecifications, data.getModel());
			currentOutputDirectory = rtfExporterUI.getFile().getParentFile();
		}

	}

	/**
	 * Registers an action as being interested by the caret's manager
	 * informations.
	 * 
	 * @param a
	 */
	public void registerCaretAwareAction(Action a) {
		caretActionManager.addAction(a);
	}
}