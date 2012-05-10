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
package org.jboss.osgi.repository;

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
final class URLBasedResource extends AbstractResource implements RepositoryContent {

    private final String contentPath;
    private final URL contentURL;

    public URLBasedResource(URL baseURL, String contentPath) {
        this.contentPath = contentPath;
        try {
            String base = baseURL.toExternalForm();
            if (!(base.endsWith("/") || contentPath.startsWith("/")))
                base += "/";
            this.contentURL = new URL(base + contentPath);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
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
            throw new IllegalStateException(ex);
        }
    }
}
