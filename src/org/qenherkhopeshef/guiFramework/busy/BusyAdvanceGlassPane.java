package org.qenherkhopeshef.guiFramework.busy;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * A glass pane which can be installed to render a "busy" state.
 * 
 * @author rosmord
 * 
 */
public class BusyAdvanceGlassPane implements BusyInterface {

	private JPanel glassPane;
	private JProgressBar progressBar;
	private JLabel label= new JLabel();

	public BusyAdvanceGlassPane(JFrame frame) {
		glassPane = new JPanel();
		//glassPane.setOpaque(false);

		progressBar = new JProgressBar();
		// glassPane.setLayout(new GridLayout(3, 3));
		// glassPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.BOTH;
		glassPane.add(progressBar,cs);
		cs.gridy= 1;
		glassPane.add(label,cs);
		//glassPane.setBorder(BorderFactory.createLineBorder(Color.RED));
		InputBlockerUtils.interceptInput(glassPane);
		frame.setGlassPane(glassPane);
	}

	/**
	 * Sets panel transparency. 
	 * @param transparency 0 is full transparency, 1 is opaque.
	 */
	public void setTransparency(double transparency) {
		if (transparency == 0) {
			glassPane.setOpaque(false);
		} else {
			glassPane.setOpaque(true);
			glassPane.setBackground(new Color(1,1,1,(float)transparency));
		}
	}
	
	public void setFont(Font font) {
		label.setFont(font);
	}
	
	public void setMessageColor(Color color) {
		label.setForeground(color);
	}
	
	public void setMessage(String message) {
		label.setText(message);
	}
	
	public void setBuzy(final boolean buzy) {
		if (SwingUtilities.isEventDispatchThread()) {
			doSetBuzy(buzy);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						doSetBuzy(buzy);
					}
				});
			} catch (InterruptedException e) {
				// NO-OP
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setAdvance(int percent) {
		progressBar.setValue(percent % 101);
	}

	private void doSetBuzy(final boolean buzy) {
		if (buzy) {
			glassPane.setVisible(true);
		} else {
			glassPane.setVisible(false);
		}
	}
}
