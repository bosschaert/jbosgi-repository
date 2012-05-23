/*
 * #%L
 * JBossOSGi Repository: Core
 * %%
 * Copyright (C) 2011 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.test.osgi.repository.tck;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryXMLReader;
import org.jboss.osgi.repository.XPersistentRepository;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.repository.Repository;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Integration with the OSGi TCK.
 *
 * @author David Bosschaert
 */
public class Activator implements BundleActivator {
    private static final String TEST_CLASS_NAME = "org.osgi.test.cases.repository.junit.RepositoryTest";

    private ServiceTracker serviceTracker;
    private ServiceRegistration servicePrimedRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        System.err.println("********* in the integration bundle");

        Filter filter = context.createFilter(
                "(&(objectClass=java.lang.String)(repository-xml=" + TEST_CLASS_NAME + "))");
        serviceTracker = new ServiceTracker(context, filter, null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object svc = super.addingService(reference);
                if (svc instanceof String) {
                    primeRepository(context, (String) svc);
                }
                return svc;
            }
        };
        serviceTracker.open();
    }

    public void primeRepository(BundleContext context, String xml) {
        synchronized (this) {
            if (servicePrimedRegistration != null)
                // not priming again, already primed
                return;
        }

        System.err.println("*********** priming from: " + xml);
        ServiceTracker st = new ServiceTracker(context, Repository.class.getName(), null);
        st.open();

        try {
            Repository rep = (Repository) st.waitForService(10000);
            if (rep == null)
                throw new IllegalStateException("Unable to find service: " + RepositoryStorage.class);
            XPersistentRepository xpr = (XPersistentRepository) rep;
            RepositoryStorage rs = xpr.getRepositoryStorage();
            System.err.println("*********** obtained Repository Storage: " + rs);

            RepositoryReader reader = RepositoryXMLReader.create(new ByteArrayInputStream(xml.getBytes()));
            XResource resource = reader.nextResource();
            while (resource != null) {
                rs.addResource(resource);
                resource = reader.nextResource();
            }

            Hashtable<String, Object> props = new Hashtable<String, Object>();
            props.put("repository-populated", TEST_CLASS_NAME);
            synchronized (this) {
                servicePrimedRegistration = context.registerService(Object.class.getName(), new Object(), props);
            }

            System.err.println("*********** finished priming repository storage: " + rs);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new IllegalStateException(ex);
        } finally {
            st.close();
        }
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        serviceTracker.close();

        if (servicePrimedRegistration != null)
            servicePrimedRegistration.unregister();
    }
}
