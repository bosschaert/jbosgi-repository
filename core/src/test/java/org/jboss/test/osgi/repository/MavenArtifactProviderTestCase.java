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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.repository.ArtifactProviderPlugin;
import org.jboss.osgi.repository.RepositoryCachePlugin;
import org.jboss.osgi.repository.core.FileBasedRepositoryCachePlugin;
import org.jboss.osgi.repository.core.MavenArtifactProvider;
import org.jboss.osgi.repository.core.RepositoryImpl;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;
import org.osgi.service.repository.RepositoryContent;

/**
 * Test the default resolver integration.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenArtifactProviderTestCase {

    private Repository repository;

    @Before
    public void setUp() throws IOException {
        File cacheFile = new File("./target/repository").getCanonicalFile();
        ArtifactProviderPlugin provider = new MavenArtifactProvider();
        RepositoryCachePlugin cache = new FileBasedRepositoryCachePlugin(cacheFile);
        repository = new RepositoryImpl(provider, cache);
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {
        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        Requirement req = XRequirementBuilder.createArtifactRequirement(mavenid);
        Collection<Capability> caps = repository.findProviders(Collections.singleton(req)).get(req);
        assertEquals("One capability", 1, caps.size());
        Capability cap = caps.iterator().next();
        verifyProvider(cap);


    }

    private void verifyProvider(Capability cap) throws Exception {
        XIdentityCapability icap = (XIdentityCapability) cap;
        assertEquals("org.apache.felix.configadmin", icap.getSymbolicName());
        assertEquals(Version.parseVersion("1.2.8"), icap.getVersion());
        assertEquals(IdentityNamespace.TYPE_BUNDLE, icap.getType());

        RepositoryContent content = (RepositoryContent) icap.getResource();
        Manifest manifest = new JarInputStream(content.getContent()).getManifest();
        OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
        assertEquals("org.apache.felix.configadmin", metaData.getBundleSymbolicName());
        assertEquals(Version.parseVersion("1.2.8"), metaData.getBundleVersion());
    }
}