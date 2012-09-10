package jsesh.glossary;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Support class for event handling, Buoy-style. When an event listener
 * disappear, it will be removed from the support (weak references are used).
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */

public class EventSupport {

	/**
	 * An attempt at using Buoy logic in JSesh objects, just to see... A
	 * callback provides either a zero-args method, or a method which accepts
	 * the event.
	 * 
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 */
	static class CallBack {

		private Object object;
		private Method method;
		private boolean withArgs = false;

		public CallBack(Object object, String methodName,
				Class<? extends EventObject> eventClass) {
			this.object = object;
			try {
				this.method = object.getClass().getMethod(methodName,
						new Class<?>[] { EventObject.class });
				this.withArgs = true;
			} catch (NoSuchMethodException e) {
				try {
					this.method = object.getClass().getMethod(methodName);
					this.withArgs = false;
				} catch (NoSuchMethodException e1) {
					throw new RuntimeException(e);
				}
			}
		}

		public void call(EventObject event) {
			try {
				if (withArgs) {
					method.invoke(object, event);
				} else {
					method.invoke(object);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
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

		/*
		 * (non-Javadoc)
		 * 
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

	private HashMap<Class<? extends EventObject>, Set<CallBack>> map = new HashMap<Class<? extends EventObject>, Set<CallBack>>();

	/**
	 * Adds an event link, which will result in calling the method methodName on
	 * object when the event is raised. If the method takes {@link EventObject}
	 * as a parameter, it will be passed. (note that we should propose a more
	 * precise scheme, with specific events classes).
	 * 
	 * @param eventClass
	 * @param object
	 * @param methodName
	 */
	public void addEventLink(Class<? extends EventObject> eventClass,
			Object object, String methodName) {
		if (!map.containsKey(eventClass)) {
			Set<CallBack> set = Collections
					.newSetFromMap(new WeakHashMap<CallBack, Boolean>());
			map.put(eventClass, set);
		}
		map.get(eventClass).add(new CallBack(object, methodName, eventClass));
	}

	public void removeEventLink(Class<? extends EventObject> eventClass,
			Object object, String methodName) {
		if (map.containsKey(eventClass)) {
			map.get(eventClass).remove(
					new CallBack(object, methodName, eventClass));
		}
	}

	public void fireEvent(EventObject e) {
		Set<CallBack> callbacks = map.get(e.getClass());
		if (callbacks != null) {
			for (CallBack c : callbacks) {
				c.call(e);
			}
		}
	}
}
