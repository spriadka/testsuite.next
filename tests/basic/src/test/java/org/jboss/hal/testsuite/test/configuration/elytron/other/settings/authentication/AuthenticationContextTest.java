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
package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.authentication;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
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

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

@RunWith(Arquillian.class)
public class AuthenticationContextTest {

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(authenticationContextAddress(AUT_CT_DELETE)).assertSuccess();
        operations.add(authenticationContextAddress(AUT_CT_UPDATE)).assertSuccess();
        ModelNode matchRuleUpdate = new ModelNode();
        matchRuleUpdate.get(MATCH_ABSTRACT_TYPE).set(AUT_CT_MR_UPDATE);
        ModelNode matchRuleDelete = new ModelNode();
        matchRuleDelete.get(MATCH_ABSTRACT_TYPE).set(AUT_CT_MR_DELETE);
        operations.add(authenticationContextAddress(AUT_CT_UPDATE2),
            Values.ofList(MATCH_RULES, matchRuleUpdate, matchRuleDelete)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(authenticationContextAddress(AUT_CT_UPDATE));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_UPDATE2));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_DELETE));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_CREATE));
        } finally {
            client.close();
        }
    }

    @Page
    private ElytronOtherSettingsPage page;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Drone
    private WebDriver browser;

    @Before
    public void navigate() {
        page.navigate();
        console.verticalNavigation().selectSecondary(AUTHENTICATION_ITEM, AUTHENTICATION_CONTEXT_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getAuthenticationContextTable();
        crud.create(authenticationContextAddress(AUT_CT_CREATE), table, AUT_CT_CREATE);
    }

    @Test
    public void editExtends() throws Exception {
        TableFragment table = page.getAuthenticationContextTable();
        FormFragment form = page.getAuthenticationContextForm();
        table.bind(form);
        table.select(AUT_CT_UPDATE);
        crud.update(authenticationContextAddress(AUT_CT_UPDATE), form, EXTENDS, AUT_CT_UPDATE2);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getAuthenticationContextTable();
        crud.delete(authenticationContextAddress(AUT_CT_DELETE), table, AUT_CT_DELETE);
    }

    @Test
    public void addMatchRules() throws Exception {
        TableFragment autTable = page.getAuthenticationContextTable();
        TableFragment table = page.getAuthenticationContextMatchRulesTable();

        autTable.action(AUT_CT_UPDATE, MATCH_RULES_TITLE);
        waitGui().until().element(table.getRoot()).is().visible();

        try {
            crud.create(authenticationContextAddress(AUT_CT_UPDATE), table,
                    f -> f.text(MATCH_ABSTRACT_TYPE, AUT_CT_MR_CREATE),
                    vc -> vc.verifyListAttributeContainsSingleValue(MATCH_RULES, MATCH_ABSTRACT_TYPE, AUT_CT_MR_CREATE));
        } finally {
            // getting rid of action selection
            page.getAuthenticationContextPages().breadcrumb().getBackToMainPage();
        }
    }

    @Test
    public void editMatchRules() throws Exception {
        TableFragment autTable = page.getAuthenticationContextTable();
        TableFragment table = page.getAuthenticationContextMatchRulesTable();
        FormFragment form = page.getAuthenticationContextMatchRulesForm();
        table.bind(form);

        autTable.action(AUT_CT_UPDATE2, MATCH_RULES_TITLE);
        waitGui().until().element(table.getRoot()).is().visible();

        table.select(AUT_CT_MR_UPDATE);
        try {
            crud.update(authenticationContextAddress(AUT_CT_UPDATE2), form, f -> f.text(MATCH_HOST, ANY_STRING),
                    vc -> vc.verifyListAttributeContainsSingleValue(MATCH_RULES, MATCH_HOST, ANY_STRING));
        } finally {
            // getting rid of action selection
            page.getAuthenticationContextPages().breadcrumb().getBackToMainPage();
        }
    }

    @Test
    public void deleteMatchRules() throws Exception {
        TableFragment autTable = page.getAuthenticationContextTable();
        TableFragment table = page.getAuthenticationContextMatchRulesTable();

        autTable.action(AUT_CT_UPDATE2, MATCH_RULES_TITLE);
        waitGui().until().element(table.getRoot()).is().visible();

        try {
            crud.delete(authenticationContextAddress(AUT_CT_UPDATE2), table, AUT_CT_MR_DELETE,
                    vc -> vc.verifyListAttributeDoesNotContainSingleValue(MATCH_RULES, MATCH_ABSTRACT_TYPE,
                            AUT_CT_MR_DELETE));
        } finally {
            // getting rid of action selection
            page.getAuthenticationContextPages().breadcrumb().getBackToMainPage();
        }
    }
}
