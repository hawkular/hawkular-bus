package org.hawkular.nest.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

class NestSubsystemRemove extends AbstractRemoveStepHandler {

    static final NestSubsystemRemove INSTANCE = new NestSubsystemRemove();

    private NestSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
            throws OperationFailedException {

        ServiceName name = NestService.SERVICE_NAME;
        context.removeService(name);
    }
}
