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

import java.util.Map;

import org.jboss.osgi.repository.RepositoryProcessor;
import org.jboss.osgi.resolver.XResource;

/**
 * A {@link RepositoryProcessor} that does nothing.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class AbstractRepositoryProcessor implements RepositoryProcessor {

    @Override
    public boolean addRepository(String namespace, Map<String, String> attributes) {
        return true;
    }

    @Override
    public boolean addResource(XResource resource) {
        return true;
    }
}