package com.diginest.aireceptionist;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    com.diginest.aireceptionist.controller.AuthControllerIntegrationTest.class,
    com.diginest.aireceptionist.controller.BookingControllerIntegrationTest.class,
    com.diginest.aireceptionist.controller.UsageControllerIntegrationTest.class
})
public class IntegrationTestSuite {
}
