/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.docdisplayer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
@SuppressWarnings("serial")
public class DocDisplayer extends JFrame {

        private static final Logger logger= Logger.getLogger(DocDisplayer.class.getName());

        

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
		docPanel= new JTextPane();
		getContentPane().add(new JScrollPane(docPanel));
		setSize(640,600);
		//URL base= jsesh.userdoc.DocBase.class.getResource("index.html");
		//URL base= this.getClass().getResource("../userdoc/index.html");
		URL base= this.getClass().getResource("/jsesh/userdoc/index.html");
		try {
			docPanel.setPage(base);
		} catch (IOException e) {
		    JOptionPane.showMessageDialog(this,"Problem while loading the documentation\n"+ e.getMessage() + "(url : "+ base + ")");
                    logger.log(Level.WARNING, e.getMessage());
		}
		docPanel.setEditable(false);
		docPanel.addHyperlinkListener(new Control());
		addWindowListener(new WindowAdapter() {
			
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
                       @Override
    			public void windowClosing(WindowEvent e) {
				instance= null;
			}
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
			 */
                       @Override
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
