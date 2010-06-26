/*******************************************************************************
 * Copyright (c) 2010 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.provider.dnssd;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Update;
import org.xbill.DNS.ZoneTransferIn;

public class DnsSdAdvertiserServiceTest extends AbstractDiscoveryTest {

	public DnsSdAdvertiserServiceTest() {
		super(DnsSdTestHelper.ECF_DISCOVERY_DNSSD + ".advertiser");
		setNamingAuthority(DnsSdTestHelper.NAMING_AUTH);
		setScope(DnsSdTestHelper.REG_DOMAIN);
		setServices(new String[]{DnsSdTestHelper.REG_SCHEME});
		setProtocol(DnsSdTestHelper.PROTO);
		setComparator(new DnsSdAdvertiserComparator());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// purge all SRV records from the test domain
		ZoneTransferIn xfr = ZoneTransferIn.newAXFR(new Name(DnsSdTestHelper.REG_DOMAIN), DnsSdTestHelper.DNS_SERVER, null);
		List records  = xfr.run();
		for (Iterator itr = records.iterator(); itr.hasNext();) {
			Record record = (Record) itr.next();
			String name = record.getName().toString();
			if(name.startsWith("_" + DnsSdTestHelper.REG_SCHEME + "._" + DnsSdTestHelper.PROTO)) {
				Update update = new Update(Name.fromString(DnsSdTestHelper.REG_DOMAIN + "."));
				update.delete(record);
				SimpleResolver resolver = new SimpleResolver(DnsSdTestHelper.DNS_SERVER);
				resolver.setTCP(true);
				resolver.setTSIGKey(new TSIG(DnsSdTestHelper.TSIG_KEY_NAME, DnsSdTestHelper.TSIG_KEY));
				Message response = resolver.send(update);
				int rcode = response.getRcode();
				assertTrue("", rcode == 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		return Activator.getDefault().getDiscoveryAdvertiser();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.AbstractDiscoveryTest#getDiscoveryLocator()
	 */
	protected IDiscoveryLocator getDiscoveryLocator() {
		return Activator.getDefault().getDiscoveryLocator();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.dnssd.DnsSdDiscoveryServiceTest#testRegisterService()
	 */
	public void testRegisterService() throws Exception {
		
		// actually register the service
		discoveryAdvertiser.registerService(serviceInfo);
		
		// check postcondition service is registered
		ZoneTransferIn xfr = ZoneTransferIn.newAXFR(new Name(DnsSdTestHelper.REG_DOMAIN), DnsSdTestHelper.DNS_SERVER, null);
		assertTrue("Mismatch between DNS response and IServiceInfo", comparator.compare(serviceInfo, xfr.run()) == 0);
	}
	
	
	/**
	 * Tests that a register is handled correctly when no key is present
	 * for that domain and the underlying ddns call fails
	 */
	public void testRegisterServiceWithoutHostKey() {
		fail("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.dnssd.DnsSdDiscoveryServiceTest#testUnregisterService()
	 */
	public void testUnregisterService() throws ContainerConnectException {
		fail("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.provider.dnssd.DnsSdDiscoveryServiceTest#testUnregisterAllServices()
	 */
	public void testUnregisterAllServices() throws ContainerConnectException {
		//TODO make sure not to accidentally remove preexisting SRV records
		fail("Not yet implemented");
	}
}
