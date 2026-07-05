package com.govn.framework.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.govn.framework.driver.DriverFactory;
import com.govn.framework.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Base64;

/**
 * ExtentReportListener – TestNG Listener tự động tạo HTML report bằng
 * ExtentReports 5.
 *
 * <p>
 * Được khai báo trong testng.xml nên không cần {@code @Listeners} annotation
 * trong test class.
 *
 * <p>
 * <b>Thread Safety:</b> ExtentReports v5 là thread-safe. Tuy nhiên, ExtentTest
 * KHÔNG
 * thread-safe, nên dùng {@link ThreadLocal} để mỗi thread có test instance
 * riêng.
 *
 * <p>
 * <b>Output:</b> Report được lưu tại
 * {@code target/extent-reports/AutomationReport.html}
 */
public class ExtentReportListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(ExtentReportListener.class);

    /** Singleton ExtentReports – thread-safe, dùng chung cho toàn bộ suite */
    private static ExtentReports extentReports;

    /** ThreadLocal ExtentTest – mỗi thread (test method) có instance riêng */
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    private static final String REPORT_PATH = "target/extent-reports/AutomationReport.html";

    // ═══════════════════════════════════════════════════════════════
    // Suite Level Events
    // ═══════════════════════════════════════════════════════════════

    @Override
    public synchronized void onStart(ITestContext context) {
        log.info("🚀 Khởi tạo ExtentReports cho suite: {}", context.getSuite().getName());
        extentReports = initExtentReports(context.getSuite().getName());
    }

    @Override
    public synchronized void onFinish(ITestContext context) {
        if (extentReports != null) {
            extentReports.flush();
            log.info("📊 ExtentReport đã flush. Xem báo cáo tại: {}", REPORT_PATH);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Test Method Level Events
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        String description = result.getMethod().getDescription();

        log.info("▶ [Extent] Test bắt đầu: {}", testName);

        ExtentTest test = extentReports.createTest(testName, description);
        test.assignCategory(result.getMethod().getGroups());
        extentTestThreadLocal.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ [Extent] Test PASSED: {}", getTestName(result));
        if (getCurrentTest() != null) {
            getCurrentTest().log(Status.PASS, "✅ Test passed thành công.");
        }
        extentTestThreadLocal.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ [Extent] Test FAILED: {}", getTestName(result));

        if (getCurrentTest() != null) {
            // Log exception
            getCurrentTest().log(Status.FAIL,
                    "❌ Test failed: " + result.getThrowable().getMessage());
            getCurrentTest().fail(result.getThrowable());

            // Đính kèm screenshot
            attachScreenshot(result);
        }
        extentTestThreadLocal.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭️ [Extent] Test SKIPPED: {}", getTestName(result));
        if (getCurrentTest() != null) {
            getCurrentTest().log(Status.SKIP,
                    "⏭️ Test bị bỏ qua: " + result.getThrowable().getMessage());
        }
        extentTestThreadLocal.remove();
    }

    // ═══════════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════════

    /** Khởi tạo và cấu hình ExtentReports với SparkReporter (HTML report). */
    private ExtentReports initExtentReports(String suiteName) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH);
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("GoVN Automation Report");
        sparkReporter.config().setReportName(suiteName);
        sparkReporter.config().setEncoding("UTF-8");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        reports.setSystemInfo("Project", "GoVN Automation Framework");
        reports.setSystemInfo("Environment", "https://gon-demo.pirago.work");
        reports.setSystemInfo("Java", System.getProperty("java.version"));
        reports.setSystemInfo("OS", System.getProperty("os.name"));
        return reports;
    }

    /** Chụp screenshot và đính kèm vào ExtentTest dưới dạng Base64. */
    private void attachScreenshot(ITestResult result) {
        try {
            WebDriver driver = DriverFactory.getDriver();
            byte[] screenshotBytes = ScreenshotUtils.captureAsBytes(driver);
            if (screenshotBytes != null && getCurrentTest() != null) {
                String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);
                getCurrentTest().fail("Screenshot khi fail:",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
            }
        } catch (Exception e) {
            log.warn("⚠️ Không thể đính kèm screenshot vào report: {}", e.getMessage());
        }
    }

    /** Tạo tên test đầy đủ dễ đọc: ClassName.methodName */
    private String getTestName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName()
                + " → " + result.getMethod().getMethodName();
    }

    /** Lấy ExtentTest của thread hiện tại một cách an toàn. */
    private static ExtentTest getCurrentTest() {
        return extentTestThreadLocal.get();
    }
}
