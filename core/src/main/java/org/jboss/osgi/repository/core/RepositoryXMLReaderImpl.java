/*
 * #%L
 * JBossOSGi Repository: API
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
package org.jboss.osgi.repository.core;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.osgi.repository.RepositoryMessages.MESSAGES;
import static org.jboss.osgi.repository.RepositoryNamespace.REPOSITORY_NAMESPACE;
import static org.jboss.osgi.repository.RepositoryNamespace.Element.REPOSITORY;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.osgi.repository.RepositoryNamespace.Attribute;
import org.jboss.osgi.repository.RepositoryNamespace.Element;
import org.jboss.osgi.repository.RepositoryNamespace.Type;
import org.jboss.osgi.repository.RepositoryProcessor;
import org.jboss.osgi.repository.RepositoryXMLReader;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLMapper;
import org.osgi.framework.Version;

/**
 * Handles capability caching
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class RepositoryXMLReaderImpl implements RepositoryXMLReader {

    private final XMLMapper mapper;

    public RepositoryXMLReaderImpl() {
        mapper = XMLMapper.Factory.create();
        RepositoryElementReader elementReader = new RepositoryElementReader();
        mapper.registerRootElement(new QName(REPOSITORY_NAMESPACE, REPOSITORY.getLocalName()), elementReader);
    }

    @Override
    public void parse(InputStream input, RepositoryProcessor model) throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
        mapper.parseDocument(model, reader);
    }

    class RepositoryElementReader implements XMLElementReader<RepositoryProcessor> {

        @Override
        public void readElement(XMLExtendedStreamReader reader, RepositoryProcessor processor) throws XMLStreamException {

            Map<String, String> attributes = new HashMap<String, String>();
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            }
            if (!processor.addRepository(reader.getNamespaceURI(), attributes))
                return;

            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                Element element = Element.forName(reader.getLocalName());
                switch (element) {
                    case RESOURCE: {
                        XResource resource = readResourceElement(reader);
                        if (!processor.addResource(resource)) {
                            return;
                        }
                    }
                }
            }
        }

        private XResource readResourceElement(XMLExtendedStreamReader reader) throws XMLStreamException {
            XResourceBuilder builder = XResourceBuilderFactory.create();
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                Element element = Element.forName(reader.getLocalName());
                switch (element) {
                    case CAPABILITY: {
                        readCapabilityElement(reader, builder);
                        break;
                    }
                    case REQUIREMENT: {
                        readRequirementElement(reader, builder);
                        break;
                    }
                }
            }
            return builder.getResource();
        }

        private void readCapabilityElement(XMLExtendedStreamReader reader, XResourceBuilder builder) throws XMLStreamException {
            String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
            Map<String, Object> attributes = new HashMap<String, Object>();
            Map<String, String> directives = new HashMap<String, String>();
            readAttributesAndDirectives(reader, attributes, directives);
            try {
                builder.addGenericCapability(namespace, attributes, directives);
            } catch (RuntimeException ex) {
                throw MESSAGES.xmlInvalidResourceDecription(ex, reader.getLocation());
            }
        }

        private void readRequirementElement(XMLExtendedStreamReader reader, XResourceBuilder builder) throws XMLStreamException {
            String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
            Map<String, Object> attributes = new HashMap<String, Object>();
            Map<String, String> directives = new HashMap<String, String>();
            readAttributesAndDirectives(reader, attributes, directives);
            try {
                builder.addGenericRequirement(namespace, attributes, directives);
            } catch (RuntimeException ex) {
                throw MESSAGES.xmlInvalidResourceDecription(ex, reader.getLocation());
            }
        }

        private void readAttributesAndDirectives(XMLExtendedStreamReader reader, Map<String, Object> atts, Map<String, String> dirs) throws XMLStreamException {
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                Element element = Element.forName(reader.getLocalName());
                switch (element) {
                    case ATTRIBUTE: {
                        readAttributeElement(reader, atts);
                        reader.discardRemainder();
                        break;
                    }
                    case DIRECTIVE: {
                        readDirectiveElement(reader, dirs);
                        reader.discardRemainder();
                        break;
                    }
                }
            }
        }

        private void readAttributeElement(XMLExtendedStreamReader reader, Map<String, Object> attributes) throws XMLStreamException {
            String name = reader.getAttributeValue(null, Attribute.NAME.toString());
            String valstr = reader.getAttributeValue(null, Attribute.VALUE.toString());
            String typespec = reader.getAttributeValue(null, Attribute.TYPE.toString());
            Type type = typespec != null ? Type.valueOf(typespec) : Type.String;
            boolean listType = valstr.indexOf(",") > 0;
            Object value;
            switch (type) {
                case String:
                    if (listType) {
                        List<String> list = new ArrayList<String>();
                        String[] split = valstr.split(",");
                        for (String val : split) {
                            list.add(val);
                        }
                        value = list;
                    } else {
                        value = valstr;
                    }
                    break;
                case Version:
                    if (listType) {
                        List<Version> list = new ArrayList<Version>();
                        String[] split = valstr.split(",");
                        for (String val : split) {
                            list.add(Version.parseVersion(val));
                        }
                        value = list;
                    } else {
                        value = Version.parseVersion(valstr);
                    }
                    break;
                case Long:
                    if (listType) {
                        List<Long> list = new ArrayList<Long>();
                        String[] split = valstr.split(",");
                        for (String val : split) {
                            list.add(Long.parseLong(val));
                        }
                        value = list;
                    } else {
                        value = Long.parseLong(valstr);
                    }
                    break;
                case Double:
                    if (listType) {
                        List<Double> list = new ArrayList<Double>();
                        String[] split = valstr.split(",");
                        for (String val : split) {
                            list.add(Double.parseDouble(val));
                        }
                        value = list;
                    } else {
                        value = Double.parseDouble(valstr);
                    }
                    break;
                default:
                    value = valstr;
                    break;
            }
            attributes.put(name, value);
        }

        private void readDirectiveElement(XMLExtendedStreamReader reader, Map<String, String> directives) throws XMLStreamException {
            String name = reader.getAttributeValue(null, Attribute.NAME.toString());
            String value = reader.getAttributeValue(null, Attribute.VALUE.toString());
            directives.put(name, value);
        }
    }

}