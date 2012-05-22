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

import java.net.URL;

import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryXMLReader;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Integration with the OSGi TCK.
 *
 * @author David Bosschaert
 */
public class Activator implements BundleActivator {
    private BundleTracker bundleTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        System.err.println("********* in the integration bundle");

        bundleTracker = new BundleTracker(context, Bundle.ACTIVE, null) {
            @Override
            public Object addingBundle(Bundle bundle, BundleEvent event) {
                if (bundle.getSymbolicName().equals("org.osgi.test.cases.repository")) {
                    primeRepository(context, bundle);
                }
                return super.addingBundle(bundle, event);
            }
        };
        bundleTracker.open();
        /*
        bundleListener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                Bundle b = event.getBundle();
                if ("org.osgi.test.cases.repository".equals(event.getBundle().getSymbolicName())) {
                    if (event.getType() == BundleEvent.STARTED) {
                        primeRepository(context, b);
                    }
                }
            }
        };
        context.addBundleListener(bundleListener);

        // The bundle could have been activated before...
        Bundle b = findBundle(context, "org.osgi.test.cases.repository");
        if (b.getState() == Bundle.ACTIVE)
         */
    }

    public void primeRepository(BundleContext context, Bundle bundle) {
        System.err.println("*********** priming from: " + bundle);
        ServiceTracker st = new ServiceTracker(context, RepositoryStorage.class.getName(), null);
        st.open();

        try {
            RepositoryStorage rs = (RepositoryStorage) st.waitForService(10000);
            if (rs == null)
                throw new IllegalStateException("Unable to find service: " + RepositoryStorage.class);

            URL contentURL = bundle.getResource("xml/content1.xml");
            RepositoryReader reader = RepositoryXMLReader.create(contentURL.openStream());
            XResource resource = reader.nextResource();
            while (resource != null) {
                rs.addResource(resource);
                resource = reader.nextResource();
            }
            System.err.println("*********** finished priming repository storage: " + rs);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new IllegalStateException(ex);
        } finally {
            st.close();
        }
    }



    private Bundle findBundle(BundleContext ctx, String name) {
        for (Bundle b : ctx.getBundles()) {
            if (name.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }



    @Override
    public void stop(BundleContext context) throws Exception {
        bundleTracker.close();
    }
}
