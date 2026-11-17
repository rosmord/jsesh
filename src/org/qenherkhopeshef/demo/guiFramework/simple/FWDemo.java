package org.qenherkhopeshef.demo.guiFramework.simple;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.ActionFactory;
import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.MenubarFactory;
import org.qenherkhopeshef.guiFramework.PropertyButtonModel;


/**
 * A quick and dirty demonstration of the simple UI framework.
 * @author rosmord
 */

public class FWDemo {
	
	public FWDemo() throws IOException {
		JFrame frame= new JFrame("Simple SWING Framework demo");
		JLabel display= new JLabel("0");
		JToolBar toolbar= new JToolBar(JToolBar.VERTICAL);

		// A.1 load I8N properties.
		AppDefaults appDefaults = new AppDefaults();
		appDefaults.addResourceBundle("org.qenherkhopeshef.demo.guiFramework.simple.demoI8n");
		
		FWWorkflow fWorkflow= new FWWorkflow(display, appDefaults);

		// A. Create the actions
		
		
		// A.2 (sort of). create an action catalog.
		HashMap actionCatalog= new HashMap();
		// A.3
		// Load the list of action id.
		// To avoid unnecessary work, the format used to describe the action IDs is the same as the one used
		// to describe the menus.
		Reader actionNamesSource= new InputStreamReader(getClass().getResourceAsStream("menu.txt"),"UTF-8");
		// A.4
		// Use the action factory to build the actions.
		new ActionFactory(fWorkflow, actionCatalog, appDefaults).buildActionsFromText(actionNamesSource);
		// A.5 ... done
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(display, BorderLayout.CENTER);
		
		// Create the menu.
		MenubarFactory menubarFactory = new MenubarFactory(actionCatalog);
		InputStream menuDescription = this.getClass().getResourceAsStream("menu.txt");
		
		JMenuBar menuBar = menubarFactory.buildMenuBar(new InputStreamReader(menuDescription, "UTF-8"));

		// Demonstration of actions used outside of menus.
		toolbar.add((Action) actionCatalog.get("increase_one"));
		toolbar.add((Action) actionCatalog.get("increase_minusOne"));
		
		// We should use a button factory !!!!

		JRadioButton b=buildJRadioButton(actionCatalog, "setIncrementValue_1");
		toolbar.add(b);
		
		b=buildJRadioButton(actionCatalog, "setIncrementValue_10");
		toolbar.add(b);
		
		b=buildJRadioButton(actionCatalog, "setIncrementValue_30");
		toolbar.add(b);
		
		toolbar.add((Action) actionCatalog.get("resetIncrement"));
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
