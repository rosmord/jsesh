/*
 * Created on 18 oct. 2004
 *
 */
package jsesh.graphics.export.html;

import jsesh.graphics.export.generic.ExportOptionPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;

import jsesh.i18n.I18n;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.swing.utils.GraphicsUtils;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Expert for exporting a Manuel de codage file to HTML.
 *
 * @author rosmord
 *
 */
public class HTMLExporter {

    private boolean respectPages;

    private File directory;

    private String baseName;

    private String title;

    private Color backgroundColor = new Color(0, 0, 0, 0);

    private int lineHeight;

    /**
     * Vertical margin to draw above and below pictures.
     */
    private int pictureMargin;

    /**
     * Chooses the way special HTML characters are dealt with.
     * <p>
     * If true, replace all &gt; &lt; and &amp; by &amp;gt: &amp;lt; and
     * &amp;amp; (for people looking directly at this comment : protects special
     * HTML characters). This is usefull mainly if you are writing a
     * documentation about the "manuel" de codage. If set
     */
    private boolean htmlSpecialProtected;

    /**
     * Should we generate height, width for IMG tags in the html file ?
     */
    private boolean generatePictureSize;

    /**
     * generation of align='center' tags for pictures ?
     */
    private boolean centerPictures;

    /**
     * Picture scale in HTML output. Normally 100%; but can be set otherwise if
     * one wants to print the page.
     */
    private int pictureScale;

    /**
     * if newLineReplacement is NEW_LINE, -! will be replaced by simple new
     * lines in html
     */
    public static final int SPACE = 0;

    /**
     * if newLineReplacement is BREAK, -! will be replaced by &lt;BR&gt;
     */
    public static final int BREAK = 1;

    /**
     * if newLineReplacement is PARAGRAPH, -! will be replaced by &lt;P&gt;
     */
    public static final int PARAGRAPH = 2;

    /**
     * One of SPACE, BREAK, PARAGRAPH
     */
    private int newLinesReplacement;

    private DrawingSpecification drawingSpecifications;

    public HTMLExporter() {
        setDefaults();
    }

    public final void setDefaults() {
        directory = new File("."); //$NON-NLS-1$
        baseName = "egyptian"; //$NON-NLS-1$
        respectPages = true;
        lineHeight = 30;
        pictureMargin = 0;
        htmlSpecialProtected = true;
        newLinesReplacement = PARAGRAPH;
        generatePictureSize = true;
        pictureScale = 100;
        centerPictures = true;

        setDrawingSpecifications(new DrawingSpecificationsImplementation());
    }

    /**
     * @param drawingSpecifications The drawingSpecifications to set.
     */
    public void setDrawingSpecifications(
            DrawingSpecification drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications.copy();
        PageLayout pageLayout = this.drawingSpecifications.getPageLayout();
        pageLayout.setTopMargin(0);
        pageLayout.setLeftMargin(0);
        this.drawingSpecifications.setPageLayout(pageLayout);
    }

    /**
     * @return Returns the drawingSpecifications.
     */
    public DrawingSpecification getDrawingSpecifications() {
        return drawingSpecifications;
    }

    /**
     * gets a panel suitable for option editing.
     *
     * @param popupParent
     * @param title
     * @return a panel
     */
    public ExportOptionPanel getOptionPanel(Component popupParent, String title) {
        return new OptionPanel(popupParent, title);
    }

    public void exportModel(TopItemList model) {
        directory.mkdirs();
        HTMLExporterAux visitor = new HTMLExporterAux();
        model.accept(visitor);
    }

    private class HTMLExporterAux extends ModelElementAdapter {

        ArrayList elements;

        Writer writer;

        int imageNumber = 0;

        int pageNumber = 0;

        /*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
         */
        public void visitTopItemList(TopItemList t) {
            try {
                int i = 0;
                elements = null;
                startPage();
                while (i < t.getNumberOfChildren()) {
                    t.getChildAt(i).accept(this);
                    i++;
                }
                closePage(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
         */
        public void visitAlphabeticText(AlphabeticText t) {
            flushElements();
            switch (t.getScriptCode()) {
                case 'b':
                    write("<b>"); //$NON-NLS-1$
                    break;
                case 'i':
                    write("<i>"); //$NON-NLS-1$
                    break;
                case 't':
                    write("<font face=\"MDCTranslitLC,TransliterationItalic\">"); //$NON-NLS-1$
                    break;
                case '+':
                    write("<!--"); //$NON-NLS-1$
            }
            if (htmlSpecialProtected) {
                write(t.getText().toString().replaceAll("&", "&amp;") //$NON-NLS-1$ //$NON-NLS-2$
                        .replaceAll("<", "&lt;").replaceAll(">", "&gt;")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } else {
                write(t.getText());
            }
            switch (t.getScriptCode()) {
                case 'b':
                    write("</b>"); //$NON-NLS-1$
                    break;
                case 'i':
                    write("</i>"); //$NON-NLS-1$
                    break;
                case 't':
                    write("</font>"); //$NON-NLS-1$
                    break;
                case '+':
                    write("-->"); //$NON-NLS-1$
                    break;
            }
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
         */
        public void visitPageBreak(PageBreak b) {
            flushElements();
            if (respectPages) {
                closePage(true);
                try {
                    startPage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                write("<br/>\n<hrule/>\n"); //$NON-NLS-1$
            }
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
         */
        public void visitLineBreak(LineBreak b) {
            flushElements();
            switch (newLinesReplacement) {
                case SPACE:
                    writeln(""); //$NON-NLS-1$
                    break;
                case BREAK:
                    writeln("<br/>"); //$NON-NLS-1$
                    break;
                case PARAGRAPH:
                    writeln("<p>"); //$NON-NLS-1$
                    break;
            }

        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitDefault(jsesh.mdc.model.ModelElement)
         */
        public void visitDefault(ModelElement t) {
            getElements().add(t);
        }

        private void write(String s) {
            try {
                writer.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeln(String s) {
            write(s);
            write("\n"); //$NON-NLS-1$
        }

        private java.util.List getElements() {
            if (elements == null) {
                elements = new ArrayList();
            }
            return elements;
        }

        /**
         * Called when an image should be written.
         *
         */
        private void flushElements() {
            if (elements != null) {
                TopItemList smallModel = new TopItemList();
                smallModel.addAll(elements);

                SimpleViewBuilder builder = new SimpleViewBuilder();

                // scale Compute
                double scale = (double) lineHeight
                        / drawingSpecifications.getMaxCadratHeight();

                MDCView view = builder.buildView(smallModel,
                        drawingSpecifications);

                if (view.getWidth() == 0 || view.getHeight() == 0) {
                    return;
                }
                ViewDrawer drawer = new ViewDrawer();

                BufferedImage image = new BufferedImage((int) Math.ceil(view
                        .getWidth()
                        * scale + 1), (int) Math.ceil(view.getHeight() * scale
                                + 2 * pictureMargin + 1), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g = image.createGraphics();
                GraphicsUtils.antialias(g);

                g.setColor(backgroundColor);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.setColor(drawingSpecifications.getBlackColor());
                g.translate(1, 1 + pictureMargin);

                g.scale(scale, scale);
                drawer.draw(g, view, drawingSpecifications);
                g.dispose();

                File fic = getImageFile(imageNumber);
                try {
                    ImageIO.write(image, "png", fic); //$NON-NLS-1$
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String center = ""; //$NON-NLS-1$

                if (centerPictures) {
                    center = " align='center' "; //$NON-NLS-1$
                }
                if (pictureScale != 100) {
                    write("<img " + center + " src='" + fic.getName() //$NON-NLS-1$ //$NON-NLS-2$
                            + "' width='" + pictureScale + "%' height='" //$NON-NLS-1$ //$NON-NLS-2$
                            + pictureScale + "%'>"); //$NON-NLS-1$

                } else if (generatePictureSize) {
                    write("<img " + center + " src='" + fic.getName() //$NON-NLS-1$ //$NON-NLS-2$
                            + "' width='" + image.getWidth() + "' height='" //$NON-NLS-1$ //$NON-NLS-2$
                            + image.getHeight() + "'>"); //$NON-NLS-1$
                } else {
                    write("<img " + center + "src='" + fic.getName() + "'>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                imageNumber++;
                image.flush();
                elements = null;
            }
        }

        private File getImageFile(int i) {
            return new File(directory, "img" + i + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        private String getFileName(int i) {
            return baseName + formatNum(i) + ".html"; //$NON-NLS-1$
        }

        private void startPage() throws IOException {
            pageNumber++;
            writer = new FileWriter(
                    new File(directory, getFileName(pageNumber)));
            outPutHeader();
        }

        private String formatNum(int i) {
            return "" + i; //$NON-NLS-1$
        }

        private void closePage(boolean hasNext) {
            outPutFooter(hasNext);
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void outPutHeader() {
            flushElements();
            writeln("<html>"); //$NON-NLS-1$
            writeln("<head>"); //$NON-NLS-1$
            writeln("<title>" + title + "(" + pageNumber + ")</title>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            writeln("</head>"); //$NON-NLS-1$
            writeln("<body>"); //$NON-NLS-1$
            if (pageNumber == 1 && title != null) {
                writeln("<h1>" + title + "</h1>"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        private void outPutFooter(boolean hasNext) {
            if (hasNext) {
                outputNextLink(pageNumber + 1);
            }
            writeln("</body>"); //$NON-NLS-1$
            writeln("</html>"); //$NON-NLS-1$
        }

        private void outputNextLink(int num) {
            writeln("<p>"); //$NON-NLS-1$
            write("<a href='" + getFileName(num) + "'> next </a>"); //$NON-NLS-1$ //$NON-NLS-2$
            writeln("</p>"); //$NON-NLS-1$
        }
    }

    /**
     * @return base name of html files.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @return the directory which will contain the files.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @return true if explicit page separation should be respected.
     */
    public boolean isRespectPages() {
        return respectPages;
    }

    /**
     * @return the page title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param string
     */
    public void setBaseName(String string) {
        baseName = string;
    }

    /**
     * @param file
     */
    public void setDirectory(File file) {
        directory = file;
    }

    /**
     * @param b
     */
    public void setRespectPages(boolean b) {
        respectPages = b;
    }

    /**
     * @param string
     */
    public void setTitle(String string) {
        title = string;
    }

    /**
     * The typical height of a line of text, in pixels.
     *
     * @return The typical height of a line of text, in pixels.
     */
    public int getLineHeight() {
        return lineHeight;
    }

    /**
     * sets the typical height of a line of text, in pixels.
     *
     * @param i
     */
    public void setLineHeight(int i) {
        lineHeight = i;
    }

    @SuppressWarnings("serial")
    class OptionPanel extends ExportOptionPanel {

        JTextField baseNameField;

        JFormattedTextField directoryField;

        JCheckBox respectPageField;

        JTextField titleField;

        JFormattedTextField lineHeightField;

        JFormattedTextField pictureMarginField;

        JFormattedTextField pictureScaleField;

        JCheckBox protectSpecialField;

        JCheckBox generatePictureSizeField;

        JCheckBox centerPictureField;

        JComboBox newLineReplacementField;

        JButton browse;

        private OptionPanel(Component popupParent, String paneltitle) {
            super(popupParent, paneltitle);

            // Title :
            titleField = new JTextField(HTMLExporter.this.title, 40);
            titleField.setToolTipText(I18n.getString("HTMLExporter.title.toolTip")); //$NON-NLS-1$

            // Directory
            directoryField = new JFormattedTextField();
            directoryField.setValue(directory);
            directoryField.setColumns(40);
            browse = new JButton(I18n.getString("HTMLExporter.browse.label")); //$NON-NLS-1$
            browse.addActionListener(e -> browse());

            // Base name.
            baseNameField = new JTextField(baseName, 20);
            baseNameField
                    .setToolTipText(I18n.getString("HTMLExporter.baseName.toolTip")); //$NON-NLS-1$
            // Respect pages :
            respectPageField = new JCheckBox(I18n.getString("HTMLExporter.respectPage.label"), respectPages); //$NON-NLS-1$
            respectPageField
                    .setToolTipText(I18n.getString("HTMLExporter.respectPage.toolTip")); //$NON-NLS-1$

            // line height.
            // NOTE : I use a java 1.4 specific class.
            lineHeightField = new JFormattedTextField();
            lineHeightField.setValue(lineHeight);
            lineHeightField
                    .setToolTipText(I18n.getString("HTMLExporter.lineHeight.toolTip")); //$NON-NLS-1$

            // Picture top and bottom margin
            pictureMarginField = new JFormattedTextField();
            pictureMarginField.setValue(new Integer(pictureMargin));
            pictureMarginField
                    .setToolTipText(I18n.getString("HTMLExporter.margin.toolTip")); //$NON-NLS-1$

            protectSpecialField = new JCheckBox(I18n.getString("HTMLExporter.protectHTML.label")); //$NON-NLS-1$
            protectSpecialField
                    .setToolTipText(I18n.getString("HTMLExporter.protectHTML.toolTip")); //$NON-NLS-1$
            protectSpecialField.setSelected(htmlSpecialProtected);

            newLineReplacementField = new JComboBox(new String[]{I18n.getString("HTMLExporter.newLineValue.label"), //$NON-NLS-1$
                "<br>", "<p>"}); //$NON-NLS-1$ //$NON-NLS-2$
            newLineReplacementField.setSelectedIndex(newLinesReplacement);

            NumberFormatter formatter = new NumberFormatter(new DecimalFormat(
                    "###")); //$NON-NLS-1$
          
            pictureScaleField = new JFormattedTextField(formatter);
            pictureScaleField.setValue(pictureScale);
            pictureScaleField.setColumns(4);

            pictureScaleField
                    .setToolTipText(I18n.getString("HTMLExporter.scale.toolTip"));

            generatePictureSizeField = new JCheckBox(I18n.getString("HTMLExporter.pictureSize.label")); //$NON-NLS-1$
            generatePictureSizeField
                    .setToolTipText(I18n.getString("HTMLExporter.pictureSize.toolTip")); //$NON-NLS-1$
            generatePictureSizeField.setSelected(generatePictureSize);

            centerPictureField = new JCheckBox(I18n.getString("HTMLExporter.centerPictures.label")); //$NON-NLS-1$
            centerPictureField
                    .setToolTipText(I18n.getString("HTMLExporter.centerPictures.toolTip")); //$NON-NLS-1$
            centerPictureField.setSelected(centerPictures);

            // OK, This layout is UGLY. REALLY.
            // TODO use the formlayout.
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(3, 3, 3, 3);
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            add(new LabeledField(I18n.getString("HTMLExporter.documentTitle.label"), titleField), c); //$NON-NLS-1$
            c.gridy++;
            add(new LabeledField(I18n.getString("HTMLExporter.directory.label"), directoryField), c); //$NON-NLS-1$
            c.gridx = 1;
            add(browse, c);
            c.gridx = 0;
            c.gridy++;
            add(new LabeledField(I18n.getString("HTMLExporter.baseName.label"), baseNameField), c); //$NON-NLS-1$
            c.gridy++;
            add(respectPageField, c);
            c.gridy++;
            add(new LabeledField(I18n.getString("HTMLExporter.lineHeight.label"), lineHeightField), c); //$NON-NLS-1$
            c.gridy++;
            Insets oldInset = c.insets;
            c.insets = new Insets(20, 0, 0, 0);
            add(new JLabel(I18n.getString("HTMLExporter.advancedSettings.label")), c); //$NON-NLS-1$
            c.insets = oldInset;
            c.gridy++;
            add(
                    new LabeledField(I18n.getString("HTMLExporter.verticalMargin.label"), //$NON-NLS-1$
                            pictureMarginField), c);
            c.gridy++;
            add(new LabeledField(I18n.getString("HTMLExporter.scale.label"), pictureScaleField), c); //$NON-NLS-1$
            c.gridy++;
            add(protectSpecialField, c);
            c.gridy++;
            add(generatePictureSizeField, c);
            c.gridy++;
            add(centerPictureField, c);
            c.gridy++;

            add(new LabeledField(I18n.getString("HTMLExporter.newLine.label"), //$NON-NLS-1$
                    newLineReplacementField), c);
        }

        @Override
        public void setOptions() {
            directory = new File(directoryField.getText());
            baseName = baseNameField.getText();

            lineHeight = ((Integer) lineHeightField.getValue());
            HTMLExporter.this.title = titleField.getText();
            respectPages = respectPageField.isSelected();
            htmlSpecialProtected = protectSpecialField.isSelected();
            newLinesReplacement = newLineReplacementField.getSelectedIndex();
            generatePictureSize = generatePictureSizeField.isSelected();
            centerPictures = centerPictureField.isSelected();
            pictureScale = ((Number) pictureScaleField.getValue()).intValue();
            pictureMargin = ((Integer) pictureMarginField.getValue());
        }

        private void browse() {
            PortableFileDialog portableFileDialog = PortableFileDialogFactory.createDirectorySaveDialog(getPopupParent());
            portableFileDialog.setCurrentDirectory(directory);
            //chooser.setApproveButtonText(I18n.getString("HTMLExporter.chooseDirectory.label")); //$NON-NLS-1$
            portableFileDialog.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return I18n.getString("HTMLExporter.directoryDescription.label"); //$NON-NLS-1$
                }

                @Override
                public boolean accept(File f) {
                    return (!f.exists() || f.isDirectory());
                }
            });

            FileOperationResult res = portableFileDialog.show();
            if (res == FileOperationResult.OK) {
                directoryField.setValue(portableFileDialog.getSelectedFile());
            }
        }
    }

    /**
     * Returns the vertical margin to draw above and below pictures.
     *
     * @return Returns the vertical margin to draw above and below pictures.
     */
    public int getPictureMargin() {
        return pictureMargin;
    }

    /**
     * Sets the vertical margin to draw above and below pictures.
     *
     * @param pictureMargin a margin, in pixels, to draw above and below
     * pictures.
     */
    public void setPictureMargin(int pictureMargin) {
        this.pictureMargin = pictureMargin;
    }

    public boolean isGeneratePictureSize() {
        return generatePictureSize;
    }

    public void setGeneratePictureSize(boolean generatePictureSize) {
        this.generatePictureSize = generatePictureSize;
    }

    /**
     * @return Returns the centerPictures.
     */
    public boolean isCenterPictures() {
        return centerPictures;
    }

    /**
     * @param centerPictures The centerPictures to set.
     */
    public void setCenterPictures(boolean centerPictures) {
        this.centerPictures = centerPictures;
    }

    /**
     * @return Returns the pictureScale.
     */
    public int getPictureScale() {
        return pictureScale;
    }

    /**
     * @param pictureScale The pictureScale to set.
     */
    public void setPictureScale(int pictureScale) {
        this.pictureScale = pictureScale;
    }
}
