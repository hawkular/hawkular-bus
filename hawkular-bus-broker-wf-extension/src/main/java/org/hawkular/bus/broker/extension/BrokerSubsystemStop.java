package org.hawkular.bus.broker.extension;

import org.hawkular.bus.broker.extension.log.MsgLogger;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

class BrokerSubsystemStop implements OperationStepHandler {

    static final BrokerSubsystemStop INSTANCE = new BrokerSubsystemStop();

    private final MsgLogger msglog = Logger.getMessageLogger(MsgLogger.class, BrokerSubsystemStop.class.getPackage()
            .getName());

    private BrokerSubsystemStop() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();
            msglog.infoAskedToStopBroker();
            service.stopBroker();
        } catch (Exception e) {
            // the broker service just isn't deployed, so obviously, the broker is already stopped. just keep going
		}

        opContext.stepCompleted();
        return;
	}
}