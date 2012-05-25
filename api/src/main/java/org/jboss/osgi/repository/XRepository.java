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

import java.util.Collection;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

/**
 * An extension of the {@link Repository} interface
 * 
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public interface XRepository extends Repository {

    /**
     * The service names that repositories are registered under
     */
    String[] SERVICE_NAMES = new String[] { XRepository.class.getName(), Repository.class.getName() };

    /**
     * Artifact coordinates may be given in simple groupId:artifactId:version form,
     * or they may be fully qualified in the form groupId:artifactId:type:version[:classifier]
     */
    String MAVEN_IDENTITY_NAMESPACE = "maven.identity";

    /**
     * Artifact coordinates may be given by {@link org.jboss.modules.ModuleIdentifier}
     */
    String MODULE_IDENTITY_NAMESPACE = "module.identity";

    /**
     * Get the name for this repository
     */
    String getName();

    /**
     * Find the capabilities that match the specified requirement.
     * 
     * @param requirement The requirements for which matching capabilities
     *        should be returned. Must not be {@code null}.
     * @return A collection of matching capabilities for the specified requirements.
     *         If there are no matching capabilities an empty collection is returned.
     *         The returned collection is the property of the caller and can be modified by the caller.
     */
    Collection<Capability> findProviders(Requirement requirement);
}
