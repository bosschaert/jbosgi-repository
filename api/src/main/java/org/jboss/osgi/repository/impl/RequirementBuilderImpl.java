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
package org.jboss.osgi.repository.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.repository.RequirementBuilder;

/**
 * @author David Bosschaert
 */
public class RequirementBuilderImpl implements RequirementBuilder {
    private final String namespace;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private Map<String, String> directives = new HashMap<String, String>();
    private Resource resource = null;

    public RequirementBuilderImpl(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public synchronized RequirementBuilder addAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public synchronized RequirementBuilder addDirective(String name, String value) {
        directives.put(name, value);
        return this;
    }

    @Override
    public synchronized RequirementBuilder setAttributes(Map<String, Object> attrs) {
        attributes = new HashMap<String, Object>(attrs);
        return this;
    }

    @Override
    public synchronized RequirementBuilder setDirectives(Map<String, String> dirs) {
        directives = new HashMap<String, String>(dirs);
        return this;
    }

    @Override
    public synchronized RequirementBuilder setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public synchronized Requirement build() {
        return new RequirementImpl(namespace, attributes, directives, resource);
    }

    private static final class RequirementImpl implements Requirement {
        private final String namespace;
        private final Map<String, Object> attributes;
        private final Map<String, String> directives;
        private final Resource resource;

        public RequirementImpl(String ns, Map<String, Object> attrs, Map<String, String> dirs, Resource res) {
            namespace = ns;
            attributes = Collections.unmodifiableMap(attrs);
            directives = Collections.unmodifiableMap(dirs);
            resource = res;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Map<String, String> getDirectives() {
            return directives;
        }

        @Override
        public Resource getResource() {
            return resource;
        }
    }
}
