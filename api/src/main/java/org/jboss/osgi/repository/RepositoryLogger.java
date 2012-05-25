/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.osgi.repository;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;

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
    @Message(id = 20404, value = "Cannot create resource for: %s")
    void resolutionCannotCreateResource(@Cause Throwable th, MavenCoordinates coordinates);
}
