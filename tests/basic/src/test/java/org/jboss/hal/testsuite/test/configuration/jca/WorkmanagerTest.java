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
package org.jboss.hal.testsuite.test.configuration.jca;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.JcaPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.testsuite.test.configuration.jca.JcaFixtures.WM_CREATE;
import static org.jboss.hal.testsuite.test.configuration.jca.JcaFixtures.WM_DELETE;
import static org.jboss.hal.testsuite.test.configuration.jca.JcaFixtures.WM_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.jca.JcaFixtures.workmanagerAddress;

@RunWith(Arquillian.class)
public class WorkmanagerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(workmanagerAddress(WM_UPDATE), Values.of(NAME, WM_UPDATE));
        operations.add(workmanagerAddress(WM_DELETE), Values.of(NAME, WM_DELETE));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        operations.removeIfExists(workmanagerAddress(WM_CREATE));
        operations.removeIfExists(workmanagerAddress(WM_UPDATE));
        operations.removeIfExists(workmanagerAddress(WM_DELETE));
    }

    @Inject private Console console;
    @Inject private CrudOperations crud;
    @Page private JcaPage page;
    private TableFragment table;

    @Before
    public void setUp() throws Exception {
        page.navigate();
        console.verticalNavigation().selectPrimary(Ids.JCA_WORKMANAGER_ITEM);

        table = page.getWmTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(workmanagerAddress(WM_CREATE), table, WM_CREATE);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(workmanagerAddress(WM_DELETE), table, WM_DELETE);
    }
}
