/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.DiscoveredServiceTracker;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * This class monitors the lifecycle of DiscoveredServiceTrackers. They will be
 * notified on registration and modification if any service has been published.
 * 
 * TODO: replace arg0 arg names with more descriptive ones
 */
public class DSTTracker implements ServiceTrackerCustomizer {

	// Map of DiscoveredServiceTracker, property map
	private Map /* <DiscoveredServiceTracker, Map> */dsTrackers = null;

	private BundleContext context = null;

	/**
	 * 
	 * @param ctx
	 */
	public DSTTracker(final BundleContext ctx) {
		context = ctx;
		dsTrackers = Collections.synchronizedMap(new HashMap());
	}

	/**
	 * @return a new instance of dsTracker map
	 */
	public Map getDsTrackers() {
		return new HashMap(dsTrackers);
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	public Object addingService(final ServiceReference dstTrackerReference) {
		DiscoveredServiceTracker tracker = (DiscoveredServiceTracker) context
				.getService(dstTrackerReference);
		if (!dsTrackers.keySet().contains(tracker)) {
			FileBasedDiscoveryImpl.log(LogService.LOG_INFO,
					"adding service tracker " + tracker);
			addTracker(dstTrackerReference);
			Map changedFilterCriteria = determineChangedFilterProperties(
					tracker, dstTrackerReference);
			FileBasedDiscoveryImpl.notifyOnAvailableSEDs(tracker,
					changedFilterCriteria);
			return tracker;
		}
		return null;
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void modifiedService(final ServiceReference dstTrackerReference,
			final Object arg1) {
		DiscoveredServiceTracker tracker = (DiscoveredServiceTracker) context
				.getService(dstTrackerReference);
		Map changedFilterCriteria = determineChangedFilterProperties(tracker,
				dstTrackerReference);
		FileBasedDiscoveryImpl.log(LogService.LOG_INFO,
				"modified service tracker " + tracker + " ;changedFilter = "
						+ changedFilterCriteria);
		dsTrackers.remove(tracker);
		addTracker(dstTrackerReference);
		FileBasedDiscoveryImpl.notifyOnAvailableSEDs(tracker,
				changedFilterCriteria);
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	public void removedService(final ServiceReference dstTrackerReference,
			final Object arg1) {
		DiscoveredServiceTracker tracker = (DiscoveredServiceTracker) context
				.getService(dstTrackerReference);
		FileBasedDiscoveryImpl.log(LogService.LOG_INFO,
				"removing service tracker " + tracker);
		dsTrackers.remove(tracker);
	}

	/**
	 * This method fills a map with key value pairs, where the key is
	 * {@link#DiscoveredServiceTracker.PROP_KEY_MATCH_CRITERIA_INTERFACES} or
	 * {@linkDiscoveredServiceTracker.PROP_KEY_MATCH_CRITERIA_FILTERS} and the
	 * value is the list of new entries or empty if nothing is new.
	 * 
	 * @param tracker
	 *            the registered or modified tracker registration
	 * @param serviceReference
	 * @return a map that contains two entries where the value contains the
	 *         added properties. It returns empty values if no new properties
	 *         have been found. It returns null values if the new properties are
	 *         null or empty
	 */
	private Map determineChangedFilterProperties(
			DiscoveredServiceTracker tracker, ServiceReference serviceReference) {
		Map result = new HashMap();

		Map props = (Map) dsTrackers.get(tracker);
		if (props != null) {
			Collection oldInterfaceCriteria = (Collection) props
					.get(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA);
			Collection oldFilterCriteria = (Collection) props
					.get(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA);

			Collection newInterfaceCriteria = (Collection) serviceReference
					.getProperty(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA);
			Collection newFilterCriteria = (Collection) serviceReference
					.getProperty(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA);

			result
					.put(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA,
							getAddedEntries(oldInterfaceCriteria,
									newInterfaceCriteria));
			result.put(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA,
					getAddedEntries(oldFilterCriteria, newFilterCriteria));
		} else {
			// set empty lists as values
			result.put(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA,
					new ArrayList());
			result.put(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA,
					new ArrayList());
		}
		return result;
	}

	/**
	 * Compares to list and returns only those who are not in the old list. Or
	 * it returns an empty list if nothing has been found.
	 * 
	 * @param oldList
	 *            the existing set of properties
	 * @param newList
	 *            the new set of properties
	 * @return a list of only new properties; empty list if no new has been
	 *         found; null if newList is null or empty
	 */
	private Collection getAddedEntries(Collection oldList, Collection newList) {
		ArrayList result = new ArrayList();
		if (newList == null || newList.isEmpty()) {
			return null;
		}
		if (oldList == null) {
			return newList;
		}
		Iterator it = newList.iterator();
		while (it.hasNext()) {
			Object val = it.next();
			if (!oldList.contains(val)) {
				result.add(val);
			}
		}
		return result;
	}

	/**
	 * Adds a DiscoveredServiceTracker with its properties to our map.
	 * 
	 * @param ref
	 *            reference to the just registered or modified
	 *            DiscoveredServiceTracker
	 */
	private void addTracker(final ServiceReference ref) {
		// Retrieve current service properties (required later when modified to
		// compute the actual modification)
		Map props = new HashMap();
		props
				.put(
						DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA,
						ref
								.getProperty(DiscoveredServiceTracker.INTERFACE_MATCH_CRITERIA));

		props.put(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA, ref
				.getProperty(DiscoveredServiceTracker.FILTER_MATCH_CRITERIA));
		dsTrackers.put((DiscoveredServiceTracker) context.getService(ref),
				props);
	}
}
