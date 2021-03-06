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
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.page.configuration.JcaPage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.testsuite.test.configuration.jca.JcaFixtures.TRACER_ADDRESS;

@RunWith(Arquillian.class)
public class ModifyTracerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @Inject private Console console;
    @Inject private CrudOperations crud;
    @Page private JcaPage page;
    private FormFragment form;

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (!operations.exists(TRACER_ADDRESS)) {
            operations.add(TRACER_ADDRESS);
        }
    }

    @Before
    public void setUp() throws Exception {
        page.navigate();
        console.verticalNavigation().selectPrimary(Ids.JCA_TRACER_ITEM);
        form = page.getTracerForm();
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void update() throws Exception {
        crud.update(TRACER_ADDRESS, form, ENABLED, true);
    }

    @Test
    public void reset() throws Exception {
        crud.reset(TRACER_ADDRESS, form);
    }
}
