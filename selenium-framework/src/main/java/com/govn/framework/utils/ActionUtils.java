package com.govn.framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * ActionUtils – Tập trung toàn bộ các hành động tương tác với UI elements.
 *
 * <p><b>Nguyên lý thiết kế:</b>
 * <ul>
 *   <li><b>SRP</b>: Class này CHỈ chịu trách nhiệm thực hiện các hành động UI.
 *       KHÔNG chứa logic wait nào – phải gọi {@link WaitUtils} trước khi dùng ActionUtils.</li>
 *   <li><b>DRY</b>: Mọi hành động phổ biến (click, type, clear) đều được wrapper ở đây
 *       để tránh lặp code trong Page Object classes.</li>
 *   <li><b>Defensive Actions</b>: Mỗi action đều chờ element sẵn sàng trước khi thực hiện
 *       thông qua WaitUtils – không giả định element đã visible.</li>
 * </ul>
 *
 * <p><b>Sử dụng:</b>
 * <pre>{@code
 *   // Trong Page Object:
 *   ActionUtils.click(driver, By.id("login-btn"));
 *   ActionUtils.clearAndType(driver, By.name("username"), "NV2");
 * }</pre>
 */
public final class ActionUtils {

    // Utility class – không cho phép khởi tạo
    private ActionUtils() {}

    // ═══════════════════════════════════════════════════════════════
    //  1. Click Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Click vào element xác định bằng {@link By}.
     * Chờ element clickable trước khi thực hiện.
     *
     * @param driver  WebDriver instance
     * @param locator By locator của element cần click
     */
    public static void click(WebDriver driver, By locator) {
        LogUtils.debug("🖱️ Click vào element: [{}]", locator);
        WebElement element = WaitUtils.waitForClickable(driver, locator);
        element.click();
    }

    /**
     * Click vào một {@link WebElement} đã có sẵn.
     * Chờ element clickable trước khi thực hiện.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần click
     */
    public static void click(WebDriver driver, WebElement element) {
        LogUtils.debug("🖱️ Click vào element: [{}]", element);
        WaitUtils.waitForClickable(driver, element);
        element.click();
    }

    /**
     * Click bằng JavaScript Executor – dùng khi element bị che khuất
     * hoặc không thể click được bằng Selenium thông thường.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần click
     */
    public static void jsClick(WebDriver driver, WebElement element) {
        LogUtils.debug("🖱️ JS Click vào element: [{}]", element);
        WaitUtils.waitForVisible(driver, element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Click vào element bằng JS theo locator.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     */
    public static void jsClick(WebDriver driver, By locator) {
        WebElement element = WaitUtils.waitForVisible(driver, locator);
        jsClick(driver, element);
    }

    /**
     * Double click vào element sử dụng Actions class.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần double click
     */
    public static void doubleClick(WebDriver driver, WebElement element) {
        LogUtils.debug("🖱️ Double Click vào element: [{}]", element);
        WaitUtils.waitForClickable(driver, element);
        new Actions(driver).doubleClick(element).perform();
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. Input Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Xóa nội dung hiện tại của input field rồi nhập text mới.
     * Đây là phương thức chuẩn nhất để điền vào form – luôn dùng thay vì chỉ gọi sendKeys.
     *
     * @param driver  WebDriver instance
     * @param locator By locator của input field
     * @param text    text cần nhập (nếu rỗng, chỉ xóa nội dung)
     */
    public static void clearAndType(WebDriver driver, By locator, String text) {
        LogUtils.debug("⌨️ ClearAndType vào [{}]: '{}'", locator,
                text.isBlank() ? "(rỗng)" : text.replaceAll(".", "*")); // Mask password
        WebElement element = WaitUtils.waitForVisible(driver, locator);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    /**
     * Xóa và nhập text vào một WebElement đã có sẵn.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement là input field
     * @param text     text cần nhập
     */
    public static void clearAndType(WebDriver driver, WebElement element, String text) {
        LogUtils.debug("⌨️ ClearAndType vào element: '{}'",
                text.isBlank() ? "(rỗng)" : text.replaceAll(".", "*")); // Mask password
        WaitUtils.waitForVisible(driver, element);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    /**
     * Chỉ nhập text mà KHÔNG xóa nội dung trước.
     * Dùng khi cần append text.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement là input field
     * @param text     text cần nhập thêm
     */
    public static void type(WebDriver driver, WebElement element, String text) {
        LogUtils.debug("⌨️ Type vào element: '{}'", text);
        WaitUtils.waitForVisible(driver, element);
        element.sendKeys(text);
    }

    /**
     * Xóa nội dung input field bằng phím tắt Ctrl+A → Delete.
     * Hữu ích khi element.clear() không hoạt động (React/Angular forms).
     *
     * @param driver   WebDriver instance
     * @param element  WebElement là input field
     */
    public static void clearByKeyboard(WebDriver driver, WebElement element) {
        LogUtils.debug("⌨️ Clear input bằng Ctrl+A + Delete");
        WaitUtils.waitForVisible(driver, element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }

    /**
     * Nhấn một phím đặc biệt (Enter, Tab, Escape, ...) trên element.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement mục tiêu
     * @param key      {@link Keys} cần nhấn
     */
    public static void pressKey(WebDriver driver, WebElement element, Keys key) {
        LogUtils.debug("⌨️ Press key [{}] trên element [{}]", key.name(), element);
        WaitUtils.waitForVisible(driver, element);
        element.sendKeys(key);
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. Read / Get Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy text hiển thị của element (element.getText()).
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần đọc text
     * @return text hiển thị của element
     */
    public static String getText(WebDriver driver, WebElement element) {
        WaitUtils.waitForVisible(driver, element);
        String text = element.getText().trim();
        LogUtils.debug("📖 getText: '{}'", text);
        return text;
    }

    /**
     * Lấy text của element theo locator.
     *
     * @param driver  WebDriver instance
     * @param locator By locator
     * @return text hiển thị của element
     */
    public static String getText(WebDriver driver, By locator) {
        WebElement element = WaitUtils.waitForVisible(driver, locator);
        return getText(driver, element);
    }

    /**
     * Lấy giá trị của attribute cụ thể của element.
     *
     * @param driver         WebDriver instance
     * @param element        WebElement
     * @param attributeName  tên attribute (ví dụ: "type", "class", "value")
     * @return giá trị của attribute, hoặc null nếu không tồn tại
     */
    public static String getAttribute(WebDriver driver, WebElement element, String attributeName) {
        WaitUtils.waitForPresence(driver, By.cssSelector("*")); // ensure DOM ready
        return element.getAttribute(attributeName);
    }

    // ═══════════════════════════════════════════════════════════════
    //  4. Scroll Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cuộn trang đến vị trí của element (scroll into view).
     * Hữu ích khi element nằm ngoài viewport.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần scroll đến
     */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        LogUtils.debug("📜 Scroll đến element: [{}]", element);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    /**
     * Cuộn trang đến vị trí element và click – xử lý trường hợp element
     * bị sticky header che khuất.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần scroll và click
     */
    public static void scrollAndClick(WebDriver driver, WebElement element) {
        scrollToElement(driver, element);
        click(driver, element);
    }

    // ═══════════════════════════════════════════════════════════════
    //  5. Hover Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Di chuột đến element (hover) – kích hoạt CSS :hover states và dropdowns.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần hover
     */
    public static void hover(WebDriver driver, WebElement element) {
        LogUtils.debug("🖱️ Hover vào element: [{}]", element);
        WaitUtils.waitForVisible(driver, element);
        new Actions(driver).moveToElement(element).perform();
    }

    // ═══════════════════════════════════════════════════════════════
    //  6. State Check Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Kiểm tra element có đang hiển thị hay không.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần kiểm tra
     * @return true nếu element đang hiển thị
     */
    public static boolean isDisplayed(WebDriver driver, WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            LogUtils.debug("Element không hiển thị hoặc không tồn tại: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra element có được enable (tương tác được) hay không.
     *
     * @param driver   WebDriver instance
     * @param element  WebElement cần kiểm tra
     * @return true nếu element enabled
     */
    public static boolean isEnabled(WebDriver driver, WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}
