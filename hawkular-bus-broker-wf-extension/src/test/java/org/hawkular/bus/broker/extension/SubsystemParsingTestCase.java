package org.hawkular.bus.broker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubsystemParsingTestCase extends SubsystemBaseParsingTestCase {

    @Override
    @Before
    public void initializeParser() throws Exception {
        super.initializeParser();
    }

    @Override
    @After
    public void cleanup() throws Exception {
        super.cleanup();
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        // Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml();
        List<ModelNode> operations = super.parse(subsystemXml);

        // /Check that we have the expected number of operations
        assertEquals(1, operations.size());

        // Check that each operation has the correct content
        // The add subsystem operation will happen first
        ModelNode addSubsystem = operations.get(0);
        assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        assertEquals(SUBSYSTEM, element.getKey());
        assertEquals(BrokerSubsystemExtension.SUBSYSTEM_NAME, element.getValue());
        assertEquals(true, addSubsystem.get(BrokerSubsystemExtension.BROKER_ENABLED_ATTR).resolve().asBoolean());
        assertEquals("foo/bar.xml", addSubsystem.get(BrokerSubsystemExtension.BROKER_CONFIG_FILE_ATTR).resolve().asString());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        // Parse the subsystem xml and install into the controller
        String subsystemXml = getSubsystemXml();
        KernelServices services = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        // Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        System.out.println(model);
        assertTrue(model.get(SUBSYSTEM).hasDefined(BrokerSubsystemExtension.SUBSYSTEM_NAME));
        assertTrue(model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME).hasDefined(BrokerSubsystemExtension.BROKER_ENABLED_ATTR));
        assertTrue(model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME, BrokerSubsystemExtension.BROKER_ENABLED_ATTR).resolve().asBoolean());

        // Sanity check to test the service was there
        BrokerService broker = (BrokerService) services.getContainer().getRequiredService(BrokerService.SERVICE_NAME).getValue();
        assertNotNull(broker);
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second controller started with the xml
     * marshalled from the first one results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        // Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();

        // Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = createKernelServicesBuilder(null).setSubsystemXml(marshalled).build();
        ModelNode modelB = servicesB.readWholeModel();

        // Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a second controller started with the
     * operations from its describe action results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        // Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
            PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME))
                .toModelNode());
        ModelNode executeOperation = servicesA.executeOperation(describeOp);
        List<ModelNode> operations = super.checkResultAndGetContents(executeOperation).asList();

        // Install the describe options from the first controller into a second controller
        KernelServices servicesB = createKernelServicesBuilder(null).setBootOperations(operations).build();
        ModelNode modelB = servicesB.readWholeModel();

        // Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices services = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        // Sanity check to test the service was there
        BrokerService broker = (BrokerService) services.getContainer().getRequiredService(BrokerService.SERVICE_NAME).getValue();
        assertNotNull(broker);

        // Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        // Check that the services that was installed was removed
        try {
            services.getContainer().getRequiredService(BrokerService.SERVICE_NAME);
            assert false : "The service should have been removed along with the subsystem";
        } catch (ServiceNotFoundException expected) {
            // test passed!
        }
    }

    @Test
    public void testResourceDescription() throws Exception {
        String subsystemXml = getSubsystemXml();
        KernelServices services = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        PathAddress brokerSubsystemPath = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME));

        // ask for resource description: /subsystem=broker:read-resource-description
        ModelNode resourceDescriptionOp = new ModelNode();
        resourceDescriptionOp.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION);
        resourceDescriptionOp.get(OP_ADDR).set(brokerSubsystemPath.toModelNode());
        resourceDescriptionOp.get("operations").set(true); // we want to see the operations also
        ModelNode result = services.executeOperation(resourceDescriptionOp);
        ModelNode content = checkResultAndGetContents(result);

        // check the attributes
        assertTrue(content.get("attributes").isDefined());
        List<Property> attributes = content.get("attributes").asPropertyList();

        List<String> expectedAttributes = Arrays.asList( //
                BrokerSubsystemExtension.DISCOVERY_SOCKET_BINDING_ELEMENT, //
                BrokerSubsystemExtension.CONNECTOR_SOCKET_BINDING_ATTR, //
                BrokerSubsystemExtension.CONNECTOR_NAME_ATTR, //
                BrokerSubsystemExtension.CONNECTOR_PROTOCOL_ATTR, //
                BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT, //
                BrokerSubsystemExtension.USE_JMX_ELEMENT, //
                BrokerSubsystemExtension.PERSISTENT_ELEMENT, //
                BrokerSubsystemExtension.BROKER_CONFIG_FILE_ATTR, //
                BrokerSubsystemExtension.BROKER_NAME_ELEMENT, //
                BrokerSubsystemExtension.BROKER_ENABLED_ATTR);
        assertEquals(attributes.size(), expectedAttributes.size());

        for (int i = 0 ; i < attributes.size(); i++) {
            String attrib = attributes.get(i).getName();
            assertTrue("missing attrib: " + attrib, expectedAttributes.contains(attrib));
        }

        // check the operations (there are many other operations that AS adds to our resource, but we only want to check for ours)
        List<String> expectedOperations = Arrays.asList( //
                BrokerSubsystemExtension.BROKER_START_OP, //
                BrokerSubsystemExtension.BROKER_STOP_OP, //
                BrokerSubsystemExtension.BROKER_STATUS_OP);
        assertTrue(content.get("operations").isDefined());
        List<Property> operations = content.get("operations").asPropertyList();
        List<String> operationNames = new ArrayList<String>();
        for (Property op : operations) {
            operationNames.add(op.getName());
        }
        for (String expectedOperation : expectedOperations) {
            assertTrue("Missing: " + expectedOperation, operationNames.contains(expectedOperation));
        }
    }

    @Test
    public void testExecuteOperations() throws Exception {
        String subsystemXml = getSubsystemXml();
        KernelServices services = createKernelServicesBuilder(null).setSubsystemXml(subsystemXml).build();

        // status check - our service should be available
        BrokerService service = (BrokerService) services.getContainer().getService(BrokerService.SERVICE_NAME).getValue();
        assertNotNull(service);

        PathAddress brokerSubsystemPath = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME));

        // get the startup model from subsystem xml
        ModelNode model = services.readWholeModel();

        // current list of config props
        ModelNode configNode = model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME).get(BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT);

        // Add another
        configNode.add("foo", "true");
        ModelNode addOp = new ModelNode();
        addOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        addOp.get(OP_ADDR).set(brokerSubsystemPath.toModelNode());
        addOp.get(NAME).set(BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT);
        addOp.get(VALUE).set(configNode);
        ModelNode result = services.executeOperation(addOp);
        assertEquals(SUCCESS, result.get(OUTCOME).asString());

        // now test that things are as they should be
        model = services.readWholeModel();
        assertTrue(model.get(SUBSYSTEM).hasDefined(BrokerSubsystemExtension.SUBSYSTEM_NAME));
        assertTrue(model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME).hasDefined(BrokerSubsystemExtension.BROKER_ENABLED_ATTR));
        assertTrue(model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME, BrokerSubsystemExtension.BROKER_ENABLED_ATTR).resolve().asBoolean());
        assertTrue(model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME).hasDefined(BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT));

        List<Property> props = model.get(SUBSYSTEM, BrokerSubsystemExtension.SUBSYSTEM_NAME).get(BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT)
                .asPropertyList();
        assertEquals(3, props.size()); // there were 2, but we added "foo" above
        assertEquals("custom-prop", props.get(0).getName());
        assertEquals("custom-prop-val", props.get(0).getValue().asString());
        assertEquals("custom-prop2", props.get(1).getName());
        assertEquals("custom-prop-val2", props.get(1).getValue().asString());
        assertEquals("foo", props.get(2).getName());
        assertEquals("true", props.get(2).getValue().asString());

        // Use read-attribute instead of reading the whole model to get an attribute value
        ModelNode readOp = new ModelNode();
        readOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readOp.get(OP_ADDR).set(brokerSubsystemPath.toModelNode().resolve());
        readOp.get(NAME).set(BrokerSubsystemExtension.BROKER_ENABLED_ATTR);
        result = services.executeOperation(readOp);
        assertTrue(checkResultAndGetContents(result).resolve().asBoolean());

        readOp.get(NAME).set(BrokerSubsystemExtension.CUSTOM_CONFIG_ELEMENT);
        result = services.executeOperation(readOp);
        ModelNode content = checkResultAndGetContents(result);
        props = content.asPropertyList();
        assertEquals(3, props.size()); // there were 2, but we added "foo" above
        assertEquals("custom-prop", props.get(0).getName());
        assertEquals("custom-prop-val", props.get(0).getValue().asString());
        assertEquals("custom-prop2", props.get(1).getName());
        assertEquals("custom-prop-val2", props.get(1).getValue().asString());
        assertEquals("foo", props.get(2).getName());
        assertEquals("true", props.get(2).getValue().asString());

        // TODO: I think we need to mock the ServerEnvironmentService dependency before we can do this
        // execute status
        // ModelNode statusOp = new ModelNode();
        // statusOp.get(OP).set(BrokerSubsystemExtension.BROKER_STATUS_OP);
        // statusOp.get(OP_ADDR).set(brokerSubsystemPath.toModelNode().resolve());
        // result = services.executeOperation(statusOp);
        // Assert.assertTrue(checkResultAndGetContents(result).asBoolean());
    }
}
