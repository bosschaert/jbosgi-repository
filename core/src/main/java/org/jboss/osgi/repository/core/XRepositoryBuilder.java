/*
 * #%L
 * JBossOSGi Repository: Core
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
package org.jboss.osgi.repository.core;

import static org.jboss.osgi.repository.XRepository.SERVICE_NAMES;

import java.io.File;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryStorageFactory;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.spi.AbstractPersistentRepository;
import org.jboss.osgi.repository.spi.AggregatingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A builder for {@link XRepository} services.
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-May-2012
 */
public class XRepositoryBuilder {

    public static final String ROOT_REPOSITORY = "root-repository";

    private final Set<ServiceRegistration> registrations = new HashSet<ServiceRegistration>();
    private final BundleContext context;

    public static XRepositoryBuilder create(BundleContext context) {
        return new XRepositoryBuilder(context);
    }

    private XRepositoryBuilder(BundleContext context) {
        this.context = context;
    }

    public void addDefaultRepositoryStorage(final File storageDir) {
        RepositoryStorageFactory factory = new RepositoryStorageFactory() {
            @Override
            public RepositoryStorage create(XRepository repository) {
                return new FileBasedRepositoryStorage(repository, storageDir);
            }
        };
        addRepositoryStorage(factory);
    }

    public void addRepositoryStorage(RepositoryStorageFactory factory) {
        registrations.add(context.registerService(RepositoryStorageFactory.class.getName(), factory, null));
    }

    public XRepository addDefaultRepositories() {

        // Register the maven artifact repository
        addRepository(new MavenArtifactRepository());

        // Get the {@link RepositoryStorageFactory} service
        ServiceReference sref = context.getServiceReference(RepositoryStorageFactory.class.getName());
        RepositoryStorageFactory factory = (RepositoryStorageFactory) context.getService(sref);

        // Register the root repository
        XRepository repository = new AbstractPersistentRepository(factory, getRepositoryServiceTracker());
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_RANKING, new Integer(1000));
        props.put(Constants.SERVICE_DESCRIPTION, repository.getName());
        props.put(ROOT_REPOSITORY, Boolean.TRUE);
        registrations.add(context.registerService(SERVICE_NAMES, repository, props));

        return repository;
    }

    public void addRepository(XRepository repository) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_DESCRIPTION, repository.getName());
        registrations.add(context.registerService(SERVICE_NAMES, repository, props));
    }

    public XRepository getRepositoryServiceTracker() {
        return new RepositoryServiceTracker(context);
    }

    public Set<ServiceRegistration> getRegistrations() {
        return Collections.unmodifiableSet(registrations);
    }

    public void unregisterServices() {
        for (ServiceRegistration reg : getRegistrations()) {
            reg.unregister();
        }
    }

    static class RepositoryServiceTracker extends AggregatingRepository {

        public RepositoryServiceTracker(final BundleContext context) {
            ServiceTracker tracker = new ServiceTracker(context, XRepository.class.getName(), null) {
                @Override
                public Object addingService(ServiceReference sref) {
                    XRepository repo = (XRepository) super.addingService(sref);
                    if (sref.getProperty(ROOT_REPOSITORY) == null) {
                        addRepository(repo);
                    }
                    return repo;
                }

                @Override
                public void removedService(ServiceReference sref, Object service) {
                    XRepository repo = (XRepository) service;
                    if (sref.getProperty(ROOT_REPOSITORY) == null) {
                        removeRepository(repo);
                    }
                    super.removedService(sref, service);
                }
            };
            tracker.open();
        }
    }
}