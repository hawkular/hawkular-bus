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
package org.hawkular.bus.common;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author jsanda
 */
@RunWith(Arquillian.class)
public class BusTest {

    @Deployment(name = "bus", order = 1, testable = false)
    public static JavaArchive createModule() {
        String moduleDescriptor =
                "<module xmlns=\"urn:jboss:module:1.0\" name=\"org.hawkular.bus\">\n" +
                "<resources>\n" +
                "<resource-root path=\"${project.build.finalName}.jar\" />";

        String dependencies = "javax.enterprise.api, javax.enterprise.api";

        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Bus.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "META-INF/beans.xml")
                .setManifest(new StringAsset("module-name: org.hawkular.bus\nDependencies: " + dependencies));
    }

    @Deployment(name = "testapp", order = 2)
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

        ZipExporter exporter = new ZipExporterImpl(archive);
        exporter.exportTo(new File("target", "test-archive.jar"));

        return archive;
    }

    @Inject
    private Bus bus;

//    @Test
//    public void greet() {
//        assertNotNull(greeter.greet());
//    }

    @Test
    public void sendMessageOnBus() {
        bus.send("hello");
    }

}
