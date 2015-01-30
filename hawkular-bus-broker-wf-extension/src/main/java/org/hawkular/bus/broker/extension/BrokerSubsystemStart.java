package org.hawkular.bus.broker.extension;

import org.hawkular.bus.broker.extension.log.MsgLogger;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.StartException;

class BrokerSubsystemStart implements OperationStepHandler {

    static final BrokerSubsystemStart INSTANCE = new BrokerSubsystemStart();

    private final MsgLogger msglog = Logger.getMessageLogger(MsgLogger.class, BrokerSubsystemStart.class.getPackage()
            .getName());

    private BrokerSubsystemStart() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();

            boolean restart = model.get(BrokerSubsystemDefinition.START_OP_PARAM_RESTART.getName()).asBoolean(false);
            if (restart) {
                msglog.infoAskedToRestartBroker();
                service.stopBroker();
            }
            service.startBroker();
        } catch (ServiceNotFoundException snfe) {
            throw new OperationFailedException("Cannot restart broker - the broker is disabled", snfe);
        } catch (StartException se) {
            throw new OperationFailedException("Cannot restart broker", se);
        }

        opContext.stepCompleted();
        return;
    }
}
