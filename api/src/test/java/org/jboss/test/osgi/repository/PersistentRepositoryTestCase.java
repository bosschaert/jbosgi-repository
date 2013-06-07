package org.jboss.test.osgi.repository;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import junit.framework.Assert;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.repository.MavenResourceHandler;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryStorageFactory;
import org.jboss.osgi.repository.XPersistentRepository;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.impl.ExpressionCombinerImpl;
import org.jboss.osgi.repository.impl.RequirementBuilderImpl;
import org.jboss.osgi.repository.spi.AbstractPersistentRepository;
import org.jboss.osgi.repository.spi.FileBasedRepositoryStorage;
import org.jboss.osgi.repository.spi.MavenDelegateRepository;
import org.jboss.osgi.repository.spi.MavenDelegateRepository.ConfigurationPropertyProvider;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.repository.ContentNamespace;
import org.osgi.service.repository.ExpressionCombiner;
import org.osgi.service.repository.RepositoryContent;
import org.osgi.service.repository.RequirementBuilder;
import org.osgi.service.repository.RequirementExpression;

/**
 * Test the {@link AbstractPersistentRepository}
 *
 * @author thomas.diesler@jboss.com
 * @author David Bosschaert
 * @since 16-Jan-2012
 */
public class PersistentRepositoryTestCase extends AbstractRepositoryTest {

    private XPersistentRepository repository;
    private File storageDir;

    @Before
    public void setUp() throws IOException {
        storageDir = new File("./target/repository");
        deleteRecursive(storageDir);
        RepositoryStorageFactory storageFactory = new RepositoryStorageFactory() {
            public RepositoryStorage create(XRepository repository) {
                return new FileBasedRepositoryStorage(repository, storageDir, Mockito.mock(ConfigurationPropertyProvider.class));
            }
        };
        repository = new AbstractPersistentRepository(storageFactory, new MavenDelegateRepository());
    }

    @Test
    public void testFindProvidersByMavenId() throws Exception {

        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        XRequirement req = XRequirementBuilder.create(mavenid).getRequirement();
        Collection<Capability> caps = repository.findProviders(req);
        assertEquals("One capability", 1, caps.size());
        XCapability cap = (XCapability) caps.iterator().next();

        Assert.assertTrue("Capability matches", req.matches(cap));

        // Add the maven resource to the repository
        RepositoryStorage storage = repository.getRepositoryStorage();
        MavenResourceHandler handler = new MavenResourceHandler();
        XResource res = handler.toBundleResource(cap.getResource());
        res = storage.addResource(res);
        cap = res.getIdentityCapability();

        verifyCapability(cap);

        // Find the same requirement again
        caps = repository.findProviders(req);
        assertEquals("One capability", 1, caps.size());
        cap = (XCapability) caps.iterator().next();

        // Check that we have a resource in storage
        res = handler.toBundleResource(cap.getResource());
        res = storage.getResource(res.getIdentityCapability());
        cap = res.getIdentityCapability();

        verifyCapability(cap);
    }

    private void verifyCapability(XCapability cap) throws IOException, MalformedURLException, BundleException {

        XResource resource = cap.getResource();
        XIdentityCapability icap = resource.getIdentityCapability();
        assertEquals("org.apache.felix.configadmin", icap.getName());
        assertEquals(Version.parseVersion("1.2.8"), icap.getVersion());
        assertEquals(IdentityNamespace.TYPE_BUNDLE, icap.getType());

        Collection<Capability> caps = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        assertEquals("One capability", 1, caps.size());
        cap = (XCapability) caps.iterator().next();
        URL url = new URL((String) cap.getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE));

        String absolutePath = storageDir.getAbsolutePath();
        // Convert the absolute path so that it works on Windows too
        absolutePath = absolutePath.replace('\\', '/');
        if (!absolutePath.startsWith("/"))
            absolutePath = "/" + absolutePath;

        Assert.assertTrue("Local path: " + url, url.getPath().startsWith(absolutePath));

        RepositoryContent content = (RepositoryContent) resource;
        Manifest manifest = new JarInputStream(content.getContent()).getManifest();
        OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
        assertEquals("org.apache.felix.configadmin", metaData.getBundleSymbolicName());
        assertEquals(Version.parseVersion("1.2.8"), metaData.getBundleVersion());
    }

    @Test
    public void testGetRequirementBuilder() {
        RequirementBuilder builder = repository.newRequirementBuilder("toastie");
        Assert.assertTrue(builder instanceof RequirementBuilderImpl);
        Requirement req = builder.build();
        Assert.assertEquals("toastie", req.getNamespace());
    }

    @Test
    public void testGetExpressionCombiner() {
        Assert.assertTrue(repository.getExpressionCombiner() instanceof ExpressionCombiner);
        Assert.assertTrue(repository.getExpressionCombiner() instanceof ExpressionCombinerImpl);
    }

    @Test
    public void testFindSimpleRequirementExpression() throws Exception {
        MavenCoordinates mavenid = MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.2.8");
        XRequirement req = XRequirementBuilder.create(mavenid).getRequirement();

        RequirementExpression re = repository.getExpressionCombiner().expression(req);
        Collection<Resource> resources = repository.findProviders(re);
        Assert.assertEquals(1, resources.size());
        XResource res = (XResource) resources.iterator().next();
        XIdentityCapability icap = res.getIdentityCapability();
        assertEquals("org.apache.felix.configadmin", icap.getName());
        assertEquals(Version.parseVersion("1.2.8"), icap.getVersion());
    }

    @Test
    public void testFindOrRequirementExpression() throws Exception {
        XRequirement req1 = XRequirementBuilder.create(MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.2.8")).getRequirement();
        XRequirement req2 = XRequirementBuilder.create(MavenCoordinates.parse("org.apache.felix:org.apache.felix.configadmin:1.4.0")).getRequirement();
        RequirementExpression re = repository.getExpressionCombiner().or(req1, req2);
        Collection<Resource> resources = repository.findProviders(re);
        Assert.assertEquals(2, resources.size());

        for (Resource res : resources) {
            XResource xres = (XResource) res;

            XIdentityCapability icap = xres.getIdentityCapability();
            assertEquals("org.apache.felix.configadmin", icap.getName());
            assertTrue(Version.parseVersion("1.2.8").equals(icap.getVersion()) ||
                       Version.parseVersion("1.4.0").equals(icap.getVersion()));
        }
    }

    @Test
    public void testFindAndRequirementExpression() throws Exception {
        RepositoryStorage storage = repository.getRepositoryStorage();

        XResourceBuilder<XResource> rbf1 = getXResourceBuilder();
        Map<String, Object> atts1 = new HashMap<String, Object>();
        atts1.put("A", "1");
        atts1.put("B", "2");
        rbf1.addCapability("foo", atts1, null);
        rbf1.addIdentityCapability("foo", Version.parseVersion("1"));
        XResource res1 = rbf1.getResource();
        storage.addResource(res1);

        XResourceBuilder<XResource> rbf2 = getXResourceBuilder();
        Map<String, Object> atts2 = new HashMap<String, Object>();
        atts2.put("A", "1");
        atts2.put("B", "3");
        rbf2.addCapability("foo", atts2, null);
        rbf2.addIdentityCapability("foo", Version.parseVersion("1.1"));
        XResource res2 = rbf2.getResource();
        storage.addResource(res2);

        ExpressionCombiner ec = repository.getExpressionCombiner();
        Requirement req1 = repository.newRequirementBuilder("foo").addDirective("filter", "(A=1)").build();
        Requirement req2 = repository.newRequirementBuilder("foo").addDirective("filter", "(B=3)").build();

        Collection<Resource> providers = repository.findProviders(ec.and(req1, req2));
        assertEquals(1, providers.size());

        Resource res = providers.iterator().next();
        XResource xres = (XResource) res;
        XIdentityCapability icap = xres.getIdentityCapability();
        assertEquals("foo", icap.getName());
        assertEquals(Version.parseVersion("1.1"), icap.getVersion());
    }

    @Test
    public void testFindAndNotRequirementExpression() throws Exception {
        RepositoryStorage storage = repository.getRepositoryStorage();

        XResourceBuilder<XResource> rbf1 = getXResourceBuilder();
        Map<String, Object> atts1 = new HashMap<String, Object>();
        atts1.put("A", "1");
        atts1.put("B", "2");
        rbf1.addCapability("foo", atts1, null);
        rbf1.addIdentityCapability("foo", Version.parseVersion("1"));
        XResource res1 = rbf1.getResource();
        storage.addResource(res1);

        XResourceBuilder<XResource> rbf2 = getXResourceBuilder();
        Map<String, Object> atts2 = new HashMap<String, Object>();
        atts2.put("A", "1");
        atts2.put("B", "3");
        rbf2.addCapability("foo", atts2, null);
        rbf2.addIdentityCapability("foo", Version.parseVersion("1.1"));
        XResource res2 = rbf2.getResource();
        storage.addResource(res2);

        ExpressionCombiner ec = repository.getExpressionCombiner();
        Requirement req1 = repository.newRequirementBuilder("foo").addDirective("filter", "(A=1)").build();
        Requirement req2 = repository.newRequirementBuilder("foo").addDirective("filter", "(B=3)").build();

        Collection<Resource> providers = repository.findProviders(ec.and(ec.expression(req1), ec.not(req2)));
        assertEquals(1, providers.size());

        Resource res = providers.iterator().next();
        XResource xres = (XResource) res;
        XIdentityCapability icap = xres.getIdentityCapability();
        assertEquals("foo", icap.getName());
        assertEquals(Version.parseVersion("1.0"), icap.getVersion());
    }

    private XResourceBuilder<XResource> getXResourceBuilder() {
        XResourceBuilder<XResource> rbf = XResourceBuilderFactory.create();

        Map<String, Object> catts = new HashMap<String, Object>();
        catts.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, getURL("/content/sample1.txt"));
        rbf.addCapability(ContentNamespace.CONTENT_NAMESPACE, catts, null);

        return rbf;
    }

    private String getURL(String fname) {
        URL url = getClass().getResource(fname);
        if (url == null)
            return "";

        return url.toExternalForm();
    }
}