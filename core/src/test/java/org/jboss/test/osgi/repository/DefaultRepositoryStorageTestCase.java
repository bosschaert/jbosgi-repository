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
package org.jboss.test.osgi.repository;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.spi.DefaultRepositoryStorage;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.osgi.resolver.XResource;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.resource.Capability;

/**
 * Test the {@link DefaultRepositoryStorage}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class DefaultRepositoryStorageTestCase extends AbstractRepositoryTest {

    private RepositoryStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new DefaultRepositoryStorage();
        List<XResource> resources = getResources("xml/sample-repository.xml");
        storage.addResource(resources.get(0));
    }

    @Test
    public void testRequireBundle() throws Exception {

        Iterator<XResource> it = storage.getResources();
        XResource resource = it.next();
        Assert.assertFalse(it.hasNext());

        XRequirement req = XRequirementBuilder.createRequirement(BundleNamespace.BUNDLE_NAMESPACE, "org.acme.pool");

        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull(providers);
        Assert.assertEquals(1, providers.size());

        XCapability cap = (XCapability) providers.iterator().next();
        Assert.assertNotNull(cap);
        Assert.assertSame(resource, cap.getResource());
    }
}