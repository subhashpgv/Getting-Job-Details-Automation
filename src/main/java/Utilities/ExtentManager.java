package Utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.IOException;

public class ExtentManager {
    private static ExtentReports extentReports;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    // Initialize the report
    public synchronized static void initReport(String reportPath) {
        if (extentReports == null) {
            extentReports = new ExtentReports();
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("Automation Test Report");
            sparkReporter.config().setReportName("Test Execution Report");
            extentReports.attachReporter(sparkReporter);
        }
    }

    // Start a new test
    public synchronized static void startTest(String testName, String description) {
        ExtentTest test = extentReports.createTest(testName, description);
        extentTest.set(test);
    }

    // Get the current test
    public synchronized static ExtentTest getTest() {
        return extentTest.get();
    }

    // Flush the report
    public synchronized static void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    // Add a screenshot to the report
    public synchronized static void addScreenshot(String screenshotPath) {
        if (extentTest.get() != null && screenshotPath != null) {
            extentTest.get().addScreenCaptureFromPath(screenshotPath);
        }
    }
}
