package org.qenherkhopeshef.guiFramework.demo.simple;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.PropertyButtonModel;
import org.qenherkhopeshef.guiFramework.SimpleApplicationFramework;


/**
 * A quick and dirty demonstration of the simple UI framework.
 * @author rosmord
 */

public class FWDemo {
	
	private static final String DEFINITION_RESOURCE = "org.qenherkhopeshef.guiFramework.demo.simple.demoI8n";

	public FWDemo() throws IOException {
		// Create needed swing objects
		JFrame frame= new JFrame("Simple SWING Framework demo");
		JLabel display= new JLabel("0");
		JToolBar toolbar= new JToolBar(JToolBar.VERTICAL);

		// Create the facade, providing the name of the resource bundle.
		SimpleApplicationFramework facade= new SimpleApplicationFramework();
		
		facade.addRessourceBundle(DEFINITION_RESOURCE);
		
		FWWorkflow fWorkflow= new FWWorkflow(display, facade.getAppDefaults());

		facade.setActionDelegate(fWorkflow);
		
		// Define the list of available actions
		facade.addActionList("menu.txt");
				
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(display, BorderLayout.CENTER);
				
		JMenuBar menuBar = facade.buildMenu("menu.txt");

		// Demonstration of actions used outside of menus.
		toolbar.add(facade.getAction("increase_one"));
		toolbar.add(facade.getAction("increase_minusOne"));
		
		// We should use a button factory !!!!

		JRadioButton b= buildJRadioButton(facade.getActionCatalog(), "setIncrementValue_1");
		toolbar.add(b);
		
		b= buildJRadioButton(facade.getActionCatalog(), "setIncrementValue_10");
		toolbar.add(b);
		
		b=buildJRadioButton(facade.getActionCatalog(), "setIncrementValue_30");
		toolbar.add(b);
		
		toolbar.add(facade.getAction("resetIncrement"));
		frame.getContentPane().add(toolbar, BorderLayout.WEST);
		
		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * Create a JRadioButton for an action.
	 * @param actionCatalog
	 * @param string TODO
	 * @return
	 */
	private JRadioButton buildJRadioButton(HashMap actionCatalog, String string) {
		BundledAction action = (BundledAction) actionCatalog.get(string);
		JRadioButton button= new JRadioButton(action); 
		PropertyButtonModel model= new PropertyButtonModel(action.getTarget(), (String)action.getValue(BundledAction.PROPERTY_NAME), (String)action.getValue(BundledAction.PROPERTY_VALUE));
		button.setModel(model);
		return button;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run()  {
				try {
					FWDemo demo= new FWDemo();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
}