/*
 * Created on 18 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphicExport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.DrawingSpecifications;
import jsesh.mdcDisplayer.draw.SimpleViewBuilder;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.utils.GraphicsUtils;

/**
 * Expert for exporting a Manuel de codage file to HTML.
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HTMLExporter {
	private boolean respectPages;
	private File directory;
	private String baseName;
	private String title;
	private Color backgroundColor = Color.WHITE;
	private int lineHeight;
	/**
	 * Chooses the way special HTML characters are dealt with.
	 * <p>If true, replace all &gt; &lt; and &amp; by &amp;gt: &amp;lt; and &amp;amp;
	 * (for people looking directly at this comment : protects special HTML characters).
	 * This is usefull mainly if you are writing a documentation about the "manuel" de codage.
	 * If set
	 */
	private boolean htmlSpecialProtected= false;
	
	/**
	 * if newLineReplacement is NEW_LINE, -! will be replaced by simple new lines in html  
	 */
	public static final int SPACE= 0;
	
	/**
	 * if newLineReplacement is BREAK, -! will be replaced by &lt;BR&gt;   
	 */
	public static final int BREAK= 1;
	/**
	 * if newLineReplacement is PARAGRAPH, -! will be replaced by &lt;P&gt;   
	 */
	public static final int PARAGRAPH= 2;

	/**
	 * One of 
	 */
	private int newLinesReplacement; 

	public HTMLExporter() {
		setDefaults();
	}

	public void setDefaults() {
		directory = new File(".");
		baseName = "egyptian";
		respectPages = true;
		lineHeight = 30;
		htmlSpecialProtected= false;
		newLinesReplacement= BREAK;
	}

	/**
	 * gets a panel suitable for option editing.
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

		/* (non-Javadoc)
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

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			flushElements();
			switch (t.getScriptCode()) {
				case 'b' :
					write("<b>");
					break;
				case 'i' :
					write("<i>");
					break;
				case 't' :
					write("<font face=\"MDCTranslitLC,TransliterationItalic\">");
					break;
				case '+' :
					write("<!--");
			}
			if (htmlSpecialProtected)
				write(t.getText().toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			else 
				write(t.getText());
			switch (t.getScriptCode()) {
				case 'b' :
					write("</b>");
					break;
				case 'i' :
					write("</i>");
					break;
				case 't' :
					write("</font>");
					break;
				case '+' :
					write("-->");
					break;
			}
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			flushElements();
			if (respectPages) {
				closePage(true);
				try {
					startPage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				write("<br/>\n<hrule/>\n");
			}
		}

		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
		 */
		public void visitLineBreak(LineBreak b) {
			flushElements();
			switch (newLinesReplacement)
			{ 
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

		/* (non-Javadoc)
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
				DrawingSpecifications specs =
					builder.getDrawingSpecifications();

				// scale Compute

				double scale = (double) lineHeight / specs.getMaxCadratHeight();

				MDCView view = builder.buildView(smallModel);

				if (view.getWidth() == 0 || view.getHeight() == 0)
					return;
				ViewDrawer drawer = new ViewDrawer();

				BufferedImage image =
					new BufferedImage(
						(int) (view.getWidth() * scale),
						(int) (view.getHeight() * scale) + 12,
						BufferedImage.TYPE_INT_RGB);

				Graphics2D g = image.createGraphics();
				GraphicsUtils.antialias(g);

				g.setBackground(backgroundColor);
				g.clearRect(0, 0, image.getWidth(), image.getHeight());
				g.translate(0, 6);

				g.scale(scale, scale);
				drawer.draw(g, view, builder.getDrawingSpecifications());
				g.dispose();

				File fic = getImageFile(imageNumber);
				try {
					ImageIO.write(image, "png", fic);
				} catch (IOException e) {
					e.printStackTrace();
				}
				write("<img align='center' src=\"" + fic.getName() + 
							"\" width=\""+ image.getWidth() +
							"\" height=\""+ image.getHeight() + 
							"\">");
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
			writer =
				new FileWriter(new File(directory, getFileName(pageNumber)));
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
	 * @return
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * @return
	 */
	public boolean isRespectPages() {
		return respectPages;
	}

	/**
	 * @return
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
	 * @return
	 */
	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * sets the typical height of a line of text, in pixels.
	 * @param i
	 */
	public void setLineHeight(int i) {
		lineHeight = i;
	}

	class OptionPanel extends ExportOptionPanel implements ActionListener {
		JTextField baseNameField;
		JFormattedTextField directoryField;
		JCheckBox respectPageField;
		JTextField titleField;
		JFormattedTextField lineHeightField;
		JCheckBox protectSpecialField;
		JComboBox newLineReplacementField;

		JButton browse;

		private OptionPanel(Component parent, String paneltitle) {
			super(parent, paneltitle);

			// Title :
			titleField= new JTextField(HTMLExporter.this.title,40);
			titleField.setToolTipText("Title of the html document");
			
			// Directory
			directoryField = new JFormattedTextField();
			directoryField.setValue(directory);
			directoryField.setColumns(40);
			browse = new JButton("Browse");
			browse.addActionListener(this);
			
			// Base name.
			baseNameField = new JTextField(baseName, 20);
			baseNameField.setToolTipText(
				"each html file name will start with this string.");
			// Respect pages :
			respectPageField= new JCheckBox("Respect pages", respectPages);
			respectPageField.setToolTipText("Output one html file for each manuel de codage page");
			
			// line height.
			// NOTE : I use a java 1.4 specific class.
			lineHeightField = new JFormattedTextField();
			lineHeightField.setValue(new java.lang.Integer(lineHeight));
			lineHeightField.setToolTipText(
				"height of a typical line of hieroglyphs");
			
			protectSpecialField= new JCheckBox("Protect html specials");
			protectSpecialField.setToolTipText("If checked, all special HTML characters (a, <, >) will be escaped, and printed as normal char in HTML");
			protectSpecialField.setSelected(htmlSpecialProtected);
			
			newLineReplacementField= new JComboBox(new String []{"new line", "<br>", "<p>"});
			newLineReplacementField.setSelectedIndex(newLinesReplacement);

			//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			GridBagLayout layout= new GridBagLayout();
			setLayout(layout);
			GridBagConstraints c= new GridBagConstraints();
			c.gridx= 0;
			c.gridy= 0;
			c.gridwidth= 1;
			c.anchor= GridBagConstraints.WEST;
			add(new LabeledField("document title", titleField),c);
			c.gridy++;
			add(new LabeledField("directory", directoryField),c);
			c.gridx= 1;
			add(browse,c);
			c.gridx= 0;
			c.gridy++;
			add(new LabeledField("base name", baseNameField),c);
			c.gridy++;
			add(respectPageField,c);
			c.gridy++;
			add(new LabeledField("line height", lineHeightField),c);
			c.gridy++;
			add(protectSpecialField,c);
			c.gridy++;
			add(new LabeledField("Html replacement for newlines", newLineReplacementField), c);
		}

		public void setOptions() {
			directory = new File(directoryField.getText());
			baseName = baseNameField.getText();

			lineHeight = Integer.parseInt(lineHeightField.getText());
			HTMLExporter.this.title= titleField.getText();
			respectPages= respectPageField.isSelected();
			htmlSpecialProtected= protectSpecialField.isSelected();
			newLinesReplacement= newLineReplacementField.getSelectedIndex();
		}

		/* (non-Javadoc)
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

}
