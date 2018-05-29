package org.jboss.hal.testsuite.test;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.page.HomePage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
public class SampleTest {

    @Drone
    private WebDriver browser;

    @Page
    private HomePage page;

    @Test
    public void test() {
        page.navigate();
        Assert.assertTrue(true);
    }

}
