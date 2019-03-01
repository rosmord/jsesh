/**
 * author : Serge ROSMORDUC This file is distributed according to the LGPL (GNU
 * lesser public license)
 */
package jsesh.graphics.export.rtf;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.graphics.export.generic.EmbeddableDrawingSpecificationHelper;
import jsesh.graphics.export.emf.EmbeddableEMFSimpleDrawer;
import jsesh.graphics.export.generic.AbstractRTFEmbeddableDrawer;
import jsesh.graphics.export.macpict.EmbeddableMacPictSimpleDrawer;
import jsesh.graphics.export.wmf.EmbeddableWMFSimpleDrawer;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.TranslitterationUtilities;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import org.qenherkhopeshef.graphics.rtfBasicWriter.RTFFontFamily;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

/**
 * Exports a MDC model into a RTF file (or byte array).
 * 
 * <p> RTF is very basic, but can be used in copy/paste, which is nice. 
 * It's the only copy/paste system for vector graphics which works 
 * on all platforms.
 * It also provides an export format which can be edited later in a
 * word processor.
 *
 * <p>
 * After considering as an option the possibility to create a generic class to
 * unify HTML, PDF and Rtf output, it appeared that the common part was quite
 * small, so we decided to write utility classes for the common parts.
 *
 * @author rosmord
 *
 */
public class RTFExporter {

    private static final String TRANSLITFONTNAME = "MDCTranslitLC";
    private static final String TIMES = "Times";
    /**
     * Constant for mac pict pictures.
     */
    public static final int MAC_PICT = 0;
    
    /**
     * Constant for EMF pictures, which is the most sophisticated
     * format for RTF embedding. 
     */
    public static final int EMF = 1;
    
    /**
     * Constants for WMF pictures.
     */
    public static final int WMF = 2;
    
    private int pictureType = EMF;
    private ViewBuilder viewBuilder;
    private DrawingSpecification drawingSpecifications;
    private RTFExportPreferences rtfPreferences = new RTFExportPreferences();
    private SimpleRTFWriter rtfWriter;

    public RTFExporter() {
    }

    public void ExportModelTo(TopItemList model, OutputStream outputStream)
            throws IOException {
        if (rtfPreferences.getExportGraphicFormat().equals(
                RTFExportGraphicFormat.WMF)) {
            pictureType = WMF;
        } else if (rtfPreferences.getExportGraphicFormat().equals(
                RTFExportGraphicFormat.EMF)) {
            pictureType = EMF;
        } else if (rtfPreferences.getExportGraphicFormat().equals(
                RTFExportGraphicFormat.MACPICT)) {
            pictureType = MAC_PICT;
        }
        rtfWriter = new SimpleRTFWriter(outputStream);
        rtfWriter.declareFont(TIMES, RTFFontFamily.ROMAN);
        rtfWriter.declareFont(TRANSLITFONTNAME, RTFFontFamily.ROMAN);
        Font f = drawingSpecifications.getFont('t');
        rtfWriter.declareFont(drawingSpecifications.getFont('t').getName(), RTFFontFamily.ROMAN);
        // Actual export, using a visitor.
        rtfWriter.writeHeader();
        if (shouldExportAsOnePicture()) {
            exportAsPicture(model);
        } else {
            // Will change...
            drawingSpecifications.setTextDirection(TextDirection.LEFT_TO_RIGHT);
            // Won't
            drawingSpecifications
                    .setTextOrientation(TextOrientation.HORIZONTAL);
            RTFExporterAux aux = new RTFExporterAux();
            model.accept(aux);
            aux.close();
        }
        rtfWriter.writeTail();
    }

    /**
     * Should we export the whole text as one picture ? This must be done when
     * explicitly requested, or when the text is in column, as it would not give
     * a good rendering in other cases. (Right now, we will do exactly the same
     * for right to left text also. Later we will try to improve it).
     *
     * @return
     */
    private boolean shouldExportAsOnePicture() {
        if (rtfPreferences.getExportGranularity().equals(
                RTFExportGranularity.ONE_LARGE_PICTURE)) {
            return true;
        } else if (rtfPreferences.respectOriginalTextLayout()) {
            return drawingSpecifications.getTextDirection().equals(
                    TextDirection.RIGHT_TO_LEFT)
                    || drawingSpecifications.getTextOrientation().equals(
                    TextOrientation.VERTICAL);
        } else {
            return false;
        }
    }

    /**
     * Export the data as one huge picture.
     *
     * @throws IOException
     */
    private void exportAsPicture(TopItemList model) throws IOException {
        AbstractRTFEmbeddableDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(model));
        simpleDrawer.drawTopItemList(model);
        simpleDrawer.writeToRTF(rtfWriter);
    }

    /**
     * @param viewBuilder The viewBuilder to set.
     */
    public void setViewBuilder(ViewBuilder viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    /**
     * @param drawingSpecifications The drawingSpecifications to set.
     */
    public void setDrawingSpecifications(
            DrawingSpecification drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications.copy();
        this.drawingSpecifications.setGrayColor(new Color(200, 200, 200));
    }

    /**
     * @param rtfPreferences The rtfPreferences to set.
     */
    public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
        this.rtfPreferences = rtfPreferences;
    }

    /**
     * Build a drawer for creating a picture.
     *
     * @param comment a comment placed in the picture (if possible and
     * supported).
     * @return
     */
    private AbstractRTFEmbeddableDrawer buildSimpleDrawer(String comment) {
        AbstractRTFEmbeddableDrawer result = null;
        switch (pictureType) {
            case MAC_PICT:
                result = new EmbeddableMacPictSimpleDrawer(viewBuilder, drawingSpecifications,
                        rtfPreferences.getCadratHeight());
                break;
            case EMF:
                result = new EmbeddableEMFSimpleDrawer(viewBuilder,  drawingSpecifications,
                        rtfPreferences.getCadratHeight(), comment);
                break;
            case WMF:
                result = new EmbeddableWMFSimpleDrawer(viewBuilder, drawingSpecifications,
                        rtfPreferences.getCadratHeight());
                break;
        }
        return result;
    }

    private String buildMdCForExport(TopItemList t) {
        MDCDocument doc = new MDCDocument(t, drawingSpecifications);
        return doc.getMdC();
    }

    private String buildMdCForExport(TopItem t) {
        TopItemList list = new TopItemList();
        list.addTopItem(t.buildTopItem());
        return buildMdCForExport(list);
    }

    public class RTFExporterAux extends ModelElementDeepAdapter {

        /**
         * list of elements to draw when the mode is
         * RTFExportGranularity.GROUPED_CADRATS.
         */
        TopItemList toDraw = null;

        @Override
        public void visitTopItemList(TopItemList t) {
            for (int i = 0; i < t.getNumberOfChildren(); i++) {
                t.getTopItemAt(i).accept(this);
            }
        }

        @Override
        public void visitAlphabeticText(AlphabeticText t) {
            try {
                flushElements();
                String text = t.getText();
                String fontName = TIMES;
                switch (t.getScriptCode()) {
                    case 'l':
                        rtfWriter.setBold(false);
                        rtfWriter.setItalic(false);
                        break;
                    case 'b':
                        rtfWriter.setBold(true);
                        rtfWriter.setItalic(false);
                        break;
                    case 'i':
                        rtfWriter.setBold(false);
                        rtfWriter.setItalic(true);
                        break;
                    case 't':
                        rtfWriter.setBold(false);
                        rtfWriter.setItalic(false); // italic choosen in the font itself.
                        text = TranslitterationUtilities
                                .getActualTransliterationString(text,
                                        drawingSpecifications
                                        .getTransliterationEncoding());
                        fontName = drawingSpecifications.getFont('t').getFontName();
                        break;
                    case '+':
                    default:
                        return;
                }
                rtfWriter.useFont(fontName);
                rtfWriter.writeString(text);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.mdc.model.ModelElementDeepAdapter#visitLineBreak(jsesh.mdc.
         * model.LineBreak)
         */
        @Override
        public void visitLineBreak(LineBreak b) {
            flushElements();
            try {
                rtfWriter.newParagraph();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * jsesh.mdc.model.ModelElementDeepAdapter#visitPageBreak(jsesh.mdc.
         * model.PageBreak)
         */
        @Override
        public void visitPageBreak(PageBreak b) {
            flushElements();
            try {
                rtfWriter.newPage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void visitTopItem(TopItem t) {
            if (drawingSpecifications.getTextDirection().equals(
                    TextDirection.RIGHT_TO_LEFT)
                    || rtfPreferences.getExportGranularity().equals(
                            RTFExportGranularity.GROUPED_CADRATS)) {
                if (toDraw == null) {
                    toDraw = new TopItemList();
                }
				// TODO : We should modify the drawing methods, so that it's
                // possible to draw any list of top items. Then
                // we would need to create fewer copies of our items.
                toDraw.addTopItem((TopItem) t.deepCopy());
            } else {
                drawElement(t);
            }
        }

        /**
         *
         */
        private void flushElements() {
            try {
                if (drawingSpecifications.getTextDirection().equals(
                        TextDirection.RIGHT_TO_LEFT)
                        || rtfPreferences.getExportGranularity().equals(
                                RTFExportGranularity.GROUPED_CADRATS)) {
                    if (toDraw != null) {
                        AbstractRTFEmbeddableDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(toDraw));
                        simpleDrawer.drawTopItemList(toDraw);
                        simpleDrawer.writeToRTF(rtfWriter);
                        toDraw = null;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() {
            flushElements();
            // rtfDocument.close();
        }

        private void drawElement(TopItem t) {

            AbstractRTFEmbeddableDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(t));
            simpleDrawer.drawElement(t);
            try {
                // float deltay = 0;
                if (simpleDrawer.getCurrentView().getFirstSubView() != null) {
					// deltay = (float) simpleDrawer.getCurrentView()
                    // .getFirstSubView().getDeltaBaseY();
                }
                simpleDrawer.writeToRTF(rtfWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
