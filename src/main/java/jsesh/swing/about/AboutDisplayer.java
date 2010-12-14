/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.about;

import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * Graphical objet used to display JSesh's "about" informations.
 * @author rosmord
 *
 */
public class AboutDisplayer extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7646798089863153268L;
	
	private JEditorPane textPane;
	
	public AboutDisplayer(Frame frame) {
		super(frame,"About JSesh",true);
		//setUndecorated(true);
		URL base= this.getClass().getResource("/jseshResources/about/about.html");
		
		// Create the textfield.
		textPane= new JEditorPane();
		textPane.setEditable(false);
		try {
			textPane.setPage(base);
			//textPane.setEditable(false);
		} catch (IOException exception) {
			exception.printStackTrace();	
		    JOptionPane.showMessageDialog(getOwner(),"Problem while loading the documentation\n"+ exception.getMessage() + "(url : "+ base + ")");
		}
		textPane.setMinimumSize(new Dimension(600,400));
	
		JScrollPane sp= new JScrollPane(textPane);
		sp.setMinimumSize(new Dimension(600,400));

		// Create a panel :
		JOptionPane panel= new JOptionPane(sp,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		panel.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
					setVisible(false);
				}
			}
		});
		setContentPane(panel);
		
		setSize(640, 480);
	}

}
