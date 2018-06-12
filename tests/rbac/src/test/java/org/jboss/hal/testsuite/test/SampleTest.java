package org.jboss.hal.testsuite.test;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Authentication;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.RbacRole;
import org.jboss.hal.testsuite.page.HomePage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
public class SampleTest {

    @Drone
    private WebDriver browser;

    @Page
    private HomePage page;

    @Inject
    private Console console;

    @Inject
    private Authentication authentication;

    @Test
    public void test() {
        authentication.with(browser).authenticate(RbacRole.DEPLOYER);
        page.navigate();
    }

}
