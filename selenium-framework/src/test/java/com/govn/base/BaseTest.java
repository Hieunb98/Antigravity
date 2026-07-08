package com.govn.base;

import com.govn.framework.config.ConfigReader;
import com.govn.framework.utils.ScreenshotUtils;
import com.govn.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import com.govn.framework.utils.LogUtils;

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

    protected final ConfigReader config = ConfigReader.getInstance();
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

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

        LogUtils.info("═══════════════════════════════════════════════════");
        LogUtils.info("▶ Setup test trên thread: {}", Thread.currentThread().getName());
        LogUtils.info("   Browser  : {}", resolvedBrowser);
        LogUtils.info("   Headless : {}", resolvedHeadless);
        LogUtils.info("═══════════════════════════════════════════════════");

        // Khởi tạo driver trực tiếp
        if (driverThreadLocal.get() != null) {
            LogUtils.warn("⚠️ Driver đã tồn tại trên thread {}. Bỏ qua việc khởi tạo lại.",
                    Thread.currentThread().getName());
        } else {
            LogUtils.info("🚀 Khởi tạo {} driver trên thread: {} (headless={})",
                    resolvedBrowser, Thread.currentThread().getName(), resolvedHeadless);
            WebDriver driver = createDriver(resolvedBrowser, Boolean.parseBoolean(resolvedHeadless));
            configureDriver(driver);
            driverThreadLocal.set(driver);
            LogUtils.info("✅ Driver khởi tạo thành công trên thread: {}", Thread.currentThread().getName());
        }

        // Navigate đến URL ban đầu và chờ trang tải xong
        getDriver().get(config.getLoginUrl());
        WaitUtils.waitForPageReady(getDriver());

        LogUtils.info("✅ Setup hoàn tất. Đang ở URL: {}", getDriver().getCurrentUrl());
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

        LogUtils.info("◀ Teardown test: [{}] | Status: {}",
                testName, getStatusName(result.getStatus()));

        // Chụp screenshot tự động khi test FAIL
        if (result.getStatus() == ITestResult.FAILURE) {
            LogUtils.warn("⚠️ Test FAILED – Chụp screenshot...");
            try {
                String screenshotPath = ScreenshotUtils.captureAndSave(getDriver(), testName);
                if (screenshotPath != null) {
                    LogUtils.info("📸 Screenshot: {}", screenshotPath);
                }
            } catch (Exception e) {
                LogUtils.error("Không thể chụp screenshot: {}", e.getMessage());
            }
        }

        // Đóng driver trực tiếp và xóa khỏi ThreadLocal
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            LogUtils.info("🔴 Đóng driver trên thread: {}", Thread.currentThread().getName());
            try {
                driver.quit();
            } catch (Exception e) {
                LogUtils.warn("⚠️ Lỗi khi đóng driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
        LogUtils.info("═══════════════════════════════════════════════════\n");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Protected API – dành cho Test classes kế thừa
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy WebDriver của thread hiện tại.
     * Được sử dụng trong các subclass, Page Object và Listeners.
     *
     * @return WebDriver instance của thread hiện tại
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver chưa được khởi tạo trên thread: "
                    + Thread.currentThread().getName()
                    + ". Hãy gọi initDriver() trước.");
        }
        return driver;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Driver Creation & Configuration Helpers
    // ═══════════════════════════════════════════════════════════════

    private WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase().trim()) {
            case "chrome"  -> createChromeDriver(headless);
            case "firefox" -> createFirefoxDriver(headless);
            case "edge"    -> createEdgeDriver(headless);
            default -> throw new IllegalArgumentException(
                    "Browser không được hỗ trợ: '" + browser
                    + "'. Chỉ chấp nhận: chrome, firefox, edge.");
        };
    }

    private WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        applyCommonChromeOptions(options, headless);
        return new ChromeDriver(options);
    }

    private WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return new FirefoxDriver(options);
    }

    private WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");
        return new EdgeDriver(options);
    }

    private void applyCommonChromeOptions(ChromeOptions options, boolean headless) {
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-popup-blocking",
                "--ignore-certificate-errors",
                "--remote-allow-origins=*"
        );
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
    }

    private void configureDriver(WebDriver driver) {
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(config.getPageLoadTimeout())
        );
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().maximize();
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
