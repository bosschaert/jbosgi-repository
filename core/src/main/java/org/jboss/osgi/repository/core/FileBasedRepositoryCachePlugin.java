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

package org.jboss.osgi.repository.core;

import static org.jboss.osgi.resolver.XResourceConstants.CONTENT_PATH;
import static org.jboss.osgi.resolver.XResourceConstants.MAVEN_IDENTITY_NAMESPACE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.osgi.repository.RepositoryResolutionException;
import org.jboss.osgi.repository.RepositoryStorageException;
import org.jboss.osgi.repository.URLBasedResourceBuilder;
import org.jboss.osgi.repository.spi.AbstractRepositoryCachePlugin;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.RepositoryContent;


/**
 * A simple {@link org.jboss.osgi.repository.RepositoryCachePlugin} that uses
 * the local file system.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class FileBasedRepositoryCachePlugin extends AbstractRepositoryCachePlugin {

    private final File repository;

    public FileBasedRepositoryCachePlugin(File repository) {
        this.repository = repository;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {

        String namespace = req.getNamespace();
        if (MAVEN_IDENTITY_NAMESPACE.equals(namespace) == false)
            return Collections.emptySet();

        String mavenId = (String) req.getAttributes().get(MAVEN_IDENTITY_NAMESPACE);
        MavenCoordinates coordinates = MavenCoordinates.parse(mavenId);
        try {
            List<Capability> result = new ArrayList<Capability>();
            URL baseURL = repository.toURI().toURL();
            URL url = coordinates.toArtifactURL(baseURL);
            if (new File(url.getPath()).exists()) {
                String contentPath = url.toExternalForm();
                contentPath = contentPath.substring(baseURL.toExternalForm().length());
                XResource resource = URLBasedResourceBuilder.createResource(baseURL, contentPath);
                result.add(resource.getIdentityCapability());
            }
            return Collections.unmodifiableList(result);
        } catch (Exception ex) {
            throw new RepositoryResolutionException(ex);
        }
    }

    @Override
    public Collection<Capability> storeCapabilities(Collection<Capability> caps) throws RepositoryStorageException {
        List<Capability> result = new ArrayList<Capability>(caps.size());
        for (Capability cap : caps) {
            XIdentityCapability icap = (XIdentityCapability) cap;
            String contentPath = (String) icap.getAttribute(CONTENT_PATH);
            if (contentPath != null) {
                File targetFile = new File(repository.getAbsolutePath() + File.separator + contentPath);
                try {
                    RepositoryContent content = (RepositoryContent) cap.getResource();
                    copyResourceContent(content, targetFile);
                    XIdentityCapability newid = recreateIdentity(targetFile);
                    result.add(newid);
                } catch (IOException ex) {
                    new RepositoryStorageException(ex);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private XIdentityCapability recreateIdentity(File contentFile) throws IOException {
        URL baseURL = repository.toURI().toURL();
        String contentPath = contentFile.getPath().substring(repository.getAbsolutePath().length() + 1);
        XResource resource = URLBasedResourceBuilder.createResource(baseURL, contentPath);
        return resource.getIdentityCapability();
    }

    private void copyResourceContent(RepositoryContent content, File targetFile) throws IOException {
        int len = 0;
        byte[] buf = new byte[4096];
        targetFile.getParentFile().mkdirs();
        InputStream in = content.getContent();
        OutputStream out = new FileOutputStream(targetFile);
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
