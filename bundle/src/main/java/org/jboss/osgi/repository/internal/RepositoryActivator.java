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

import org.jboss.osgi.repository.ArtifactProviderPlugin;
import org.jboss.osgi.repository.RepositoryCachePlugin;
import org.jboss.osgi.repository.core.FileBasedRepositoryCachePlugin;
import org.jboss.osgi.repository.core.MavenArtifactProvider;
import org.jboss.osgi.repository.core.RepositoryImpl;
import org.jboss.osgi.repository.core.TrackingArtifactProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.repository.Repository;

/**
 * An activator for the {@link Repository} service.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class RepositoryActivator implements BundleActivator {

    @Override
    public void start(final BundleContext context) throws Exception {
        // Register the MavenArtifactProvider
        MavenArtifactProvider simpleProvider = new MavenArtifactProvider();
        context.registerService(ArtifactProviderPlugin.class.getName(), simpleProvider, null);
        // Register the Repository
        ArtifactProviderPlugin provider = new TrackingArtifactProvider(context);
        RepositoryCachePlugin cache = new FileBasedRepositoryCachePlugin(context.getDataFile("repository"));
        RepositoryImpl service = new RepositoryImpl(provider, cache);
        context.registerService(Repository.class.getName(), service, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}