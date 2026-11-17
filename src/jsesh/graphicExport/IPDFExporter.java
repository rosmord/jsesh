/*
 * Created on 4 may 2004
 *
 * S. Rosmorduc
 * This file is distributed according to the LGPL.
 */
package jsesh.graphicExport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.utils.FileUtils;
import jsesh.utils.TranslitterationUtilities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Graphic;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
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

public class IPDFExporter {
	
	

	private boolean respectPages;

    /**
     * Output file.
     */
    private File file;

    /**
     * PDF Document title.
     */
    private String title;
    
    private String author;
    
    private String subject;
    
    private String keywords;
    
    private boolean showPageNumbers;
    
    private DrawingSpecifications drawingSpecifications= new DrawingSpecifications();
    

    private Color backgroundColor = Color.WHITE;

    /**
     * The possible page sizes. keys are formats, values are rectangles. Could
     * move to a class of its own if needed.
     */

    TreeMap pageFormats;

    /**
     * The size of the current document (as a textual format : A4, A3...).
     */

    String pageSize;

    private int lineHeight;

    public IPDFExporter() {
        fillPageSizes();
        setDefaults();
    }

    public void setDefaults() {
        file = new File("default.pdf");
        title= "";
        keywords="";
        subject= "";
        author= System.getProperty("user.name");
        respectPages = true;
        pageSize = "A4";
        lineHeight = 14;
        showPageNumbers= true;
    }

    
    /**
     * We fill a table mapping names of page dimensions to actual values.
     * this is done using reflection on the class PageSize.
     */
    private void fillPageSizes() {
        pageFormats = new TreeMap();
        Field[] fields = PageSize.class.getDeclaredFields();
        for (int i= 0; i < fields.length; i++) {
            if (fields[i].getType().equals(Rectangle.class)) {
                try {
                    pageFormats.put(fields[i].getName(), fields[i].get(null));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }     
    }

    /**
     * gets a panel suitable for option editing.
     * 
     * @param parent
     * @param title
     * @return a panel
     */
    
    public ExportOptionPanel getOptionPanel(Component parent, String title) {
        return new OptionPanel(parent, title);
    }

    public void exportModel(TopItemList model) throws IOException {
        try {
            // As requested by the IFAO, we save the Manuel de codage content in
            // the picture as a comment.
            MdCModelWriter mdCModelWriter= new MdCModelWriter();
            StringWriter comment= new StringWriter();
            // We output a "pseudo" mime header, because we may later have 
            // a more complex output.
            // jsesh2007 stands for the current version of JSesh.
            comment.write("Content-type: application/jsesh2007\n\n");
            mdCModelWriter.write(comment, model);
            
            IPDFExporterAux visitor = new IPDFExporterAux(comment.toString());
            
            model.accept(visitor);
            visitor.close();
            
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        
        /*
         It is possible to re-read the result of a pdf dump.
        PdfReader reader= new PdfReader(new FileInputStream(file));
        Iterator it= reader.getInfo().keySet().iterator();
        while (it.hasNext()) {
            String key= (String) it.next();
            System.out.println(key +" => "+ reader.getInfo().get(key));
        }
        */
        
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
    };

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
         * The font mapper is used to transform awt fonts into pdf fonts.
         * If we don't provide one, fonts in embedded graphics might be incorrect.
         */
        DefaultFontMapper fontMapper;
        
        /**
         * A cache for all drawn elements. That way, a given cadrat is only
         * stored once in the document, even if it's repeated.
         */
        TreeMap imageCache;

        DrawingSpecifications actualDrawingSpecifications;

        SimpleViewBuilder builder;
        
       
        
        public IPDFExporterAux(String comment) throws IOException, DocumentException {
            /*
             * Create various utilitary objects.
             */
            imageCache = new TreeMap();
            prepareFonts();


            // Classes used to draw the cadrats.
            builder = new SimpleViewBuilder();
            // TODO : really organise a coherent system for
            // drawingspecifications !!!!
            actualDrawingSpecifications= drawingSpecifications.copy();
            // TODO : fix fonts passed to graphics2D.
            //specs.setTranslitterationFont(translitFont);
            actualDrawingSpecifications.setLeftMargin(0.1f);
 
            /*
             * Create the PDF document itself : 
             */
            pdfDocument = new Document();

            pdfDocument.setPageSize((Rectangle) pageFormats.get(pageSize));
              
            writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(
                    file));
           
            
            if (showPageNumbers)
            {
            	writer.setPageEvent(new PDFPageNumberHandler());
            }
            /*
             * Headers can be added only <em>after</em> the getInstance call,
             * but before the call to open().
             */
               
            pdfDocument.addTitle(title);
            pdfDocument.addAuthor(author);
            pdfDocument.addSubject(subject);
            pdfDocument.addKeywords(keywords);
            pdfDocument.addCreator("JSesh");
            pdfDocument.addSubject(comment);
            currentParagraph = null;

            pdfDocument.open();
        }

       
        /**
         * Initialize the objects which represent the various fonts used in the document.
         * @throws DocumentException
         * @throws IOException
         */
        private void prepareFonts() throws DocumentException, IOException {
            BaseFont bf;
            // Build fonts to use
            bf = BaseFont.createFont(
                    "/jseshResources/fonts/MDCTranslitLC.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            translitFont = new Font(bf, 12);
            romanFont = FontFactory.getFont(FontFactory.TIMES, 12, Font.NORMAL);
            italicFont = FontFactory
                    .getFont(FontFactory.TIMES, 12, Font.ITALIC);
            boldFont = FontFactory.getFont(FontFactory.TIMES, 12, Font.BOLD);
            
            fontMapper= new DefaultFontMapper();
            fontMapper.putName("MDCTranslitLC", new DefaultFontMapper.BaseFontParameters("/jseshResources/fonts/MDCTranslitLC.ttf"));

        }

        /**
         * Method to call when the document creation is finished.
         */

        public void close() {
            // I had forgotten the following line.
            // Thanks to Christian Bayer for pointing out that some lines disapeared in the pdf.
            flushParagraph();
            pdfDocument.close();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
         */
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
                e.printStackTrace();
            }
        }

        
        /* (non-Javadoc)
         * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHRule(jsesh.mdc.model.HRule)
         */
        public void visitHRule(HRule h) {
        	Graphic pdfGraphics= new Graphic();
        	
        	//float lineWidth;
			float x1;
			float x2;
					
			x1= h.getStartPos() * actualDrawingSpecifications.getBaseLength()/ 200f;
			x2= h.getEndPos()* actualDrawingSpecifications.getBaseLength()/ 200f;
			
			float y= writer.getVerticalPosition(false);
			if (h.getType()== 'l')
				pdfGraphics.setLineWidth(actualDrawingSpecifications.getFineLineWidth());
			else
				pdfGraphics.setLineWidth(actualDrawingSpecifications.getWideLineWidth());
			pdfGraphics.moveTo(x1, y);
			pdfGraphics.lineTo(x2, y);
			pdfGraphics.stroke();
			try {
				pdfDocument.add(pdfGraphics);
			} catch (DocumentException exception) {
				exception.printStackTrace();
			}
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
         */
        public void visitAlphabeticText(AlphabeticText t) {
            Font f = null;
            String text= t.getText();
            
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
                text= TranslitterationUtilities.toLowerCase(text);
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
         * @see jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
         */
        public void visitPageBreak(PageBreak b) {
            flushParagraph();
            if (respectPages) {
                try {
                    pdfDocument.newPage();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            } else {
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
         */
        public void visitLineBreak(LineBreak b) {
        	if (currentParagraph == null)
        	{
        		// Empty paragraphs are ignored by IText.
        		// Hence, we create a fake paragraph with a white space.
        		currentParagraph= new Paragraph();
        		currentParagraph.add(new Chunk(" "));
        	}
        	flushParagraph();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.mdc.model.ModelElementDeepAdapter#visitTopItem(jsesh.mdc.model.TopItem)
         */
        public void visitTopItem(TopItem t) {
            drawElement(t);
        }

        /* (non-Javadoc)
         * @see jsesh.mdc.model.ModelElementDeepAdapter#visitTabStop(jsesh.mdc.model.TabStop)
         */
        public void visitTabStop(TabStop t) {
        	// IText has no tabulation system. Hence, we simply draw a white space
        	// It's currently false, but better than nothing.
        	//drawElement(t);
        	if (currentParagraph != null) {
        		System.out.println(currentParagraph.leading());
        		currentParagraph.setSpacingAfter(-currentParagraph.leading());
        		flushParagraph();
        	}
        	
        	currentParagraph= new Paragraph();
        	currentParagraph.setIndentationLeft(t.getStopPos()*actualDrawingSpecifications.getBaseLength() / 200f);
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
            if (currentParagraph != null)
                try {
                	float space = drawingSpecifications.getLineSkip();
                	//if (currentParagraph.getMultipliedLeading() < space)
                		//currentParagraph.setLeading(space);
                	//currentParagraph.setLeading(0,1);
                	//currentParagraph.setSpacingAfter(space/2);
                	//currentParagraph.setSpacingBefore(space/2);
                	currentParagraph.setLeading(space+currentParagraph.leading());
                	pdfDocument.add(currentParagraph);
                } catch (DocumentException e) {
                    e.printStackTrace();
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

            TemplateInfo templateInfo = (TemplateInfo) imageCache.get(elt);
            // scale Compute

            double scale = (double) lineHeight / actualDrawingSpecifications.getMaxCadratHeight();

            if (templateInfo == null) {
                TopItemList smallModel = new TopItemList();
                smallModel.addTopItem((TopItem)(elt.deepCopy()));

              
                MDCView view = builder.buildView(smallModel,actualDrawingSpecifications);

                if (view.getWidth() == 0 || view.getHeight() == 0)
                    return;
                ViewDrawer drawer = new ViewDrawer();

                PdfContentByte cb = writer.getDirectContent();
                float width = (float) (view.getWidth() * scale);
                float height = (float) (view.getHeight() * scale);

                // Compute the size
                //        (int) (view.getWidth() * scale),
                //        (int) (view.getHeight() * scale) + 12,

                templateInfo = new TemplateInfo();
                // TODO : improve inter-sign spacing.
                templateInfo.template = cb.createTemplate(width, height);

                
                
                Graphics2D g = templateInfo.template.createGraphics(width,
                        height,fontMapper);

                g.setColor(Color.BLACK);
                g.setBackground(backgroundColor);

                //g.translate(0, 6);
                //g.translate(0.2,0);
                g.scale(scale, scale);
                drawer.draw(g, view, actualDrawingSpecifications);
                //g.setColor(Color.RED);
                //g.draw(new Rectangle2D.Float(0, 0, view.getWidth(),
                // view.getHeight()));
                g.dispose();
                //imageCache.put(elt,tp);
                // Vertical ajustment (for cartouches for instance).
                double deltay = 0;
                if (view.getFirstSubView() != null)
                    {
                		deltay = view.getFirstSubView().getDeltaBaseY()*scale;
                    }
                templateInfo.dx = 0; 
                templateInfo.dy = -6f + deltay;
                templateInfo.template.setLeading((float) (view.getHeight()*scale));
                imageCache.put(elt, templateInfo);
            }
            try {
                Image img = Image.getInstance(templateInfo.template);
                Chunk chunk= new Chunk(img,(float)templateInfo.dx, (float)templateInfo.dy, true);
                addChunck(chunk);
                // Add a space.
                addChunck(new Chunk(' ').setHorizontalScaling(getInterCadratSkip()));
                // The actual "good" size is next to impossible to compute. We should create a special drawing for it             
                //addChunck(new Chunk(' ').setHorizontalScaling((float)(actualDrawingSpecifications.getSmallSkip()*scale)));
                if (templateInfo.template.getLeading() > currentParagraph
                        .leading())
                    currentParagraph.setLeading(templateInfo.template
                            .getLeading() );
            } catch (BadElementException e) {
                e.printStackTrace();
            }
        }

		private float getInterCadratSkip() {
			return 0.3f * lineHeight /14.0f;
		}

        /**
         * Adds data to the current paragraph, creating one if needed.
         * @param chunk
         */
        //private void addChunck(Chunk chunk) {
        private void addChunck(Element chunk) {
            if (currentParagraph == null)
                currentParagraph = new Paragraph();
            currentParagraph.add(chunk);            
            
        }

        private void startPage() throws IOException {
        }

      

        private void closePage(boolean hasNext) {
        }

       
        /**
    	 * Helper class for dealing with page numbers.
    	 * @author rosmord
    	 *
    	 */
        public class PDFPageNumberHandler extends PdfPageEventHelper {

    		public void onStartPage(PdfWriter arg0, Document arg1) {

    		}

    		public void onEndPage(PdfWriter writer, Document document) {
    			// From an example in the IText tutorial.
    	        PdfContentByte cb = writer.getDirectContent();
    	        cb.saveState();
    	        // compose the footer
    	        String text = ""+ writer.getPageNumber();
    	        float textSize = romanFont.getBaseFont().getWidthPoint(text, 12);
    	        float textBase = document.bottom() - 20;
    	        cb.beginText();
    	        cb.setFontAndSize(romanFont.getBaseFont(), 12);
    	        // for odd pagenumbers, show the footer at the left
    	        if ((writer.getPageNumber() & 1) == 1) {
    	            cb.setTextMatrix(document.left(), textBase);
    	            cb.showText(text);
    	            cb.endText();
    	        }
    	        // for even numbers, show the footer at the right
    	        else {
    	            float adjust = romanFont.getBaseFont().getWidthPoint("0", 12);
    	            cb.setTextMatrix(document.right() - textSize - adjust, textBase);
    	            cb.showText(text);
    	            cb.endText();
    	        }
    	        cb.restoreState();
    		}
    	}
    }

    /**
     * @return the file which will contain the files.
     */
    public File getFile() {
        return file;
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
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
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
     * The typical height of a line of text.
     * 
     * @return The typical height of a line of text.
     */
    public int getLineHeight() {
        return lineHeight;
    }

    /**
     * sets the typical height of a line of text.
     * 
     * @param i
     */
    public void setLineHeight(int i) {
        lineHeight = i;
    }

    
    
    class OptionPanel extends ExportOptionPanel implements ActionListener {

        /**
		 * 
		 */
		private static final long serialVersionUID = -1571634707470234047L;

		JFormattedTextField fileField;
        
        JTextField titleField;
        JTextField authorField;
        JTextField subjectField;
        JTextField keywordsField;

        JFormattedTextField lineHeightField;
        
        JCheckBox showPageNumberBox;
        

        JComboBox formatField;
        
        JButton browse;

        
       
        private OptionPanel(Component parent, String paneltitle) {
            super(parent, paneltitle);

            // Title :
            titleField = new JTextField(IPDFExporter.this.title, 40);
            titleField.setToolTipText("Title of the pdf document");

            authorField= new JTextField(40);
            authorField.setText(author);
            
            subjectField= new JTextField(40);
            
            keywordsField= new JTextField(40);                    
            
            // Directory
            fileField = new JFormattedTextField();
            fileField.setValue(file);
            fileField.setColumns(40);
            fileField.setEditable(false);
            browse = new JButton("Browse");
            browse.addActionListener(this);

            // line height.
            // NOTE : I use a java 1.4 specific class.
            lineHeightField = new JFormattedTextField();
            lineHeightField.setValue(new java.lang.Integer(lineHeight));
            lineHeightField
                    .setToolTipText("height of a typical line of hieroglyphs");

            // Page dimensions
            formatField= new JComboBox(pageFormats.keySet().toArray());
            formatField.setSelectedItem(pageSize);
            
            showPageNumberBox= new JCheckBox("Show page numbers", isShowPageNumbers());
            
            //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            //GridBagLayout layout = new GridBagLayout();
            FormLayout layout = new FormLayout(
                    //"right:p,4dlu,p,4dlu,p,4dlu,p,p,p,p,p,p,p",
                    "right:p,4dlu,left:max(40dlu;pref),4dlu,left:max(20dlu;pref)",
                    "");

            DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);

            formBuilder.setDefaultDialogBorder();
            formBuilder.append("Document Title", titleField,3);
            formBuilder.nextLine();
            formBuilder.append("Author", authorField,3);
            formBuilder.append("Subject", subjectField,3);
            formBuilder.append("Keywords", keywordsField,3);
            
            formBuilder.append("Line Height", lineHeightField);
            lineHeightField
                    .setToolTipText("height of standard hieroglyphic cadrats, in points");
            formBuilder.nextLine();
            //formBuilder.appendRow(new RowSpec(""));
            formBuilder.append("Page Format", formatField,1);
            formBuilder.nextLine();
            formBuilder.append(showPageNumberBox);
            formBuilder.nextLine();
            formBuilder.appendSeparator("Output");
            formBuilder.nextLine();
            formBuilder.append("File", fileField, browse);
            
            
            
            setLayout(new BorderLayout());
            add(formBuilder.getPanel(), BorderLayout.CENTER);
            
        }

        public void setOptions() {
            file = new File(fileField.getText());

            title= titleField.getText();
            author= authorField.getText();
            subject= subjectField.getText();
            keywords= keywordsField.getText();
            
            lineHeight = Integer.parseInt(lineHeightField.getText());
            IPDFExporter.this.title = titleField.getText();
            pageSize= (String) formatField.getSelectedItem();
            respectPages = true;
            showPageNumbers= showPageNumberBox.isSelected();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == browse) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(file);
                chooser.setApproveButtonText("Choose file");
                chooser.setFileFilter(new FileFilter() {
                    public String getDescription() {
                        return "pdf files";
                    }

                    public boolean accept(File f) {
                        return (f.getName().endsWith(".pdf") || f.getName()
                                .endsWith(".PDF"))
                                || f.isDirectory();
                    }
                });
                int res = chooser.showSaveDialog(this);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    selected = FileUtils
                            .buildFileWithExtension(selected, "pdf");
                    fileField.setValue(selected);
                }
            }
        }
    }

	/**
	 * @return should we display page numbers ?
	 */
	public boolean isShowPageNumbers() {
		return showPageNumbers;
	}

	/**
	 * Decides if page numbers should be displayed.
	 * @param showPageNumbers true if page numbers should be displayed
	 */
	public void setShowPageNumbers(boolean includePageNumber) {
		this.showPageNumbers = includePageNumber;
	}

	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}

	public void setDrawingSpecifications(DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

}
