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



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import junit.framework.Assert;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.core.FileBasedRepositoryStorage;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.service.repository.ContentNamespace;

/**
 * Test the default resolver integration.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class FileRepositoryStorageTestCase extends AbstractRepositoryTest {

    private File cacheFile;
    private RepositoryStorage storage;

    @Before
    public void setUp() throws IOException {
        cacheFile = new File("./target/repository");
        storage = new FileBasedRepositoryStorage(cacheFile);
        deleteRecursive(cacheFile);
    }

    @Test
    public void testResourceStorage() throws Exception {

        Assert.assertFalse(storage.getResources().hasNext());

        // Add a bundle resource
        JavaArchive archiveA = getBundleA();
        InputStream input = archiveA.as(ZipExporter.class).exportAsInputStream();
        XResource resource = storage.addResource("application/vnd.osgi.bundle", input);

        XCapability ccap = (XCapability) resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE).get(0);
        Assert.assertEquals("application/vnd.osgi.bundle", ccap.getAttribute(ContentNamespace.CAPABILITY_MIME_ATTRIBUTE));
        Assert.assertNotNull(ccap.getAttribute(ContentNamespace.CAPABILITY_SIZE_ATTRIBUTE));
        Assert.assertNotNull(ccap.getAttribute(ContentNamespace.CONTENT_NAMESPACE));
        URL fileURL = (URL) ccap.getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
        Assert.assertTrue("File exists: " + fileURL, new File(fileURL.getPath()).exists());

        XRequirement req = XRequirementBuilder.createRequirement(PackageNamespace.PACKAGE_NAMESPACE, "org.acme.foo");
        Collection<Capability> providers = storage.findProviders(req);
        Assert.assertNotNull(providers);
        Assert.assertEquals(1, providers.size());

        XPackageCapability cap = (XPackageCapability) providers.iterator().next();
        Assert.assertNotNull(cap);
        Assert.assertEquals("org.acme.foo", cap.getPackageName());
        Assert.assertSame(resource, cap.getResource());

        Assert.assertTrue(storage.removeResource(resource));
        Assert.assertFalse("File removed: " + fileURL, new File(fileURL.getPath()).exists());
    }

    private JavaArchive getBundleA() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.bundleA");
        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archive.getName());
                builder.addExportPackages("org.acme.foo");
                return builder.openStream();
            }
        });
        return archive;
    }
}