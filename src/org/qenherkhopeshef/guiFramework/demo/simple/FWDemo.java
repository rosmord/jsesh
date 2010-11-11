package org.qenherkhopeshef.guiFramework.demo.simple;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.PropertyButtonModel;
import org.qenherkhopeshef.guiFramework.SimpleApplicationFactory;

/**
 * A quick and dirty demonstration of the simple UI framework.
 * 
 * @author rosmord
 */

public class FWDemo {

	private static final String DEFINITION_RESOURCE = "org.qenherkhopeshef.guiFramework.demo.simple.demoI8n";

	public FWDemo() throws IOException {
		// Create needed swing objects
		// The way the ui is created might be changed and is probably not the
		// best.
		// (the toolbar, for one, could be created by the ui).
		FWUI ui = new FWUI();

		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);


		FWWorkflow fWorkflow = new FWWorkflow(ui);
		
		// Create the facade, providing the name of the resource bundle.
		SimpleApplicationFactory simpleApplicationFW = new SimpleApplicationFactory(fWorkflow);
		
		simpleApplicationFW.addRessourceBundle(DEFINITION_RESOURCE);


		// Define the list of available actions
		simpleApplicationFW.addActionList("menu.txt");

		JMenuBar menuBar = simpleApplicationFW.buildMenuBar("menu.txt");

		// Demonstration of actions used outside of menus.
		toolbar.add(simpleApplicationFW.getAction("increase_one"));
		toolbar.add(simpleApplicationFW.getAction("increase_minusOne"));

		// We should use a button factory !!!!

		JRadioButton b = buildJRadioButton(simpleApplicationFW
				.getActionCatalog(), "setIncrementValue_1");
		toolbar.add(b);

		b = buildJRadioButton(simpleApplicationFW.getActionCatalog(),
				"setIncrementValue_10");
		toolbar.add(b);

		b = buildJRadioButton(simpleApplicationFW.getActionCatalog(),
				"setIncrementValue_30");
		toolbar.add(b);

		toolbar.add(simpleApplicationFW.getAction("resetIncrement"));
		ui.addToolbar(toolbar);
		ui.setMenu(menuBar);
		ui.pack();

	}

	/**
	 * Create a JRadioButton for an action.
	 * 
	 * The action must be a BundleAction.
	 * 
	 * TODO : remove this type limitation, replace it with another kind.
	 * 
	 * @param actionCatalog
	 *            a catalog of actions
	 * @param string
	 *            the name of the action.
	 * @return
	 */
	private JRadioButton buildJRadioButton(Map<String, Action> actionCatalog,
			String string) {
		try {
			BundledAction action = (BundledAction) actionCatalog.get(string);
			JRadioButton button = new JRadioButton(action);
			PropertyButtonModel model = new PropertyButtonModel(action
					.getTarget(), (String) action
					.getValue(BundledAction.PROPERTY_NAME), (String) action
					.getValue(BundledAction.PROPERTY_VALUE));
			button.setModel(model);
			return button;
		} catch (ClassCastException e) {
			throw new RuntimeException(
					"Can only build radio button for bundle actions");
		}
	}

	public static void main(String[] args) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					FWDemo demo = new FWDemo();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
	}
}