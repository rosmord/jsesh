package jsesh.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import jsesh.mdcDisplayer.preferences.PageLayout;

import org.qenherkhopeshef.graphics.utils.GraphicsUtils;

/**
 * A field like editor for Manuel de Codage texts.
 * Displays only one line of text.
 * 
 * @author rosmord
 *
 */
public class JMDCField extends JMDCEditor {
	
	private static Container p;
	private Dimension preferedSize;
	// Drawing margin to move.
	private float drawingHorizontalOrigin=0;
	
	/**
	 * Name of the action called to validate input (when enter is typed)
	 */
	private static final String VALIDATE_INPUT= "VALIDATE_INPUT";

	/**
	 * List of action listeners.
	 */
	private ArrayList actionListeners= new ArrayList();
	
	/**
	 * Create a hieroglyphic field with the given dimensions, in pixels.
	 * @param width
	 * @param height
	 */
	public JMDCField(int width, int height) {
		setCached(false);
		preferedSize= new Dimension(width,height);
		setDrawingSpecifications(getDrawingSpecifications().copy());
		int textHeight= (int)(height * 0.8);
		//textHeight= height;
		// Compute the space left for margin
		int margin= height - textHeight;
		// Ensure it's even :
		if (margin % 2 != 0) {
			margin++;
			textHeight --;
		}
		setScale(textHeight/getDrawingSpecifications().getMaxCadratHeight());
		//System.out.println(getScale());
		
		PageLayout pageLayout= getDrawingSpecifications().getPageLayout();
		pageLayout.setTopMargin((int)(margin/2.0/getScale()));
		pageLayout.setLeftMargin(0);
		
		getDrawingSpecifications().setLineSkip(0);
		// Build an input map using the default MDCEditor inputmap as parent.
		InputMap inputMap= new InputMap();
		inputMap.setParent(getInputMap()); // Normally, the MDCEditor inputMap is already set.
		ActionMap actionMap= new ActionMap();
		actionMap.setParent(getActionMap()); // idem.
		actionMap.put(VALIDATE_INPUT, new ValidateInputAction());
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), VALIDATE_INPUT);
		setActionMap(actionMap);
		setInputMap(WHEN_FOCUSED, inputMap);
	}
	
	public JMDCField() {
		this(320,40);
	}
	
	public Dimension getPreferredSize() {
		return preferedSize;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		Dimension d= super.getMaximumSize();
		return new Dimension(d.width, preferedSize.height);
	}
	
	protected void paintComponent(Graphics g) {
		super.drawBaseComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		GraphicsUtils.antialias(g2d);

		Rectangle r = getPointerRectangle();
		
		double lastX= drawingHorizontalOrigin + getWidth()  ;
	
		if (r.getMinX() < drawingHorizontalOrigin ) {
			drawingHorizontalOrigin= (float) (r.getMinX() - 2f);
			//System.err.println("setting origin to "+ drawingHorizontalOrigin);
		} else
		if (r.getMaxX() > lastX) {
			drawingHorizontalOrigin= (float) (r.getMaxX() - getWidth());
			//System.err.println("setting origin to "+ drawingHorizontalOrigin);
		}
		g2d.translate(-drawingHorizontalOrigin,0);
		g2d.scale(getScale(),getScale());
		drawer.setClip(true);
		drawer.drawViewAndCursor(g2d, getView(), getMDCCaret(), getDrawingSpecifications());
	}
	
	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}
	
	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
	}
	
	private class ValidateInputAction extends AbstractAction {

		private static final long serialVersionUID = -37706887980500015L;
		private int actionId= 1;
		
		public void actionPerformed(ActionEvent e) {
			actionId++;
			for (int i=0; i < actionListeners.size();i++) {
				ActionListener a= (ActionListener)actionListeners.get(i);
				a.actionPerformed(new ActionEvent(JMDCField.this, actionId,VALIDATE_INPUT));
			}
		}

	}

	/**
	 * Demo of use.
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame jf= new JFrame();
		JMDCField f1= new JMDCField();
		f1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JMDCField f= (JMDCField) e.getSource();
				System.out.println("hello "+ f.getMDCText());
			}
			
		});
		p= jf.getContentPane();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.add(new JTextField());
		new JTextField().addActionListener(null);
		p.add(f1);
		p.add(new JLabel("hello"));
		p.add(new JMDCField());
		jf.pack();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}
}
