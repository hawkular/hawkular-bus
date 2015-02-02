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
package org.hawkular.bus.broker;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.net.URI;
import java.util.Arrays;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.hawkular.bus.broker.log.MsgLogger;
import org.jboss.logging.Logger;

/**
 * Provides a slim wrapper around the message broker. You can simply provide a config file (either a ActiveMQ
 * .properties or .xml file) to the constructor, then start/stop the broker.
 *
 * You can start the broker on the command line if you want a standalone broker.
 *
 * You can subclass this to provide additional functionality around configuration and management of the broker.
 */
public class EmbeddedBroker {
    private final MsgLogger msglog = MsgLogger.LOGGER;
    private final Logger log = Logger.getLogger(EmbeddedBroker.class);
    private InitializationParameters initialParameters;
    private BrokerService brokerService;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing arguments. Please specify configuration properties file.");
        }

        EmbeddedBroker embeddedBroker = new EmbeddedBroker(args);
        embeddedBroker.startBroker();

        // go to sleep indefinitely
        synchronized (args) {
            args.wait();
        }
    }

    public EmbeddedBroker(String[] cmdlineArgs) throws Exception {
        InitializationParameters initParams = processArguments(cmdlineArgs);
        setInitializationParameters(initParams);
        initializeBrokerService();
    }

    public EmbeddedBroker(InitializationParameters initParams) throws Exception {
        setInitializationParameters(initParams);
        initializeBrokerService();
    }

    public boolean isBrokerStarted() {
        BrokerService broker = getBrokerService();
        if (broker == null) {
            return false;
        }
        return broker.isStarted();
    }

    public void startBroker() throws Exception {
        BrokerService broker = getBrokerService();
        if (broker == null) {
            throw new IllegalStateException("Broker was not initialized");
        }
        msglog.infoStartingBroker();
        broker.start();
        msglog.infoStartedBroker();
    }

    public void stopBroker() throws Exception {
        BrokerService broker = getBrokerService();
        if (broker == null) {
            return; // nothing to do
        }

        try {
            msglog.infoStoppingBroker();
            broker.stop();
            msglog.infoStoppedBroker();
        } finally {
            setBrokerService(null); // we do not want to attempt to reuse or restart this broker instance again
        }
    }

    protected InitializationParameters getInitializationParameters() {
        return this.initialParameters;
    }

    protected void setInitializationParameters(InitializationParameters ip) {
        this.initialParameters = ip;
    }

    protected void initializeBrokerService() throws Exception {
        if (getBrokerService() != null) {
            throw new IllegalStateException("Broker is already initialized");
        }

        InitializationParameters initParams = getInitializationParameters();
        if (initParams == null) {
            throw new IllegalStateException("Missing initialization parameters");
        }

        BrokerService broker = BrokerFactory.createBroker(initParams.configFile, false);
        setBrokerService(broker);
        msglog.infoInitializedBroker();
    }

    protected void setBrokerService(BrokerService broker) {
        brokerService = broker;
    }

    /**
     * This is protected because we don't want it to leak out. If it is ever stopped, it typically can't be reused or
     * restarted again.
     *
     * @return the actual broker instance
     */
    protected BrokerService getBrokerService() {
        return brokerService;
    }

    protected InitializationParameters processArguments(String[] cmdlineArgs) throws Exception {
        log.debugf("Processing arguments: %s", Arrays.asList(cmdlineArgs));

        String configFileArg = null;

        String sopts = "-:hD:c:";
        LongOpt[] lopts = { new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'), //
                new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c') };

        Getopt getopt = new Getopt("hawkular-bus-broker", cmdlineArgs, sopts, lopts);
        int code;

        while ((code = getopt.getopt()) != -1) {
            switch (code) {
                case ':':
                case '?': {
                    // for now both of these should exit
                    displayUsage();
                    throw new IllegalArgumentException("Invalid argument(s)");
                }

                case 1: {
                    // this will catch non-option arguments (which we don't currently care about)
                    System.err.println("Unused argument: " + getopt.getOptarg());
                    break;
                }

                case 'h': {
                    displayUsage();
                    throw new HelpException("Help displayed");
                }

                case 'D': {
                    String sysprop = getopt.getOptarg();
                    int i = sysprop.indexOf("=");
                    String name;
                    String value;

                    if (i == -1) {
                        name = sysprop;
                        value = "true";
                    } else {
                        name = sysprop.substring(0, i);
                        value = sysprop.substring(i + 1, sysprop.length());
                    }

                    System.setProperty(name, value);
                    log.debugf("System property set: %s=%s", name, value);

                    break;
                }

                case 'c': {
                    configFileArg = getopt.getOptarg();
                    break;
                }
            }
        }

        if (configFileArg == null) {
            throw new IllegalArgumentException("Missing configuration file (-c)");
        }

        // ensure forward slashes for a valid URI
        configFileArg = configFileArg.replace("\\", "/");

        InitializationParameters initParamsFromArguments = new InitializationParameters();

        // help the user out - if they gave a file without the proper prefix, add the prefix for them
        if (configFileArg.endsWith(".properties") && !configFileArg.startsWith("properties:")) {
            configFileArg = "properties:" + configFileArg;
        } else if (configFileArg.endsWith(".xml") && !configFileArg.startsWith("xbean:")) {
            configFileArg = "xbean:" + configFileArg;
        }

        initParamsFromArguments.configFile = new URI(configFileArg);

        return initParamsFromArguments;
    }

    private void displayUsage() {
        StringBuilder str = new StringBuilder();
        str.append("Options:").append("\n");
        str.append("\t--help, -h: Displays this help text.").append("\n");
        str.append("\t-Dname=value: Sets a system property.").append("\n");
        str.append("\t--config=<file>, -c: Specifies the file used to configure the broker.").append("\n");
        msglog.infoUsage(str.toString());
    }

    private class HelpException extends Exception {
        private static final long serialVersionUID = 1L;

        public HelpException(String msg) {
            super(msg);
        }
    }

    public static class InitializationParameters {
        public URI configFile;
    }
}
