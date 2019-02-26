/*
 * Created on 4 may 2004
 *
 * S. Rosmorduc
 * This file is distributed according to the LGPL.
 */
package jsesh.graphics.export.pdfExport;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeMap;

import jsesh.editor.caret.MDCCaret;
import jsesh.graphics.export.generic.ExportData;
import jsesh.graphics.export.generic.ExportOptionPanel;
import jsesh.graphics.export.generic.SelectionExporter;
import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.TranslitterationUtilities;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.resources.ResourcesManager;

import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;
import org.qenherkhopeshef.utils.PlatformDetection;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Expert for exporting a Manuel de codage file to PDF, using the IText library.
 * We use the very complete and mature IText library (which we will also use for
 * RTF output. dwAw Hr aW.y pA nTr).
 *
 * <p>
 * This class was written quite fast, from a cut and paste of HTMLExporter. It
 * should be cleaned a lot.
 *
 * @author rosmord
 *
 */
public class PDFExporter {

    private PDFExportPreferences pdfExportPreferences;

    public PDFExporter() {
        pdfExportPreferences = new PDFExportPreferences();
    }

    /**
     * gets a panel suitable for option editing.
     *
     * @param parent
     * @param title
     * @return a panel
     */
    public ExportOptionPanel getOptionPanel(Component parent, String title) {
        return new JPDFOptionPanel(parent, title, pdfExportPreferences);
    }

    public void exportModel(TopItemList model, MDCCaret caret)
            throws IOException {
        if (pdfExportPreferences.isEncapsulated()) {
            DrawingSpecification specs = pdfExportPreferences
                    .getDrawingSpecifications().copy();
            //  PDFDataSaver is somehow redundant with PDFExporter...  
            PDFDataSaver pdfDataSaver = new PDFDataSaver(specs,
                    pdfExportPreferences);
            FileOutputStream out = new FileOutputStream(
                    pdfExportPreferences.getFile());

            if (caret.hasSelection()) {
                // TODO WE SHOULD USE EXPORTDATA
                TopItemList subModel = new TopItemList();
                subModel.addAll(model.getTopItemListBetween(caret.getMin(),
                        caret.getMax()));
                pdfDataSaver.writeSinglePagePDF(out, subModel);
            } else {
                int[] limits = model.getPageLimitsAround(caret.getInsert().getIndex());
                TopItemList subModel = new TopItemList();
                subModel.addAll(model.getTopItemListBetween(limits[0], limits[1]));
                pdfDataSaver.writeSinglePagePDF(out, subModel);
            }
        } else if (pdfExportPreferences.isRespectTextLayout()) {
            double scale = 1.0; // Change later.
            if (!caret.hasSelection()) {
                caret = MDCCaret.buildWholeTextCaret(model);
            }

            DrawingSpecification actualDrawingSpecification = pdfExportPreferences
                    .getDrawingSpecifications().copy();

            // TODO : do something better here..
            PageLayout pageLayout = actualDrawingSpecification.getPageLayout();
            pageLayout.setLeftMargin(32);
            pageLayout.setTopMargin(32);
            actualDrawingSpecification.setPageLayout(pageLayout);

            ExportData exportData = new ExportData(actualDrawingSpecification,
                    caret, model, scale);

            PDFGraphics2DFactory graphicFactory = new PDFGraphics2DFactory(
                    pdfExportPreferences, PDFExportHelper.buildCommentText(
                            actualDrawingSpecification, model));

            SelectionExporter selectionExporter = new SelectionExporter(
                    exportData, graphicFactory);

            selectionExporter.exportToPages();
            graphicFactory.close();
        } else {
            try {
                // TODO : correct the architecture. We are sending strange
                // drawing specifications here
                // It would be way better to initialize drawingSpecifications
                // once and for all for this class, and to get them when needed.
                String mdcText = PDFExportHelper.buildCommentText(
                        pdfExportPreferences.getDrawingSpecifications().copy(),
                        model);
                IPDFExporterAux visitor = new IPDFExporterAux(mdcText);

                model.accept(visitor);
                visitor.close();

            } catch (DocumentException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Data for graphical element cache.
     *
     * @author rosmord
     *
     * TODO document the type TemplateInfo
     */
    static private class TemplateInfo {

        PdfTemplate template;

        double dx, dy;
    }

    private class IPDFExporterAux extends ModelElementDeepAdapter {

        Document pdfDocument;

        PdfWriter writer;

        Paragraph currentParagraph;

        /**
         * Font oriented stuff.
         */
        Font translitFont;

        Font italicFont;

        Font romanFont;

        Font boldFont;

        /**
         * The font mapper is used to transform awt fonts into pdf fonts. If we
         * don't provide one, fonts in embedded graphics might be incorrect.
         */
        DefaultFontMapper fontMapper;

        /**
         * A cache for all drawn elements. That way, a given cadrat is only
         * stored once in the document, even if it's repeated.
         */
        TreeMap<TopItem, TemplateInfo> imageCache;

        DrawingSpecification drawingSpecifications;

        SimpleViewBuilder builder;

        public IPDFExporterAux(String comment) throws IOException,
                DocumentException {

            drawingSpecifications = pdfExportPreferences
                    .getDrawingSpecifications().copy();
            /*
			 * Create various utilitary objects.
             */
            imageCache = new TreeMap<TopItem, TemplateInfo>();
            prepareFonts();

            // Classes used to draw the cadrats.
            builder = new SimpleViewBuilder();

            PDFExportHelper.ensureCMYKColorSpace(drawingSpecifications);

            // TODO : fix fonts passed to graphics2D.
            // specs.setTranslitterationFont(translitFont);
            PageLayout pageLayout = drawingSpecifications.getPageLayout();
            pageLayout.setLeftMargin(0.1f);
            drawingSpecifications.setPageLayout(pageLayout);

            /*
			 * Create the PDF document itself :
             */
            pdfDocument = new Document();

            pdfDocument.setPageSize(pdfExportPreferences.getPageRectangle());

            writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(
                    pdfExportPreferences.getFile()));

            if (pdfExportPreferences.isShowPageNumbers()) {
                writer.setPageEvent(new PDFPageNumberHandler());
            }
            /*
			 * Headers can be added only <em>after</em> the getInstance call,
			 * but before the call to open().
             */

            pdfDocument.addTitle(pdfExportPreferences.getTitle());
            pdfDocument.addAuthor(pdfExportPreferences.getAuthor());
            pdfDocument.addSubject(pdfExportPreferences.getSubject());
            pdfDocument.addKeywords(pdfExportPreferences.getKeywords());
            pdfDocument.addCreator("JSesh");
            pdfDocument.addSubject(comment);
            currentParagraph = null;

            pdfDocument.open();
        }

        /**
         * Initialize the objects which represent the various fonts used in the
         * document.
         *
         * @throws DocumentException
         * @throws IOException
         */
        private void prepareFonts() throws DocumentException, IOException {
            fontMapper = new DefaultFontMapper();

            if (drawingSpecifications
                    .getFont(ScriptCodes.TRANSLITERATION)
                    .getName()
                    .equals(ResourcesManager.getInstance()
                            .getTransliterationFont().getName())) {
                // Build fonts to use

                BaseFont bf = BaseFont.createFont(
                        "/jseshResources/fonts/MDCTranslitLC.ttf",
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                translitFont = new Font(bf, drawingSpecifications.getFont(
                        ScriptCodes.TRANSLITERATION).getSize());
                fontMapper.putName("MDCTranslitLC",
                        new DefaultFontMapper.BaseFontParameters(
                                "/jseshResources/fonts/MDCTranslitLC.ttf"));
            } else {
                java.awt.Font g2dTranslitFont = drawingSpecifications
                        .getFont(ScriptCodes.TRANSLITERATION);
                // translitFont = FontFactory.getFont(g2dTranslitFont.getName(),
                // g2dTranslitFont.getSize(), Font.NORMAL);
                // BaseFont bf = BaseFont.createFont(g2dTranslitFont.getName() +
                // ".ttf",""
                // , true);
                // translitFont = new Font(bf, drawingSpecifications.getFont(
                // ScriptCodes.TRANSLITERATION).getSize());
                translitFont = FontFactory.getFont(g2dTranslitFont.getName(),
                        BaseFont.IDENTITY_H, true, g2dTranslitFont.getSize());

                fontMapper.putName(g2dTranslitFont.getName(),
                        new DefaultFontMapper.BaseFontParameters(
                                g2dTranslitFont.getName()));
            }
            romanFont = FontFactory.getFont(FontFactory.TIMES, 12, Font.NORMAL);
            italicFont = FontFactory
                    .getFont(FontFactory.TIMES, 12, Font.ITALIC);
            boldFont = FontFactory.getFont(FontFactory.TIMES, 12, Font.BOLD);

        }

        /**
         * Method to call when the document creation is finished.
         */
        public void close() {
            // I had forgotten the following line.
            // Thanks to Christian Bayer for pointing out that some lines
            // disapeared in the pdf.
            flushParagraph();
            pdfDocument.close();
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model
		 * .TopItemList)
         */
        @Override
        public void visitTopItemList(TopItemList t) {
            try {
                int i = 0;
                startPage();
                while (i < t.getNumberOfChildren()) {
                    t.getChildAt(i).accept(this);
                    i++;
                }
                closePage(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitHRule(jsesh.mdc.model
		 * .HRule)
         */
        @Override
        public void visitHRule(HRule h) {
            PdfContentByte cb = writer.getDirectContent();

            // float lineWidth;
            float x1;
            float x2;

            x1 = h.getStartPos() * drawingSpecifications.getTabUnitWidth();
            x2 = h.getEndPos() * drawingSpecifications.getTabUnitWidth();

            float y = writer.getVerticalPosition(false);
            if (h.getType() == 'l') {
                cb.setLineWidth(drawingSpecifications.getFineLineWidth());
            } else {
                cb.setLineWidth(drawingSpecifications.getWideLineWidth());
            }
            cb.moveTo(x1, y);
            cb.lineTo(x2, y);
            cb.stroke();

            /*
			 * Graphic pdfGraphics= new Graphic();
			 * 
			 * //float lineWidth; float x1; float x2;
			 * 
			 * x1= h.getStartPos() drawingSpecifications.getTabUnitWidth(); x2=
			 * h.getEndPos() drawingSpecifications.getTabUnitWidth();
			 * 
			 * float y= writer.getVerticalPosition(false); if (h.getType()==
			 * 'l')
			 * pdfGraphics.setLineWidth(drawingSpecifications.getFineLineWidth
			 * ()); else
			 * pdfGraphics.setLineWidth(drawingSpecifications.getWideLineWidth
			 * ()); pdfGraphics.moveTo(x1, y); pdfGraphics.lineTo(x2, y);
			 * pdfGraphics.stroke(); try {
			 * 
			 * pdfDocument.add(pdfGraphics); } catch (DocumentException
			 * exception) { exception.printStackTrace(); }
             */
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc
		 * .model.AlphabeticText)
         */
        @Override
        public void visitAlphabeticText(AlphabeticText t) {
            Font f;
            String text = t.getText();

            switch (t.getScriptCode()) {
                case 'l':
                    f = romanFont;
                    break;
                case 'b':
                    f = boldFont;
                    break;
                case 'i':
                    f = italicFont;
                    break;
                case 't':
                    f = translitFont;
                    //text = TranslitterationUtilities.toLowerCase(text);
                    text = TranslitterationUtilities.getActualTransliterationString(text, drawingSpecifications.getTransliterationEncoding());
                    break;
                case '+':
                default:
                    return;
            }

            write(text, f);
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model
		 * .PageBreak)
         */
        @Override
        public void visitPageBreak(PageBreak b) {
            flushParagraph();
            if (pdfExportPreferences.isRespectPages()) {
                pdfDocument.newPage();
            } else {
                // Nothing to do ?
            }
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model
		 * .LineBreak)
         */
        @Override
        public void visitLineBreak(LineBreak b) {
            if (currentParagraph == null) {
                // Empty paragraphs are ignored by IText.
                // Hence, we create a fake paragraph with a white space.
                currentParagraph = new Paragraph();
                currentParagraph.add(new Chunk(" "));
            }
            flushParagraph();
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitTopItem(jsesh.mdc.model
		 * .TopItem)
         */
        @Override
        public void visitTopItem(TopItem t) {
            drawElement(t);
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitTabStop(jsesh.mdc.model
		 * .TabStop)
         */
        @Override
        public void visitTabStop(TabStop t) {
            // IText has no tabulation system. Hence, we simply draw a white
            // space
            // It's currently false, but better than nothing.
            // drawElement(t);
            if (currentParagraph != null) {
                currentParagraph
                        .setSpacingAfter(-currentParagraph.getLeading());
                flushParagraph();
            }

            currentParagraph = new Paragraph();
            currentParagraph.setIndentationLeft(t.getStopPos()
                    * drawingSpecifications.getTabUnitWidth());
        }

        /**
         * Add an element to the current paragraph.
         *
         * @param s
         */
        private void write(String s, Font font) {
            addChunck(new Chunk(s, font));
        }

        public void flushParagraph() {
            if (currentParagraph != null) {
                try {
                    float space = drawingSpecifications.getLineSkip();
                    // if (currentParagraph.getMultipliedLeading() < space)
                    // currentParagraph.setLeading(space);
                    // currentParagraph.setLeading(0,1);
                    // currentParagraph.setSpacingAfter(space/2);
                    // currentParagraph.setSpacingBefore(space/2);
                    currentParagraph.setLeading(space
                            + currentParagraph.getLeading());
                    pdfDocument.add(currentParagraph);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
            currentParagraph = null;
        }

        /**
         * Creates or retrieve a drawing of the model element, and insert it
         * into the list of elements.
         *
         * @param elt
         */
        private void drawElement(TopItem elt) {

            TemplateInfo templateInfo = imageCache.get(elt);
            // scale Compute

            double scale = (double) pdfExportPreferences.getLineHeight()
                    / drawingSpecifications.getMaxCadratHeight();

            if (templateInfo == null) {
                TopItemList smallModel = new TopItemList();
                smallModel.addTopItem((TopItem) (elt.deepCopy()));

                MDCView view = builder.buildView(smallModel,
                        drawingSpecifications);

                if (view.getWidth() == 0 || view.getHeight() == 0) {
                    return;
                }
                ViewDrawer drawer = new ViewDrawer();

                PdfContentByte cb = writer.getDirectContent();

                float width = (float) (view.getWidth() * scale);
                float height = (float) (view.getHeight() * scale);

                // Compute the size
                // (int) (view.getWidth() * scale),
                // (int) (view.getHeight() * scale) + 12,
                templateInfo = new TemplateInfo();
                // TODO : improve inter-sign spacing.
                templateInfo.template = cb.createTemplate(width, height);

                Graphics2D g = templateInfo.template.createGraphics(width,
                        height, fontMapper);

                g.setColor(drawingSpecifications.getBlackColor());
                g.scale(scale, scale);
                drawer.setShadeAfter(false);
                drawer.draw(g, view, drawingSpecifications);
                // g.setColor(Color.RED);
                // g.draw(new Rectangle2D.Float(0, 0, view.getWidth(),
                // view.getHeight()));
                g.dispose();
                // imageCache.put(elt,tp);
                // Vertical ajustment (for cartouches for instance).
                double deltay = 0;
                if (view.getFirstSubView() != null) {
                    deltay = view.getFirstSubView().getDeltaBaseY() * scale;
                }
                templateInfo.dx = 0;
                templateInfo.dy = -6f + deltay;
                templateInfo.template
                        .setLeading((float) (view.getHeight() * scale));
                imageCache.put(elt, templateInfo);
            }
            try {
                Image img = Image.getInstance(templateInfo.template);
                Chunk chunk = new Chunk(img, (float) templateInfo.dx,
                        (float) templateInfo.dy, true);
                addChunck(chunk);
                // Add a space.
                addChunck(new Chunk(' ')
                        .setHorizontalScaling(getInterCadratSkip()));
                // The actual "good" size is next to impossible to compute. We
                // should create a special drawing for it
                // addChunck(new
                // Chunk(' ').setHorizontalScaling((float)(drawingSpecifications.getSmallSkip()*scale)));
                if (templateInfo.template.getLeading() > currentParagraph
                        .getLeading()) {
                    currentParagraph.setLeading(templateInfo.template
                            .getLeading());
                }
            } catch (BadElementException e) {
                throw new RuntimeException(e);
            }
        }

        private float getInterCadratSkip() {
            return 0.3f * pdfExportPreferences.getLineHeight() / 14.0f;
        }

        /**
         * Adds data to the current paragraph, creating one if needed.
         *
         * @param chunk
         */
        // private void addChunck(Chunk chunk) {
        private void addChunck(Element chunk) {
            if (currentParagraph == null) {
                currentParagraph = new Paragraph();
            }
            currentParagraph.add(chunk);

        }

        private void startPage() throws IOException {
        }

        private void closePage(boolean hasNext) {
        }

        /**
         * Helper class for dealing with page numbers.
         *
         * @author rosmord
         *
         */
        public class PDFPageNumberHandler extends PdfPageEventHelper {

            @Override
            public void onStartPage(PdfWriter arg0, Document arg1) {

            }

            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                // From an example in the IText tutorial.
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                // compose the footer
                String text = "" + writer.getPageNumber();
                float textSize = romanFont.getBaseFont()
                        .getWidthPoint(text, 12);
                float textBase = document.bottom() - 20;
                cb.beginText();
                cb.setFontAndSize(romanFont.getBaseFont(), 12);
                // for odd pagenumbers, show the footer at the left
                if ((writer.getPageNumber() & 1) == 1) {
                    cb.setTextMatrix(document.left(), textBase);
                    cb.showText(text);
                    cb.endText();
                } // for even numbers, show the footer at the right
                else {
                    float adjust = romanFont.getBaseFont().getWidthPoint("0",
                            12);
                    cb.setTextMatrix(document.right() - textSize - adjust,
                            textBase);
                    cb.showText(text);
                    cb.endText();
                }
                cb.restoreState();
            }
        }
    }

    public PDFExportPreferences getPdfExportPreferences() {
        return pdfExportPreferences;
    }

    public void setPdfExportPreferences(
            PDFExportPreferences pdfExportPreferences) {
        this.pdfExportPreferences = pdfExportPreferences;
    }

    // On the mac, look for the user's fonts.
    static {
        if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
            FontFactory
                    .registerDirectory(PlatformDetection.getMacUserFontDir());
        }
    }
}
