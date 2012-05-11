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

import org.jboss.logging.Logger;
import org.jboss.osgi.repository.ArtifactProviderPlugin;
import org.jboss.osgi.repository.RepositoryCachePlugin;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a Repository that delegates to Mavan.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class RepositoryImpl implements Repository {

    private static Logger log = Logger.getLogger(RepositoryImpl.class);

    private final ArtifactProviderPlugin provider;
    private final RepositoryCachePlugin cache;

    public RepositoryImpl(ArtifactProviderPlugin provider, RepositoryCachePlugin cache) {
        this.provider = provider;
        this.cache = cache;
    }

    @Override
    public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> reqs) {
        log.infof("find providers for: %s", reqs);
        Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
        for (Requirement req : reqs) {
            
            // First get the matching capabilities from the cache
            Collection<Capability> caps = cache.findProviders(req);
            if (caps.isEmpty()) {
                
                // Next, get matching capabilities from the provider
                caps = provider.findProviders(req);

                // Store the provided cpabilities in the cache
                caps = cache.storeCapabilities(caps);
            }
            log.infof("found matching caps: %s", caps);
            result.put(req, Collections.unmodifiableCollection(caps));
        }
        return Collections.unmodifiableMap(result);
    }
}