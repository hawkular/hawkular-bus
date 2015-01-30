/*
 * Copyright 2014-2015 Red Hat, Inc. and/or its affiliates and other contributors as indicated by the @author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.hawkular.bus.broker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.hawkular.bus.broker.extension.log.MsgLogger;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.Attribute;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.dmr.ValueExpression;
import org.jboss.logging.Logger;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public class BrokerSubsystemExtension implements Extension {

    private final MsgLogger msglog = Logger.getMessageLogger(MsgLogger.class, BrokerSubsystemExtension.class
            .getPackage().getName());
    private final Logger log = Logger.getLogger(BrokerSubsystemExtension.class);

    public static final String NAMESPACE = "urn:org.hawkular.bus:broker:1.0";
    public static final String SUBSYSTEM_NAME = "hawkular-bus-broker";

    public static final String DEPLOYMENT_REST_WAR = "rest.war";

    private final SubsystemParser parser = new SubsystemParser();

    private static final String RESOURCE_NAME = BrokerSubsystemExtension.class.getPackage().getName() + ".LocalDescriptions";

    // The following are standard ${x} variables that are defined in the default-broker.xml configuration file. These
    // will be set as system properties whose values will be the values that are given to the extension.
    // If a user wants to use their own broker XML file, then they can re-use these ${x} variables in that configuration
    // file to maintain configurability via the extension rather than hardcoding them into the xml file itself.
    protected static final String BROKER_NAME_SYSPROP = "org.hawkular.bus.broker.name";
    protected static final String BROKER_PERSISTENT_SYSPROP = "org.hawkular.bus.broker.persistent";
    protected static final String BROKER_USE_JMX_SYSPROP = "org.hawkular.bus.broker.use-jmx";
    protected static final String BROKER_CONNECTOR_NAME_SYSPROP = "org.hawkular.bus.broker.connector.name";
    protected static final String BROKER_CONNECTOR_PROTOCOL_SYSPROP = "org.hawkular.bus.broker.connector.protocol";
    protected static final String BROKER_CONNECTOR_ADDRESS_SYSPROP = "org.hawkular.bus.broker.connector.address";
    protected static final String BROKER_CONNECTOR_PORT_SYSPROP = "org.hawkular.bus.broker.connector.port";
    protected static final String BROKER_DISCOVERY_ADDRESS_SYSPROP = "org.hawkular.bus.broker.discovery.address";
    protected static final String BROKER_DISCOVERY_PORT_SYSPROP = "org.hawkular.bus.broker.discovery.port";

    // The following define the XML elements and attributes of the extension itself (these appear in WildFly's
    // standalone.xml for this extension).
    protected static final String BROKER_ENABLED_ATTR = "enabled";
    protected static final boolean BROKER_ENABLED_DEFAULT = false;

    protected static final String BROKER_CONFIG_FILE_ATTR = "configuration-file";
    protected static final String BROKER_CONFIG_FILE_DEFAULT = "default-broker.xml";

    protected static final String BROKER_NAME_ELEMENT = BROKER_NAME_SYSPROP;
    protected static final String BROKER_NAME_DEFAULT = "org.hawkular.bus.broker";

    protected static final String PERSISTENT_ELEMENT = BROKER_PERSISTENT_SYSPROP;
    protected static final boolean PERSISTENT_DEFAULT = false;

    protected static final String USE_JMX_ELEMENT = BROKER_USE_JMX_SYSPROP;
    protected static final boolean USE_JMX_DEFAULT = false;

    protected static final String CONNECTOR_ELEMENT = "connector";
    protected static final String CONNECTOR_NAME_ATTR = BROKER_CONNECTOR_NAME_SYSPROP;
    protected static final String CONNECTOR_PROTOCOL_ATTR = BROKER_CONNECTOR_PROTOCOL_SYSPROP;
    protected static final String CONNECTOR_SOCKET_BINDING_ATTR = "socket-binding";
    protected static final String CONNECTOR_SOCKET_BINDING_DEFAULT = "org.hawkular.bus.broker";
    protected static final String CONNECTOR_NAME_DEFAULT = "openwire";
    protected static final String CONNECTOR_PROTOCOL_DEFAULT = "tcp";

    protected static final String DISCOVERY_SOCKET_BINDING_ELEMENT = "discovery-socket-binding";
    protected static final String DISCOVERY_SOCKET_BINDING_DEFAULT = "org.hawkular.bus.broker.discovery";

    protected static final String CUSTOM_CONFIG_ELEMENT = "custom-configuration";
    protected static final String PROPERTY_ELEMENT = "property";

    protected static final String BROKER_START_OP = "start";
    protected static final String BROKER_STOP_OP = "stop";
    protected static final String BROKER_STATUS_OP = "status";

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, BrokerSubsystemExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    @Override
    public void initialize(ExtensionContext context) {
        msglog.infoInitializingBrokerSubsystem();

        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(BrokerSubsystemDefinition.INSTANCE);

        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // The "enabled" attribute is required, "configuration-file" is optional
            ParseUtils.requireAttributes(reader, BROKER_ENABLED_ATTR);

            // Add the main subsystem 'add' operation
            final ModelNode opAdd = new ModelNode();
            opAdd.get(OP).set(ADD);
            opAdd.get(OP_ADDR).set(PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode());
            String brokerEnabledValue = reader.getAttributeValue(null, BROKER_ENABLED_ATTR);
            if (brokerEnabledValue != null) {
                opAdd.get(BROKER_ENABLED_ATTR).set(new ValueExpression(brokerEnabledValue));
            }
            String brokerConfigFileValue = reader.getAttributeValue(null, BROKER_CONFIG_FILE_ATTR);
            if (brokerConfigFileValue != null) {
                opAdd.get(BROKER_CONFIG_FILE_ATTR).set(new ValueExpression(brokerConfigFileValue));
            }

            // Read the children elements
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                String elementName = reader.getLocalName();
                if (elementName.equals(CUSTOM_CONFIG_ELEMENT)) {
                    ModelNode configAttributeNode = opAdd.get(CUSTOM_CONFIG_ELEMENT);
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readCustomConfigurationProperty(reader, configAttributeNode);
                        }
                    }
                } else if (elementName.equals(CONNECTOR_ELEMENT)) {
                    String val = reader.getAttributeValue(null, CONNECTOR_NAME_ATTR);
                    if (val != null) {
                        opAdd.get(CONNECTOR_NAME_ATTR).set(new ValueExpression(val));
                    }
                    val = reader.getAttributeValue(null, CONNECTOR_PROTOCOL_ATTR);
                    if (val != null) {
                        opAdd.get(CONNECTOR_PROTOCOL_ATTR).set(new ValueExpression(val));
                    }
                    val = reader.getAttributeValue(null, CONNECTOR_SOCKET_BINDING_ATTR);
                    if (val != null) {
                        // we don't support expression here, must be the actual name
                        opAdd.get(CONNECTOR_SOCKET_BINDING_ATTR).set(val);
                    }
                    ParseUtils.requireNoContent(reader);
                } else if (elementName.equals(BROKER_NAME_ELEMENT)) {
                    opAdd.get(BROKER_NAME_ELEMENT).set(new ValueExpression(reader.getElementText()));
                } else if (elementName.equals(PERSISTENT_ELEMENT)) {
                    opAdd.get(PERSISTENT_ELEMENT).set(new ValueExpression(reader.getElementText()));
                } else if (elementName.equals(USE_JMX_ELEMENT)) {
                    opAdd.get(USE_JMX_ELEMENT).set(new ValueExpression(reader.getElementText()));
                } else if (elementName.equals(DISCOVERY_SOCKET_BINDING_ELEMENT)) {
                    // we don't support expression here, must be the actual name
                    opAdd.get(DISCOVERY_SOCKET_BINDING_ELEMENT).set(reader.getElementText());
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }

            list.add(opAdd);
        }

        private void readCustomConfigurationProperty(XMLExtendedStreamReader reader, ModelNode configAttributeNode) throws XMLStreamException {
            if (!reader.getLocalName().equals(PROPERTY_ELEMENT)) {
                throw ParseUtils.unexpectedElement(reader);
            }

            ParseUtils.requireAttributes(reader, Attribute.NAME.getLocalName(), Attribute.VALUE.getLocalName());
            String attr = reader.getAttributeValue(null, Attribute.NAME.getLocalName());
            String val = reader.getAttributeValue(null, Attribute.VALUE.getLocalName());
            ParseUtils.requireNoContent(reader);

            configAttributeNode.add(attr, val);
        }

        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
            ModelNode node = context.getModelNode();

            // <subsystem>
            context.startSubsystemElement(BrokerSubsystemExtension.NAMESPACE, false);
            writer.writeAttribute(BROKER_ENABLED_ATTR, node.get(BROKER_ENABLED_ATTR).asString());
            writer.writeAttribute(BROKER_CONFIG_FILE_ATTR, node.get(BROKER_CONFIG_FILE_ATTR).asString());

            // our main broker config elements
            writeElement(writer, node, BROKER_NAME_ELEMENT);
            writeElement(writer, node, PERSISTENT_ELEMENT);
            writeElement(writer, node, USE_JMX_ELEMENT);

            // <connector>
            writer.writeStartElement(CONNECTOR_ELEMENT);
            ModelNode connectorNameNode = node.get(CONNECTOR_NAME_ATTR);
            ModelNode connectorProtocolNode = node.get(CONNECTOR_PROTOCOL_ATTR);
            ModelNode connectorSocketBindingNode = node.get(CONNECTOR_SOCKET_BINDING_ATTR);

            if (connectorNameNode.isDefined()) {
                writer.writeAttribute(CONNECTOR_NAME_ATTR, connectorNameNode.asString());
            }
            if (connectorProtocolNode.isDefined()) {
                writer.writeAttribute(CONNECTOR_PROTOCOL_ATTR, connectorProtocolNode.asString());
            }
            if (connectorSocketBindingNode.isDefined()) {
                writer.writeAttribute(CONNECTOR_SOCKET_BINDING_ATTR, connectorSocketBindingNode.asString());
            }
            // </connector>
            writer.writeEndElement();

            // <socket-binding-element />
            writeElement(writer, node, DISCOVERY_SOCKET_BINDING_ELEMENT);

            // <custom-configuration>
            writer.writeStartElement(CUSTOM_CONFIG_ELEMENT);
            ModelNode configNode = node.get(CUSTOM_CONFIG_ELEMENT);
            if (configNode != null && configNode.isDefined()) {
                for (Property property : configNode.asPropertyList()) {
                    // <propery>
                    writer.writeStartElement(PROPERTY_ELEMENT);
                    writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
                    writer.writeAttribute(Attribute.VALUE.getLocalName(), property.getValue().asString());
                    // </property>
                    writer.writeEndElement();
                }
            }
            // </custom-configuration>
            writer.writeEndElement();

            // </subsystem>
            writer.writeEndElement();
        }

        private void writeElement(final XMLExtendedStreamWriter writer, ModelNode node, String attribName) throws XMLStreamException {
            ModelNode attribNode = node.get(attribName);
            if (attribNode.isDefined()) {
                writer.writeStartElement(attribName);
                writer.writeCharacters(attribNode.asString());
                writer.writeEndElement();
            }
        }
    }
}
