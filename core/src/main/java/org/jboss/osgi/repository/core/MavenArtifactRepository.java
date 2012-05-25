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

import static org.jboss.osgi.repository.RepositoryLogger.LOGGER;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.osgi.repository.URLResourceBuilderFactory;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.spi.AbstractRepository;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A simple {@link XRepository} that delegates to a maven repositories.
 * 
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenArtifactRepository extends AbstractRepository implements XRepository {

    private static String JBOSS_NEXUS_BASE = "http://repository.jboss.org/nexus/content/groups/public";
    private static String MAVEN_CENTRAL_BASE = "http://repo1.maven.org/maven2";

    private final URL[] baserepos;

    public MavenArtifactRepository() {
        List<URL> repos = new ArrayList<URL>();
        String userhome = System.getProperty("user.home");
        File localrepo = new File(userhome + File.separator + ".m2" + File.separator + "repository");
        if (localrepo.isDirectory()) {
            repos.add(getBaseURL(localrepo.toURI().toString()));
        }
        repos.add(getBaseURL(JBOSS_NEXUS_BASE));
        repos.add(getBaseURL(MAVEN_CENTRAL_BASE));
        baserepos = repos.toArray(new URL[repos.size()]);
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        String namespace = req.getNamespace();
        List<Capability> result = new ArrayList<Capability>();
        if (MAVEN_IDENTITY_NAMESPACE.equals(namespace)) {
            String mavenId = (String) req.getAttributes().get(MAVEN_IDENTITY_NAMESPACE);
            MavenCoordinates coordinates = MavenCoordinates.parse(mavenId);
            LOGGER.infoFindMavenProviders(coordinates);
            for (URL baseURL : baserepos) {
                URL url = coordinates.getArtifactURL(baseURL);
                try {
                    url.openStream().close();
                } catch (IOException e) {
                    LOGGER.debugf("Cannot access input stream for: %s", url);
                    continue;
                }
                try {
                    XResourceBuilder builder = URLResourceBuilderFactory.create(url, null, true);
                    result.add(builder.addGenericCapability(MAVEN_IDENTITY_NAMESPACE, mavenId));
                    LOGGER.infoFoundMavenResource(builder.getResource());
                    break;
                } catch (Exception ex) {
                    LOGGER.resolutionCannotCreateResource(ex, coordinates);
                }
            }
        }
        return result;
    }

    private URL getBaseURL(String basestr) {
        URL baseURL = null;
        try {
            baseURL = new URL(basestr);
        } catch (MalformedURLException e) {
            // ignore
        }
        return baseURL;
    }
}
