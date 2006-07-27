package org.eclipse.ecf.provider.remoteservice.generic.registry;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteCallImpl;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

public class RemoteServiceRegistrationImpl implements
		IRemoteServiceRegistration, Serializable {

	private static final long serialVersionUID = -3206899332723536545L;

	transient Object service;

	/** service classes for this registration. */
	protected String[] clazzes;

	/** service id. */
	protected long serviceid;

	/** properties for this registration. */
	protected Properties properties;

	/** service ranking. */
	protected int serviceranking;

	/* internal object to use for synchronization */
	transient protected Object registrationLock = new Object();

	/** The registration state */
	protected int state = REGISTERED;

	protected ID containerID = null;

	public static final int REGISTERED = 0x00;

	public static final int UNREGISTERING = 0x01;

	public static final int UNREGISTERED = 0x02;

	protected transient RemoteServiceReferenceImpl reference = null;
	
	public RemoteServiceRegistrationImpl() {

	}

	public void publish(RemoteServiceRegistryImpl registry, Object service,
			String[] clazzes, Dictionary properties) {
		this.service = service;
		this.clazzes = clazzes;
		this.containerID = registry.getContainerID();
		this.reference = new RemoteServiceReferenceImpl(this);
		synchronized (registry) {
			serviceid = registry.getNextServiceId();
			this.properties = createProperties(properties);
			registry.publishService(this);
		}
		notifyPublishServiceEvent();
	}

	private void notifyPublishServiceEvent() {
		// XXX should notify via event here
		System.out.println("TBD notification of publish service event needed");
	}

	public Object getService() {
		return service;
	}

	public ID getContainerID() {
		return containerID;
	}

	public IRemoteServiceReference getReference() {
		if (reference == null) {
			synchronized (this) {
				reference = new RemoteServiceReferenceImpl(this);
			}
		}
		return reference;
	}

	public void setProperties(Dictionary properties) {
		synchronized (registrationLock) {
			/* in the process of unregistering */
			if (state != REGISTERED) {
				throw new IllegalStateException("Service already registered");
			}
			this.properties = createProperties(properties);
		}

		// XXX Need to notify that registration modified
	}

	public void unregister() {
		// TODO Auto-generated method stub

	}

	/**
	 * Construct a properties object from the dictionary for this
	 * ServiceRegistration.
	 * 
	 * @param props
	 *            The properties for this service.
	 * @return A Properties object for this ServiceRegistration.
	 */
	protected Properties createProperties(Dictionary props) {
		Properties properties = new Properties(props);

		properties.setProperty(RemoteServiceRegistryImpl.REMOTEOBJECTCLASS,
				clazzes);

		properties.setProperty(RemoteServiceRegistryImpl.REMOTESERVICE_ID,
				new Long(serviceid));

		Object ranking = properties
				.getProperty(RemoteServiceRegistryImpl.REMOTESERVICE_RANKING);

		serviceranking = (ranking instanceof Integer) ? ((Integer) ranking)
				.intValue() : 0;

		return (properties);
	}

	static class Properties extends Hashtable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3684607010228779249L;

		/**
		 * Create a properties object for the service.
		 * 
		 * @param props
		 *            The properties for this service.
		 */
		private Properties(int size, Dictionary props) {
			super((size << 1) + 1);

			if (props != null) {
				synchronized (props) {
					Enumeration keysEnum = props.keys();

					while (keysEnum.hasMoreElements()) {
						Object key = keysEnum.nextElement();

						if (key instanceof String) {
							String header = (String) key;

							setProperty(header, props.get(header));
						}
					}
				}
			}
		}

		/**
		 * Create a properties object for the service.
		 * 
		 * @param props
		 *            The properties for this service.
		 */
		protected Properties(Dictionary props) {
			this((props == null) ? 2 : Math.max(2, props.size()), props);
		}

		/**
		 * Get a clone of the value of a service's property.
		 * 
		 * @param key
		 *            header name.
		 * @return Clone of the value of the property or <code>null</code> if
		 *         there is no property by that name.
		 */
		protected Object getProperty(String key) {
			return (cloneValue(get(key)));
		}

		/**
		 * Get the list of key names for the service's properties.
		 * 
		 * @return The list of property key names.
		 */
		protected synchronized String[] getPropertyKeys() {
			int size = size();

			String[] keynames = new String[size];

			Enumeration keysEnum = keys();

			for (int i = 0; i < size; i++) {
				keynames[i] = (String) keysEnum.nextElement();
			}

			return (keynames);
		}

		/**
		 * Put a clone of the property value into this property object.
		 * 
		 * @param key
		 *            Name of property.
		 * @param value
		 *            Value of property.
		 * @return previous property value.
		 */
		protected synchronized Object setProperty(String key, Object value) {
			return (put(key, cloneValue(value)));
		}

		/**
		 * Attempt to clone the value if necessary and possible.
		 * 
		 * For some strange reason, you can test to see of an Object is
		 * Cloneable but you can't call the clone method since it is protected
		 * on Object!
		 * 
		 * @param value
		 *            object to be cloned.
		 * @return cloned object or original object if we didn't clone it.
		 */
		protected static Object cloneValue(Object value) {
			if (value == null)
				return null;
			if (value instanceof String) /* shortcut String */
				return (value);

			Class clazz = value.getClass();
			if (clazz.isArray()) {
				// Do an array copy
				Class type = clazz.getComponentType();
				int len = Array.getLength(value);
				Object clonedArray = Array.newInstance(type, len);
				System.arraycopy(value, 0, clonedArray, 0, len);
				return clonedArray;
			}
			// must use reflection because Object clone method is protected!!
			try {
				return (clazz.getMethod("clone", null).invoke(value, null));
			} catch (Exception e) {
				/* clone is not a public method on value's class */
			} catch (Error e) {
				/* JCL does not support reflection; try some well known types */
				if (value instanceof Vector)
					return (((Vector) value).clone());
				if (value instanceof Hashtable)
					return (((Hashtable) value).clone());
			}
			return (value);
		}

		public synchronized String toString() {
			String keys[] = getPropertyKeys();

			int size = keys.length;

			StringBuffer sb = new StringBuffer(20 * size);

			sb.append('{');

			int n = 0;
			for (int i = 0; i < size; i++) {
				String key = keys[i];
				if (!key.equals(RemoteServiceRegistryImpl.REMOTEOBJECTCLASS)) {
					if (n > 0)
						sb.append(", "); //$NON-NLS-1$

					sb.append(key);
					sb.append('=');
					Object value = get(key);
					if (value.getClass().isArray()) {
						sb.append('[');
						int length = Array.getLength(value);
						for (int j = 0; j < length; j++) {
							if (j > 0)
								sb.append(',');
							sb.append(Array.get(value, j));
						}
						sb.append(']');
					} else {
						sb.append(value);
					}
					n++;
				}
			}

			sb.append('}');

			return (sb.toString());
		}
	}

	public Object getProperty(String key) {
		return properties.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return properties.getPropertyKeys();
	}

	public long getServiceId() {
		return serviceid;
	}

	public Object callService(RemoteCallImpl call) throws Exception {
		return call.invoke(service);
	}
}
