package org.hawkular.nest.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;

class NestSubsystemStatus implements OperationStepHandler {

    static final NestSubsystemStatus INSTANCE = new NestSubsystemStatus();

    private NestSubsystemStatus() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        boolean isStarted = false;
        try {
            ServiceName name = NestService.SERVICE_NAME;
            NestService service = (NestService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();
            isStarted = service.isStarted();
        } catch (ServiceNotFoundException snfe) {
            // the agent just isn't deployed, so obviously, it isn't started
            isStarted = false;
		}
        opContext.getResult().set(isStarted ? "STARTED" : "STOPPED");
        opContext.stepCompleted();
	}
}
