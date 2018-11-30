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
package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.trust.manager;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.Ids.ELYTRON_TRUST_MANAGER;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

@RunWith(Arquillian.class)
public class TrustManagerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values ksParams = Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credRef);
        operations.add(keyStoreAddress(KEY_ST_UPDATE), ksParams).assertSuccess();
        operations.add(trustManagerAddress(TRU_MAN_UPDATE), Values.of(KEY_STORE, KEY_ST_UPDATE)).assertSuccess();
        operations.add(trustManagerAddress(TRU_MAN_DELETE), Values.of(KEY_STORE, KEY_ST_UPDATE)).assertSuccess();
        Values trustParams = Values.of(KEY_STORE, KEY_ST_UPDATE).andObject(CERTIFICATE_REVOCATION_LIST,
            Values.of(PATH, "${jboss.server.config.dir}/logging.properties"));
        operations.add(trustManagerAddress(TRU_MAN_CRL_CRT), Values.of(KEY_STORE, KEY_ST_UPDATE)).assertSuccess();
        operations.add(trustManagerAddress(TRU_MAN_CRL_UPD), trustParams).assertSuccess();
        operations.add(trustManagerAddress(TRU_MAN_CRL_DEL), trustParams).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(trustManagerAddress(TRU_MAN_UPDATE));
            operations.removeIfExists(trustManagerAddress(TRU_MAN_CREATE));
            operations.removeIfExists(trustManagerAddress(TRU_MAN_DELETE));
            operations.removeIfExists(trustManagerAddress(TRU_MAN_CRL_CRT));
            operations.removeIfExists(trustManagerAddress(TRU_MAN_CRL_UPD));
            operations.removeIfExists(trustManagerAddress(TRU_MAN_CRL_DEL));
            operations.removeIfExists(keyStoreAddress(KEY_ST_UPDATE));
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

    private TableFragment table;

    @Before
    public void before() {
        page.navigate();
        console.verticalNavigation().selectSecondary(SSL_ITEM, TRUST_MANAGER_ITEM);
        table = page.getTrustManagerTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(trustManagerAddress(TRU_MAN_CREATE), table, f -> {
            f.text(NAME, TRU_MAN_CREATE);
            f.text(KEY_STORE, KEY_ST_UPDATE);
        });
    }

    @Test
    public void tryCreate() {
        crud.createWithErrorAndCancelDialog(table, TRU_MAN_CREATE, KEY_STORE);
    }

    @Test
    public void editProviderName() throws Exception {
        FormFragment form = page.getTrustManagerForm();
        table.bind(form);
        table.select(TRU_MAN_UPDATE);
        page.getTrustManagerTab().select(Ids.build(ELYTRON_TRUST_MANAGER, ATTRIBUTES, TAB));
        crud.update(trustManagerAddress(TRU_MAN_UPDATE), form, f -> f.text(PROVIDER_NAME, ANY_STRING),
            verify -> verify.verifyAttribute(PROVIDER_NAME, ANY_STRING));
    }

    @Test
    public void addCertificationRevocationList() throws Exception {
        FormFragment form = page.getTrustManagerCertificateRevocationListForm();
        table.bind(form);
        table.select(TRU_MAN_CRL_CRT);
        page.getTrustManagerTab().select(Ids.build(ELYTRON_TRUST_MANAGER, CERTIFICATE_REVOCATION_LIST, TAB));
        form.emptyState().mainAction();
        console.verifySuccess();
        // the UI "add" operation adds a certificate-revocation-list with no inner attributes, as they are not required
        ModelNodeResult actualResult = operations.readAttribute(trustManagerAddress(TRU_MAN_CRL_CRT),
            CERTIFICATE_REVOCATION_LIST);
        Assert.assertTrue("attribute certificate-revocation-list should exist", actualResult.get(RESULT).isDefined());
    }

    @Test
    public void editCertificateRevocationList() throws Exception {
        FormFragment form = page.getTrustManagerCertificateRevocationListForm();
        table.bind(form);
        table.select(TRU_MAN_CRL_UPD);
        page.getTrustManagerTab().select(Ids.build(ELYTRON_TRUST_MANAGER, CERTIFICATE_REVOCATION_LIST, TAB));
        crud.update(trustManagerAddress(TRU_MAN_CRL_UPD), form, f -> f.text(PATH, ANY_STRING),
            verify -> verify.verifyAttribute(CERTIFICATE_REVOCATION_LIST + "." + PATH, ANY_STRING));
    }

    @Test
    public void deleteCertificateRevocationList() throws Exception {
        FormFragment form = page.getTrustManagerCertificateRevocationListForm();
        table.bind(form);
        table.select(TRU_MAN_CRL_DEL);
        page.getTrustManagerTab().select(Ids.build(ELYTRON_TRUST_MANAGER, CERTIFICATE_REVOCATION_LIST, TAB));
        crud.deleteSingleton(trustManagerAddress(TRU_MAN_CRL_DEL), form,
            verify -> verify.verifyAttributeIsUndefined(CERTIFICATE_REVOCATION_LIST));
    }

    @Test
    public void delete() throws Exception {
        crud.delete(trustManagerAddress(TRU_MAN_DELETE), table, TRU_MAN_DELETE);
    }
}
