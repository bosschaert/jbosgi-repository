package org.jboss.osgi.repository.spi;
/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import static org.jboss.osgi.repository.RepositoryLogger.LOGGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.osgi.repository.XRepository;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.repository.ExpressionCombiner;
import org.osgi.service.repository.RequirementBuilder;
import org.osgi.service.repository.RequirementExpression;

/**
 * A {@link XRepository} that aggregates other repositories.
 *
 * @author thomas.diesler@jboss.com
 * @author David Bosschaert
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

    @Override
    public Collection<Resource> findProviders(RequirementExpression re) {
        Collection<Resource> providers = new ArrayList<Resource>();
        LOGGER.debugf("Find providers for: %s", re);
        synchronized (repositories) {
            for (XRepository repo : repositories) {
                Collection<Resource> resources = repo.findProviders(re);
                LOGGER.debugf("Found providers in %s: %s", repo, resources);
                providers.addAll(resources);
            }
        }
        return providers;
    }

    @Override
    public ExpressionCombiner getExpressionCombiner() {
        synchronized (repositories) {
            for (XRepository repo : repositories) {
                ExpressionCombiner ec = repo.getExpressionCombiner();
                if (ec != null)
                    return ec;
            }
        }
        return null;
    }

    @Override
    public RequirementBuilder newRequirementBuilder(String namespace) {
        synchronized (repositories) {
            for (XRepository repo : repositories) {
                RequirementBuilder rb = repo.newRequirementBuilder(namespace);
                if (rb != null)
                    return rb;
            }
        }
        return null;
    }
}