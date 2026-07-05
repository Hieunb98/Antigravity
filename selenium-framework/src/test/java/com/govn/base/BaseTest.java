package com.govn.base;

import com.govn.framework.config.ConfigReader;
import com.govn.framework.driver.DriverFactory;
import com.govn.framework.utils.ScreenshotUtils;
import com.govn.framework.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * BaseTest – Lớp cha trừu tượng cho tất cả Test class.
 *
 * <p><b>Trách nhiệm:</b>
 * <ol>
 *   <li>Khởi tạo WebDriver cho mỗi test method thông qua {@link DriverFactory} (ThreadLocal).</li>
 *   <li>Cấu hình browser và navigate đến URL ban đầu.</li>
 *   <li>Dọn dẹp: đóng browser sau mỗi test method.</li>
 *   <li>Chụp screenshot tự động khi test FAIL.</li>
 * </ol>
 *
 * <p><b>Tại sao @BeforeMethod/@AfterMethod thay vì @BeforeClass?</b><br>
 * Khi chạy parallel ở cấp độ "methods", mỗi method chạy trên một thread riêng.
 * @BeforeMethod đảm bảo mỗi test method có một browser riêng hoàn toàn độc lập,
 * không bị ảnh hưởng bởi state của test trước đó.
 *
 * <p><b>Nguyên lý SOLID áp dụng:</b>
 * <ul>
 *   <li><b>SRP</b>: BaseTest chỉ lo setup/teardown, không chứa logic test.</li>
 *   <li><b>OCP</b>: Test class kế thừa có thể override các method setup nếu cần.</li>
 *   <li><b>LSP</b>: Mọi subclass đều có thể dùng {@code getDriver()} an toàn.</li>
 * </ul>
 */
public abstract class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);
    protected final ConfigReader config = ConfigReader.getInstance();

    // ═══════════════════════════════════════════════════════════════
    //  Setup: Trước mỗi test method
    // ═══════════════════════════════════════════════════════════════

    /**
     * Khởi tạo WebDriver và navigate đến trang Login.
     *
     * <p>Nhận tham số từ testng.xml:
     * <ul>
     *   <li>{@code browser}: tên browser (chrome | firefox | edge), mặc định "chrome"</li>
     *   <li>{@code headless}: "true" để chạy headless (CI mode), mặc định "false"</li>
     * </ul>
     *
     * <p>@Optional cho phép test chạy ngay cả khi testng.xml không khai báo tham số.
     */
    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(
            @Optional("chrome") String browser,
            @Optional("false")  String headless) {

        // Ưu tiên System property từ CLI/CI; nếu không có thì dùng param từ testng.xml
        String resolvedBrowser  = System.getProperty("browser",  browser);
        String resolvedHeadless = System.getProperty("headless", headless);

        log.info("═══════════════════════════════════════════════════");
        log.info("▶ Setup test trên thread: {}", Thread.currentThread().getName());
        log.info("   Browser  : {}", resolvedBrowser);
        log.info("   Headless : {}", resolvedHeadless);
        log.info("═══════════════════════════════════════════════════");

        // Khởi tạo driver thông qua DriverFactory (ThreadLocal)
        DriverFactory.initDriver(resolvedBrowser, Boolean.parseBoolean(resolvedHeadless));

        // Navigate đến URL ban đầu và chờ trang tải xong
        getDriver().get(config.getLoginUrl());
        WaitUtils.waitForPageReady(getDriver());

        log.info("✅ Setup hoàn tất. Đang ở URL: {}", getDriver().getCurrentUrl());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Teardown: Sau mỗi test method
    // ═══════════════════════════════════════════════════════════════

    /**
     * Dọn dẹp sau mỗi test method:
     * <ol>
     *   <li>Chụp screenshot nếu test FAIL.</li>
     *   <li>Đóng browser và giải phóng ThreadLocal.</li>
     * </ol>
     *
     * <p>{@code alwaysRun = true} đảm bảo teardown luôn được chạy,
     * ngay cả khi test throw Exception.
     *
     * @param result kết quả của test method vừa chạy (inject bởi TestNG)
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        log.info("◀ Teardown test: [{}] | Status: {}",
                testName, getStatusName(result.getStatus()));

        // Chụp screenshot tự động khi test FAIL
        if (result.getStatus() == ITestResult.FAILURE) {
            log.warn("⚠️ Test FAILED – Chụp screenshot...");
            try {
                String screenshotPath = ScreenshotUtils.captureAndSave(getDriver(), testName);
                if (screenshotPath != null) {
                    log.info("📸 Screenshot: {}", screenshotPath);
                }
            } catch (Exception e) {
                log.error("Không thể chụp screenshot: {}", e.getMessage());
            }
        }

        // Đóng driver và xóa khỏi ThreadLocal
        DriverFactory.quitDriver();
        log.info("═══════════════════════════════════════════════════\n");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Protected API – dành cho Test classes kế thừa
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy WebDriver của thread hiện tại.
     * Được sử dụng trong các subclass và Page Object.
     *
     * @return WebDriver instance của thread hiện tại
     */
    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════════════════

    /** Chuyển mã status của TestNG thành chuỗi dễ đọc. */
    private String getStatusName(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "✅ PASSED";
            case ITestResult.FAILURE -> "❌ FAILED";
            case ITestResult.SKIP    -> "⏭️ SKIPPED";
            default -> "UNKNOWN";
        };
    }
}
