package org.hawkular.nest.extension;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.MapAttributeDefinition;
import org.jboss.as.controller.SimpleMapAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.parsing.Attribute;
import org.jboss.as.controller.registry.AttributeAccess.Flag;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * A generic catch-all to allow the nest to have any config property set.
 *
 * @author John Mazzitelli
 */
public class CustomConfigAttributeDefinition extends MapAttributeDefinition {

    public CustomConfigAttributeDefinition() {
        super(new SimpleMapAttributeDefinition.Builder(NestSubsystemExtension.CUSTOM_CONFIG_ELEMENT, true)
                .setAllowExpression(false).setMinSize(0).setMaxSize(Integer.MAX_VALUE)
                .setElementValidator(new ModelTypeValidator(ModelType.STRING)).setFlags(Flag.RESTART_RESOURCE_SERVICES));
    }

    @Override
    protected void addValueTypeDescription(ModelNode node, ResourceBundle bundle) {
        node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
    }

    @Override
    protected void addAttributeValueTypeDescription(ModelNode node, ResourceDescriptionResolver resolver,
        Locale locale, ResourceBundle bundle) {
        node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
    }

    @Override
    protected void addOperationParameterValueTypeDescription(ModelNode node, String operationName,
        ResourceDescriptionResolver resolver, Locale locale, ResourceBundle bundle) {
        node.get(ModelDescriptionConstants.VALUE_TYPE).set(ModelType.STRING);
    }

    @Override
    public void marshallAsElement(ModelNode resourceModel, XMLStreamWriter writer) throws XMLStreamException {
        if (!isMarshallable(resourceModel))
            return;

        resourceModel = resourceModel.get(getName());
        writer.writeStartElement(getName());
        for (ModelNode property : resourceModel.asList()) {
            writer.writeEmptyElement(getXmlName());
            writer.writeAttribute(Attribute.NAME.getLocalName(), property.asProperty().getName());
            writer.writeAttribute(Attribute.VALUE.getLocalName(), property.asProperty().getValue().asString());
        }
        writer.writeEndElement();
    }
}
