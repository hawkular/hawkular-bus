package org.hawkular.nest.extension;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

class NestEnabledAttributeHandler extends AbstractWriteAttributeHandler<Void> {

    public static final NestEnabledAttributeHandler INSTANCE = new NestEnabledAttributeHandler();

    private final Logger log = Logger.getLogger(NestEnabledAttributeHandler.class);

    private NestEnabledAttributeHandler() {
        super(NestSubsystemDefinition.AGENT_ENABLED_ATTRIBDEF);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
        ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder)
        throws OperationFailedException {
        log.debug("Nest enabled attribute changed: " + attributeName + "=" + resolvedValue);
        // there is nothing for us to do - this only affects us when we are restarted, return true to say we must reload
        return true;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
        ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        // no-op
    }
}
