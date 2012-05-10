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
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.repository.core;

import org.jboss.osgi.repository.ArtifactProviderPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


/**
 * An {@link ArtifactProviderPlugin} that tracks and iterates over other
 * ArtifactProviderPlugin services.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class TrackingArtifactProvider implements ArtifactProviderPlugin {

    private final ServiceTracker tracker;

    public TrackingArtifactProvider(BundleContext context) {
        tracker = new ServiceTracker(context, ArtifactProviderPlugin.class.getName(), null) {};
        tracker.open();
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        Collection<Capability> result = new ArrayList<Capability>();
        for (Object service : tracker.getServices()) {
            ArtifactProviderPlugin plugin = (ArtifactProviderPlugin) service;
            Collection<Capability> caps = plugin.findProviders(req);
            if (!caps.isEmpty()) {
                result.addAll(caps);
                break;
            }
        }
        return Collections.unmodifiableCollection(result);
    }
}