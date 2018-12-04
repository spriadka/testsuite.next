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
package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.logs;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

@RunWith(Arquillian.class)
public class AggregateSecurityEventListenerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        Values periodicHandlerParams = Values.of(PATH, ANY_STRING).and(SUFFIX, SUFFIX_LOG);
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE), periodicHandlerParams).assertSuccess();
        operations.add(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE), Values.of(PATH, ANY_STRING)).assertSuccess();
        Values syslogParams = Values.of(HOSTNAME, ANY_STRING).and(PORT, Random.number()).and(SERVER_ADDRESS, LOCALHOST);
        operations.add(syslogAuditLogAddress(SYS_LOG_UPDATE), syslogParams).assertSuccess();
        Values secEventParams = Values.ofList(SECURITY_EVENT_LISTENERS, SYS_LOG_UPDATE, SIZ_LOG_UPDATE);
        operations.add(aggregateSecurityEventListenerAddress(AGG_SEC_UPDATE), secEventParams).assertSuccess();
        operations.add(aggregateSecurityEventListenerAddress(AGG_SEC_DELETE), secEventParams).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_UPDATE));
        operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_CREATE));
        operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_DELETE));
        operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE));
        operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_UPDATE));
        operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE));
    }


    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Drone
    private WebDriver browser;

    @Page
    private ElytronOtherSettingsPage page;

    @Before
    public void before() {
        page.navigate();
        console.verticalNavigation().selectSecondary(LOGS_ITEM, AGGREGATE_SECURITY_EVENT_LISTENER_ITEM);    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getAggregateSecurityEventListenerTable();

        crud.create(aggregateSecurityEventListenerAddress(AGG_SEC_CREATE), table, f -> {
            f.text(NAME, AGG_SEC_CREATE);
            f.list(SECURITY_EVENT_LISTENERS).add(SIZ_LOG_UPDATE).add(SYS_LOG_UPDATE);
        });
    }

    @Test
    public void tryCreate() {
        console.verticalNavigation().selectSecondary(LOGS_ITEM, AGGREGATE_SECURITY_EVENT_LISTENER_ITEM);        TableFragment table = page.getAggregateSecurityEventListenerTable();
        crud.createWithErrorAndCancelDialog(table, NAME, SECURITY_EVENT_LISTENERS);
    }

    @Test
    public void editSecurityEventListeners() throws Exception {
        console.verticalNavigation().selectSecondary(LOGS_ITEM, AGGREGATE_SECURITY_EVENT_LISTENER_ITEM);        TableFragment table = page.getAggregateSecurityEventListenerTable();
        FormFragment form = page.getAggregateSecurityEventListenerForm();
        table.bind(form);
        table.select(AGG_SEC_UPDATE);
        crud.update(aggregateSecurityEventListenerAddress(AGG_SEC_UPDATE), form,
                f -> f.list(SECURITY_EVENT_LISTENERS).add(PER_LOG_UPDATE),
                verify -> verify.verifyListAttributeContainsValue(SECURITY_EVENT_LISTENERS, PER_LOG_UPDATE));
    }

    @Test
    public void tryEditSecurityEventListeners() {
        console.verticalNavigation().selectSecondary(LOGS_ITEM, AGGREGATE_SECURITY_EVENT_LISTENER_ITEM);        TableFragment table = page.getAggregateSecurityEventListenerTable();
        FormFragment form = page.getAggregateSecurityEventListenerForm();
        table.bind(form);
        table.select(AGG_SEC_UPDATE);
        crud.updateWithError(form, f -> f.list(SECURITY_EVENT_LISTENERS).removeTags(), SECURITY_EVENT_LISTENERS);
    }

    @Test
    public void delete() throws Exception {
        console.verticalNavigation().selectSecondary(LOGS_ITEM, AGGREGATE_SECURITY_EVENT_LISTENER_ITEM);        TableFragment table = page.getAggregateSecurityEventListenerTable();
        crud.delete(aggregateSecurityEventListenerAddress(AGG_SEC_DELETE), table, AGG_SEC_DELETE);
    }
}
