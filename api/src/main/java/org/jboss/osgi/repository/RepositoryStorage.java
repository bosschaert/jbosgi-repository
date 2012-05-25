/*
 * #%L
 * JBossOSGi Repository: API
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
package org.jboss.osgi.repository;

import java.io.InputStream;
import java.util.Collection;

import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * Repository resource storage
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public interface RepositoryStorage {

    /**
     * Get the associated reposistory;
     */
    XRepository getRepository();

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
     * Get the repository reader for this storage
     */
    RepositoryReader getRepositoryReader();

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
