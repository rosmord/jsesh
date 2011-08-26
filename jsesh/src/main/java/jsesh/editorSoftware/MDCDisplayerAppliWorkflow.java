package jsesh.editorSoftware;

/**
 * Changelog:
 * 2004/04/28 : modified saveText so that all texts get an extensions.
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jsesh.editor.HieroglyphicTextModel;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelTransferableBroker;
import jsesh.editor.actionsUtils.PreferenceBoundAction;
import jsesh.editor.actionsUtils.PreferencesChangeListener;
import jsesh.editor.caret.MDCCaret;
import jsesh.editorSoftware.actions.AddNewSignAction;
import jsesh.editorSoftware.actions.AddPhilologyAction;
import jsesh.editorSoftware.actions.CartoucheAction;
import jsesh.editorSoftware.actions.EditGroupAction;
import jsesh.editorSoftware.actions.InsertElementAction;
import jsesh.editorSoftware.actions.InsertElementIconAction;
import jsesh.editorSoftware.actions.RotationAction;
import jsesh.editorSoftware.actions.ShadeAction;
import jsesh.editorSoftware.actions.ShadeSignAction;
import jsesh.editorSoftware.actions.SizeAction;
import jsesh.graphics.export.BitmapExporter;
import jsesh.graphics.export.CaretBroker;
import jsesh.graphics.export.EMFExporter;
import jsesh.graphics.export.EPSExporter;
import jsesh.graphics.export.ExportData;
import jsesh.graphics.export.HTMLExporter;
import jsesh.graphics.export.MacPictExporter;
import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.export.RTFExporterUI;
import jsesh.graphics.export.SVGExporter;
import jsesh.graphics.export.WMFExporter;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.graphics.glyphs.ExternalSignImporter;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.io.importer.pdf.PDFImportException;
import jsesh.io.importer.pdf.PDFImporter;
import jsesh.io.importer.rtf.RTFImportException;
import jsesh.io.importer.rtf.RTFImporter;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.ShadingStyle;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.swing.groupEditor.GroupEditorDialog;
import jsesh.swing.preferencesEditor.PreferencesEditor;
import jsesh.swing.preferencesEditor.PreferencesFacade;
import jsesh.swing.shadingMenuBuilder.ShadingMenuBuilder;
import jsesh.swing.signPalette.HieroglyphPaletteListener;
import jsesh.swing.signPalette.HieroglyphicPaletteDialog;
import jsesh.swing.utils.MdcFileDialog;
import jsesh.swing.utils.SimpleStringModel;

import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;
import org.qenherkhopeshef.utils.PlatformDetection;

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

	/**
	 * The current document being edited.
	 */
	private MDCDocument currentDocument = new MDCDocument();

	private BitmapExporter bitmapExporter = null;

	// manages caret actions...
	private CaretActionManager caretActionManager;

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

	private MDCDisplayerAppliFrame frame;

	private HieroglyphicPaletteDialog paletteDialog;

	private DrawingSpecification drawingSpecifications;

	// Data for HTML export (created on request).
	private HTMLExporter htmlExporter = null;

	private PDFExportPreferences pdfExportPreferences = null;

	/**
	 * The current directory for the export of small PDF pictures...
	 */
	private File quickPDFExportDirectory = null;

	/**
	 * Informations to display.
	 * 
	 */
	private SimpleStringModel message;

	private Preferences preferences;

	private WMFExporter wmfExporter;
	private EMFExporter emfExporter;
	private MacPictExporter macPictExporter;
	private SVGExporter svgExporter;
	private List preferencesListeners;
	/**
	 * Various preferences for RTF export. The last one is used when exporting
	 * to a FILE, the first three are used for cut and paste.
	 */
	private RTFExportPreferences rtfExportPreferences[];
	private LengthUnit preferedUnit;
	/**
	 * Index of rtfExportPreferences which will be used when copying Note that
	 * if the index is 0 or 1, it will be taken from the export preference
	 * array. Index 2 corresponds to "Wysiwyg" export. The dimensional data will
	 * be taken from the "large" export preferences, but other data will be
	 * ignored.
	 * 
	 */
	private int rtfExportPreferencesIndex;

	// private PDFExportPreferences pdfExportPreferences= new
	// PDFExportPreferences();

	private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

	// ACTIONS

	private PreferenceBoundAction addNewSignsAction = new AddNewSignAction(
			"Add new signs", this);
	// List of cartouche-related actions...
	private CartoucheAction[] cartoucheActions;

	private AbstractAction doubleZoomAction;

	private AbstractAction editGroupAction = new EditGroupAction("Edit group",
			this);
	private AbstractAction explodeGroupAction;

	private AbstractAction groupHorizontallyAction;
	private AbstractAction groupVerticallyAction;
	private AbstractAction halfZoomAction;

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

	private AbstractAction insertVerticalShadingAction = new InsertElementAction(
			"Insert Vertical Shading",
			new Hieroglyph(SymbolCodes.VERTICALSHADE), this);

	private AbstractAction insertFullShadingAction = new InsertElementAction(
			"Insert Full Size Shading", new Hieroglyph(SymbolCodes.FULLSHADE),
			this);

	private AbstractAction insertHorizontalShadingAction = new InsertElementAction(
			"Insert Horizontal Shading", new Hieroglyph(
					SymbolCodes.HORIZONTALSHADE), this);

	private AbstractAction insertQuarterShadingAction = new InsertElementAction(
			"Insert Quarter Shading", new Hieroglyph(SymbolCodes.QUATERSHADE),
			this);

	private AbstractAction ligatureAction = new AbstractAction(
			"Ligature elements") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureElements();
		}
	};
	private AbstractAction ligatureBeforeAction = new AbstractAction(
			"ligature group with hieroglyph") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureGroupWithHieroglyph();
		}
	};
	private AbstractAction ligatureAfterAction = new AbstractAction(
			"ligature hieroglyph with group") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().ligatureHieroglyphWithGroup();
		}
	};

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

	private AbstractAction[] philologicalActions = null;

	private AbstractAction resetZoomAction;

	private AbstractAction reverseSignAction = new AbstractAction(
			"Reverse sign") {
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
	private Action[] shadeActions;

	private Action[] shadeSignActions;

	private AbstractAction shadeZoneAction = new AbstractAction("Shade zone") {

		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().shadeZone(true);
		}
	};
	private AbstractAction signIsInWordAction = new AbstractAction(
			"Sign inside a word") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsInsideWord();
		}
	};
	private AbstractAction signIsSentenceEndAction = new AbstractAction(
			"Sign is at sentence end") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsAtSentenceEnd();
		}
	};
	private AbstractAction signIsWordEndAction = new AbstractAction(
			"Sign is at word end") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().setSignIsAtWordEnd();
		}
	};
	private AbstractAction toggleGrammarAction = new AbstractAction(
			"Toggle Grammar") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleGrammar();
		}
	};
	private AbstractAction toggleIgnoredSignAction = new AbstractAction(
			"Toggle Ignored Sign") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleIgnoredSign();
		}
	};
	private AbstractAction toggleRedSignAction = new AbstractAction(
			"Toggle sign is red") {
		public void actionPerformed(ActionEvent e) {
			getEditor().getWorkflow().toggleRedSign();
		}
	};
	private AbstractAction toggleWideSignAction = new AbstractAction(
			"Toggle wide sign") {
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

	public MDCDisplayerAppliWorkflow() {
		currentDocument = new MDCDocument();

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
		emfExporter = new EMFExporter();
		macPictExporter = new MacPictExporter();
		svgExporter = new SVGExporter();
		setPhilologySign(true);
		this.frame = null;
		String dir = System.getProperty("user.dir");
		currentMDCDirectory = new File(dir);
		currentOutputDirectory = new File(dir);
		currentHieroglyphsSource = new File(dir);
		message = new SimpleStringModel("");

		buildActions();
		drawingSpecifications = new DrawingSpecificationsImplementation();
		loadPreferences();

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
			int choice = JOptionPane.showConfirmDialog(frame, d,
					"Group editor", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				getEditor().getWorkflow().replaceSelectionByAbsoluteGroup(g);

			} else {

			}
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
	 * Shade the sign in front of the cursor. (we might change this. It would be
	 * clever to shade : the sign in front of the cursor if there is no
	 * selection and only one sign in front of the cursor the last quadrant or
	 * the selected zone else.
	 * 
	 * @param shade
	 */
	public void doShadeSign(int shade) {
		getEditor().getWorkflow().doShadeSign(shade);
	}

	/**
	 * @param code
	 */
	public void addPhilologicalMarkup(int code) {
		getEditor().getWorkflow().addPhilologicalMarkup(code);
	}

	public void addShortText() {
		String s = JOptionPane.showInputDialog(frame, "Enter short text");
		String protectedText = s.replaceAll("\\\\", "\\\\");
		protectedText = protectedText.replaceAll("\"", "\\\\\"");
		getEditor().getWorkflow().insertMDC("\"" + protectedText + "\"");
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

	public void addPreferencesChangeListener(PreferencesChangeListener l) {
		preferencesListeners.add(l);
	}

	public void firePreferencesChange() {
		for (int i = 0; i < preferencesListeners.size(); i++) {
			((PreferencesChangeListener) preferencesListeners.get(i))
					.preferencesChanged();
		}
	}

	private void buildActions() {

		halfZoomAction = new AbstractAction("Zoom out") {
			public void actionPerformed(ActionEvent e) {
				getEditor().setScale(getEditor().getScale() / Math.sqrt(2.0));
			}
		};
		doubleZoomAction = new AbstractAction("Zoom in") {
			public void actionPerformed(ActionEvent e) {
				getEditor().setScale(getEditor().getScale() * Math.sqrt(2.0));
			}
		};

		resetZoomAction = new AbstractAction("Reset zoom") {
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
				getEditor().getWorkflow().groupVertically();
			}
		};

		groupHorizontallyAction = new AbstractAction("Group Horizontally") {
			public void actionPerformed(ActionEvent e) {
				getEditor().getWorkflow().groupHorizontally();
			}
		};

		buildShadeActions();
		buildCartoucheActions();

		int angles[] = { 0, 30, 45, 60, 90, 120, 135, 150, 180, 210, 225, 240,
				270, 300, 315, 330 };
		rotationActions = new AbstractAction[angles.length];
		for (int i = 0; i < rotationActions.length; i++) {
			rotationActions[i] = new RotationAction(angles[i], this);
		}

	}

	/**
	 * Clear the current text and prepare for a new one.
	 */
	public void newText() {
		if (confirmNewText()) {
			setWindowTitle("new document");
			getEditor().getWorkflow().clear();
			currentDocument = new MDCDocument(getEditor()
					.getHieroglyphicTextModel());
		}

	}

	/**
	 * Returns true if the current document can be replaced by something else.
	 * Ask the user if needed.
	 * 
	 * @return
	 */
	private boolean confirmNewText() {
		boolean canClearText = true;
		if (getEditor().mustSave()) {
			canClearText = JOptionPane.showConfirmDialog(frame,
					"Current text has been modified.\nReally replace it ?",
					"Confirm replacement", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		}
		return canClearText;
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
		ShadingMenuBuilder builder = new ShadingMenuBuilder() {

			protected Action buildAction(int shadingCode, String mdc) {
				return new ShadeAction(shadingCode, mdc,
						MDCDisplayerAppliWorkflow.this);
			}
		};
		shadeActions = builder.buildShadeActions();
		ShadingMenuBuilder builder2 = new ShadingMenuBuilder() {

			protected Action buildAction(int shadingCode, String mdc) {
				return new ShadeSignAction(shadingCode, mdc,
						MDCDisplayerAppliWorkflow.this);
			}
		};
		shadeSignActions = builder2.buildShadeActions();

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
		List l = CompositeHieroglyphsManager.getInstance().getValuesFor(code);
		if (l != null) {
			Iterator i = l.iterator();
			while (i.hasNext()) {
				info += i.next().toString() + " ";
			}
		}
		setMessage(info);
	}

	/**
	 * Method called when the program exits normally.
	 */
	public void exit() {
		boolean reallyExit = true;
		if (getEditor().mustSave()) {
			// JOptionPane.showConfirmDialog(frame,
			// "Unsaved changes exists. Do you want to quit without", title,
			// optionType)
			String documentName = "unnamed.gly";
			if (currentDocument.getFile() != null) {
				documentName = currentDocument.getFile().getName();
			}
			String[] options = new String[] { "Save", "Exit without saving",
					"cancel" };
			int anwser = JOptionPane.showOptionDialog(frame,
					"Unsaved changes exists for " + documentName
							+ "\nDo you want to save them", "Save document ?",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			// int anwser = JOptionPane.showConfirmDialog(frame,
			// "Unsaved changes exists. Do you really want to quit ?",
			// "Confirm Exit", JOptionPane.YES_NO_OPTION);

			if (anwser == JOptionPane.YES_OPTION) {
				reallyExit = saveText();
			} else
				reallyExit = anwser != JOptionPane.CANCEL_OPTION;
		}
		if (reallyExit) {
			savePreferences();
			System.exit(0);
		}
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
	 * @see jsesh.graphics.export.CaretProvider#getCaret()
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

	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return currentDocument.getHieroglyphicTextModel();
	}

	public AbstractAction getDoubleZoomAction() {
		return doubleZoomAction;
	}

	/**
	 * @return Returns the drawingSpecifications.
	 */
	public DrawingSpecification getDrawingSpecifications() {
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

	public AbstractAction getInsertVerticalShadingAction() {
		return insertVerticalShadingAction;
	}

	public AbstractAction getInsertFullShadingAction() {
		return insertFullShadingAction;
	}

	public AbstractAction getInsertHorizontalShadingAction() {
		return insertHorizontalShadingAction;
	}

	public AbstractAction getInsertQuarterShadingAction() {
		return insertQuarterShadingAction;
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
	 * @return Returns the philologicalActions.
	 */
	public AbstractAction[] getPhilologicalActions() {
		return philologicalActions;
	}

	public AbstractAction[] getPhilologyActions() {
		if (philologicalActions == null) {
			ArrayList l = new ArrayList();

			l.add(new AddPhilologyAction("[&-gm-D3-&]",
					SymbolCodes.EDITORADDITION, this));

			l.add(new AddPhilologyAction("[[-gm-D3-]]",
					SymbolCodes.ERASEDSIGNS, this));

			l.add(new AddPhilologyAction("[\"-gm-D3-\"]",
					SymbolCodes.PREVIOUSLYREADABLE, this));

			l.add(new AddPhilologyAction("['-gm-D3-']",
					SymbolCodes.SCRIBEADDITION, this));

			l.add(new AddPhilologyAction("[{-gm-D3-}]",
					SymbolCodes.EDITORSUPERFLUOUS, this));

			l.add(new AddPhilologyAction("[(-gm-D3-)]",
					SymbolCodes.MINORADDITION, this));

			l.add(new AddPhilologyAction("[?-gm-D3-?]", SymbolCodes.DUBIOUS,
					this));

			l.add(new InsertElementIconAction(SymbolCodes.BEGINERASE, this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDERASE, this));
			l.add(new InsertElementIconAction(SymbolCodes.BEGINDUBIOUS, this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDDUBIOUS, this));
			l.add(new InsertElementIconAction(SymbolCodes.BEGINMINORADDITION,
					this));
			l.add(new InsertElementIconAction(SymbolCodes.ENDMINORADDITION,
					this));
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

	public Action[] getShadeActions() {
		return shadeActions;
	}

	public Action[] getShadeSignActions() {
		return shadeSignActions;
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
	 * Open a file selection dialog, and load the corresponding text.
	 * 
	 */
	public void loadText() {
		MdcFileDialog dialog = new MdcFileDialog(currentMDCDirectory,
				"choose a manuel de codage text");
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
		loadFile(file, null);
	}

	public void setWindowTitle(String name) {
		frame.setTitle(name);
	}

	/**
	 * Open a file in a given encoding.
	 * 
	 * @param file
	 * @param encoding
	 */
	public void loadFile(File file, String encoding) {
		try {
			if (confirmNewText()) {
				setWindowTitle(file.getName());
				// Mac specific stuff. Will compile anywhere, though.
				if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX
						&& file != null) {
					// Sets the "file" icon...
					if (file.exists()) {
						frame.getRootPane().putClientProperty(
								"Window.documentFile", file);
						// frame.getRootPane().putClientProperty("Window.documentModified",
						// Boolean.TRUE);
					}
				}
				// Two possibilities : PDF files or JSesh files...
				if (file.getName().toLowerCase().endsWith(".pdf")) {
					try {
						FileInputStream in = new FileInputStream(file);
						setCurrentDocument(PDFImporter.createPDFStreamImporter(
								in, file).getMdcDocument());
					} catch (PDFImportException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(frame,
								"Error opening pdf. Sorry", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else { // Regular JSesh file...
					try {
						MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
						mdcDocumentReader.setEncoding(encoding);
						setCurrentDocument(mdcDocumentReader.loadFile(file));
					} catch (MDCSyntaxError e) {
						String msg = "error at line " + e.getLine();
						msg += " near token: " + e.getToken();
						JOptionPane.showMessageDialog(frame, msg,
								"Syntax Error", JOptionPane.ERROR_MESSAGE);
						// e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame,
					"problem with file : " + e.getLocalizedMessage(),
					"File Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setCurrentDocument(MDCDocument doc) {
		currentDocument = doc;
		this.getEditor().setHieroglyphiTextModel(
				currentDocument.getHieroglyphicTextModel());
		DocumentPreferences prefs = currentDocument.getDocumentPreferences();
		this.getEditor().setTextDirection(prefs.getTextDirection());
		this.getEditor().setTextOrientation(prefs.getTextOrientation());
		this.getDrawingSpecifications().setSmallSignsCentered(
				prefs.isSmallSignCentered());
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
	public void loadPreferences() {
		// working directories
		String path = preferences.get("currentMDCDirectory",
				currentMDCDirectory.getAbsolutePath());
		currentMDCDirectory = new File(path);
		String outputPath = preferences.get("currentOutputDirectory",
				currentOutputDirectory.getAbsolutePath());
		currentOutputDirectory = new File(outputPath);
		currentRTFExportFile = new File(preferences.get("RTFExportFile",
				new File(currentOutputDirectory, "unnamed.rtf")
						.getAbsolutePath()));

		// Quick pdf export...
		quickPDFExportDirectory = new File(preferences.get(
				"quickPdfExportDirectory",
				new File(System.getProperty("user.home"), "quickPdf")
						.getAbsolutePath()));
		// Dimensions...
		drawingSpecifications.setSmallBodyScaleLimit(preferences.getDouble(
				"smallBodyScaleLimit", 12.0));
		drawingSpecifications.setCartoucheLineWidth((float) preferences
				.getDouble("cartoucheLineWidth", 1.0));

		// Shading
		if (preferences.getBoolean("useLinesForShading", true)) {
			drawingSpecifications.setShadingStyle(ShadingStyle.LINE_HATCHING);
		} else {
			drawingSpecifications.setShadingStyle(ShadingStyle.GRAY_SHADING);
		}
		// wmf export :
		wmfExporter.setExportFile(new File(preferences.get("wmfExportFile",
				new File(currentOutputDirectory, "unnamed.wmf").getPath())));

		// emf export :
		emfExporter.setExportFile(new File(preferences.get("emfExportFile",
				new File(currentOutputDirectory, "unnamed.emf").getPath())));

		// Mac Pict export:
		macPictExporter.setExportFile(new File(preferences.get(
				"macpictExportFile", new File(currentOutputDirectory,
						"unnamed.pct").getPath())));

		// directory looked for loading external hieroglyph fonts
		path = preferences.get("currentHieroglyphsSource",
				currentHieroglyphsSource.getAbsolutePath());
		currentHieroglyphsSource = new File(path);

		// Cut and paste preferences.
		String prefNames[] = { "large", "small", "file" };
		int defaultHeight[] = { 20, 12, 12 };
		RTFExportGranularity defaultGranularity[] = {
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.GROUPED_CADRATS };

		for (int i = 0; i < 3; i++) {
			String name = prefNames[i];
			rtfExportPreferences[i].setCadratHeight(preferences.getInt("rtf_"
					+ name + "_size", defaultHeight[i]));

			rtfExportPreferences[i].setExportGranularity(RTFExportGranularity
					.getGranularity(preferences.getInt("rtf_" + name + "_mode",
							defaultGranularity[i].getId())));

			rtfExportPreferences[i]
					.setExportGraphicFormat(RTFExportPreferences.RTFExportGraphicFormat
							.getMode(preferences.getInt("rtf_" + name
									+ "_graphicformat", 0)));

			rtfExportPreferences[i].setRespectOriginalTextLayout(preferences
					.getBoolean("rtf_" + name + "_respect_layout", true));
		}

		// Clipboard preferences...
		clipboardPreferences = new MDCClipboardPreferences()
				.withImageWanted(preferences.getBoolean("imageWanted", false))
				.withPdfWanted(preferences.getBoolean("pdfWanted", false))
				.withRtfWanted(preferences.getBoolean("rtfWanted", true))
				.withTextWanted(preferences.getBoolean("textWanted", true));
	}

	public void savePreferences() {
		// Also save the creator software version.
		preferences.put("prefversion", "2.9.1");
		preferences.put("currentMDCDirectory",
				currentMDCDirectory.getAbsolutePath());
		preferences.put("currentOutputDirectory",
				currentOutputDirectory.getAbsolutePath());
		preferences.put("wmfExportFile", wmfExporter.getExportFile()
				.getAbsolutePath());
		preferences.put("emfExportFile", emfExporter.getExportFile()
				.getAbsolutePath());
		preferences.put("macpictExportFile", macPictExporter.getExportFile()
				.getAbsolutePath());
		preferences.put("currentHieroglyphsSource",
				currentHieroglyphsSource.getAbsolutePath());

		preferences.put("quickPdfExportDirectory",
				quickPDFExportDirectory.getAbsolutePath());
		// Dimensions...
		preferences.putDouble("smallBodyScaleLimit",
				drawingSpecifications.getSmallBodyScaleLimit());
		preferences.putDouble("cartoucheLineWidth",
				drawingSpecifications.getCartoucheLineWidth());

		// shading
		preferences.putBoolean("useLinesForShading", drawingSpecifications
				.getShadingStyle().equals(ShadingStyle.LINE_HATCHING));

		// Save cut and paste preferences
		String prefNames[] = { "large", "small", "file" };

		for (int i = 0; i < prefNames.length; i++) {
			String name = prefNames[i];
			preferences.putInt("rtf_" + name + "_size",
					rtfExportPreferences[i].getCadratHeight());
			preferences.putInt("rtf_" + name + "_mode", rtfExportPreferences[i]
					.getExportGranularity().getId());
			preferences.putInt("rtf_" + name + "_graphicformat",
					rtfExportPreferences[i].getExportGraphicFormat().getId());

			preferences.putBoolean("rtf_" + name + "_respect_layout",
					rtfExportPreferences[i].respectOriginalTextLayout());

		}

		// Clipboard preferences...

		preferences.putBoolean("imageWanted",
				clipboardPreferences.isImageWanted());
		preferences.putBoolean("pdfWanted", clipboardPreferences.isPdfWanted());
		preferences.putBoolean("rtfWanted", clipboardPreferences.isRtfWanted());
		preferences.putBoolean("textWanted",
				clipboardPreferences.isTextWanted());

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save the text under a given name.
	 * 
	 * @return true if the text was saved.
	 */
	public boolean saveTextAs() {
		boolean saved = false;
		MdcFileDialog fc = new MdcFileDialog(currentMDCDirectory, "Save to");

		File currentFile = new File(currentMDCDirectory, "unnamed.gly");
		if (currentDocument != null) {
			if (currentDocument.getFile() != null)
				currentFile = currentDocument.getFile();
			fc.setSelectedFile(currentFile);
		}

		int result = fc.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			if (((currentFile == null) || !currentFile.equals(fc
					.getSelectedFile())) && fc.getSelectedFile().exists()) {
				int opt = JOptionPane.showConfirmDialog(frame, ""
						+ fc.getSelectedFile().getName()
						+ " will be replaced. Are you sure ?", "confirm",
						JOptionPane.YES_NO_OPTION);
				if (opt == JOptionPane.NO_OPTION) {
					return false;
				}
			}
			currentDocument.setFile(fc.getSelectedFile());
			currentDocument.setEncoding(fc.getEncoding());
			saveText();
			saved = true;
		}
		return saved;
	}

	/**
	 * Save the current text. Returns true if the text was saved.
	 */
	public boolean saveText() {
		boolean saved = false;
		if (currentDocument != null) {
			File currentFile = currentDocument.getFile();
			if (currentFile == null) {
				return saveTextAs();

			}

			// TODO : create a sane system for dealing with text orientation and
			// direction.
			// Currently, the "document" data is not synchronized with the
			// content of the editor.
			// TODO TEMPORARY PATCH
			DocumentPreferences docprefs = currentDocument
					.getDocumentPreferences()
					.withTextDirection(
							getEditor().getDrawingSpecifications()
									.getTextDirection())
					.withTextOrientation(
							getEditor().getDrawingSpecifications()
									.getTextOrientation());
			// Sign centering SHOULD BE DONE...
			// TODO END OF TEMPORARY PATCH
			try {
				if (currentDocument.getFile().getName().toLowerCase()
						.endsWith(".pdf")) {
					// Create the prefs for this document... move the code to
					// document ? or what ?
					// more info should also be saved in the case of PDF files
					// (pdf prefs).
					// TODO save PDF prefs in pdf files...
					PDFExportPreferences prefs = new PDFExportPreferences();
					prefs.setFile(currentDocument.getFile());
					prefs.setDrawingSpecifications(getDrawingSpecifications()
							.copy());
					// prefs.getDrawingSpecifications().setTextDirection(
					// currentDocument.getMainDirection());
					// prefs.getDrawingSpecifications().setTextOrientation(
					// currentDocument.getMainOrientation());
					// prefs.getDrawingSpecifications().setSmallSignsCentered(
					// currentDocument.isSmallSignsCentred());
					PDFExporter exporter = new PDFExporter();
					exporter.setPdfExportPreferences(prefs);
					TopItemList model = currentDocument
							.getHieroglyphicTextModel().getModel();
					exporter.exportModel(model,
							MDCCaret.buildWholeTextCaret(model));
				} else {
					currentDocument.save();
				}
				currentMDCDirectory = currentDocument.getFile().getParentFile();
				saved = true;
			} catch (IOException e) {
				String message = "Could not write to "
						+ currentDocument.getFile().getAbsolutePath();
				if (!currentDocument.getFile().canWrite()) {
					message += " (no write permission)";
				}
				JOptionPane.showMessageDialog(frame, message);
			}
		}
		return saved;
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
		getHieroglyphicTextModel().setPhilologyIsSign(philologySign);
		for (int i = 5; i < getPhilologyActions().length; i++) {
			getPhilologyActions()[i].setEnabled(philologySign);
		}
	}

	/**
	 * Ugly system for editing preferences. A better one shall be used. FIXME :
	 * refactor the system for preferences.
	 */
	public void editPreferences() {
		PreferencesFacade preferencesFacade = new PreferencesFacade(
				getDrawingSpecifications(), preferedUnit, rtfExportPreferences,
				clipboardPreferences);
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
		return new AbstractAction("Centre single signs") {

			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) e
							.getSource();
					getDrawingSpecifications().setSmallSignsCentered(
							checkBox.isSelected());
					// Rather bad design: the info is kept both in drawingspecs
					// and in the document.
					getEditor().getDrawingSpecifications()
							.setSmallSignsCentered(checkBox.isSelected());
					// TODO REMOVE ALL THIS CODE !!!!!
					//currentDocument.setSmallSignCentered(checkBox.isSelected());
					getEditor().invalidateView();
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
	 * @see jsesh.editor.MDCModelTransferableBroker#buildTransferable
	 * (jsesh.mdc.model.TopItemList)
	 */
	public MDCModelTransferable buildTransferable(TopItemList top) {
		return buildTransferable(top,
				JSeshPasteFlavors.getTransferDataFlavors(clipboardPreferences));
	}

	public MDCModelTransferable buildTransferable(TopItemList top,
			DataFlavor[] dataFlavors) {
		MDCModelTransferable result = new MDCModelTransferable(dataFlavors, top);
		result.setDrawingSpecifications(getDrawingSpecifications());
		result.setRtfPreferences(getCurrentRTFPreferences());
		result.setClipboardPreferences(clipboardPreferences);
		return result;
	}

	private RTFExportPreferences getCurrentRTFPreferences() {
		if (rtfExportPreferencesIndex == 2) {
			RTFExportPreferences prefs = new RTFExportPreferences();
			prefs.setCadratHeight(rtfExportPreferences[1].getCadratHeight());
			prefs.setExportGraphicFormat(rtfExportPreferences[1]
					.getExportGraphicFormat());
			prefs.setExportGranularity(RTFExportGranularity.ONE_LARGE_PICTURE);
			prefs.setRespectOriginalTextLayout(true);
			return prefs;
		} else {
			return rtfExportPreferences[rtfExportPreferencesIndex];
		}
	}

	public void exportAsWMF() {
		if (getCaret().hasSelection()
				&& wmfExporter.askUser(frame) == JOptionPane.OK_OPTION) {
			/*
			 * DrawingSpecifications ds = frame.editor
			 * .getDrawingSpecifications().copy(); ds.setTopMargin(1f);
			 * ds.setLeftMargin(1f); new ExportData(ds,
			 * MDCDisplayerAppliWorkflow.this, data, 2.0f)
			 */
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getHieroglyphicTextModel().getModel(), 1f);
			wmfExporter.export(exportData);
		}
	}

	public void exportAsEMF() {
		if (getCaret().hasSelection()
				&& emfExporter.askUser(frame) == JOptionPane.OK_OPTION) {
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getHieroglyphicTextModel().getModel(), 1f);
			emfExporter.export(exportData);
		}
	}

	public void exportAsEPS() {
		EPSExporter epsExporter = new EPSExporter();
		if (getCaret().hasSelection()
				&& epsExporter.askUser(frame) == JOptionPane.OK_OPTION) {

			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getHieroglyphicTextModel().getModel(), 1f);
			epsExporter.export(exportData);
		}
	}

	public void exportAsMacPict() {
		if (getCaret().hasSelection()
				&& macPictExporter.askUser(frame) == JOptionPane.OK_OPTION) {
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getHieroglyphicTextModel().getModel(), 1f);
			macPictExporter.export(exportData);
		}
	}

	public void exportAsSVG() {
		if (getCaret().hasSelection()
				&& svgExporter.askUser(frame) == JOptionPane.OK_OPTION) {
			ExportData exportData = new ExportData(getDrawingSpecifications(),
					getCaret(), getHieroglyphicTextModel().getModel(), 1f);
			svgExporter.export(exportData);
		}
	}

	/**
     *
     */
	public void exportAsBitmap() {
		if (bitmapExporter == null) {
			bitmapExporter = new BitmapExporter(frame);
		}

		boolean selectionOnly = getCaret().hasSelection();
		if (bitmapExporter.askUser(selectionOnly) == JOptionPane.OK_OPTION) {
			if (selectionOnly) {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(),
						getHieroglyphicTextModel().getModel(), 2f);
				bitmapExporter.export(exportData);
			} else {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(),
						getHieroglyphicTextModel().getModel(), 2f);
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
			htmlExporter.exportModel(getHieroglyphicTextModel().getModel());
		}
	}

	public void exportAsPdf() {
		if (pdfExportPreferences == null) {
			pdfExportPreferences = new PDFExportPreferences();
			pdfExportPreferences.setFile(new File(currentOutputDirectory,
					"default.pdf"));
		}

		PDFExporter pdfExporter = new PDFExporter();
		pdfExporter.setPdfExportPreferences(pdfExportPreferences);
		pdfExportPreferences
				.setDrawingSpecifications(getDrawingSpecifications());

		if (pdfExporter.getOptionPanel(frame, "Export as PDF").askAndSet() == JOptionPane.OK_OPTION) {
			try {
				pdfExporter.exportModel(getHieroglyphicTextModel().getModel(),
						getCaret());
				currentOutputDirectory = pdfExportPreferences.getFile()
						.getParentFile();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,
						"Error while exporting to pdf " + e1.getMessage(),
						"Problem when exporting", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

	public void exportAsRtf() {
		RTFExporterUI rtfExporterUI;
		rtfExporterUI = new RTFExporterUI(currentRTFExportFile,
				rtfExportPreferences[2]);

		if (rtfExporterUI.getOptionPanel(frame, "Export as RTF").askAndSet() == JOptionPane.OK_OPTION) {
			rtfExporterUI.exportModel(drawingSpecifications,
					getHieroglyphicTextModel().getModel());
			currentOutputDirectory = rtfExporterUI.getFile().getParentFile();
		}
	}

	public void importPastedPDF() {
		try {
			setCurrentDocument(PDFImporter.createPDFPasteImporter(
					new File("Unnamed.gly")).getMdcDocument());
		} catch (PDFImportException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error pasting pdf. Sorry",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void importPastedRTF() {
		try {
			setCurrentDocument(RTFImporter.createRTFPasteImporter(
					new File("Unnamed.gly")).getMdcDocument());
		} catch (RTFImportException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Error pasting rtf. Sorry",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void toggleHieroglyphPalette() {
		if (paletteDialog == null) {
			paletteDialog = new HieroglyphicPaletteDialog(frame);
			paletteDialog
					.setHieroglyphPaletteListener(new HieroglyphPaletteListener() {

						public void signSelected(String code) {
							insert(code);
						}
					});
		}
		paletteDialog.getDialog().setVisible(
				!paletteDialog.getDialog().isVisible());
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

	public void loadData(Reader r) {
		try {
			getHieroglyphicTextModel().readTopItemList(r);
			r.close();
		} catch (MDCSyntaxError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerUndoRedoActions(Action undoAction, Action redoAction) {
		new UndoRedoActionManager(getEditor().getWorkflow(), undoAction,
				redoAction);
	}

	/**
	 * Selects the folder where PDF exports will be saved.
	 */
	public void quickPDFExportFolderSelect() {
		PortableFileDialog dialog = PortableFileDialogFactory
				.createDirectorySaveDialog(frame);
		dialog.setTitle("Select folder for PDF files");
		FileOperationResult op = dialog.show();
		if (op == FileOperationResult.OK) {
			quickPDFExportDirectory = dialog.getSelectedFile();
			message("Selected " + quickPDFExportDirectory.getAbsolutePath()
					+ " for PDF output");
		}
	}

	/**
	 * Saves the selected text (if any) or everything (if nothing is selected)
	 * in a pdf file.
	 */
	public void quickPDFExport() {

		// Ensures the folder exists :
		if (!quickPDFExportDirectory.exists()) {
			quickPDFExportDirectory.mkdir();
		}

		if (!(quickPDFExportDirectory.exists() && quickPDFExportDirectory
				.isDirectory())) {
			JOptionPane.showMessageDialog(null,
					"" + quickPDFExportDirectory.getAbsolutePath()
							+ " is not a folder or can't be created",
					"Incorrect folder", JOptionPane.ERROR_MESSAGE);
			return;
		}

		PDFExportPreferences quickExportPreferences = new PDFExportPreferences();

		PDFExporter pdfExporter = new PDFExporter();
		quickExportPreferences
				.setDrawingSpecifications(getDrawingSpecifications());

		// Find the next file name...
		int maxNum = 0;

		for (File f : quickPDFExportDirectory.listFiles()) {
			String fname = f.getName();
			// File names : jsesh + number + .pdf
			if (fname.matches("jsesh[0-9]*\\.pdf")) {
				String numString = fname.substring(5, fname.lastIndexOf('.'));
				try {
					int num = Integer.parseInt(numString);
					if (num > maxNum)
						maxNum = num;
				} catch (NumberFormatException e) {
					// DO NOTHING ? DON'T STOP PROCESSING, but WARN JUST IN
					// CASE.
					e.printStackTrace();
				}
			}
		}
		String numAsString = String.format("%06d", maxNum + 1);

		File pdfFile = new File(quickPDFExportDirectory, "jsesh" + numAsString
				+ ".pdf");
		quickExportPreferences.setFile(pdfFile);
		quickExportPreferences.setEncapsulated(true);

		pdfExporter.setPdfExportPreferences(quickExportPreferences);

		try {
			pdfExporter.exportModel(getHieroglyphicTextModel().getModel(),
					getCaret());
			message.setText(pdfFile.getAbsolutePath() + " exported");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Error while exporting to pdf "
					+ e1.getMessage(), "Problem when exporting",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void message(String message) {
		frame.setMessage(message);
	}
}