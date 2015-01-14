package org.hawkular.nest.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;

class NestSubsystemStart implements OperationStepHandler {

    static final NestSubsystemStart INSTANCE = new NestSubsystemStart();

    private final Logger log = Logger.getLogger(NestSubsystemStart.class);

    private NestSubsystemStart() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = NestService.SERVICE_NAME;
            NestService service = (NestService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();

            boolean restart = model.get(NestSubsystemDefinition.START_OP_PARAM_RESTART.getName()).asBoolean(false);
            if (restart) {
                log.info("Asked to restart the nest. Will stop it, then restart it now.");
                service.stopNest();
            }
            service.startNest();
        } catch (ServiceNotFoundException snfe) {
            throw new OperationFailedException("Cannot restart nest - the nest is disabled", snfe);
        } catch (Exception e) {
            throw new OperationFailedException("Cannot restart nest", e);
        }

        opContext.stepCompleted();
        return;
    }
}
