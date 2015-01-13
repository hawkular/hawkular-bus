package org.hawkular.bus.broker.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;

class BrokerSubsystemStatus implements OperationStepHandler {

    static final BrokerSubsystemStatus INSTANCE = new BrokerSubsystemStatus();

    private BrokerSubsystemStatus() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        boolean isStarted = false;
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();
            isStarted = service.isBrokerStarted();
        } catch (ServiceNotFoundException snfe) {
            // the broker just isn't deployed, so obviously, is isn't started
            isStarted = false;
		}
        opContext.getResult().set(isStarted ? "STARTED" : "STOPPED");
        opContext.stepCompleted();
	}
}
