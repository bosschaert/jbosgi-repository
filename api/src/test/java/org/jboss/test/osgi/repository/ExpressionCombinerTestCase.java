/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.test.osgi.repository;

import junit.framework.Assert;

import org.jboss.osgi.repository.impl.ExpressionCombinerImpl;
import org.jboss.osgi.repository.impl.RequirementBuilderImpl;
import org.junit.Test;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.AndExpression;
import org.osgi.service.repository.ExpressionCombiner;
import org.osgi.service.repository.RequirementExpression;

/**
 * @author David Bosschaert
 */
public class ExpressionCombinerTestCase {
    @Test
    public void testExpressionCombiner() {
        Requirement req1 = new RequirementBuilderImpl("ns1").build();
        Requirement req2 = new RequirementBuilderImpl("ns2").build();
        ExpressionCombiner ec = new ExpressionCombinerImpl();

        RequirementExpression a = ec.and(req1, req2);
        Assert.assertTrue(a instanceof AndExpression);
        AndExpression ae = (AndExpression) a;
        Assert.assertEquals(2, ae.getRequirements().size());
        Assert.assertTrue(ae.getRequirements().contains(req1));
        Assert.assertTrue(ae.getRequirements().contains(req2));
    }
}
