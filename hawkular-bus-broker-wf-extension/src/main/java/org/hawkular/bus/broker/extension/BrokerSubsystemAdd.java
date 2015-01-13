package org.hawkular.bus.broker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ARCHIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PERSISTENT;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * Handler responsible for adding the subsystem resource to the model
 */
class BrokerSubsystemAdd extends AbstractAddStepHandler {

    static final BrokerSubsystemAdd INSTANCE = new BrokerSubsystemAdd();

    private final Logger log = Logger.getLogger(BrokerSubsystemAdd.class);

    private BrokerSubsystemAdd() {
    }

    @Override
    protected void populateModel(OperationContext context, ModelNode operation, Resource resource) throws OperationFailedException {

        // ask that the REST war be deployed
        try {
            if (requiresRuntime(context)) { // only add the step if we are going to actually deploy the war
                PathAddress deploymentAddress = PathAddress.pathAddress(PathElement.pathElement(DEPLOYMENT, BrokerSubsystemExtension.DEPLOYMENT_REST_WAR));
                ModelNode op = Util.getEmptyOperation(ADD, deploymentAddress.toModelNode());
                op.get(ENABLED).set(true);
                op.get(PERSISTENT).set(false); // prevents writing this deployment out to standalone.xml

                Module module = Module.forClass(BrokerService.class);
                URL url = module.getExportedResource(BrokerSubsystemExtension.DEPLOYMENT_REST_WAR);
                if (url == null) {
                    throw new FileNotFoundException("Could not find the REST WAR");
                }
                ModelNode contentItem = new ModelNode();

                String urlString = new File(url.toURI()).getAbsolutePath();
                if (!(new File(urlString).exists())) {
                    throw new FileNotFoundException("Missing the WAR at [" + urlString + "]");
                }
                contentItem.get(PATH).set(urlString);
                contentItem.get(ARCHIVE).set(false);

                op.get(CONTENT).add(contentItem);

                ImmutableManagementResourceRegistration rootResourceRegistration;
                rootResourceRegistration = context.getRootResourceRegistration();
                OperationStepHandler handler = rootResourceRegistration.getOperationHandler(deploymentAddress, ADD);

                context.addStep(op, handler, OperationContext.Stage.MODEL);
            }
        } catch (Exception e) {
            // log an error but keep going; this just means we lose our REST interface but the broker should still work
            log.error("The REST WAR could not be deployed; REST interface to the broker is unavailable: " + e);
            log.debug("The REST WAR deployment exception stack is logged with this message", e);
        }

        // finish the broker subsystem model
        populateModel(operation, resource.getModel());
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        BrokerSubsystemDefinition.BROKER_ENABLED_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.BROKER_CONFIG_FILE_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.BROKER_NAME_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.BROKER_PERSISTENT_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.BROKER_USE_JMX_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.CUSTOM_CONFIG_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.CONNECTOR_NAME_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.CONNECTOR_PROTOCOL_ATTRIBDEF.validateAndSet(operation, model);
        BrokerSubsystemDefinition.SOCKET_BINDING_ATTRIBDEF.validateAndSet(operation, model);
        log.debug("Populating the Broker subsystem model: " + operation + "=" + model);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        boolean enabled = BrokerSubsystemDefinition.BROKER_ENABLED_ATTRIBDEF.resolveModelAttribute(context, model).asBoolean(
                BrokerSubsystemExtension.BROKER_ENABLED_DEFAULT);

        if (!enabled) {
            log.info("Broker is not enabled and will not be deployed");
            return;
        }

        String configFile = BrokerSubsystemDefinition.BROKER_CONFIG_FILE_ATTRIBDEF.resolveModelAttribute(context, model).asString();
        if (configFile == null || configFile.trim().isEmpty()) {
            configFile = BrokerSubsystemExtension.BROKER_CONFIG_FILE_DEFAULT;
        }

        log.info("Broker is enabled and will be deployed using config file [" + configFile + "]");

        // set up our runtime custom configuration properties that should be used instead of the out-of-box config
        Map<String, String> customConfigProps = new HashMap<String, String>();
        addCustomConfigProperty(context, model, customConfigProps, BrokerSubsystemDefinition.BROKER_NAME_ATTRIBDEF);
        addCustomConfigProperty(context, model, customConfigProps, BrokerSubsystemDefinition.BROKER_PERSISTENT_ATTRIBDEF);
        addCustomConfigProperty(context, model, customConfigProps, BrokerSubsystemDefinition.BROKER_USE_JMX_ATTRIBDEF);
        addCustomConfigProperty(context, model, customConfigProps, BrokerSubsystemDefinition.CONNECTOR_NAME_ATTRIBDEF);
        addCustomConfigProperty(context, model, customConfigProps, BrokerSubsystemDefinition.CONNECTOR_PROTOCOL_ATTRIBDEF);

        // allow the user to provide their own config props
        ModelNode customConfigNode = BrokerSubsystemDefinition.CUSTOM_CONFIG_ATTRIBDEF.resolveModelAttribute(context, model);
        if (customConfigNode != null && customConfigNode.isDefined()) {
            HashMap<String, String> customConfig = new HashMap<String, String>();
            List<Property> propList = customConfigNode.asPropertyList();
            for (Property prop : propList) {
                String name = prop.getName();
                String val = prop.getValue().asString();
                customConfig.put(name, val);
            }
            customConfigProps.putAll(customConfig);
        }

        // create our service
        BrokerService service = new BrokerService();
        service.setConfigurationFile(configFile);
        service.setCustomConfigurationProperties(customConfigProps);

        // install the service
        String binding = BrokerSubsystemDefinition.SOCKET_BINDING_ATTRIBDEF.resolveModelAttribute(context, model).asString();
        ServiceName name = BrokerService.SERVICE_NAME;
        ServiceController<BrokerService> controller = context.getServiceTarget() //
                .addService(name, service) //
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.envServiceValue) //
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(binding), SocketBinding.class, service.connectorSocketBinding) //
                .addListener(verificationHandler) //
                .setInitialMode(Mode.ACTIVE) //
                .install();
        newControllers.add(controller);
        return;
    }

    private void addCustomConfigProperty(OperationContext context, ModelNode model, Map<String, String> customConfigProps, AttributeDefinition attribDef)
            throws OperationFailedException {
        addCustomConfigProperty(context, model, customConfigProps, attribDef, null);
    }

    private void addCustomConfigProperty(OperationContext context, ModelNode model, Map<String, String> customConfigProps, AttributeDefinition attribDef,
            String customConfigPropName) throws OperationFailedException {
        ModelNode node = attribDef.resolveModelAttribute(context, model);
        if (node.isDefined()) {
            customConfigProps.put((customConfigPropName == null) ? attribDef.getName() : customConfigPropName, node.asString());
        }
    }
}
