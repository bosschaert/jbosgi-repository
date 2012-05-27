/*
 * #%L
 * JBossOSGi Repository: Integration Tests
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

import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryXMLReader;
import org.jboss.osgi.repository.XPersistentRepository;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.repository.tb1.pkg1.TestInterface;
import org.jboss.test.osgi.repository.tb1.pkg2.TestInterface2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.repository.Repository;

/**
 * Tests based on the OSGi Repository TCK
 *
 * @author David Bosschaert
 */
@RunWith(Arquillian.class)
public class RepositoryTCKBasedTestCase {

    @Inject
    public BundleContext context;

    @Deployment
    public static JavaArchive createDeployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tb1");
        URL xmlURL = RepositoryTCKBasedTestCase.class.getResource("/xml/test-repository1.xml");
        System.out.println("~~~ " + xmlURL);
        archive.addAsResource(xmlURL, "/xml/test-repository1.xml");
        archive.setManifest(new Asset () {
            @Override
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName("org.jboss.test.osgi.repository.tb1");
                builder.addBundleManifestVersion(2);
                builder.addBundleVersion("1.0");
                builder.addExportPackages(TestInterface.class, TestInterface2.class);
                builder.addImportPackages(XPersistentRepository.class);
                return builder.openStream();
            }
        });
        return archive;
    }

    @Before
    public void setUp() throws Exception {
        XPersistentRepository xpr = (XPersistentRepository) getRepository();
        RepositoryStorage rs = xpr.getRepositoryStorage();

        URL xmlURL = getClass().getResource("/xml/test-repository1.xml");
        System.out.println("^^^ " + xmlURL);
        RepositoryReader reader = RepositoryXMLReader.create(xmlURL.openStream());

        XResource resource = reader.nextResource();
        while (resource != null) {
            rs.addResource(resource);
            resource = reader.nextResource();
        }

    }

    @Test
    public void testFoo() {
        for (Bundle b : context.getBundles()) {
            System.out.println("$$$$ " + b.getSymbolicName());

        }
        System.out.println("*** " + getRepository());
    }

    private Repository getRepository() {
        ServiceReference sref = context.getServiceReference(Repository.class.getName());
        return sref != null ? (Repository) context.getService(sref) : null;
    }
}
