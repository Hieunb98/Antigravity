package com.govn.framework.base;

import com.govn.framework.driver.DriverFactory;
import com.govn.framework.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * BasePage – Lớp cha trừu tượng cho tất cả Page Object classes.
 *
 * <p>
 * <b>Trách nhiệm:</b>
 * <ol>
 * <li>Lưu trữ tham chiếu đến {@link WebDriver} của thread hiện tại.</li>
 * <li>Khởi tạo {@link PageFactory} để inject các {@code @FindBy} elements.</li>
 * <li>Cung cấp các phương thức chung (navigate, getTitle, getCurrentUrl...)
 * mà mọi Page Object đều cần.</li>
 * </ol>
 *
 * <p>
 * <b>Nguyên lý quan trọng – SRP:</b><br>
 * BasePage KHÔNG chứa logic wait hay action trực tiếp (click, sendKeys, v.v).
 * Mọi action đều được ủy quyền cho {@link com.govn.framework.utils.ActionUtils}
 * và mọi wait đều đi qua {@link WaitUtils}.
 * BasePage là một "thin base class" – chỉ chứa infrastructure code.
 *
 * <p>
 * <b>Cách sử dụng:</b>
 * 
 * <pre>{@code
 * public class LoginPage extends BasePage { @FindBy(css = ".input-login")
 *     private List<WebElement> inputFields;
 *
 *     public LoginPage(WebDriver driver) {
 *         super(driver); // Gọi PageFactory.initElements
 *     }
 * }
 * }</pre>
 */
public abstract class BasePage {

    private static final Logger log = LogManager.getLogger(BasePage.class);
    /**
     * WebDriver của thread hiện tại – thread-safe vì lấy từ
     * DriverFactory.getDriver()
     */
    protected final WebDriver driver;

    /**
     * Constructor: khởi tạo PageFactory để inject các WebElement được annotate
     * bằng @FindBy.
     *
     * @param driver WebDriver instance của thread hiện tại
     */
    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // PageFactory.initElements sẽ tạo Proxy cho các @FindBy fields
        // → Element chỉ được tìm kiếm thực sự khi method được gọi (lazy evaluation)
        PageFactory.initElements(driver, this);
        log.debug("✅ Khởi tạo Page Object: {}", this.getClass().getSimpleName());
    }

    // ═══════════════════════════════════════════════════════════════
    // Navigation Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Navigate đến URL cụ thể.
     *
     * @param url URL đích
     */
    protected void navigateTo(String url) {
        log.info("🌐 Navigate đến: {}", url);
        driver.get(url);
        WaitUtils.waitForPageReady(driver);
    }

    /**
     * @return URL hiện tại của trang
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * @return Title của trang hiện tại
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Chờ URL chứa chuỗi fragment – dùng để xác minh navigation thành công.
     *
     * @param urlFragment chuỗi cần có trong URL
     * @return BasePage (this) để dùng fluent API nếu cần
     */
    protected boolean isUrlContains(String urlFragment) {
        return WaitUtils.waitForUrlContains(driver, urlFragment);
    }

    /**
     * Chờ title chứa chuỗi fragment.
     *
     * @param titleFragment chuỗi cần có trong title
     */
    protected boolean isTitleContains(String titleFragment) {
        return WaitUtils.waitForTitleContains(driver, titleFragment);
    }

    // ═══════════════════════════════════════════════════════════════
    // Element State Methods – trả về thông tin, không thực hiện action
    // ═══════════════════════════════════════════════════════════════

    /**
     * Kiểm tra element có đang hiển thị không mà không throw Exception.
     *
     * @param locator By locator
     * @return true nếu element hiển thị, false nếu không tìm thấy hoặc ẩn
     */
    protected boolean isElementVisible(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Đọc text của element theo locator (wrapper của WaitUtils + getText).
     *
     * @param locator By locator
     * @return text của element
     */
    protected String getElementText(By locator) {
        return WaitUtils.waitForVisible(driver, locator).getText().trim();
    }

    /**
     * Lấy giá trị attribute của element theo locator.
     *
     * @param locator       By locator
     * @param attributeName tên attribute
     * @return giá trị attribute
     */
    protected String getElementAttribute(By locator, String attributeName) {
        return WaitUtils.waitForPresence(driver, locator).getAttribute(attributeName);
    }
}
