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

import static org.jboss.osgi.repository.RepositoryLogger.LOGGER;
import static org.jboss.osgi.repository.RepositoryMessages.MESSAGES;
import static org.jboss.osgi.resolver.spi.AbstractRequirement.namespaceValueFromFilter;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.osgi.repository.RepositoryMessages;
import org.jboss.osgi.repository.RepositoryReader;
import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryStorageException;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A {@link RepositoryStorage} that maintains its state in local memory
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MemoryRepositoryStorage implements RepositoryStorage {

    private final XRepository repository;
    private final AtomicLong increment = new AtomicLong();
    private final Map<CacheKey, XResource> resourceCache = new HashMap<CacheKey, XResource>();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new HashMap<CacheKey, Set<Capability>>();

    public MemoryRepositoryStorage(XRepository repository) {
        if (repository == null)
            throw MESSAGES.illegalArgumentNull("repository");
        this.repository = repository;
    }

    protected AtomicLong getAtomicIncrement() {
        return increment;
    }

    @Override
    public XRepository getRepository() {
        return repository;
    }

    @Override
    public RepositoryReader getRepositoryReader() {
        synchronized (capabilityCache) {
            return new RepositoryReader() {
                private final Iterator<XResource> iterator;
                {
                    synchronized (capabilityCache) {
                        iterator = new HashSet<XResource>(resourceCache.values()).iterator();
                    }
                }

                @Override
                public Map<String, String> getRepositoryAttributes() {
                    HashMap<String, String> attributes = new HashMap<String, String>();
                    attributes.put("name", getRepository().getName());
                    attributes.put("increment", new Long(increment.get()).toString());
                    return Collections.unmodifiableMap(attributes);
                }

                @Override
                public XResource nextResource() {
                    return iterator.hasNext() ? iterator.next() : null;
                }

                @Override
                public void close() {
                    // do nothing
                }
            };
        }
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        Set<Capability> result = findCachedProviders(CacheKey.create(req), false);
        LOGGER.tracef("Find cached providers: %s => %s", req, result);
        return result;
    }

    @Override
    public XResource addResource(XResource res) throws RepositoryStorageException {
        if (res == null)
            throw MESSAGES.illegalArgumentNull("resource");

        XIdentityCapability icap = res.getIdentityCapability();
        CacheKey ikey = CacheKey.create(icap);
        synchronized (capabilityCache) {
            if (capabilityCache.get(ikey) != null)
                throw MESSAGES.illegalStateResourceAlreadyExists(res);

            resourceCache.put(ikey, res);
            for (Capability cap : res.getCapabilities(null)) {
                CacheKey cachekey = CacheKey.create(cap);
                Set<Capability> capset = findCachedProviders(cachekey, true);
                capset.add(cap);
            }
            increment.incrementAndGet();
            LOGGER.infoResourceAdded(res);
        }
        return res;
    }

    @Override
    public XResource addResource(String mime, InputStream input) throws RepositoryStorageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeResource(XResource res) throws RepositoryStorageException {
        if (res == null)
            throw RepositoryMessages.MESSAGES.illegalArgumentNull("resource");

        XResource result = null;
        XIdentityCapability icap = res.getIdentityCapability();
        CacheKey ikey = CacheKey.create(icap);
        synchronized (capabilityCache) {
            result = resourceCache.remove(ikey);
            if (result != null) {
                for (Capability cap : result.getCapabilities(null)) {
                    CacheKey cachekey = CacheKey.create(cap);
                    Set<Capability> capset = findCachedProviders(cachekey, true);
                    capset.remove(cap);
                }
                LOGGER.infoResourceRemoved(res);
            }
        }
        return result != null;
    }

    private Set<Capability> findCachedProviders(CacheKey cachekey, boolean create) {
        synchronized (capabilityCache) {
            Set<Capability> result = capabilityCache.get(cachekey);
            if (result == null) {
                result = new HashSet<Capability>();
                if (create) {
                    capabilityCache.put(cachekey, result);
                }
            }
            return result;
        }
    }

    /**
     * An key to cached capabilities
     */
    static class CacheKey {

        private final String key;

        /**
         * Create a cache key from the given capability
         */
        static CacheKey create(Capability cap) {
            String namespace = cap.getNamespace();
            return new CacheKey(namespace, (String) cap.getAttributes().get(namespace));
        }

        /**
         * Create a cache key from the given requirement
         */
        static CacheKey create(Requirement req) {
            String namespace = req.getNamespace();
            String value = (String) req.getAttributes().get(namespace);
            if (value == null) {
                Filter filter = getRequiredFilter(req);
                value = namespaceValueFromFilter(filter, namespace);
            }
            return new CacheKey(namespace, value);
        }

        private static Filter getRequiredFilter(Requirement req) {
            String filterdir = req.getDirectives().get(AbstractWiringNamespace.REQUIREMENT_FILTER_DIRECTIVE);
            try {
                return FrameworkUtil.createFilter("" + filterdir);
            } catch (InvalidSyntaxException e) {
                throw MESSAGES.illegalArgumentInvalidFilterDirective(filterdir);
            }
        }

        private CacheKey(String namespace, String value) {
            if (namespace == null)
                throw MESSAGES.illegalArgumentNull("namespace");
            if (value == null)
                throw MESSAGES.illegalArgumentNull("value");
            key = namespace + ":" + value;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            return key.equals(other.key);
        }

        @Override
        public String toString() {
            return "[" + key + "]";
        }
    }
}