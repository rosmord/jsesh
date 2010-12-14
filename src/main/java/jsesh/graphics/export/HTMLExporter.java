/*
 * Created on 18 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphics.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;

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

import org.qenherkhopeshef.graphics.utils.GraphicsUtils;

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

	public void setDefaults() {
		directory = new File(".");
		baseName = "egyptian";
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
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
	 */
	public void setDrawingSpecifications(
			DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications.copy();
		PageLayout pageLayout= this.drawingSpecifications.getPageLayout();
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
	 * @param parent
	 * @param title
	 * @return a panel
	 */
	public ExportOptionPanel getOptionPanel(Component parent, String title) {
		return new OptionPanel(parent, title);
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
				write("<b>");
				break;
			case 'i':
				write("<i>");
				break;
			case 't':
				write("<font face=\"MDCTranslitLC,TransliterationItalic\">");
				break;
			case '+':
				write("<!--");
			}
			if (htmlSpecialProtected)
				write(t.getText().toString().replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			else
				write(t.getText());
			switch (t.getScriptCode()) {
			case 'b':
				write("</b>");
				break;
			case 'i':
				write("</i>");
				break;
			case 't':
				write("</font>");
				break;
			case '+':
				write("-->");
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
				write("<br/>\n<hrule/>\n");
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
				writeln("");
				break;
			case BREAK:
				writeln("<br/>");
				break;
			case PARAGRAPH:
				writeln("<p>");
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
			write("\n");
		}

		private java.util.List getElements() {
			if (elements == null)
				elements = new ArrayList();
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

				if (view.getWidth() == 0 || view.getHeight() == 0)
					return;
				ViewDrawer drawer = new ViewDrawer();

				BufferedImage image = new BufferedImage((int) Math.ceil(view
						.getWidth()
						* scale + 1), (int) Math.ceil(view.getHeight() * scale
						+ 2 * pictureMargin + 1), BufferedImage.TYPE_INT_ARGB);

				Graphics2D g = image.createGraphics();
				GraphicsUtils.antialias(g);

				g.setColor(backgroundColor);
				g.fillRect(0, 0,image.getWidth(), image.getHeight());
				g.setColor(drawingSpecifications.getBlackColor());
				g.translate(1, 1 + pictureMargin);

				g.scale(scale, scale);
				drawer.draw(g, view, drawingSpecifications);
				g.dispose();

				File fic = getImageFile(imageNumber);
				try {
					ImageIO.write(image, "png", fic);
				} catch (IOException e) {
					e.printStackTrace();
				}

				String center = "";

				if (centerPictures)
					center = " align='center' ";

				if (pictureScale != 100) {
					write("<img " + center + " src='" + fic.getName()
							+ "' width='" + pictureScale + "%' height='"
							+ pictureScale + "%'>");

				} else if (generatePictureSize) {
					write("<img " + center + " src='" + fic.getName()
							+ "' width='" + image.getWidth() + "' height='"
							+ image.getHeight() + "'>");
				} else {
					write("<img " + center + "src='" + fic.getName() + "'>");
				}
				imageNumber++;
				image.flush();
				elements = null;
			}
		}

		private File getImageFile(int i) {
			return new File(directory, "img" + i + ".png");
		}

		private String getFileName(int i) {
			return baseName + formatNum(i) + ".html";
		}

		private void startPage() throws IOException {
			pageNumber++;
			writer = new FileWriter(
					new File(directory, getFileName(pageNumber)));
			outPutHeader();
		}

		private String formatNum(int i) {
			return "" + i;
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
			writeln("<html>");
			writeln("<head>");
			writeln("<title>" + title + "(" + pageNumber + ")</title>");
			writeln("</head>");
			writeln("<body>");
			if (pageNumber == 1 && title != null)
				writeln("<h1>" + title + "</h1>");
		}

		private void outPutFooter(boolean hasNext) {
			if (hasNext) {
				outputNextLink(pageNumber + 1);
			}
			writeln("</body>");
			writeln("</html>");
		}

		private void outputNextLink(int num) {
			writeln("<p>");
			write("<a href='" + getFileName(num) + "'> next </a>");
			writeln("</p>");
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

	class OptionPanel extends ExportOptionPanel implements ActionListener {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -4257399135647530503L;

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

		private OptionPanel(Component parent, String paneltitle) {
			super(parent, paneltitle);

			// Title :
			titleField = new JTextField(HTMLExporter.this.title, 40);
			titleField.setToolTipText("Title of the html document");

			// Directory
			directoryField = new JFormattedTextField();
			directoryField.setValue(directory);
			directoryField.setColumns(40);
			browse = new JButton("Browse");
			browse.addActionListener(this);

			// Base name.
			baseNameField = new JTextField(baseName, 20);
			baseNameField
					.setToolTipText("each html file name will start with this string.");
			// Respect pages :
			respectPageField = new JCheckBox("Respect pages", respectPages);
			respectPageField
					.setToolTipText("Output one html file for each manuel de codage page");

			// line height.
			// NOTE : I use a java 1.4 specific class.
			lineHeightField = new JFormattedTextField();
			lineHeightField.setValue(new java.lang.Integer(lineHeight));
			lineHeightField
					.setToolTipText("height of a typical line of hieroglyphs");

			// Picture top and bottom margin
			pictureMarginField = new JFormattedTextField();
			pictureMarginField.setValue(new Integer(pictureMargin));
			pictureMarginField
					.setToolTipText("Top and bottom margin around pictures, expressed in pixels.");

			protectSpecialField = new JCheckBox("Protect html specials");
			protectSpecialField
					.setToolTipText("If checked, all special HTML characters (a, <, >) will be escaped, and printed as normal char in HTML");
			protectSpecialField.setSelected(htmlSpecialProtected);

			newLineReplacementField = new JComboBox(new String[] { "new line",
					"<br>", "<p>" });
			newLineReplacementField.setSelectedIndex(newLinesReplacement);

			NumberFormatter formatter = new NumberFormatter(new DecimalFormat(
					"###"));
			// formatter.setAllowsInvalid(false);
			// formatter.setOverwriteMode(true);
			// formatter.setCommitsOnValidEdit(true);

			pictureScaleField = new JFormattedTextField(formatter);
			pictureScaleField.setValue(new Integer(pictureScale));
			pictureScaleField.setColumns(4);

			pictureScaleField
					.setToolTipText("Picture scale in HTML output.\n"
							+ "Normally 100%; but can be set to a smaller value if one wants to print the page.\n"
							+ "You can for example, set picture height to 300 and picture scale to 10");

			generatePictureSizeField = new JCheckBox("Set picture size in HTML");
			generatePictureSizeField
					.setToolTipText("If checked, the HTML code IMG tags will contain WIDTH=... HEIGHT=... declaration\n");
			generatePictureSizeField.setSelected(generatePictureSize);

			centerPictureField = new JCheckBox("Center pictures vertically");
			centerPictureField
					.setToolTipText("If checked, the HTML code IMG tags will align='center' declaration\n");
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
			add(new LabeledField("document title", titleField), c);
			c.gridy++;
			add(new LabeledField("directory", directoryField), c);
			c.gridx = 1;
			add(browse, c);
			c.gridx = 0;
			c.gridy++;
			add(new LabeledField("base name", baseNameField), c);
			c.gridy++;
			add(respectPageField, c);
			c.gridy++;
			add(new LabeledField("line height", lineHeightField), c);
			c.gridy++;
			Insets oldInset = c.insets;
			c.insets = new Insets(20, 0, 0, 0);
			add(new JLabel("Advanced settings:"), c);
			c.insets = oldInset;
			c.gridy++;
			add(
					new LabeledField("Pictures vertical margin",
							pictureMarginField), c);
			c.gridy++;
			add(new LabeledField("Pictures scales", pictureScaleField), c);
			c.gridy++;
			add(protectSpecialField, c);
			c.gridy++;
			add(generatePictureSizeField, c);
			c.gridy++;
			add(centerPictureField, c);
			c.gridy++;

			add(new LabeledField("Html replacement for newlines",
					newLineReplacementField), c);
		}

		public void setOptions() {
			directory = new File(directoryField.getText());
			baseName = baseNameField.getText();

			lineHeight = ((Integer) lineHeightField.getValue()).intValue();
			HTMLExporter.this.title = titleField.getText();
			respectPages = respectPageField.isSelected();
			htmlSpecialProtected = protectSpecialField.isSelected();
			newLinesReplacement = newLineReplacementField.getSelectedIndex();
			generatePictureSize = generatePictureSizeField.isSelected();
			centerPictures = centerPictureField.isSelected();
			pictureScale = ((Number) pictureScaleField.getValue()).intValue();
			pictureMargin = ((Integer) pictureMarginField.getValue())
					.intValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == browse) {
				JFileChooser chooser = new JFileChooser(directory);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setApproveButtonText("Choose Directory");
				chooser.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "directory for the html files";
					}

					public boolean accept(File f) {
						return (!f.exists() || f.isDirectory());
					}
				});
				int res = chooser.showOpenDialog(this);
				if (res == JFileChooser.APPROVE_OPTION) {
					directoryField.setValue(chooser.getSelectedFile());
				}
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
	 * @param pictureMargin
	 *            a margin, in pixels, to draw above and below pictures.
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
	 * @param centerPictures
	 *            The centerPictures to set.
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
	 * @param pictureScale
	 *            The pictureScale to set.
	 */
	public void setPictureScale(int pictureScale) {
		this.pictureScale = pictureScale;
	}

}
