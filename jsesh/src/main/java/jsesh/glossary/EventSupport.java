package jsesh.glossary;

import java.awt.Event;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Support class for event handling, Buoy-style. When an event listener
 * disappear, it will be removed from the support (weak references are used).
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
public class EventSupport {

	/**
	 * An attempt at using Buoy logic in JSesh objects, just to see...
	 * 
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 */
	static class CallBack {

		private Object object;
		private Method method;

		public CallBack(Object object, String methodName) {
			this.object = object;
			try {
				this.method = object.getClass().getMethod(methodName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void call() {
			try {
				method.invoke(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((method == null) ? 0 : method.hashCode());
			result = prime * result
					+ ((object == null) ? 0 : object.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof CallBack))
				return false;
			CallBack other = (CallBack) obj;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}
		
		
	}

	private HashMap<Class<? extends Event>, Set<CallBack>> map = new HashMap<Class<? extends Event>, Set<CallBack>>();

	public void addEventLink(Class<? extends Event> eventClass, Object object,
			String methodName) {
		if (! map.containsKey(eventClass)) {
			Set<CallBack> set = Collections.newSetFromMap(
			        new WeakHashMap<CallBack, Boolean>());
			map.put(eventClass, set);
		}
		map.get(eventClass).add(new CallBack(object, methodName));
	}
	
	public void removeEventLink(Class<? extends Event> eventClass, Object object,
			String methodName) {
		if (map.containsKey(eventClass)) {
			map.get(eventClass).remove(new CallBack(object, methodName));
		}
	}

	public void fireEvent(Event e) {
		Set<CallBack> callbacks = map.get(e.getClass());
		for (CallBack c : callbacks) {
			c.call();
		}
	}
}
