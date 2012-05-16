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
package org.jboss.osgi.repository;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;

import java.net.URL;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XResource;

/**
 * Logging Id ranges: 20400-20499
 *
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 *
 * @author Thomas.Diesler@jboss.com
 */
@MessageLogger(projectCode = "JBOSGI")
public interface RepositoryLogger extends BasicLogger {

    RepositoryLogger LOGGER = Logger.getMessageLogger(RepositoryLogger.class, "org.jboss.osgi.repository");

    @LogMessage(level = INFO)
    @Message(id = 20400, value = "Resource added: %s")
    void infoResourceAdded(XResource res);

    @LogMessage(level = INFO)
    @Message(id = 20401, value = "Resource removed: %s")
    void infoResourceRemoved(XResource res);

    @LogMessage(level = INFO)
    @Message(id = 20402, value = "Find maven providers for: %s")
    void infoFindMavenProviders(MavenCoordinates coordinates);

    @LogMessage(level = INFO)
    @Message(id = 20403, value = "Found maven resource: %s")
    void infoFoundMavenResource(XResource resource);

    @LogMessage(level = ERROR)
    @Message(id = 20404, value = "Cannot access input stream for: %s")
    void errorCannotOpenInputStream(URL url);

    @LogMessage(level = ERROR)
    @Message(id = 20405, value = "Cannot create resource for: %s")
    void resolutionCannotCreateResource(@Cause Throwable th, MavenCoordinates coordinates);
}
