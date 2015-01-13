package org.hawkular.bus.broker.extension;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class BrokerSubsystemDefinition extends SimpleResourceDefinition {

	public static final BrokerSubsystemDefinition INSTANCE = new BrokerSubsystemDefinition();

    protected static final SimpleAttributeDefinition BROKER_ENABLED_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.BROKER_ENABLED_ATTR, ModelType.BOOLEAN).setAllowExpression(true).setXmlName(BrokerSubsystemExtension.BROKER_ENABLED_ATTR)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES).setDefaultValue(new ModelNode(BrokerSubsystemExtension.BROKER_ENABLED_DEFAULT))
            .setAllowNull(false).build();

    protected static final SimpleAttributeDefinition BROKER_CONFIG_FILE_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.BROKER_CONFIG_FILE_ATTR, ModelType.STRING).setAllowExpression(true)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES).setDefaultValue(new ModelNode(BrokerSubsystemExtension.BROKER_CONFIG_FILE_DEFAULT))
            .setAllowNull(true).build();

    protected static final SimpleAttributeDefinition BROKER_NAME_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(BrokerSubsystemExtension.BROKER_NAME_ELEMENT,
            ModelType.STRING).setAllowExpression(true).setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode(BrokerSubsystemExtension.BROKER_NAME_DEFAULT)).setAllowNull(true).build();

    protected static final SimpleAttributeDefinition BROKER_PERSISTENT_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.PERSISTENT_ELEMENT, ModelType.BOOLEAN).setAllowExpression(true).setXmlName(BrokerSubsystemExtension.PERSISTENT_ELEMENT)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES).setDefaultValue(new ModelNode(BrokerSubsystemExtension.PERSISTENT_DEFAULT))
            .setAllowNull(true).build();

    protected static final SimpleAttributeDefinition BROKER_USE_JMX_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(BrokerSubsystemExtension.USE_JMX_ELEMENT,
            ModelType.BOOLEAN).setAllowExpression(true).setXmlName(BrokerSubsystemExtension.USE_JMX_ELEMENT)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES).setDefaultValue(new ModelNode(BrokerSubsystemExtension.USE_JMX_DEFAULT))
            .setAllowNull(true).build();

    protected static final CustomConfigAttributeDefinition CUSTOM_CONFIG_ATTRIBDEF = new CustomConfigAttributeDefinition();

    protected static final SimpleAttributeDefinition CONNECTOR_NAME_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.CONNECTOR_NAME_ATTR, ModelType.STRING).setAllowExpression(true).setXmlName(BrokerSubsystemExtension.CONNECTOR_NAME_ATTR)
            .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES).setDefaultValue(new ModelNode(BrokerSubsystemExtension.CONNECTOR_NAME_DEFAULT))
            .setAllowNull(true).build();

    protected static final SimpleAttributeDefinition CONNECTOR_PROTOCOL_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.CONNECTOR_PROTOCOL_ATTR, ModelType.STRING).setAllowExpression(true)
            .setXmlName(BrokerSubsystemExtension.CONNECTOR_PROTOCOL_ATTR).setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode(BrokerSubsystemExtension.CONNECTOR_PROTOCOL_DEFAULT)).setAllowNull(true).build();

    protected static final SimpleAttributeDefinition SOCKET_BINDING_ATTRIBDEF = new SimpleAttributeDefinitionBuilder(
            BrokerSubsystemExtension.CONNECTOR_SOCKET_BINDING_ATTR, ModelType.STRING).setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setDefaultValue(new ModelNode("org.hawkular.bus.broker")).setValidator(new StringLengthValidator(1)).setAllowNull(false).build();

    // operation parameters
    protected static final SimpleAttributeDefinition START_OP_PARAM_RESTART = new SimpleAttributeDefinitionBuilder("restart", ModelType.BOOLEAN)
            .setAllowExpression(true).setDefaultValue(new ModelNode(false)).build();

	private BrokerSubsystemDefinition() {
        super(BrokerSubsystemExtension.SUBSYSTEM_PATH, BrokerSubsystemExtension.getResourceDescriptionResolver(null), BrokerSubsystemAdd.INSTANCE,
                BrokerSubsystemRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration rr) {
        rr.registerReadWriteAttribute(BROKER_ENABLED_ATTRIBDEF, null, BrokerEnabledAttributeHandler.INSTANCE);
        registerReloadRequiredWriteAttributeHandler(rr, BROKER_CONFIG_FILE_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, BROKER_NAME_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, BROKER_PERSISTENT_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, BROKER_USE_JMX_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, CUSTOM_CONFIG_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, CONNECTOR_NAME_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, CONNECTOR_PROTOCOL_ATTRIBDEF);
        registerReloadRequiredWriteAttributeHandler(rr, SOCKET_BINDING_ATTRIBDEF);
    }

    private void registerReloadRequiredWriteAttributeHandler(ManagementResourceRegistration rr, AttributeDefinition def) {
        rr.registerReadWriteAttribute(def, null, new ReloadRequiredWriteAttributeHandler(def));
    }

    @Override
    public void registerOperations(ManagementResourceRegistration rr) {
        super.registerOperations(rr);

        // We always need to add a 'describe' operation
        rr.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);

        rr.registerOperationHandler(
                new SimpleOperationDefinitionBuilder(BrokerSubsystemExtension.BROKER_START_OP, BrokerSubsystemExtension.getResourceDescriptionResolver(null))
                        .addParameter(START_OP_PARAM_RESTART).build(), BrokerSubsystemStart.INSTANCE);

        rr.registerOperationHandler(
                new SimpleOperationDefinitionBuilder(BrokerSubsystemExtension.BROKER_STOP_OP, BrokerSubsystemExtension.getResourceDescriptionResolver(null))
                        .build(), BrokerSubsystemStop.INSTANCE);

        rr.registerOperationHandler(
                new SimpleOperationDefinitionBuilder(BrokerSubsystemExtension.BROKER_STATUS_OP, BrokerSubsystemExtension.getResourceDescriptionResolver(null))
                        .build(), BrokerSubsystemStatus.INSTANCE);

        return;
    }
}
