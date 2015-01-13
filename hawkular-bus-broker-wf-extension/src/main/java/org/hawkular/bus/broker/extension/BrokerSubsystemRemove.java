package org.hawkular.bus.broker.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

class BrokerSubsystemRemove extends AbstractRemoveStepHandler {

    static final BrokerSubsystemRemove INSTANCE = new BrokerSubsystemRemove();

    private BrokerSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
        throws OperationFailedException {

        ServiceName name = BrokerService.SERVICE_NAME;
        context.removeService(name);
    }
}
