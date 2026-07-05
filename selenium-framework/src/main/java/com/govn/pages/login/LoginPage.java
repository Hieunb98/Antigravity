package com.govn.pages.login;

import com.govn.framework.base.BasePage;
import com.govn.framework.utils.ActionUtils;
import com.govn.framework.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * LoginPage – Page Object đại diện cho trang đăng nhập GoVN.
 *
 * <p>URL: https://gon-demo.pirago.work/login
 *
 * <p><b>Ghi chú về DOM của Quasar Framework:</b><br>
 * ID của các input field được tạo động (ví dụ: {@code f_7fc7c26a...}), thay đổi mỗi lần tải trang.
 * Vì vậy, locator sử dụng {@code placeholder} attribute hoặc CSS class {@code input-login}
 * thay vì ID để đảm bảo tính ổn định của selector.
 *
 * <p><b>Nguyên lý POM:</b>
 * <ul>
 *   <li>Mọi locator chỉ được định nghĩa tại đây, không rải rác trong test.</li>
 *   <li>Mọi hành động với UI element được đặt trong method riêng (encapsulation).</li>
 *   <li>Test class chỉ gọi các method cấp cao (enterUsername, clickLogin) – không biết selector.</li>
 *   <li>Tất cả action đều đi qua {@link ActionUtils}, wait đi qua {@link WaitUtils}.</li>
 * </ul>
 */
public class LoginPage extends BasePage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);

    // ═══════════════════════════════════════════════════════════════
    //  Locators – Định nghĩa tập trung tất cả selector
    // ═══════════════════════════════════════════════════════════════

    /**
     * Input Tên đăng nhập.
     * Dùng placeholder thay vì ID (ID thay đổi động theo Quasar Framework).
     */
    @FindBy(css = "input.input-login[placeholder='Tên đăng nhập']")
    private WebElement usernameInput;

    /**
     * Input Mật khẩu.
     * Dùng placeholder để xác định chính xác field này.
     */
    @FindBy(css = "input.input-login[placeholder='Mật khẩu']")
    private WebElement passwordInput;

    /**
     * Nút Đăng nhập.
     * Selector dựa trên class Quasar của button và text con.
     */
    @FindBy(css = "button.q-btn.bg-primary")
    private WebElement loginButton;

    /**
     * Icon con mắt bên cạnh trường mật khẩu (toggle visibility).
     * class="q-icon cursor-pointer" chứa text "visibility_off" hoặc "visibility"
     */
    @FindBy(css = ".q-icon.cursor-pointer")
    private WebElement passwordToggleIcon;

    /**
     * Link "Quên mật khẩu?"
     */
    @FindBy(css = "a[href='/forget-password']")
    private WebElement forgotPasswordLink;

    // Locators By – dùng cho WaitUtils khi cần chờ dynamic elements
    // (lỗi validation xuất hiện SAU khi click submit)

    /** Lỗi validation dưới trường Tên đăng nhập */
    private static final By USERNAME_ERROR_MSG = By.cssSelector(
            ".q-field:has(input[placeholder='Tên đăng nhập']) .q-field__messages .text-negative"
    );

    /** Lỗi validation dưới trường Mật khẩu */
    private static final By PASSWORD_ERROR_MSG = By.cssSelector(
            ".q-field:has(input[placeholder='Mật khẩu']) .q-field__messages .text-negative"
    );

    /** Thông báo lỗi chung (sai tài khoản/mật khẩu) – xuất hiện phía trên form */
    private static final By GENERAL_ERROR_MSG = By.cssSelector(
            ".text-negative:not(.q-field__messages .text-negative)"
    );

    // ═══════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════

    /**
     * Khởi tạo LoginPage và gọi PageFactory để inject @FindBy elements.
     *
     * @param driver WebDriver instance của thread hiện tại
     */
    public LoginPage(WebDriver driver) {
        super(driver);
        log.info("📄 Khởi tạo LoginPage tại URL: {}", driver.getCurrentUrl());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Page Actions – Public API cho Test class
    // ═══════════════════════════════════════════════════════════════

    /**
     * Nhập Tên đăng nhập vào ô input.
     *
     * @param username tên đăng nhập
     * @return LoginPage (fluent API để chain actions)
     */
    public LoginPage enterUsername(String username) {
        log.info("⌨️ Nhập username: '{}'", username.isEmpty() ? "(rỗng)" : username);
        ActionUtils.clearAndType(driver, usernameInput, username);
        return this;
    }

    /**
     * Nhập Mật khẩu vào ô input.
     *
     * @param password mật khẩu (sẽ được mask trong log)
     * @return LoginPage (fluent API)
     */
    public LoginPage enterPassword(String password) {
        // Không log mật khẩu thực – chỉ log trạng thái rỗng hay không
        log.info("⌨️ Nhập password: {}", password.isEmpty() ? "(rỗng)" : "***");
        ActionUtils.clearAndType(driver, passwordInput, password);
        return this;
    }

    /**
     * Click nút Đăng nhập.
     *
     * @return LoginPage (giữ nguyên context; test sẽ verify URL để xác định kết quả)
     */
    public LoginPage clickLoginButton() {
        log.info("🖱️ Click nút Đăng nhập");
        ActionUtils.click(driver, loginButton);
        return this;
    }

    /**
     * Thực hiện đầy đủ flow đăng nhập (username → password → click).
     * Dùng cho happy path test.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @return LoginPage
     */
    public LoginPage login(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLoginButton();
    }

    /**
     * Click vào icon con mắt để toggle hiển thị/ẩn mật khẩu.
     *
     * @return LoginPage (fluent API)
     */
    public LoginPage togglePasswordVisibility() {
        log.info("🖱️ Click icon toggle hiển thị mật khẩu");
        ActionUtils.click(driver, passwordToggleIcon);
        return this;
    }

    /**
     * Click vào link "Quên mật khẩu?".
     */
    public void clickForgotPassword() {
        log.info("🖱️ Click link 'Quên mật khẩu?'");
        ActionUtils.click(driver, forgotPasswordLink);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Verification Methods – Trả về trạng thái/giá trị để test assert
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy text thông báo lỗi validation dưới ô Tên đăng nhập.
     * Chờ element xuất hiện trước khi đọc.
     *
     * @return text lỗi, hoặc chuỗi rỗng nếu không có lỗi
     */
    public String getUsernameErrorMessage() {
        try {
            WebElement errorEl = WaitUtils.waitForVisible(driver, USERNAME_ERROR_MSG, 5);
            return errorEl.getText().trim();
        } catch (Exception e) {
            log.debug("Không tìm thấy lỗi username validation.");
            return "";
        }
    }

    /**
     * Lấy text thông báo lỗi validation dưới ô Mật khẩu.
     *
     * @return text lỗi, hoặc chuỗi rỗng nếu không có lỗi
     */
    public String getPasswordErrorMessage() {
        try {
            WebElement errorEl = WaitUtils.waitForVisible(driver, PASSWORD_ERROR_MSG, 5);
            return errorEl.getText().trim();
        } catch (Exception e) {
            log.debug("Không tìm thấy lỗi password validation.");
            return "";
        }
    }

    /**
     * Lấy text thông báo lỗi chung (tên đăng nhập/mật khẩu không đúng).
     *
     * @return text lỗi, hoặc chuỗi rỗng nếu không có thông báo
     */
    public String getGeneralErrorMessage() {
        try {
            WebElement errorEl = WaitUtils.waitForVisible(driver, GENERAL_ERROR_MSG, 10);
            return errorEl.getText().trim();
        } catch (Exception e) {
            log.debug("Không tìm thấy general error message.");
            return "";
        }
    }

    /**
     * Lấy type attribute hiện tại của input mật khẩu.
     * Dùng để verify trạng thái toggle (password / text).
     *
     * @return "password" hoặc "text"
     */
    public String getPasswordFieldType() {
        return passwordInput.getAttribute("type");
    }

    /**
     * Lấy class của icon toggle mật khẩu để verify trạng thái icon.
     *
     * @return class string của icon element
     */
    public String getPasswordToggleIconClass() {
        return passwordToggleIcon.getText().trim(); // Material Icon dùng text để hiển thị icon
    }

    /**
     * Kiểm tra trang đăng nhập có đang hiển thị không
     * (dựa trên sự xuất hiện của nút Đăng nhập).
     *
     * @return true nếu đang ở trang đăng nhập
     */
    public boolean isLoginPageDisplayed() {
        return ActionUtils.isDisplayed(driver, loginButton);
    }

    /**
     * Kiểm tra trang đã redirect thành công về trang chủ.
     * Dùng sau khi click login với thông tin hợp lệ.
     *
     * @return true nếu URL hiện tại là trang chủ (không còn /login)
     */
    public boolean isRedirectedToHomePage() {
        try {
            // Chờ URL thay đổi – không còn chứa "/login"
            return WaitUtils.waitForUrlContains(driver, "gon-demo.pirago.work/")
                    && !driver.getCurrentUrl().contains("/login");
        } catch (Exception e) {
            return false;
        }
    }
}
