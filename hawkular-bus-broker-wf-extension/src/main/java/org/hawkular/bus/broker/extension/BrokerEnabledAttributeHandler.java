package org.hawkular.bus.broker.extension;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

class BrokerEnabledAttributeHandler extends AbstractWriteAttributeHandler<Void> {

    public static final BrokerEnabledAttributeHandler INSTANCE = new BrokerEnabledAttributeHandler();

    private final Logger log = Logger.getLogger(BrokerEnabledAttributeHandler.class);

    private BrokerEnabledAttributeHandler() {
        super(BrokerSubsystemDefinition.BROKER_ENABLED_ATTRIBDEF);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
        ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder)
        throws OperationFailedException {
        log.debug("Broker enabled attribute changed: " + attributeName + "=" + resolvedValue);
        // there is nothing for us to do - this only affects us when we are restarted, return true to say we must reload
        return true;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
        ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        // no-op
    }
}
