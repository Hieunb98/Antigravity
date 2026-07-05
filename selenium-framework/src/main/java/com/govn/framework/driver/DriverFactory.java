package com.govn.framework.driver;

import com.govn.framework.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * DriverFactory – Factory class quản lý WebDriver bằng ThreadLocal.
 *
 * <p><b>Tại sao dùng ThreadLocal?</b><br>
 * Khi TestNG chạy parallel (nhiều test method/class cùng lúc trên nhiều thread),
 * mỗi thread cần một instance WebDriver riêng biệt để tránh conflict session.
 * ThreadLocal đảm bảo mỗi thread chỉ thấy driver của chính nó – hoàn toàn thread-safe.
 *
 * <p><b>Nguyên lý áp dụng:</b>
 * <ul>
 *   <li><b>SRP</b>: Class chỉ lo việc tạo và quản lý WebDriver.</li>
 *   <li><b>OCP</b>: Dễ mở rộng thêm browser mới (Safari, Opera) mà không sửa code cũ.</li>
 *   <li><b>Factory Pattern</b>: Tách logic tạo đối tượng khỏi nơi sử dụng.</li>
 * </ul>
 */
public final class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    /**
     * ThreadLocal chứa WebDriver riêng cho từng thread.
     * InheritableThreadLocal KHÔNG được dùng ở đây để tránh rò rỉ session.
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Utility class – không cho phép khởi tạo
    private DriverFactory() {
        throw new UnsupportedOperationException("DriverFactory là utility class, không thể khởi tạo.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Khởi tạo WebDriver cho thread hiện tại và lưu vào ThreadLocal.
     * Nếu driver đã tồn tại, phương thức này sẽ không tạo lại.
     *
     * @param browser   tên browser (chrome | firefox | edge)
     * @param headless  true để chạy không có giao diện (CI mode)
     */
    public static void initDriver(String browser, boolean headless) {
        if (driverThreadLocal.get() != null) {
            log.warn("⚠️ Driver đã tồn tại trên thread {}. Bỏ qua việc khởi tạo lại.",
                    Thread.currentThread().getName());
            return;
        }

        log.info("🚀 Khởi tạo {} driver trên thread: {} (headless={})",
                browser, Thread.currentThread().getName(), headless);

        WebDriver driver = createDriver(browser, headless);
        configureDriver(driver);
        driverThreadLocal.set(driver);

        log.info("✅ Driver khởi tạo thành công trên thread: {}", Thread.currentThread().getName());
    }

    /**
     * Lấy WebDriver của thread hiện tại.
     *
     * @return WebDriver instance
     * @throws IllegalStateException nếu driver chưa được khởi tạo
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

    /**
     * Đóng browser và xóa driver khỏi ThreadLocal để tránh memory leak.
     * Phải được gọi trong @AfterMethod/@AfterClass của BaseTest.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            log.info("🔴 Đóng driver trên thread: {}", Thread.currentThread().getName());
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("⚠️ Lỗi khi đóng driver: {}", e.getMessage());
            } finally {
                // QUAN TRỌNG: Luôn xóa khỏi ThreadLocal dù quit() có lỗi hay không
                driverThreadLocal.remove();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Private Helper Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Factory method tạo WebDriver theo tên browser.
     */
    private static WebDriver createDriver(String browser, boolean headless) {
        return switch (browser.toLowerCase().trim()) {
            case "chrome"  -> createChromeDriver(headless);
            case "firefox" -> createFirefoxDriver(headless);
            case "edge"    -> createEdgeDriver(headless);
            default -> throw new IllegalArgumentException(
                    "Browser không được hỗ trợ: '" + browser
                    + "'. Chỉ chấp nhận: chrome, firefox, edge.");
        };
    }

    /** Khởi tạo ChromeDriver với các options tối ưu cho CI/CD. */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        applyCommonChromeOptions(options, headless);
        return new ChromeDriver(options);
    }

    /** Khởi tạo FirefoxDriver. */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return new FirefoxDriver(options);
    }

    /** Khởi tạo EdgeDriver. */
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");
        return new EdgeDriver(options);
    }

    /**
     * Áp dụng các Chrome options phổ biến.
     * "--headless=new" sử dụng headless mode mới của Chrome 112+ (ổn định hơn cũ).
     */
    private static void applyCommonChromeOptions(ChromeOptions options, boolean headless) {
        if (headless) {
            options.addArguments("--headless=new");  // Chrome 112+ headless mode
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--no-sandbox",                    // Bắt buộc cho Docker/Linux CI
                "--disable-dev-shm-usage",         // Tránh lỗi OOM trong Docker
                "--disable-gpu",                   // Ổn định hơn trong CI
                "--disable-extensions",
                "--disable-popup-blocking",
                "--ignore-certificate-errors",
                "--remote-allow-origins=*"         // Cần thiết cho Selenium 4 CDP
        );
        // Tắt thông báo "Chrome is being controlled by automated software"
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
    }

    /**
     * Cấu hình timeout cho driver sau khi khởi tạo.
     */
    private static void configureDriver(WebDriver driver) {
        ConfigReader config = ConfigReader.getInstance();
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(config.getPageLoadTimeout())
        );
        // Implicit wait đặt = 0 để dùng hoàn toàn Explicit Wait (best practice)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().window().maximize();
    }
}
