package jsesh.glossary;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

/**
 * A simple, explicit and flat policy for focus traversal.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class ExplicitFocusPolicy extends FocusTraversalPolicy {

	private Component[] components;
	private Component defaultComponent;

	public ExplicitFocusPolicy(Component defaultComponent, Component[] components) {
		this.components = components;
	}

	@Override
	public Component getComponentAfter(Container arg0, Component arg1) {
		boolean nextGood= false;
		for (Component c: components) {
			if (nextGood)
				return c;
			if (c== arg1)
				nextGood= true;
				
		}
		return components[0];
	}

	@Override
	public Component getComponentBefore(Container arg0, Component arg1) {
		for (int i= components.length -1; i > 0; i-- ) {
			Component c= components[i];
			if (c== arg1) {
				return components[i-1];
			}				
		}		
		return components[components.length -1];

	}

	@Override
	public Component getDefaultComponent(Container arg0) {
		return defaultComponent;
	}

	@Override
	public Component getFirstComponent(Container arg0) {
		return components[0];
	}

	@Override
	public Component getLastComponent(Container arg0) {
		return components[components.length-1];
	}

}
