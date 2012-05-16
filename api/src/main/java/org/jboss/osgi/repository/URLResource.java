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

import static org.jboss.osgi.repository.RepositoryMessages.MESSAGES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.osgi.resolver.spi.AbstractResource;
import org.osgi.service.repository.RepositoryContent;

/**
 * A resource based on an URL
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
final class URLResource extends AbstractResource implements RepositoryContent {

    private final String contentPath;
    private final URL contentURL;

    public URLResource(URL baseURL, String contentPath) throws MalformedURLException {
        this.contentPath = contentPath;
        String base = baseURL.toExternalForm();
        if (!(base.endsWith("/") || contentPath.startsWith("/")))
            base += "/";
        this.contentURL = new URL(base + contentPath);
    }

    public URL getContentURL() {
        return contentURL;
    }

    public String getContentPath() {
        return contentPath;
    }

    @Override
    public InputStream getContent() {
        try {
            if (contentURL.getProtocol().equals("file")) {
                return new FileInputStream(new File(contentURL.getPath()));
            } else {
                return contentURL.openStream();
            }
        } catch (IOException ex) {
            throw MESSAGES.storageCannotObtainInputStream(ex, this);
        }
    }
}
