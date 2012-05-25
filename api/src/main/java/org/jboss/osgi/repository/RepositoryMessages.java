/*
 * #%L
 * JBossOSGi Repository: API
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
package org.jboss.osgi.repository;

import javax.xml.stream.Location;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.osgi.resolver.XResource;

/**
 * Logging Id ranges: 20500-20599
 * 
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 * 
 * @author Thomas.Diesler@jboss.com
 */
@MessageBundle(projectCode = "JBOSGI")
public interface RepositoryMessages {

    RepositoryMessages MESSAGES = Messages.getBundle(RepositoryMessages.class);

    @Message(id = 20500, value = "%s is null")
    IllegalArgumentException illegalArgumentNull(String name);

    @Message(id = 20501, value = "Resource already exists: %s")
    IllegalStateException illegalStateResourceAlreadyExists(XResource res);

    @Message(id = 20502, value = "Cannot initialize repository reader")
    IllegalStateException illegalStateCannotInitializeRepositoryReader(@Cause Throwable th);

    @Message(id = 20503, value = "Cannot read repository element: %s")
    IllegalStateException illegalStateCannotReadRepositoryElement(@Cause Throwable th, Location location);

    @Message(id = 20504, value = "Cannot read resource element: %s")
    IllegalStateException storageCannotReadResourceElement(@Cause Throwable th, Location location);

    @Message(id = 20505, value = "Cannot obtain content capability: %s")
    RepositoryStorageException storageCannotObtainContentCapablility(XResource res);

    @Message(id = 20506, value = "Cannot obtain content URL: %s")
    RepositoryStorageException storageCannotObtainContentURL(XResource res);

    @Message(id = 20507, value = "Cannot access content URL: %s")
    RepositoryStorageException storageCannotAccessContentURL(@Cause Throwable th, String contentURL);

    @Message(id = 20508, value = "Invalid content URL: %s")
    RepositoryStorageException storageInvalidContentURL(String contentURL);

    @Message(id = 20509, value = "No such digest algorithm: %s")
    RepositoryStorageException storageNoSuchAlgorithm(@Cause Throwable th, String algorithm);

    @Message(id = 20510, value = "Cannot add resource to storage: %s")
    RepositoryStorageException storageCannotAddResourceToStorage(@Cause Throwable th, String mime);

    @Message(id = 20511, value = "Cannot obtain input stream for: %s")
    RepositoryStorageException storageCannotObtainInputStream(@Cause Throwable th, XResource res);

    @Message(id = 20512, value = "Cannot initialize repository writer")
    IllegalStateException illegalStateCannotInitializeRepositoryWriter(@Cause Throwable th);

    @Message(id = 20513, value = "Cannot write repository element")
    IllegalStateException illegalStateCannotWriteRepositoryElement(@Cause Throwable th);

    @Message(id = 20514, value = "Invalid filter directive: %s")
    IllegalArgumentException illegalArgumentInvalidFilterDirective(String filter);
}
