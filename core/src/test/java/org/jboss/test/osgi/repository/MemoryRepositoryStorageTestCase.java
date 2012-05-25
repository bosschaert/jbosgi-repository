/*
 * #%L
 * JBossOSGi Repository: Core
 * %%
 * Copyright (C) 2011 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package org.jboss.test.osgi.repository;

import java.util.Collection;

import junit.framework.Assert;

import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.repository.spi.MemoryRepositoryStorage;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.resource.Capability;

/**
 * Test the {@link MemoryRepositoryStorage}
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MemoryRepositoryStorageTestCase extends AbstractRepositoryTest {

    private RepositoryStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = new MemoryRepositoryStorage(Mockito.mock(XRepository.class));
        RepositoryReader reader = getRepositoryReader("xml/sample-repository.xml");
        storage.addResource(reader.nextResource());
    }

    @Test
    public void testRequireBundle() throws Exception {

        RepositoryReader reader = storage.getRepositoryReader();
        XResource resource = reader.nextResource();
        Assert.assertNotNull("Resource not null", resource);
        Assert.assertNull("One resource only", reader.nextResource());

        XRequirementBuilder builder = XRequirementBuilder.create(BundleNamespace.BUNDLE_NAMESPACE, "org.acme.pool");
        XRequirement req = builder.getRequirement();

        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull("Providers not null", providers);
        Assert.assertEquals("One provider", 1, providers.size());

        XCapability cap = (XCapability) providers.iterator().next();
        Assert.assertNotNull("Capability not null", cap);
        Assert.assertSame(resource, cap.getResource());
    }

    @Test
    public void testRequireBundleWithFilter() throws Exception {

        XRequirementBuilder builder = XRequirementBuilder.create(BundleNamespace.BUNDLE_NAMESPACE);
        builder.getDirectives().put(BundleNamespace.REQUIREMENT_FILTER_DIRECTIVE, "(osgi.wiring.bundle=org.acme.pool)");
        XRequirement req = builder.getRequirement();

        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull("Providers not null", providers);
        Assert.assertEquals("One provider", 1, providers.size());
    }
}