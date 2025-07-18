package org.example.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class JunitReporter implements TestWatcher, BeforeAllCallback, BeforeEachCallback {

    private ExtentTest test;
    private ExtentReports extent;
    private static ExtentSparkReporter reporter;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        reporter = new ExtentSparkReporter("target/extent-report.html");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        test = extent.createTest(extensionContext.getDisplayName());
        test.info("Test - " + test.getModel().getFullName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        test.fail("Fail: " + cause);
        extent.flush();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        test.pass("Pass");
        extent.flush();
    }
}
