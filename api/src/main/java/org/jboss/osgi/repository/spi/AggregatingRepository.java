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
package org.jboss.osgi.repository.spi;

import static org.jboss.osgi.repository.RepositoryLogger.LOGGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.osgi.repository.XRepository;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A {@link XRepository} that aggregates other repositories.
 * 
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class AggregatingRepository extends AbstractRepository {

    private final List<XRepository> repositories = new ArrayList<XRepository>();

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        Collection<Capability> providers = new ArrayList<Capability>();
        LOGGER.debugf("Find providers for: %s", req);
        synchronized (repositories) {
            for (XRepository repo : repositories) {
                Collection<Capability> aux = repo.findProviders(req);
                LOGGER.debugf("Found providers in %s: %s", repo, aux);
                providers.addAll(aux);
            }
        }
        return providers;
    }

    public void addRepository(XRepository repo) {
        synchronized (repositories) {
            if (repo != this) {
                LOGGER.debugf("Add repository: %s", repo);
                repositories.add(repo);
            }
        }
    }

    public void removeRepository(XRepository repo) {
        synchronized (repositories) {
            if (repo != this) {
                LOGGER.debugf("Remove repository: %s", repo);
                repositories.remove(repo);
            }
        }
    }
}