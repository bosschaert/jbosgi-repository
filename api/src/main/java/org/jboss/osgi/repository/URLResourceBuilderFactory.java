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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.XResourceConstants;
import org.osgi.service.repository.ContentNamespace;


/**
 * Create an URL based resource
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class URLResourceBuilderFactory extends XResourceBuilderFactory {

    private final URLResource urlres;

    private URLResourceBuilderFactory(URLResource urlres) {
        this.urlres = urlres;
    }

    public static XResourceBuilder create(URL baseURL, String contentPath, Map<String, Object> atts, boolean loadMetadata) throws MalformedURLException {
        final URLResource urlres = new URLResource(baseURL, contentPath);
        URLResourceBuilderFactory factory = new URLResourceBuilderFactory(urlres);

        XResourceBuilder builder = XResourceBuilderFactory.create(factory);
        XCapability ccap = builder.addGenericCapability(ContentNamespace.CONTENT_NAMESPACE, atts, null);
        ccap.getAttributes().put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, urlres.getContentURL());
        ccap.getAttributes().put(XResourceConstants.CAPABILITY_PATH_ATTRIBUTE, urlres.getContentPath());

        if (loadMetadata) {
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
        }
        return builder;
    }

    @Override
    public XResource createResource() {
        return urlres;
    }
}
