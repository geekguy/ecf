/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.discovery;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFRuntimeException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.discovery.Activator;
import org.eclipse.ecf.internal.provider.discovery.CompositeNamespace;

public class CompositeDiscoveryContainer extends AbstractDiscoveryContainerAdapter implements IDiscoveryService {

	public static final String NAME = "ecf.discovery.composite"; //$NON-NLS-1$

	protected class CompositeContainerServiceListener implements IServiceListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceDiscovered(final IServiceEvent event) {
			final Collection col = getListeners(event.getServiceInfo().getServiceID().getServiceTypeID());
			if (!col.isEmpty()) {
				for (final Iterator itr = col.iterator(); itr.hasNext();) {
					final IServiceListener isl = (IServiceListener) itr.next();
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					isl.serviceDiscovered(new CompositeServiceContainerEvent(event, getConnectedID()));
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceDiscovered", //$NON-NLS-1$
							"serviceResolved fired for listener " //$NON-NLS-1$
									+ isl.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			} else {
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceDiscovered", //$NON-NLS-1$
						"serviceResolved fired without any listeners present"); //$NON-NLS-1$
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceUndiscovered(final IServiceEvent event) {
			final Collection col = getListeners(event.getServiceInfo().getServiceID().getServiceTypeID());
			if (!col.isEmpty()) {
				for (final Iterator itr = col.iterator(); itr.hasNext();) {
					final IServiceListener isl = (IServiceListener) itr.next();
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					isl.serviceUndiscovered(new CompositeServiceContainerEvent(event, getConnectedID()));
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceUndiscovered", //$NON-NLS-1$
							"serviceRemoved fired for listener " //$NON-NLS-1$
									+ isl.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			} else {
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceUndiscovered", //$NON-NLS-1$
						"serviceRemoved fired without any listeners present"); //$NON-NLS-1$
			}
		}
	}

	protected class CompositeContainerServiceTypeListener implements IServiceTypeListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public synchronized void serviceTypeDiscovered(final IServiceTypeEvent event) {
			// notify our listeners first so they get a chance to register for
			// the type before the underlying provider fires service added
			synchronized (serviceTypeListeners) {
				for (final Iterator itr = serviceTypeListeners.iterator(); itr.hasNext();) {
					final IServiceTypeListener listener = (IServiceTypeListener) itr.next();
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					listener.serviceTypeDiscovered(new CompositeServiceTypeContainerEvent(event, getConnectedID()));
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceTypeDiscovered", //$NON-NLS-1$
							"serviceTypeDiscovered fired for listener " //$NON-NLS-1$
									+ listener.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			}
			// add ourself as a listener to the underlying providers. This might
			// trigger a serviceAdded alread
			final IServiceTypeID istid = event.getServiceTypeID();
			synchronized (containers) {
				for (final Iterator itr = containers.iterator(); itr.hasNext();) {
					// TODO ccstl doesn't have to be a listener for a non
					// matching (namespace) container, but it doesn't hurt
					// either
					final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
					idca.addServiceListener(istid, ccsl);
				}
			}
		}
	}

	protected static final String METHODS_CATCHING = Activator.PLUGIN_ID + "/debug/methods/catching"; //$NON-NLS-1$

	protected static final String METHODS_TRACING = Activator.PLUGIN_ID + "/debug/methods/tracing"; //$NON-NLS-1$

	protected final CompositeContainerServiceListener ccsl = new CompositeContainerServiceListener();
	protected final CompositeContainerServiceTypeListener ccstl = new CompositeContainerServiceTypeListener();

	/**
	 * History of services registered with this IDCA
	 * 
	 * Used on newly added IDCAs 
	 */
	protected Set registeredServices;

	protected final Collection containers;

	private ID targetID;

	/**
	 * @param containers
	 */
	public CompositeDiscoveryContainer(final Collection containers) {
		super(CompositeNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(CompositeDiscoveryContainer.class.getName())));
		this.containers = containers;
		this.registeredServices = new HashSet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(final ID aTargetID, final IConnectContext connectContext) throws ContainerConnectException {
		if (targetID != null || getConfig() == null) {
			throw new ContainerConnectException("Already connected"); //$NON-NLS-1$
		}
		targetID = (aTargetID == null) ? getConfig().getID() : aTargetID;
		fireContainerEvent(new ContainerConnectingEvent(this.getID(), targetID, connectContext));
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IContainer container = (IContainer) itr.next();
				if (container.getConnectedID() == null) {
					container.connect(targetID, connectContext);
				}
				final IDiscoveryLocator idca = (IDiscoveryLocator) container;
				idca.addServiceListener(ccsl);
				idca.addServiceTypeListener(ccstl);
			}
		}
		fireContainerEvent(new ContainerConnectedEvent(this.getID(), targetID));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), getConnectedID()));
		targetID = null;
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IContainer container = (IContainer) itr.next();
				container.disconnect();
			}
			containers.clear();
		}
		synchronized (registeredServices) {
			registeredServices.clear();
		}
		synchronized (allServiceListeners) {
			allServiceListeners.clear();
		}
		synchronized (serviceListeners) {
			serviceListeners.clear();
		}
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.clear();
		}
		fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), getConnectedID()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#dispose()
	 */
	public void dispose() {
		disconnect();
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IContainer container = (IContainer) itr.next();
				container.dispose();
			}
			containers.clear();
		}
		targetID = null;
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return targetID;
	}

	private IServiceID getServiceIDForDiscoveryContainer(final IServiceID service, final IDiscoveryLocator dca) {
		final Namespace connectNamespace = dca.getServicesNamespace();
		if (!connectNamespace.equals(service.getNamespace())) {
			return (IServiceID) connectNamespace.createInstance(new Object[] {service.getServiceTypeID().getName(), service.getLocation()});
		}
		return service;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(final IServiceID aService) {
		Assert.isNotNull(aService);
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
				final IServiceID isi = getServiceIDForDiscoveryContainer(aService, idca);
				final IServiceInfo service = idca.getServiceInfo(isi);
				if (service != null) {
					return service;
				}
			}
		}
		return null;
	}

	private IServiceInfo getServiceInfoForDiscoveryContainer(final IServiceInfo aSi, final IDiscoveryLocator idca) {
		final IServiceID serviceId = aSi.getServiceID();
		final IServiceID sid = getServiceIDForDiscoveryContainer(serviceId, idca);
		final IServiceTypeID serviceTypeID = sid.getServiceTypeID();
		return new ServiceInfo(serviceId.getLocation(), aSi.getServiceName(), serviceTypeID, aSi.getPriority(), aSi.getWeight(), aSi.getServiceProperties());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices()
	 */
	public IServiceInfo[] getServices() {
		final Set set = new HashSet();
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
				final IServiceInfo[] services = idca.getServices();
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceInfo[]) set.toArray(new IServiceInfo[set.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(final IServiceTypeID type) {
		Assert.isNotNull(type);
		final Set set = new HashSet();
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
				final IServiceTypeID isti = getServiceTypeIDForDiscoveryContainer(type, idca);
				final IServiceInfo[] services = idca.getServices(isti);
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceInfo[]) set.toArray(new IServiceInfo[set.size()]);
	}

	private IServiceTypeID getServiceTypeIDForDiscoveryContainer(final IServiceTypeID type, final IDiscoveryLocator dca) {
		final Namespace connectNamespace = dca.getServicesNamespace();
		if (!connectNamespace.equals(type.getNamespace())) {
			return ServiceIDFactory.getDefault().createServiceTypeID(connectNamespace, type);
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceTypes()
	 */
	public IServiceTypeID[] getServiceTypes() {
		final Set set = new HashSet();
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
				final IServiceTypeID[] services = idca.getServiceTypes();
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceTypeID[]) set.toArray(new IServiceTypeID[set.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(final IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		synchronized (registeredServices) {
			Assert.isTrue(registeredServices.add(serviceInfo));
		}
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryAdvertiser dca = (IDiscoveryAdvertiser) itr.next();
				final IServiceInfo isi = getServiceInfoForDiscoveryContainer(serviceInfo, (IDiscoveryLocator) dca);
				dca.registerService(isi);
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "registerService", "registeredService " //$NON-NLS-1$ //$NON-NLS-2$
						+ serviceInfo.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(final IServiceInfo serviceInfo) {
		Assert.isNotNull(serviceInfo);
		synchronized (registeredServices) {
			// no assert as unregisterService might be called with an non-existing ISI
			registeredServices.remove(serviceInfo);
		}
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryAdvertiser idca = (IDiscoveryAdvertiser) itr.next();
				final IServiceInfo isi = getServiceInfoForDiscoveryContainer(serviceInfo, (IDiscoveryLocator) idca);
				idca.unregisterService(isi);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#unregisterAllServices()
	 */
	public void unregisterAllServices() {
		synchronized (registeredServices) {
			synchronized (containers) {
				for (final Iterator itr = containers.iterator(); itr.hasNext();) {
					final IDiscoveryAdvertiser idca = (IDiscoveryAdvertiser) itr.next();
					for (Iterator itr2 = registeredServices.iterator(); itr2.hasNext();) {
						final IServiceInfo serviceInfo = (IServiceInfo) itr2.next();
						final IServiceInfo isi = getServiceInfoForDiscoveryContainer(serviceInfo, (IDiscoveryLocator) idca);
						idca.unregisterService(isi);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#purgeCache()
	 */
	public IServiceInfo[] purgeCache() {
		final Set set = new HashSet();
		synchronized (containers) {
			for (final Iterator itr = containers.iterator(); itr.hasNext();) {
				final IDiscoveryLocator idca = (IDiscoveryLocator) itr.next();
				final IServiceInfo[] services = idca.purgeCache();
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceInfo[]) set.toArray(new IServiceInfo[set.size()]);
	}

	/**
	 * @param object
	 * @return true on success
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addContainer(final Object object) {
		// connect the new container if necessary and register ourself as listeners
		IContainer iContainer = (IContainer) object;
		if (iContainer.getConnectedID() == null) {
			try {
				iContainer.connect(targetID, null);
			} catch (ContainerConnectException e) {
				// we eat the exception here
				Trace.catching(Activator.PLUGIN_ID, METHODS_CATCHING, this.getClass(), "addContainer(Object)", e); //$NON-NLS-1$
			}
		}
		final IDiscoveryLocator idca = (IDiscoveryLocator) object;
		idca.addServiceListener(ccsl);
		idca.addServiceTypeListener(ccstl);
		// register previously registered with the new IDS
		synchronized (registeredServices) {
			final IDiscoveryAdvertiser ida = (IDiscoveryAdvertiser) object;
			for (final Iterator itr = registeredServices.iterator(); itr.hasNext();) {
				final IServiceInfo serviceInfo = (IServiceInfo) itr.next();
				try {
					ida.registerService(serviceInfo);
				} catch (final ECFRuntimeException e) {
					// we eat the exception here since the original registerService call is long done
					Trace.catching(Activator.PLUGIN_ID, METHODS_CATCHING, this.getClass(), "addContainer(Object)", e); //$NON-NLS-1$
				}
			}
		}
		synchronized (containers) {
			Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "addContainer(Object)", "addContainer " //$NON-NLS-1$ //$NON-NLS-2$
					+ object.toString());
			return containers.add(object);
		}
	}

	/**
	 * @param object
	 * @return true on success
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeContainer(final Object object) {
		final IDiscoveryLocator idca = (IDiscoveryLocator) object;
		idca.removeServiceListener(ccsl);
		idca.removeServiceTypeListener(ccstl);
		synchronized (containers) {
			Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "removeContainer(Object)", "removeContainer " //$NON-NLS-1$ //$NON-NLS-2$
					+ object.toString());
			return containers.remove(object);
		}
	}

	/**
	 * @return The List of currently registered containers.
	 * @since 2.1
	 */
	public Collection getDiscoveryContainers() {
		return Collections.unmodifiableCollection(containers);
	}
}
