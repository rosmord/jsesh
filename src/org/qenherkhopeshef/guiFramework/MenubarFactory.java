package org.qenherkhopeshef.guiFramework;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.qenherkhopeshef.utils.PlatformDetection;



/**
 * Builder for the menubar.
 * @author rosmord
 */

public class MenubarFactory {

	private Map<String, Action> actions;

	private MenuItemFactory menuItemFactory;

	/**
	 * The menubar we are building.
	 */
	private JMenuBar menuBar;
	
	/**
	 * If we are working on a menu and not on a menuBar, the menu we are filling.
	 */
	
	private JMenu menuToFill; 

	private int currentLevel = 0;

	private Stack<JMenu> menusStack;

	private HashMap<String, JMenuItem> menuMap;
	
	int lineNumber= 0;

	public MenubarFactory(Map<String,Action> actions) {
		this.actions = actions;
	}

	/**
	 * Build a menu using named Actions and a menu hierarchy description. The
	 * resulting menus entries will be accessible through a map, should the
	 * programmer want to modify them afterwards.
	 * 
	 * <p>
	 * Format for the menus: a text file (let's say ASCII). empty lines or
	 * comments lines (#) are ignored.
	 * 
	 * <p>
	 * Else, each line should contain an action identifier (as those used in
	 * BundleActions). The name of the action will be used to retrieve it and
	 * associate it with the entry. Menu hierarchy will be described by
	 * indentation (leading spaces). Entries without leading spaces are menu
	 * entries. Others are menu items.
	 * 
	 * <p>
	 * Actions whose name ends in "Menu" are supposed to be menus or sub-menus.
	 * 
	 * <pre>
	 *  # Example file.
	 *  FileMenu
	 *   OpenAction
	 *   SaveAction
	 *   CloseAction
	 *  EditMenu
	 *   CutAction
	 *   CopyAction
	 *   PasteAction
	 *  </pre>
     *  
	 *  @param reader a file describing the menus
	 *  @return
	 *  @throws IOException
	 * 
	 */
	public JMenuBar buildMenuBar(InputStreamReader reader) throws IOException {
		menuBar = new JMenuBar();
		fillMenuBar(menuBar, reader);
		return menuBar;
		}
	
	/**
	 * Fills an existing menu bar.
	 * @param menuBar
	 * @param reader
	 * @throws IOException
	 */
	public void fillMenuBar(JMenuBar menuBar, InputStreamReader reader) throws IOException {
		menuToFill= null;
		menusStack = new Stack<JMenu>();
		processFile(reader);
	}

	/**
	 * Read a file containing a menu description.
	 * @param reader
	 * @throws IOException
	 */
	private void processFile(InputStreamReader reader) throws IOException {
		// Clear and set all variables.		
		menuItemFactory = buildMenuItemFactory();
		currentLevel = 0;
		lineNumber= 0;
		menuMap = new HashMap<String, JMenuItem>();

		BufferedReader bufferedReader = new BufferedReader(reader);


		String line=null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				lineNumber++;
				if (!line.matches("\\s*|\\s*#.*"))
					addMenuLine(line);
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem with line "+ lineNumber + " : " + line, e);
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Fills an existing menu from the content of a file.
	 * @param menu
	 * @param reader
	 * @throws IOException
	 */
	public void fillJMenu(JMenu menu, InputStreamReader reader) throws IOException {
		menuBar = null;
		menuToFill= menu;
		menusStack = new Stack<JMenu>();
		menusStack.push(menuToFill);
		processFile(reader);
		menuToFill= null;
	}
	
	/**
	 * Menu item factory method. Can be redefined in subclasses if needed.
	 * @return
	 */
	protected MenuItemFactory buildMenuItemFactory() {
		return new MenuItemFactory();
	}

	private void addMenuLine(String line) {
		// Find the indentation level
		int indent = getIndentation(line);
		// The action name is the line striped of leading spaces.
		String actionName = line.substring(indent);

        // Avoid some actions depending on the platform.
        // TODO make this configurable.
        if ("quitApplication".equals(actionName) && PlatformDetection.getPlatform() ==  PlatformDetection.MACOSX)
        {
            return;
        }
        
		if (indent > currentLevel + 1)
			throw new RuntimeException("indentation too wide at line " + line);

		// Find insertion point in menu stack :

		while (currentLevel > indent) {
			menusStack.pop();
			currentLevel--;
		}

		currentLevel = indent;
		// Now, the upper menu object in the stack is the menu entry where
		// the item should go.

		if (line.endsWith("Menu")) {
			
			Action action = getAction(actionName);
			JMenu newMenu;
			if ("y".equals(action.getValue(BundledAction.TEAR_OFF))) {
				newMenu= new JMenu("",true);
				newMenu.setAction(action);
			} else 
				newMenu= new JMenu(action);
			// Multi-columns menus.
			if (action.getValue(BundledAction.NUMBER_OF_COLUMNS) != null) {
				int ncol= ((Integer)action.getValue(BundledAction.NUMBER_OF_COLUMNS)).intValue();
				newMenu.getPopupMenu().setLayout(new GridLayout(0, ncol));
			}
			menuMap.put(actionName, newMenu);
            newMenu.setName(actionName);
			if (indent == 0) {
				if (menuBar != null)
					menuBar.add(newMenu);
				else
					menuToFill.add(newMenu);
			} else {
				JMenu menu = menusStack.peek();
				menu.add(newMenu);
			}
			menusStack.push(newMenu);
			currentLevel = indent + 1; // We expect the next entries to be part
										// of this menu.
		} else if (line.equals("HGLUE")) {
			if (menuBar != null)
				menuBar.add(Box.createHorizontalGlue());
		} else {
			JMenu menu = menusStack.peek();
			if (actionName.startsWith("-"))
				menu.addSeparator();
			else
				add(menu, actionName);
		}

	}

	private int getIndentation(String line) {
		int result = 0;
		while (result < line.length()
				&& Character.isWhitespace(line.charAt(result)))
			result++;
		if (result == line.length())
			throw new RuntimeException("Badly formed line " + line);
		return result;
	}

	private Action getAction(String name) {
		Action result=  actions.get(name);
		if (result == null)
			throw new RuntimeException("Action not found "+ name);
		if (null==result.getValue("Name") && null== result.getValue("Icon") && null== result.getValue("SmallIcon") && null == result.getValue("IsLabelled")) {
			System.err.println("Warning. No text or icon for action  "+ name);
			System.err.println("If you think it's right (e.g. you have defined some other display system,\n" +
					"you can add the property 'IsLabelled' to the action.");			
		}
		return result;
	}

	private void add(JMenu menu, String actionName) {
		JMenuItem item = menuItemFactory.buildItem(getAction(actionName));
        item.setName(actionName);
		menuMap.put(actionName, item);
		menu.add(item);
	}

	/**
	 * Retrieve the menu item associated with a given action.
	 * <p>
	 * Should be called after buildMenu !!!
	 * 
	 * @param actionName
	 * @return a menu item.
	 */
	public JMenuItem getItem(String actionName) {
		return menuMap.get(actionName);
	}

}
