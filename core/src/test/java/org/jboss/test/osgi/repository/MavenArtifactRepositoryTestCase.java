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



import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.XRequirementBuilder;
import org.jboss.osgi.repository.core.MavenArtifactRepository;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.RepositoryContent;

/**
 * Test the {@link MavenArtifactRepository}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenArtifactRepositoryTestCase {

    private XRepository repository;

    @Before
    public void setUp() throws IOException {
        repository = new MavenArtifactRepository();
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {
        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        Requirement req = XRequirementBuilder.create(mavenid).getRequirement();
        Collection<Capability> caps = repository.findProviders(req);
        assertEquals("One capability", 1, caps.size());
        Capability cap = caps.iterator().next();

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