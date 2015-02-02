/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.bus.broker.extension;

import org.hawkular.bus.broker.extension.log.MsgLogger;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.StartException;

class BrokerSubsystemStart implements OperationStepHandler {

    static final BrokerSubsystemStart INSTANCE = new BrokerSubsystemStart();

    private final MsgLogger msglog = MsgLogger.LOGGER;

    private BrokerSubsystemStart() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name)
                    .getValue();

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
