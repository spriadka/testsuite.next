/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.testsuite.test.configuration.batch;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.creaper.command.BackupAndRestoreAttributes;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.page.configuration.BatchPage;
import org.jboss.hal.testsuite.util.Notification;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import static org.jboss.hal.testsuite.test.configuration.batch.BatchSubsystem.CONFIGURATION_ADDRESS;

@RunWith(Arquillian.class)
public class BatchConfigurationTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static BackupAndRestoreAttributes backup;

    @Drone private WebDriver browser;
    @Page private BatchPage page;

    @BeforeClass
    public static void beforeClass() throws CommandFailedException {
        backup = new BackupAndRestoreAttributes.Builder(CONFIGURATION_ADDRESS).build();
        client.apply(backup.backup());
    }

    @Before
    public void setUp() throws Exception {
        page.navigate();
        page.getConfigurationItem().click();
    }

    @AfterClass
    public static void afterClass() throws CommandFailedException {
        client.apply(backup.restore());
    }

    @Test
    public void update() throws Exception {
        FormFragment form = page.getConfigurationForm();
        form.edit();
        form.checkbox("batch-configuration-form-restart-jobs-on-resume-editing", false);
        form.save();
        Notification.withBrowser(browser).success();

        new ResourceVerifier(CONFIGURATION_ADDRESS, client, 500)
                .verifyAttribute("restart-jobs-on-resume", false);
    }
}