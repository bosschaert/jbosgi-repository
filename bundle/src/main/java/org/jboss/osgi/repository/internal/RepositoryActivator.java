/*
 * #%L
 * JBossOSGi Repository: Bundle
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

package org.jboss.osgi.repository.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.core.FileBasedRepositoryStorage;
import org.jboss.osgi.repository.core.MavenArtifactRepository;
import org.jboss.osgi.repository.spi.AbstractCachingRepository;
import org.jboss.osgi.repository.spi.AggregatingRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.repository.Repository;
import org.osgi.util.tracker.ServiceTracker;

/**
 * An activator for the {@link Repository} service.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class RepositoryActivator implements BundleActivator {

    private final String[] SERVICE_NAMES = new String[] { XRepository.class.getName(), Repository.class.getName() };

    private XRepository repository;

    @Override
    public void start(final BundleContext context) throws Exception {

        // Register the repository storage service
        RepositoryStorage storage = new FileBasedRepositoryStorage(context.getDataFile("repository"));
        context.registerService(RepositoryStorage.class.getName(), storage, null);

        // Register the maven artifact repository
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_DESCRIPTION, "JBossOSGi Maven Artifact Repository");
        context.registerService(SERVICE_NAMES, new MavenArtifactRepository(), props);

        ServiceTracker storageTracker = new ServiceTracker(context, RepositoryStorage.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                RepositoryStorage storage = (RepositoryStorage) super.addingService(reference);
                if (repository == null) {
                    Dictionary<String, Object> props = new Hashtable<String, Object>();
                    props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
                    props.put(Constants.SERVICE_DESCRIPTION, "JBossOSGi Aggregating Repository");
                    repository = new AbstractCachingRepository(storage, new RepositoryDelegate(context));
                    context.registerService(SERVICE_NAMES, repository, props);
                }
                return storage;
            }
        };
        storageTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    class RepositoryDelegate extends AggregatingRepository {

        public RepositoryDelegate(final BundleContext context) {
            ServiceTracker repoTracker = new ServiceTracker(context, XRepository.class.getName(), null) {
                @Override
                public Object addingService(ServiceReference reference) {
                    XRepository repo = (XRepository) super.addingService(reference);
                    if (repo != repository) {
                        addRepository(repo);
                    }
                    return repo;
                }

                @Override
                public void removedService(ServiceReference reference, Object service) {
                    XRepository repo = (XRepository) service;
                    if (repo != repository) {
                        removeRepository(repo);
                    }
                    super.removedService(reference, service);
                }
            };
            repoTracker.open();
        }
    }
}