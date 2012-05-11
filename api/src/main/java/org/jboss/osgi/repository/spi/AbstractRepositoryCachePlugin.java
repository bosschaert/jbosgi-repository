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

import org.jboss.osgi.repository.RepositoryCachePlugin;
import org.jboss.osgi.repository.RepositoryStorageException;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

import java.util.Collection;
import java.util.Collections;

/**
 * An abstract  {@link RepositoryCachePlugin} that does nothing
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class AbstractRepositoryCachePlugin implements RepositoryCachePlugin {

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Capability> storeCapabilities(Collection<Capability> caps) throws RepositoryStorageException {
        return caps;
    }
}