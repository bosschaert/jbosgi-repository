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
package org.jboss.osgi.repository.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.XCachingRepository;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A {@link XRepository} that delegates to {@link RepositoryStorage}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class DefaultStorageRepository extends AbstractRepository implements XCachingRepository {

    private final RepositoryStorage storage;
    private final XRepository delegate;

    public DefaultStorageRepository(RepositoryStorage storage, XRepository delegate) {
        this.storage = storage;
        this.delegate = delegate;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        Collection<Capability> providers = storage.findProviders(req);
        if (providers.isEmpty()) {
            providers = delegate.findProviders(req);
            if (providers.isEmpty() == false) {
                List<Capability> caplist = new ArrayList<Capability>(providers);
                for (int i =0; i < caplist.size(); i++) {
                    XCapability orgcap = (XCapability) caplist.get(i);
                    XResource orgres = (XResource) orgcap.getResource();
                    XResource newres = storage.addResource(orgres);
                    if (newres != orgres) {
                        String namespace = orgcap.getNamespace();
                        Object orgval = orgcap.getAttributes().get(namespace);
                        for (Capability newcap : newres.getCapabilities(namespace)) {
                            Object newval = newcap.getAttributes().get(namespace);
                            if (orgval.equals(newval)) {
                                caplist.set(i, newcap);
                            }
                        }
                    }
                }
                providers = caplist;
            }
        }
        return providers;
    }

    @Override
    public RepositoryStorage getRepositoryStorage() {
        return storage;
    }

    @Override
    public XRepository getRepositoryDelegate() {
        return delegate;
    }

}