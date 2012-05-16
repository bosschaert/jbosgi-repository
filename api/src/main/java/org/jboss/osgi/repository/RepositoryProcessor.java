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

import java.util.Map;

import org.jboss.osgi.resolver.XResource;


/**
 * A callback interface for the {@link RepositoryXMLReader}
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public interface RepositoryProcessor {

    boolean addRepository(String namespace, Map<String, String> attributes);

    boolean addResource(XResource resource);
}