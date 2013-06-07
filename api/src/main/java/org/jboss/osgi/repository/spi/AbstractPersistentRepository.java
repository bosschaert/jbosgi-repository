/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import static org.jboss.osgi.repository.RepositoryMessages.MESSAGES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.osgi.repository.RepositoryStorage;
import org.jboss.osgi.repository.RepositoryStorageFactory;
import org.jboss.osgi.repository.XPersistentRepository;
import org.jboss.osgi.repository.XRepository;
import org.jboss.osgi.repository.impl.ExpressionCombinerImpl;
import org.jboss.osgi.repository.impl.RequirementBuilderImpl;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.repository.AndExpression;
import org.osgi.service.repository.ExpressionCombiner;
import org.osgi.service.repository.NotExpression;
import org.osgi.service.repository.OrExpression;
import org.osgi.service.repository.RequirementBuilder;
import org.osgi.service.repository.RequirementExpression;
import org.osgi.service.repository.SimpleRequirementExpression;

/**
 * A {@link XRepository} that delegates to {@link RepositoryStorage}.
 *
 * @author thomas.diesler@jboss.com
 * @author David Bosschaert
 * @since 11-May-2012
 */
public class AbstractPersistentRepository extends AbstractRepository implements XPersistentRepository {

    private final RepositoryStorage storage;
    private XRepository delegate;

    public AbstractPersistentRepository(RepositoryStorageFactory factory) {
        this(factory, null);
    }

    public AbstractPersistentRepository(RepositoryStorageFactory factory, XRepository delegate) {
        if (factory == null)
            throw MESSAGES.illegalArgumentNull("factory");

        this.storage = factory.create(this);
        this.delegate = delegate;
    }

    @Override
    public RepositoryStorage getRepositoryStorage() {
        return storage;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        if (req == null)
            throw MESSAGES.illegalArgumentNull("req");

        Collection<Capability> providers = storage.findProviders(req);
        if (providers.isEmpty() && delegate != null) {
            providers = delegate.findProviders(req);
        }
        return providers;
    }

    @Override
    public Collection<Resource> findProviders(RequirementExpression re) {
        if (re == null)
            throw MESSAGES.illegalArgumentNull("req");

        if (re instanceof SimpleRequirementExpression) {
            return findSimpleRequirementExpression((SimpleRequirementExpression) re);
        } else if (re instanceof AndExpression) {
            return findAndExpression((AndExpression) re);
        } else if (re instanceof OrExpression) {
            return findOrExpression((OrExpression) re);
        } else if (re instanceof NotExpression) {
            throw MESSAGES.unsupportedExpression(re);
        }

        throw MESSAGES.malformedRequirementExpression(re);
    }

    private Collection<Resource> findSimpleRequirementExpression(SimpleRequirementExpression re) {
        Requirement req = re.getRequirement();
        Collection<Capability> capabilities = findProviders(req);

        List<Resource> resources = new ArrayList<Resource>();
        for (Capability cap : capabilities) {
            Resource res = cap.getResource();
            if (res != null) {
                resources.add(res);
            }
        }
        return resources;
    }

    private Collection<Resource> findAndExpression(AndExpression re) {
        // TODO Auto-generated method stub
        return null;
    }

    private Collection<Resource> findOrExpression(OrExpression re) {
        List<Resource> l = new ArrayList<Resource>();
        for (RequirementExpression req : re.getRequirements()) {
            l.addAll(findProviders(req));
        }
        return l;
    }

    @Override
    public ExpressionCombiner getExpressionCombiner() {
        return new ExpressionCombinerImpl();
    }

    @Override
    public RequirementBuilder newRequirementBuilder(String namespace) {
        return new RequirementBuilderImpl(namespace);
    }
}