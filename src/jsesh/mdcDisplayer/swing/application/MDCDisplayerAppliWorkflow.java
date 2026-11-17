package jsesh.mdcDisplayer.swing.application;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.graphicExport.CaretProvider;
import jsesh.graphicExport.HTMLExporter;
import jsesh.graphicExport.WMFExporter;
import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelWriter;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;
import jsesh.mdcDisplayer.swing.groupEditor.GroupEditorDialog;
import jsesh.utils.SimpleStringModel;

/**
 * This class represents the top-level logic of the editor. This file is free
 * Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * IMPORTANT : this class is WAY TOO LARGE. WE SHOULD CLEAN UP THE
 * INITIALIZATION PROCESS OF ALL DATA.
 * 
 * TODO Sepatate in a) action catalog b) shared data (in particular, a list of
 * editors)
 * 
 * (c) Serge Rosmorduc
 * 
 * @author Serge Rosmorduc
 */

public class MDCDisplayerAppliWorkflow implements CaretProvider {

    class CartoucheAction extends MDCIconAction {
        private int end;

        private int start;

        private int type;

        /**
         * @param mdcText
         * @param type
         * @param start
         * @param end
         */
        public CartoucheAction(int type, int start, int end, String mdcText) {
            super(mdcText);
            this.type = type;
            this.start = start;
            this.end = end;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addCartouche(type, start, end);
        }

    }

    /**
     * A simple class for easy text addition.
     * 
     * @author S. Rosmorduc
     *  
     */
    class InsertElementAction extends MDCIconAction {

        ModelElement element;

        /**
         *  
         */
        public InsertElementAction(ModelElement element, String mdcText) {
            super(mdcText);
            this.element = element;
        }

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().insertElement(element);
        }

    }

    class RotationAction extends AbstractAction {
        private int angle;

        public RotationAction(int angle) {
            super("" + angle + "°");
            this.angle = angle;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().changeAngle(angle);
        }
    }

    // IMPORTANT : UNIFY THE ACTION HERE AND IN the action package
    // actions are either linked to an editor object or to the application.
    // they could be independant.

    class ShadeAction extends MDCIconAction {

        /**
         * The shading to apply to the current cadrat.
         */
        private int shade;

        /**
         * @param shade
         * @param string
         */
        public ShadeAction(int shade, String mdc) {
            super(mdc);
            this.shade = shade;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().doShade(shade);
        }
    }

    class SizeAction extends AbstractAction {
        private int size;

        public SizeAction(int size) {
            super("" + size + "%");
            this.size = size;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().resizeSign(size);
        }
    }

    private AbstractAction buildAddedSignsBlock = new MDCIconAction(
            "[&-gm-D3-&]") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addPhilologicalMarkup(
                    SymbolCodes.EDITORADDITION);
        }

    };

    private AbstractAction buildErasedSignBlock = new MDCIconAction(
            "[[-gm-D3-]]") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addPhilologicalMarkup(
                    SymbolCodes.ERASEDSIGNS);

        }

    };

    private AbstractAction buildPreviouslyReadableBlock = new MDCIconAction(
            "[\"-gm-D3-\"]") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addPhilologicalMarkup(
                    SymbolCodes.PREVIOUSLYREADABLE);
        }

    };

    private AbstractAction buildScribeAdditionBlock = new MDCIconAction(
            "['-gm-D3-']") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addPhilologicalMarkup(
                    SymbolCodes.SCRIBEADDITION);

        }

    };

    private AbstractAction buildSuperfluousSignsBlock = new MDCIconAction(
            "[{-gm-D3-}]") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().addPhilologicalMarkup(
                    SymbolCodes.EDITORSUPERFLUOUS);

        }

    };

    private CaretActionManager caretActionManager;

    private CartoucheAction[] cartoucheActions;

    private AbstractAction copyAction;

    private File currentDirectory;

    private File currentFile;

    private AbstractAction cutAction;

    private HieroglyphicTextModel data;

    private AbstractAction doubleZoomAction;

    private AbstractAction editGroupAction = new AbstractAction("Edit group") {

        public void actionPerformed(ActionEvent e) {
            AbsoluteGroup g = getEditor().getWorkflow().buildAbsoluteGroup();
            if (g != null) {
                GroupEditorDialog d = new GroupEditorDialog();
                d.setGroup(g);
                JOptionPane
                        .showConfirmDialog(frame, d, "Group editor",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);
                getEditor().getWorkflow().fixAbsoluteGroup(g);
            }
        }

    };

    public AbstractAction getEditGroupAction() {
        return editGroupAction;
    }

    private AbstractAction exitAction;

    private AbstractAction explodeGroupAction;

    private MDCDisplayerAppliFrame frame;

    private AbstractAction groupHorizontallyAction;

    private AbstractAction groupVerticallyAction;

    private AbstractAction halfZoomAction;

    private AbstractAction htmlExportAction;

    // Data for HTML export (created on request).
    private HTMLExporter htmlExporter = null;

    private AbstractAction insertBlackPointAction = new InsertElementAction(
            new Hieroglyph(SymbolCodes.BLACKPOINT), "O");

    private AbstractAction insertHalfSpaceAction = new AbstractAction(
            "Insert Half Space") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().insertHalfSpace();
        }

    };

    private AbstractAction insertPageBreakAction;

    private AbstractAction insertRedPointAction = new InsertElementAction(
            new Hieroglyph(SymbolCodes.REDPOINT), "o");

    private AbstractAction insertSpaceAction = new AbstractAction(
            "Insert Space") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().insertSpace();
        }

    };

    private AbstractAction ligatureAction = new AbstractAction(
            "Ligature elements") {

        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().ligatureElements();
        }

    };

    private AbstractAction loadTextAction;

    /**
     * Informations to display.
     *  
     */
    private SimpleStringModel message;

    private AbstractAction newAction;

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

    private AbstractAction saveTextAction;

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

    private WMFExporter wmfExportAction;

    public MDCDisplayerAppliWorkflow(HieroglyphicTextModel data) {
        this.data = data;
        preferences = Preferences.userNodeForPackage(this.getClass());
        setPhilologySign(true);
        this.frame = null;
        String dir = System.getProperty("user.dir");
        currentDirectory = new File(dir);
        message = new SimpleStringModel("");
        buildActions();
        refreshFromPreferences();
        currentFile = new File(currentDirectory, "default.gly");
    }

    private void buildActions() {
        loadTextAction = new AbstractAction("Load") {
            public void actionPerformed(ActionEvent e) {
                loadText();
            }
        };
        saveTextAction = new AbstractAction("Save") {
            /*
             * (non-Javadoc)
             * 
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {
                saveText();
            }
        };
        exitAction = new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };
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

        htmlExportAction = new AbstractAction("export as HTML") {

            public void actionPerformed(ActionEvent e) {
                if (htmlExporter == null) {
                    htmlExporter = new HTMLExporter();
                    //htmlExporter.setDirectory(new
                    // File(currentDirectory,"testhtml"));
                    htmlExporter.setDirectory(new File("testhtml"));
                    htmlExporter.setBaseName("test");
                }
                if (htmlExporter.getOptionPanel(frame, "Export as HTML")
                        .askAndSet() == JOptionPane.OK_OPTION)
                    htmlExporter.exportModel(data.getModel());
            }

        };

        wmfExportAction = new WMFExporter(data, frame, this, "Export as wmf",
                currentDirectory);

        newAction = new AbstractAction("New") {

            public void actionPerformed(ActionEvent e) {
                getEditor().getWorkflow().clear();
            }

        };

        cutAction = new AbstractAction("Cut") {

            public void actionPerformed(ActionEvent e) {
                getEditor().getWorkflow().cut();

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

        pasteAction = new AbstractAction("Paste") {

            public void actionPerformed(ActionEvent e) {
                getEditor().getWorkflow().paste();

            }

        };

        copyAction = new AbstractAction("Copy") {

            public void actionPerformed(ActionEvent e) {
                getEditor().getWorkflow().copy();
            }

        };

        buildShadeActions();
        buildCartoucheActions();

        int angles[] = { 0, 30, 45, 60, 90, 120, 135, 150, 180, 210, 225, 240,
                270, 300, 315, 330 };
        rotationActions = new AbstractAction[angles.length];
        for (int i = 0; i < rotationActions.length; i++)
            rotationActions[i] = new RotationAction(angles[i]);

    }

    private void buildCartoucheActions() {
        cartoucheActions = new CartoucheAction[23];
        int i = 0;
        cartoucheActions[i++] = new CartoucheAction('c', 1, 2,
                "<-ra-mn:n-xpr\\R270->");
        cartoucheActions[i++] = new CartoucheAction('c', 1, 1,
                "<1-ra-mn:n-xpr\\R270-1>");
        cartoucheActions[i++] = new CartoucheAction('c', 2, 1,
                "<2-ra-mn:n-xpr\\R270-1>");
        cartoucheActions[i++] = new CartoucheAction('c', 2, 1,
                "<2-ra-mn:n-xpr\\R270-2>");
        cartoucheActions[i++] = new CartoucheAction('c', 0, 1,
                "<0-ra-mn:n-xpr\\R270-1>");
        cartoucheActions[i++] = new CartoucheAction('c', 1, 0,
                "<1-ra-mn:n-xpr\\R270-0>");
        cartoucheActions[i++] = new CartoucheAction('c', 2, 0,
                "<2-ra-mn:n-xpr\\R270-0>");
        cartoucheActions[i++] = new CartoucheAction('c', 0, 2,
                "<0-ra-mn:n-xpr\\R270-2>");

        cartoucheActions[i++] = new CartoucheAction('s', 1, 2,
                "<s-E1:D40-xa:M-R19->");
        cartoucheActions[i++] = new CartoucheAction('s', 1, 2,
                "<s2-E1:D40-xa:M-R19-s1>");

        cartoucheActions[i++] = new CartoucheAction('h', 1, 2,
                "<h1-ra-xa-f-h2>");
        cartoucheActions[i++] = new CartoucheAction('h', 1, 3,
                "<h1-ra-xa-f-h3>");
        cartoucheActions[i++] = new CartoucheAction('h', 1, 1,
                "<h1-ra-xa-f-h1>");
        cartoucheActions[i++] = new CartoucheAction('h', 1, 0,
                "<h1-ra-xa-f-h0>");

        cartoucheActions[i++] = new CartoucheAction('h', 2, 1,
                "<h2-ra-xa-f-h1>");
        cartoucheActions[i++] = new CartoucheAction('h', 2, 0,
                "<h2-ra-xa-f-h0>");

        cartoucheActions[i++] = new CartoucheAction('h', 3, 1,
                "<h3-ra-xa-f-h1>");
        cartoucheActions[i++] = new CartoucheAction('h', 3, 0,
                "<h3-ra-xa-f-h0>");

        cartoucheActions[i++] = new CartoucheAction('h', 0, 2,
                "<h0-ra-xa-f-h2>");
        cartoucheActions[i++] = new CartoucheAction('h', 0, 3,
                "<h0-ra-xa-f-h3>");
        cartoucheActions[i++] = new CartoucheAction('h', 0, 1,
                "<h0-ra-xa-f-h1>");
        cartoucheActions[i++] = new CartoucheAction('h', 0, 0,
                "<h0-ra-xa-f-h0>");

        cartoucheActions[i++] = new CartoucheAction('F', 1, 2, "<F-ra-xa-f->");

    }

    /**
     * Build actions for shading.
     */
    private void buildShadeActions() {
        shadeActions = new ShadeAction[16];
        shadeActions[0] = new ShadeAction(Cadrat.NONE, "..");
        shadeActions[1] = new ShadeAction(Cadrat.TOP_START, "..#1");
        shadeActions[2] = new ShadeAction(Cadrat.TOP_END, "..#2");
        shadeActions[3] = new ShadeAction(Cadrat.TOP_END | Cadrat.TOP_START,
                "..#12");
        shadeActions[4] = new ShadeAction(Cadrat.BOTTOM_START, "..#3");
        shadeActions[5] = new ShadeAction(Cadrat.BOTTOM_START
                | Cadrat.TOP_START, "..#13");
        shadeActions[6] = new ShadeAction(Cadrat.BOTTOM_START | Cadrat.TOP_END,
                "..#23");
        shadeActions[7] = new ShadeAction(Cadrat.BOTTOM_START | Cadrat.TOP_END
                | Cadrat.TOP_START, "..#123");
        shadeActions[8] = new ShadeAction(Cadrat.BOTTOM_END, "..#4");
        shadeActions[9] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_START,
                "..#14");
        shadeActions[10] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_END,
                "..#24");
        shadeActions[11] = new ShadeAction(Cadrat.BOTTOM_END | Cadrat.TOP_END
                | Cadrat.TOP_START, "..#124");
        shadeActions[12] = new ShadeAction(Cadrat.BOTTOM_END
                | Cadrat.BOTTOM_START, "..#34");
        shadeActions[13] = new ShadeAction(Cadrat.BOTTOM_END
                | Cadrat.BOTTOM_START | Cadrat.TOP_START, "..#134");
        shadeActions[14] = new ShadeAction(Cadrat.BOTTOM_END
                | Cadrat.BOTTOM_START | Cadrat.TOP_END, "..#234");
        shadeActions[15] = new ShadeAction(Cadrat.BOTTOM_END
                | Cadrat.BOTTOM_START | Cadrat.TOP_END | Cadrat.TOP_START,
                "..#1234");
    }

    /**
     * @param i
     * @return
     */
    public AbstractAction buildSizeAction(int size) {
        return new SizeAction(size);
    }

    /**
     * Method called when the program exits normally.
     */
    public void exit() {
        savePreferences();
        System.exit(0);
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
     * @return
     */
    public AbstractAction getCopyAction() {
        return copyAction;
    }

    /**
     * Returns the current directory for this application.
     * 
     * @return Returns the currentDirectory.
     */
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * @return
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
     * Returns the current editor. (currently, the only editor !)
     * 
     * @return
     */
    private JMDCEditor getEditor() {
        return frame.editor;
    }

    /**
     * Returns the exitAction.
     * 
     * @return AbstractAction
     *  
     */
    public AbstractAction getExitAction() {
        return exitAction;
    }

    /**
     * @return
     */
    public AbstractAction getExplodeGroupAction() {
        return explodeGroupAction;
    }

    /**
     * @return
     */
    public AbstractAction getGroupHorizontallyAction() {
        return groupHorizontallyAction;
    }

    /**
     * @return
     */
    public AbstractAction getGroupVerticallyAction() {
        return groupVerticallyAction;
    }

    public AbstractAction getHalfZoomAction() {
        return halfZoomAction;
    }

    public AbstractAction getHtmlExportAction() {
        return htmlExportAction;
    }

    /**
     * @return Returns the insertBlackPointAction.
     */
    public AbstractAction getInsertBlackPointAction() {
        return insertBlackPointAction;
    }

    /**
     * @return
     */
    public AbstractAction getInsertHalfSpaceAction() {
        return insertHalfSpaceAction;
    }

    /**
     * @return
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
     * @return
     */
    public AbstractAction getInsertSpaceAction() {
        return insertSpaceAction;
    }

    /**
     * @return
     */
    public AbstractAction getLigatureAction() {
        return ligatureAction;
    }

    /**
     * Returns the loadTextAction.
     * 
     * @return AbstractAction
     *  
     */
    public AbstractAction getLoadTextAction() {
        return loadTextAction;
    }

    /**
     *  
     */
    public SimpleStringModel getMessage() {
        return message;
    }

    /**
     * @return
     */
    public AbstractAction getNewAction() {
        return newAction;
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
     * @return
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
            List l = new ArrayList();
            AbstractAction block[] = { buildErasedSignBlock,
                    buildAddedSignsBlock, buildSuperfluousSignsBlock,
                    buildPreviouslyReadableBlock, buildScribeAdditionBlock };
            l.addAll(Arrays.asList(block));
            l.add(new InsertElementAction(
                    new Hieroglyph(SymbolCodes.BEGINERASE), "[["));
            l.add(new InsertElementAction(new Hieroglyph(SymbolCodes.ENDERASE),
                    "]]"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.BEGINEDITORADDITION), "[&"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.ENDEDITORADDITION), "&]"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.BEGINEDITORSUPERFLUOUS), "[{"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.ENDEDITORSUPERFLUOUS), "}]"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.BEGINPREVIOUSLYREADABLE), "[\""));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.ENDPREVIOUSLYREADABLE), "\"]"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.BEGINSCRIBEADDITION), "['"));
            l.add(new InsertElementAction(new Hieroglyph(
                    SymbolCodes.ENDSCRIBEADDITION), "']"));
            philologicalActions = (AbstractAction[]) l
                    .toArray(new AbstractAction[l.size()]);
        }
        return philologicalActions;
    }

    /**
     * @return
     *  
     */
    public AbstractAction getResetZoomAction() {
        return resetZoomAction;
    }

    /**
     * @return
     */
    public AbstractAction getReverseSignAction() {
        return reverseSignAction;
    }

    /**
     * @return
     */
    public AbstractAction[] getRotations() {
        return rotationActions;
    }

    /**
     * @return Returns the saveTextAction.
     */
    public AbstractAction getSaveTextAction() {
        return saveTextAction;
    }

    /**
     * @return
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

    public AbstractAction[] getShadeActions() {
        return shadeActions;
    }

    /**
     * @return
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
     * @return
     */
    public AbstractAction getToggleGrammarAction() {
        return toggleGrammarAction;
    }

    /**
     * @return
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
     * @return
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
     * @return Returns the wmfExportAction.
     */
    public AbstractAction getWmfExportAction() {
        return wmfExportAction;
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

    public void loadText() {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("choose a manuel de codage text");
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".hie")
                		|| pathname.getName().endsWith(".HIE")
                        || pathname.getName().endsWith(".gly")
                        || pathname.getName().endsWith(".GLY")
                        || pathname.isDirectory();
            }

            public String getDescription() {
                return "Manuel de codage files";
            }
        };

        fc.addChoosableFileFilter(filter);
        fc.setCurrentDirectory(currentDirectory);
        int result = fc.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Erase the old text.
                // NOTE : this is a
                getEditor().getWorkflow().setMDCCode("");
            } catch (MDCSyntaxError e1) {
                e1.printStackTrace();
            }
            currentDirectory = fc.getCurrentDirectory();
            File file = fc.getSelectedFile();
            StringBuffer buffer = new StringBuffer();
            try {
                // We read the file in memory,
                // because we need to suppress non manuel de codage headers, if
                // any.
                Reader r = new FileReader(file);
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
    }

    /**
     * Switch between a text in which philological signs are a kind of
     * parenthesis and a text in which it's a simple sign. The current system is
     * pretty stupid when going from "signs" to parenthesis : it deletes all
     * philological signs !
     *  
     */
    public void philologyToggle() {
        // IMPORTANT : write this.
    }

    /**
     * Change a number of preferences for this program according to the user
     * preferences.
     *  
     */
    public void refreshFromPreferences() {
        // working directory
        String path = preferences.get("currentDirectory", currentDirectory
                .getAbsolutePath());
        currentDirectory = new File(path);
        // wmf export :
        wmfExportAction.setExportFile(new File(preferences.get("wmfExportFile",
                new File(currentDirectory, "unnamed.wmf").getPath())));

    }

    public void savePreferences() {
        // Also save the creator software version.
        preferences.put("prefversion", "1.1");
        preferences.put("currentDirectory", currentDirectory.getAbsolutePath());
        preferences.put("wmfExportFile", wmfExportAction.getExportFile()
                .getAbsolutePath());
    }

    public void saveText() {
        JFileChooser fc = new JFileChooser(currentDirectory);
        fc.setDialogTitle("save to");
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".hie")
                        || pathname.getName().endsWith(".gly")
                        || pathname.getName().endsWith(".GLY")
                        || pathname.isDirectory();
            }

            public String getDescription() {
                return "Manuel de codage files";
            }
        };
        if (currentFile != null)
            fc.setSelectedFile(currentFile);
        fc.addChoosableFileFilter(filter);

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
            currentFile = fc.getSelectedFile();
            try {
                Writer f = new FileWriter(currentFile);
                ModelWriter w = new ModelWriter();
                w.write(f, data.getModel());
                f.close();
            } catch (IOException e) {
                String message = "Could not write to "
                        + currentFile.getAbsolutePath();
                if (!currentFile.canWrite())
                    message += " (no write permission)";
                JOptionPane.showMessageDialog(frame, message);
            }
        }
    }

    /**
     * @param code
     */
    public void setMessage(String code) {
        message.setText(code);
    }

    /**
     * @param frame
     *  
     */
    public void setFrame(MDCDisplayerAppliFrame frame) {
        this.frame = frame;

        // NOW, WE can set the caret Action Manager.
        // IMPORTANT ; the relationship between this workflow and frame is
        // unhealthy to say the least.
        // We should find a good abtraction for the system. See next line for
        // the general direction.
        // IMPORTANT : prepare for the time of multiple editing sessions !!!!
        caretActionManager = new CaretActionManager(getEditor().getWorkflow());
        caretActionManager.addAction(cutAction);
        caretActionManager.addAction(copyAction);
        caretActionManager.addAction(wmfExportAction);
        caretActionManager.addAction(groupHorizontallyAction);
        caretActionManager.addAction(groupVerticallyAction);
        caretActionManager.addAction(shadeZoneAction);
        caretActionManager.addAction(unShadeZoneAction);
        caretActionManager.addAction(paintRedAction);
        caretActionManager.addAction(paintBlackAction);

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
}