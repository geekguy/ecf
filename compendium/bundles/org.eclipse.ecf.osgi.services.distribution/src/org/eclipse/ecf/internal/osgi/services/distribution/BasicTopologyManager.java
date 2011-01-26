package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Properties;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.remoteserviceadmin.EndpointListener;

public class BasicTopologyManager extends AbstractTopologyManager implements
		EventHook, EndpointListener {

	private ServiceRegistration endpointListenerRegistration;

	private ServiceRegistration eventHookRegistration;

	public BasicTopologyManager(BundleContext context) {
		super(context);
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				"(" //$NON-NLS-1$
						+ org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID
						+ "=*)"); //$NON-NLS-1$
		endpointListenerRegistration = getContext().registerService(
				EndpointListener.class.getName(), this, (Dictionary) props);

		// Register as EventHook, so that we get notified when remote services
		// are registered
		eventHookRegistration = getContext().registerService(
				EventHook.class.getName(), this, null);
	}

	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointAdded(endpoint, matchedFilter);
	}

	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		handleEndpointRemoved(endpoint, matchedFilter);
	}

	public void event(ServiceEvent event, Collection contexts) {
		handleEvent(event, contexts);
	}

	public void close() {
		if (eventHookRegistration != null) {
			eventHookRegistration.unregister();
			eventHookRegistration = null;
		}
		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		super.close();
	}

}
