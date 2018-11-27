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
package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.stores.ldap.key.store;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.EmptyState;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.DIR_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_ITEM_ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_ITEM_PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_ITEM_RDN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_ITEM_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SEARCH_PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.Ids.ELYTRON_LDAP_KEY_STORE;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

@RunWith(Arquillian.class)
public class LDAPKeyStoreTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(dirContextAddress(DIR_UPDATE), Values.of(URL, ANY_STRING)).assertSuccess();
        Values ldapKsValues = Values.of(DIR_CONTEXT, DIR_UPDATE).and(SEARCH_PATH, ANY_STRING);
        ModelNode props = new ModelNode();
        props.get(NAME).set("p1");
        props.get(VALUE).add(Random.name());
        ModelNode newItemTemplate = new ModelNode();
        newItemTemplate.get(NEW_ITEM_PATH).set(ANY_STRING);
        newItemTemplate.get(NEW_ITEM_RDN).set(ANY_STRING);
        newItemTemplate.get(NEW_ITEM_ATTRIBUTES).add(props);
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_UPDATE), ldapKsValues).assertSuccess();
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_DELETE), ldapKsValues).assertSuccess();
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_TEMP1_UPDATE), ldapKsValues).assertSuccess();
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_TEMP2_DELETE), ldapKsValues).assertSuccess();
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_TEMP3_ADD), ldapKsValues).assertSuccess();
        operations.add(ldapKeyStoreAddress(LDAPKEY_ST_TEMP4_TRY_ADD), ldapKsValues).assertSuccess();
        operations.writeAttribute(ldapKeyStoreAddress(LDAPKEY_ST_TEMP1_UPDATE), NEW_ITEM_TEMPLATE, newItemTemplate).assertSuccess();
        operations.writeAttribute(ldapKeyStoreAddress(LDAPKEY_ST_TEMP2_DELETE), NEW_ITEM_TEMPLATE, newItemTemplate).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_DELETE));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_UPDATE));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_TEMP1_UPDATE));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_TEMP2_DELETE));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_TEMP3_ADD));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_TEMP4_TRY_ADD));
            operations.removeIfExists(ldapKeyStoreAddress(LDAPKEY_ST_CREATE));
            operations.removeIfExists(dirContextAddress(DIR_UPDATE));
        } finally {
            client.close();
        }
    }

    @Drone
    private WebDriver browser;

    @Page
    private ElytronOtherSettingsPage page;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Before
    public void navigate() {
        page.navigate();
    }

    @Test
    public void create() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        crud.create(ldapKeyStoreAddress(LDAPKEY_ST_CREATE), table, f -> {
            f.text(NAME, LDAPKEY_ST_CREATE);
            f.text(DIR_CONTEXT, DIR_UPDATE);
            f.text(SEARCH_PATH, ANY_STRING);
        });
    }

    @Test
    public void tryCreate() {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, LDAPKEY_ST_CREATE), DIR_CONTEXT);
    }

    @Test
    public void editFilterAlias() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        FormFragment form = page.getLdapKeyStoreForm();
        table.bind(form);
        table.select(LDAPKEY_ST_UPDATE);
        page.getLdapKeyStoreTab().select(Ids.build(ELYTRON_LDAP_KEY_STORE, TAB));

        crud.update(ldapKeyStoreAddress(LDAPKEY_ST_UPDATE), form, FILTER_ALIAS);
    }

    @Test
    public void delete() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();

        crud.delete(ldapKeyStoreAddress(LDAPKEY_ST_DELETE), table, LDAPKEY_ST_DELETE);
    }

    @Test
    public void tryAddNewItemTemplate() {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        FormFragment form = page.getLdapKeyStoreNewItemTemplateForm();
        table.bindBlank(form);
        table.select(LDAPKEY_ST_TEMP4_TRY_ADD);
        page.getLdapKeyStoreTab().select(Ids.build(ELYTRON_LDAP_KEY_STORE, NEW_ITEM_TEMPLATE, TAB));
        EmptyState emptyState = form.emptyState();
        emptyState.mainAction();
        AddResourceDialogFragment addResource = console.addResourceDialog();
        addResource.getForm().text(NEW_ITEM_PATH, ANY_STRING);
        addResource.getForm().text(NEW_ITEM_RDN, ANY_STRING);
        addResource.getPrimaryButton().click();
        try {
            addResource.getForm().expectError(NEW_ITEM_ATTRIBUTES);
        } finally {
            addResource.getSecondaryButton().click(); // close dialog to cleanup
        }
    }

    @Test
    public void addNewItemTemplate() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        FormFragment form = page.getLdapKeyStoreNewItemTemplateForm();
        table.bindBlank(form);
        table.select(LDAPKEY_ST_TEMP3_ADD);
        page.getLdapKeyStoreTab().select(Ids.build(ELYTRON_LDAP_KEY_STORE, NEW_ITEM_TEMPLATE, TAB));

        String rndName = "p1";
        String rndValue = Random.name();
        ModelNode props = new ModelNode();
        props.get(NAME).set(rndName);
        props.get(VALUE).add(rndValue);
        ModelNode newItemTemplate = new ModelNode();
        newItemTemplate.get(NEW_ITEM_PATH).set(ANY_STRING);
        newItemTemplate.get(NEW_ITEM_RDN).set(ANY_STRING);
        newItemTemplate.get(NEW_ITEM_ATTRIBUTES).add(props);

        crud.createSingleton(ldapKeyStoreAddress(LDAPKEY_ST_TEMP3_ADD), form, f -> {
            f.text(NEW_ITEM_PATH, ANY_STRING);
            f.text(NEW_ITEM_RDN, ANY_STRING);
            f.list(NEW_ITEM_ATTRIBUTES).add(rndName, rndValue);
        }, verifier -> verifier.verifyAttribute(NEW_ITEM_TEMPLATE, newItemTemplate));
    }

    @Test
    public void updateNewItemTemplate() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        FormFragment form = page.getLdapKeyStoreNewItemTemplateForm();
        table.bind(form);
        table.select(LDAPKEY_ST_TEMP1_UPDATE);
        page.getLdapKeyStoreTab().select(Ids.build(ELYTRON_LDAP_KEY_STORE, NEW_ITEM_TEMPLATE, TAB));

        String rndValue = Random.name();

        crud.update(ldapKeyStoreAddress(LDAPKEY_ST_TEMP1_UPDATE), form, f -> f.text(NEW_ITEM_RDN, rndValue),
            verifier -> verifier.verifyAttribute(NEW_ITEM_TEMPLATE + "." + NEW_ITEM_RDN, rndValue));
    }

    @Test
    public void removeNewItemTemplate() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, LDAP_KEY_STORE_ITEM);
        TableFragment table = page.getLdapKeyStoreTable();
        FormFragment form = page.getLdapKeyStoreNewItemTemplateForm();
        table.bind(form);
        table.select(LDAPKEY_ST_TEMP2_DELETE);
        page.getLdapKeyStoreTab().select(Ids.build(ELYTRON_LDAP_KEY_STORE, NEW_ITEM_TEMPLATE, TAB));

        crud.deleteSingleton(ldapKeyStoreAddress(LDAPKEY_ST_TEMP2_DELETE), form,
            verifier -> verifier.verifyAttributeIsUndefined(NEW_ITEM_TEMPLATE));
    }
}
