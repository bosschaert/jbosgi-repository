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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

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

    @Message(id = 20502, value = "Invalid resource description at: %s")
    XMLStreamException xmlInvalidResourceDecription(@Cause Throwable th, Location location);

    @Message(id = 20503, value = "Cannot obtain content capability: %s")
    RepositoryStorageException storageCannotObtainContentCapablility(XResource res);

    @Message(id = 20504, value = "Cannot obtain content URL: %s")
    RepositoryStorageException storageCannotObtainContentURL(XResource res);

    @Message(id = 20505, value = "No such digest algorithm: %s")
    RepositoryStorageException storageNoSuchAlgorithm(@Cause Throwable th, String algorithm);

    @Message(id = 20506, value = "Cannot add resource to storage: %s")
    RepositoryStorageException storageCannotAddResourceToStorage(@Cause Throwable th, String mime);

    @Message(id = 20507, value = "Cannot obtain input stream for: %s")
    RepositoryStorageException storageCannotObtainInputStream(@Cause Throwable th, XResource res);
}
