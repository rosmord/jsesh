/*
 * Created on 9 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.docdisplayer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * This class is a simple HTML Browser used to display documentation.
 * @author S. Rosmorduc
 *
 */
public class DocDisplayer extends JFrame {
	
	/**
	 * Singleton.
	 */
	static private DocDisplayer instance;
	
	private JTextPane docPanel;
	
	/**
	 * 
	 */
	private DocDisplayer() {
		super("JSesh Documentation");
		// A trick to have an anti aliased TextPanel. Why on earth did the conceptor not provide a simple
		// way of doing this ????
		// THIS MIGHT NOT BE A GOOD IDEA. IT SEEMS TO WORK, ANYWAY
		/*docPanel= new JTextPane() {

			public void paintComponent(Graphics g)
			{	Graphics2D g2 = (Graphics2D) g;
				GraphicsUtils.antialias(g);
				g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				super.paintComponent(g2);
			}
		};*/
		docPanel= new JTextPane();
		getContentPane().add(new JScrollPane(docPanel));
		setSize(640,600);
		//URL base= jsesh.userdoc.DocBase.class.getResource("index.html");
		//URL base= this.getClass().getResource("../userdoc/index.html");
		URL base= this.getClass().getResource("/jseshResources/userdoc/index.html");
		try {
			docPanel.setPage(base);
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(this,"Problem while loading the documentation\n"+ e.getMessage() + "(url : "+ base + ")");
			e.printStackTrace();	
		}
		docPanel.setEditable(false);
		docPanel.addHyperlinkListener(new Control());
		addWindowListener(new WindowAdapter() {
			
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				instance= null;
			}
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
			 */
			public void windowClosed(WindowEvent e) {
				dispose();
				instance= null;
			}
		});
	}
	
	private class Control implements HyperlinkListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
		 */
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					docPanel.setPage(e.getURL());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}
	
	static public DocDisplayer getInstance() {
		if (instance == null) {
			instance= new DocDisplayer();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		//getInstance().show();
		getInstance().setVisible(true);
		
	}
}
