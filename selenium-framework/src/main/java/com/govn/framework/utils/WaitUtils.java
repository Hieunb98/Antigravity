package com.govn.framework.utils;

import com.govn.framework.config.ConfigReader;
import com.govn.framework.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * WaitUtils – Tập trung toàn bộ logic chờ đợi (Explicit Wait / Smart Wait).
 *
 * <p><b>Nguyên lý thiết kế:</b>
 * <ul>
 *   <li><b>SRP</b>: Class này CHỈ chịu trách nhiệm quản lý các cơ chế chờ đợi.
 *       Không chứa logic click, sendKeys, hay bất kỳ hành động nào khác.</li>
 *   <li><b>Tuyệt đối KHÔNG dùng Thread.sleep()</b>: Mọi wait đều dựa trên
 *       {@link WebDriverWait} với {@link ExpectedConditions} – còn gọi là "Smart Wait".</li>
 *   <li><b>Stale Element Resilience</b>: Một số method xử lý {@link StaleElementReferenceException}
 *       để tăng độ ổn định khi DOM thay đổi (SPA/dynamic content).</li>
 * </ul>
 *
 * <p><b>Cách sử dụng từ BasePage/Page Object:</b>
 * <pre>{@code
 *   WaitUtils.waitForVisible(driver, By.cssSelector(".login-btn"));
 *   WebElement el = WaitUtils.waitForClickable(driver, By.id("submit"));
 * }</pre>
 */
public final class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = ConfigReader.getInstance().getExplicitWaitTimeout();

    // Utility class – không cho phép khởi tạo
    private WaitUtils() {}

    // ═══════════════════════════════════════════════════════════════
    //  1. Visibility Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ cho đến khi element xác định bằng {@link By} trở nên hiển thị.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return WebElement đã hiển thị
     */
    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return waitForVisible(driver, locator, DEFAULT_TIMEOUT);
    }

    /**
     * Chờ element hiển thị với timeout tùy chỉnh.
     *
     * @param driver          WebDriver instance
     * @param locator         By locator
     * @param timeoutSeconds  timeout (giây)
     * @return WebElement đã hiển thị
     */
    public static WebElement waitForVisible(WebDriver driver, By locator, int timeoutSeconds) {
        log.debug("⏳ Chờ element hiển thị [{}] (timeout: {}s)", locator, timeoutSeconds);
        return createWait(driver, timeoutSeconds)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Chờ cho đến khi một {@link WebElement} đã có sẵn trở nên hiển thị.
     * Dùng khi đã có tham chiếu đến element (ví dụ qua {@code @FindBy}).
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần chờ
     * @return WebElement đã hiển thị
     */
    public static WebElement waitForVisible(WebDriver driver, WebElement element) {
        log.debug("⏳ Chờ element [{}] hiển thị", element);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Chờ danh sách elements tất cả đều hiển thị.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return List WebElement đã hiển thị
     */
    public static List<WebElement> waitForAllVisible(WebDriver driver, By locator) {
        log.debug("⏳ Chờ tất cả elements hiển thị [{}]", locator);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. Clickability Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ element có thể click được (visible + enabled).
     * Đây là wait phổ biến nhất trước khi thực hiện hành động click.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return WebElement đã sẵn sàng để click
     */
    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, DEFAULT_TIMEOUT);
    }

    /**
     * Chờ element có thể click được với timeout tùy chỉnh.
     */
    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        log.debug("⏳ Chờ element clickable [{}] (timeout: {}s)", locator, timeoutSeconds);
        return createWait(driver, timeoutSeconds)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Chờ một WebElement (đã có tham chiếu) có thể click được.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần chờ
     * @return WebElement đã sẵn sàng để click
     */
    public static WebElement waitForClickable(WebDriver driver, WebElement element) {
        log.debug("⏳ Chờ element clickable [{}]", element);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. Presence Waits (element có trong DOM nhưng chưa nhất thiết visible)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ element xuất hiện trong DOM (không cần visible).
     * Hữu ích khi cần kiểm tra sự tồn tại của element ẩn.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return WebElement đã có trong DOM
     */
    public static WebElement waitForPresence(WebDriver driver, By locator) {
        log.debug("⏳ Chờ element có mặt trong DOM [{}]", locator);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ═══════════════════════════════════════════════════════════════
    //  4. Invisibility Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ element biến mất khỏi giao diện.
     * Hữu ích sau khi thực hiện action và chờ loading spinner ẩn đi.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return true nếu element đã biến mất
     */
    public static boolean waitForInvisible(WebDriver driver, By locator) {
        log.debug("⏳ Chờ element ẩn đi [{}]", locator);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ═══════════════════════════════════════════════════════════════
    //  5. URL / Title Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ URL hiện tại chứa chuỗi cụ thể.
     * Dùng để xác minh navigation sau khi đăng nhập / redirect.
     *
     * @param driver      WebDriver instance
     * @param urlFragment chuỗi con cần xuất hiện trong URL
     * @return true nếu URL chứa chuỗi cần tìm
     */
    public static boolean waitForUrlContains(WebDriver driver, String urlFragment) {
        log.debug("⏳ Chờ URL chứa: '{}'", urlFragment);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.urlContains(urlFragment));
    }

    /**
     * Chờ title của trang chứa chuỗi cụ thể.
     *
     * @param driver         WebDriver instance
     * @param titleFragment  chuỗi con cần xuất hiện trong title
     * @return true nếu title chứa chuỗi cần tìm
     */
    public static boolean waitForTitleContains(WebDriver driver, String titleFragment) {
        log.debug("⏳ Chờ title chứa: '{}'", titleFragment);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.titleContains(titleFragment));
    }

    // ═══════════════════════════════════════════════════════════════
    //  6. Text Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ text cụ thể xuất hiện trong element (so sánh exact match).
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @param text    text cần chờ
     * @return true nếu text xuất hiện trong element
     */
    public static boolean waitForTextPresent(WebDriver driver, By locator, String text) {
        log.debug("⏳ Chờ text '{}' xuất hiện trong element [{}]", text, locator);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ═══════════════════════════════════════════════════════════════
    //  7. Page Ready Wait (DOM + AJAX)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ trang hoàn thành tải (document.readyState == 'complete').
     * Hữu ích sau khi navigate hoặc refresh trang.
     *
     * @param driver WebDriver instance
     */
    public static void waitForPageReady(WebDriver driver) {
        log.debug("⏳ Chờ trang tải xong (readyState = complete)...");
        createWait(driver, DEFAULT_TIMEOUT).until((ExpectedCondition<Boolean>) d -> {
            String readyState = (String) ((JavascriptExecutor) d)
                    .executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
        log.debug("✅ Trang đã tải xong.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  8. Attribute / CSS Waits
    // ═══════════════════════════════════════════════════════════════

    /**
     * Chờ attribute của element chứa giá trị cụ thể.
     * Ví dụ: chờ type="text" để xác nhận mật khẩu đã được hiển thị.
     *
     * @param driver         WebDriver instance
     * @param locator        By locator
     * @param attributeName  tên attribute
     * @param value          giá trị cần kiểm tra
     * @return true nếu attribute chứa giá trị
     */
    public static boolean waitForAttributeContains(WebDriver driver, By locator,
                                                    String attributeName, String value) {
        log.debug("⏳ Chờ attribute '{}' của [{}] chứa '{}'", attributeName, locator, value);
        return createWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.attributeContains(locator, attributeName, value));
    }

    /**
     * Chờ element có class cụ thể.
     *
     * @param driver      WebDriver instance
     * @param element     WebElement
     * @param className   tên class cần kiểm tra
     */
    public static boolean waitForElementHasClass(WebDriver driver, WebElement element, String className) {
        log.debug("⏳ Chờ element có class: '{}'", className);
        return createWait(driver, DEFAULT_TIMEOUT).until(
                d -> element.getAttribute("class") != null
                        && element.getAttribute("class").contains(className)
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  Private Factory Method
    // ═══════════════════════════════════════════════════════════════

    /**
     * Tạo {@link WebDriverWait} với driver và timeout tùy chỉnh.
     * Polling interval mặc định của Selenium là 500ms – đủ tốt cho hầu hết use case.
     *
     * @param driver          WebDriver instance
     * @param timeoutSeconds  timeout (giây)
     * @return WebDriverWait instance
     */
    private static WebDriverWait createWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }
}
