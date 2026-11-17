package jsesh.uiFramework;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * Action which takes (most of) its informations from a ressource bundle. from
 * an article by Hans Muller on java.net. Lots of bells and wistles added (too many ?).
 */

public class BundledAction extends AbstractAction {

	private static final long serialVersionUID = -5071382513186199304L;

	
	/**
	 * Boolean conditions that should be met so that the action is possible.
	 */
	public static final String PRECONDITIONS = "Preconditions";
	
	/**
	 * For "radio-button" actions, the name of their groups.
	 */
	public static final String GROUP_PROPERTY= "GroupProperty";

	/**
	 * For actions linked with a property, the name of the said property.
	 */
	public static final String PROPERTY_NAME= "PropertyName";
	
	/**
	 * For actions linked with a boolean property, the name of the said property.
	 * (the action will toggle the property).
	 */
	
	public static final String BOOLEAN_PROPERTY= "BooleanProperty";
	
	/**
	 * For actions linked with a property, the name of the said property, with a capital letter.
	 */
	public static final String PROPERTY_NAME_CAPITALIZED= "PropertyNameCapitalized";
	
	/**
	 * For actions linked with a property, the value of the property.
	 */
	
	public static final String PROPERTY_VALUE= "PropertyValue";
	
	/**
	 * For Menu actions, optionally, the number of columns.
	 */

	public static final String NUMBER_OF_COLUMNS= "NumberOfColumns";

	
	/**
	 * For Menu actions, can the menu be torn off ?
	 * possible values are "y" and "n". 
	 */
	
	public static final String TEAR_OFF= "TearOff";
	
	/**
	 * Property for explicit argument setting.
	 * For some actions, the default system for arguments can't work (for instance, their arguments contain spaces and the like).
	 * Hence we propose an explicit way.
	 */
	
	public static final String METHOD_ARGUMENT= "Argument";
	private final static String actionKeys[] = { Action.NAME,
			Action.SHORT_DESCRIPTION, Action.LONG_DESCRIPTION,
			Action.SMALL_ICON, Action.ACTION_COMMAND_KEY,
			Action.ACCELERATOR_KEY, Action.MNEMONIC_KEY,
			TEAR_OFF,
			PRECONDITIONS, GROUP_PROPERTY, BOOLEAN_PROPERTY, METHOD_ARGUMENT, NUMBER_OF_COLUMNS};

	/**
	 * The object which will receive this action's input.
	 */

	private PropertyHolder target;

	/**
	 * The unique name of the action.
	 */
	private String actionName;

	/**
	 * The method to call.
	 */
	private String methodName;

	/**
	 * Arguments to pass to the method.
	 */
	private String[] methodArguments;

	/**
	 * Create an action which will take most of its data from a property file.
	 * <p>
	 * This allows multilingual softwares and the like.
	 * <p>
	 * When the action is called, a specific method will be called on the target
	 * object. If the "method" name contains underscores, the parts after the
	 * underscores will be understood as arguments (of type String).
	 * 
	 * @param target
	 *            the object the action will apply to.
	 * @param actionName
	 *            the name of this action (in reality, the name of the method
	 *            plus its arguments).
	 * @param defaults
	 *            the bundle of properties to use.
	 */
	public BundledAction(PropertyHolder target, String actionName, AppDefaults defaults) {
		super(actionName); // methodName is the default label text
		this.target = target;
		this.actionName = actionName;
		// Deal with possible arguments.
		String[] m = actionName.split("_");
		this.methodName = m[0];
		methodArguments = new String[m.length - 1];
		System.arraycopy(m, 1, methodArguments, 0, methodArguments.length);

		// Empty precondition table.
		putValue(PRECONDITIONS, new String [0]);
		
		for (int i = 0; i < actionKeys.length; i++) {
			String k = actionKeys[i];
			String mk = actionName + "." + k;
			if (k == Action.MNEMONIC_KEY) {
				putValue(k, defaults.getKeyCode(mk));
			} else if (k == Action.ACCELERATOR_KEY) {
				putValue(k, defaults.getKeyStroke(mk));
			} else if (k == Action.SMALL_ICON) {
				putValue(k, defaults.getIcon(mk));
			} else if (k == PRECONDITIONS) {
				if (defaults.getString(mk) != null) {
					String s[] = defaults.getString(mk).split(", *");
					putValue(k, s);
				}
			} else if (k == NUMBER_OF_COLUMNS) {
				if (defaults.getString(mk) != null) {
					int ncols= Integer.parseInt(defaults.getString(mk));
					putValue(NUMBER_OF_COLUMNS, new Integer(ncols));
				}
			}
			else {
				putValue(k, defaults.get(mk));
			}
		}

		// Explicit setting of arguments.
		if (getValue(METHOD_ARGUMENT) != null)
			methodArguments= new String [] { (String) getValue(METHOD_ARGUMENT)};
		
		if (getValue(GROUP_PROPERTY) != null)
			prepareGroupValue();
		
		if (getValue(BOOLEAN_PROPERTY) != null)
			prepareBooleanValue();
	}

	private void prepareBooleanValue() {
		putValue(PROPERTY_NAME, getValue(BOOLEAN_PROPERTY));
		putValue(PROPERTY_NAME_CAPITALIZED, capitalizeString((String)getValue(PROPERTY_NAME)));
	}

	private void prepareGroupValue() {
		putValue(PROPERTY_NAME, getValue(GROUP_PROPERTY));
		putValue(PROPERTY_VALUE, getMethodArguments()[0]);
		putValue(PROPERTY_NAME_CAPITALIZED, capitalizeString((String)getValue(PROPERTY_NAME)));
	}

	private static String capitalizeString(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			/**
			 * Boolean and group properties are dealt with through their properties. 
			 */
			if (getValue(BOOLEAN_PROPERTY) != null
					|| getValue(GROUP_PROPERTY) != null)
				return;
			
			Class cls = target.getClass();
			Class argsClasses[];
			
			argsClasses = new Class[this.methodArguments.length];
			Arrays.fill(argsClasses, String.class);
			
			Method m = cls.getMethod(methodName, argsClasses);
			m.invoke(target, methodArguments);
		} catch (Exception exception) {
			// Let the exception propagate.
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Returns the target of this action.
	 * 
	 * @return
	 */
	public PropertyHolder getTarget() {
		return target;
	}

	/**
	 * Returns the method name for this action.
	 * 
	 * @return
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Return a name which identifies this action. It might be more complex than
	 * just a method name, as parameters may be taken into account.
	 * 
	 * @return
	 */

	public String getActionName() {
		return actionName;
	}

	public String[] getMethodArguments() {
		return methodArguments;
	}
	
	public String[] getPreconditions() {
		return (String [])getValue(PRECONDITIONS); 
	}
	
}