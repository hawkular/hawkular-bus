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
package org.hawkular.bus;

import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Topic;

import org.hawkular.bus.common.Bus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author jsanda
 */
@RunWith(Arquillian.class)
public class BusTest {

    @Deployment
    public static JavaArchive createDeployment() {
//        WebArchive archive = ShrinkWrap.create(WebArchive.class)
//                .addClass(Greeter.class)
//                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
//                .addAsWebInfResource(BusTest.class.getResource("/jboss-deployment-structure.xml"),
//                        "jboss-deployment-structure.xml");


        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
//                .addClass(Greeter.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "META-INF/beans.xml")
                .setManifest(new StringAsset("Dependencies: org.hawkular.bus"));
//                .addAsManifestResource(BusTest.class.getResource("/jboss-deployment-structure.xml"),
//                        "jboss-deployment-structure.xml");

//        ZipExporter exporter = new ZipExporterImpl(archive);
//        exporter.exportTo(new File("target", "test-archive.jar"));

        return archive;
    }

//    @Inject
//    private Greeter greeter;

    @Inject
    private Bus bus;

    @Resource(name = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/jms/topic/HawkularMetricData")
    private Topic gaugeTopic;

//    @Test
//    public void greet() {
//        assertNotNull(greeter.greet());
//    }

    @Test
    public void sendMessageOnBus() {
        assertNotNull(connectionFactory);

        bus.send("hello");
        bus.send(gaugeTopic, "{\"value\": 1.2345}");
    }

}
