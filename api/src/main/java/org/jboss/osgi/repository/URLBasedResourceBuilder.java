/*
 * #%L
 * JBossOSGi Repository: API
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

package org.jboss.osgi.repository;

import static org.jboss.osgi.resolver.XResourceConstants.CONTENT_PATH;
import static org.jboss.osgi.resolver.XResourceConstants.CONTENT_URL;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.spi.AbstractResource;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.osgi.framework.Version;


/**
 * Create an URL based resource
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class URLBasedResourceBuilder {

    public static XResource createResource(URL baseURL, String contentPath) {
        final URLBasedResource urlres = new URLBasedResource(baseURL, contentPath);
        XResourceBuilderFactory factory = new XResourceBuilderFactory() {
            
            @Override
            public AbstractResourceBuilder createResourceBuilder() {
                AbstractResourceBuilder builder = new AbstractResourceBuilder(this) {
                    @Override
                    public XIdentityCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs) {
                        atts.put(CONTENT_URL, urlres.getContentURL());
                        atts.put(CONTENT_PATH, urlres.getContentPath());
                        return super.addIdentityCapability(symbolicName, version, type, atts, dirs);
                    }
                };
                return builder;
            }
            
            @Override
            public AbstractResource createResource() {
                return urlres;
            }
        };
        
        XResourceBuilder builder = XResourceBuilderFactory.create(factory);
        InputStream content = urlres.getContent();
        try {
            Manifest manifest = new JarInputStream(content).getManifest();
            OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
            builder.loadFrom(metaData);
        } catch (Exception ex) {
            URL contentURL = urlres.getContentURL();
            throw new IllegalStateException("Cannot create capability from: " + contentURL, ex);
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        XResource resource = builder.getResource();
        return resource;
    }
}
