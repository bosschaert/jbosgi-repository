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


/**
 * An extension of the {@link XRepository} that provides capability caching.
 *
 * Implementations are expected to first search the cache for matching capabilities.
 * If there is no capability in the cache the search continues in the repository delegate.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public interface XCachingRepository extends XRepository {

    /**
     * Get the associated repository storage
     */
    RepositoryStorage getRepositoryStorage();

    /**
     * Get the associated repository delegate
     */
    XRepository getRepositoryDelegate();
}
