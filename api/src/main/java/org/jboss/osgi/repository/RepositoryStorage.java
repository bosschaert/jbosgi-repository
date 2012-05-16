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

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * Handles capability caching
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public interface RepositoryStorage {

    /**
     * Find the capabilities that match the specified requirement.
     *
     * @param requirement The requirements for which matching capabilities should be returned. Must not be {@code null}.
     * @return A collection of matching capabilities for the specified requirements.
     *         If there are no matching capabilities an empty collection is returned.
     *         The returned collection is the property of the caller and can be modified by the caller.
     */
    Collection<Capability> findProviders(Requirement requirement);

    /**
     * Return an iterator over the resource collection known to this storage
     */
    Iterator<XResource> getResources();

    /**
     * Add the given resource to storage
     *
     * @param resource The resource to add
     * @return The resource being added, which may be a modified copy of the give resource
     * @throws RepositoryStorageException If there is a problem storing the resource
     */
    XResource addResource(XResource resource) throws RepositoryStorageException;

    /**
     * Add a resource from the given input stream
     *
     * @param mime An IANA defined MIME type for the format
     * @param input The bytes for the resource
     * @return The resource being added, which may be a modified copy of the give resource
     * @throws RepositoryStorageException If there is a problem storing the resource
     */
    XResource addResource(String mime, InputStream input) throws RepositoryStorageException;

    /**
     * Remove a the given resource from the cache.
     *
     * @param resource The resource to remove
     * @return true if the resource could be found and removed
     * @throws RepositoryStorageException If there is a problem removing the resource from storage
     */
    boolean removeResource(XResource resource) throws RepositoryStorageException;
}
