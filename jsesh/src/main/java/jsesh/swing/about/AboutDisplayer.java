/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.about;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Graphical objet used to display JSesh's "about" informations. One-use only.
 * 
 * @author rosmord
 * 
 */
public class AboutDisplayer {
	/**
	 * display panel.
	 * <p>
	 * Important developper note : for HTML content, the loading is
	 * asynchronous. Which means we need to wait for it to be done in order to
	 * know the page size. Now, this can be done by listening to the "page"
	 * property.
	 * <p>
	 * See Core Swing advanced programming, p. 366-369.
	 * <p>
	 * Note that all methods here are executed on the EDT.
	 */
	private JEditorPane textPane;

	private JPanel panel;

	private Component parent;

	private boolean pageLoaded = false;
	private boolean displayAsked = false;

	public AboutDisplayer(Component parent) {
		this.parent = parent;
		panel = new JPanel();
		URL base = getHtmlResource();
		textPane = new JEditorPane();
		textPane.setEditable(false);
		try {
			textPane.addPropertyChangeListener(new PageLoadedListener());
			textPane.setPage(base);
		} catch (IOException exception) {
			exception.printStackTrace();
			throw new RuntimeException("loading problem", exception);
		}
		JScrollPane sc = new JScrollPane(textPane);
		sc.setPreferredSize(new Dimension(800, 600));
		panel.add(sc);
	}

	/**
	 * @return
	 */
	private URL getHtmlResource() {
		String countryCode = Locale.getDefault().getCountry().toLowerCase();
		if (countryCode.equals("us"))
			countryCode = "gb";
		String countryFile = "/jseshResources/about/about-" + countryCode
				+ ".html";
		URL result = this.getClass().getResource(countryFile);
		if (result == null) {
			String defaultFile = "/jseshResources/about/about-gb.html";
			result= this.getClass().getResource(defaultFile);
		}
		return result;
	}

	public void show() {
		if (pageLoaded) {
			displayAsked = true;
			doShow();
		} else {
			displayAsked = true;
		}
	}

	/**
	 * Actual display. Call only when the HTML document has been loaded.
	 */
	private void doShow() {
		SwingUtilities.invokeLater(() -> {
                    // panel.setSize(800, 600);
                    JOptionPane.showMessageDialog(parent, panel, "About JSesh",
                            JOptionPane.PLAIN_MESSAGE);
                    displayAsked = false;
                });
	}

	private final class PageLoadedListener implements PropertyChangeListener {
		/**
		 * Method called when the page has been loaded. Note to self : the
		 * property change is NOT advertized by the EDT ????
		 */
                @Override
		public void propertyChange(PropertyChangeEvent evt) {
			final PropertyChangeEvent e = evt;
			SwingUtilities.invokeLater(() -> {
                            // if (!SwingUtilities.isEventDispatchThread())
                            // throw new RuntimeException("Is this not the EDT ???");
                            
                            if (e.getPropertyName().equals("page")) {
                                pageLoaded = true;
                                if (displayAsked)
                                    doShow();
                            }
                        });
		}
	}

}
