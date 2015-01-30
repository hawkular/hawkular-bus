package org.hawkular.nest.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

class NestSubsystemStop implements OperationStepHandler {

    static final NestSubsystemStop INSTANCE = new NestSubsystemStop();

    private final Logger log = Logger.getLogger(NestSubsystemStop.class);

    private NestSubsystemStop() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        log.info("Asked to stop the nest");

        NestService service = null;

        try {
            ServiceName name = NestService.SERVICE_NAME;
            service = (NestService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();
        } catch (Exception e) {
            // The nest service just isn't deployed, so obviously, it is already stopped. Just keep going.
        }

        if (service != null) {
            service.stopNest();
        }

        opContext.stepCompleted();
        return;
    }
}
