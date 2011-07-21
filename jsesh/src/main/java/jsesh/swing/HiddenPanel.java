package jsesh.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class HiddenPanel extends JPanel {

	private static final String SHOWN = "SHOWN";
	private static final String EMPTY = "EMPTY";
	private JButton hidingButton;
	private JPanel innerPanel;
	private CardLayout cardLayout;
	private String showText;
	private String hideText;
	boolean shown;
	private JPanel subPanel;
	
	public HiddenPanel(String showText, String hideText, JPanel innerPanel) {
		this.innerPanel= innerPanel;
		this.showText= showText;
		this.hideText= hideText;
		shown= false;
		hidingButton= new JButton(showText);
		cardLayout= new CardLayout();
		
		subPanel = new JPanel();
		subPanel.setLayout(cardLayout);
		
		JPanel emptyPanel= new JPanel();		
		
		subPanel.add(EMPTY,emptyPanel);
		subPanel.add(SHOWN,innerPanel);
		
		//cardLayout.addLayoutComponent(EMPTY, emptyPanel);
		//cardLayout.addLayoutComponent(SHOWN, innerPanel);
		
		setLayout(new BorderLayout());
		add(hidingButton, BorderLayout.NORTH);
		add(subPanel, BorderLayout.CENTER);
		hidingButton.addActionListener(new MyListener());
		doHide();
	}
	
	/**
	 * Listener for the button.
	 * @author rosmord
	 *
	 */
	private class MyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (isShown()) {
				doHide();
			} else
				doShow();
		}
	}
	
	public void doHide () {
		shown= false;
		cardLayout.show(subPanel, EMPTY);
		hidingButton.setText(showText);	
	}

	public void doShow () {
		shown= true;
		cardLayout.show(subPanel, SHOWN);
		hidingButton.setText(hideText);	
	}

	public boolean isShown() {
		return shown;
	}

	public void setShown(boolean shown) {
		this.shown = shown;
	}

	public JPanel getInnerPanel() {
		return innerPanel;
	}
	
	
	
}
