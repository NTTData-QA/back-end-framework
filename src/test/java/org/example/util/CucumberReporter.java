package org.example.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.cucumber.java.*;


public class CucumberReporter {

    private ExtentTest test;
    private static final ExtentReports extent = new ExtentReports();
    private static final ExtentSparkReporter reporter = new ExtentSparkReporter("target/extent-report.html");

    private static String testName;

    @BeforeAll
    public static void beforeAll() {
        extent.attachReporter(reporter);
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        test = extent.createTest(scenario.getName());
        String testName = test.getModel().getName();
        test.info("Test - " + testName);
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.getStatus().equals(Status.FAILED)) {
            test.fail("Fail: " + scenario.getName());
            extent.flush();
        } else {
            test.pass("Pass");
            extent.flush();
        }
    }
}
