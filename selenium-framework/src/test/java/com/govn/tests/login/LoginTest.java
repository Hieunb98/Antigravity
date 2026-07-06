package com.govn.tests.login;

import com.govn.base.BaseTest;
import com.govn.framework.utils.DataUtils;
import com.govn.pages.login.LoginPage;
import com.govn.testdata.login.LoginTestData;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.govn.framework.utils.LogUtils;

/**
 * LoginTest – Test class chứa tất cả test case cho chức năng Đăng nhập.
 *
 * <p><b>Chiến lược test:</b>
 * <ul>
 *   <li>Mỗi method test một scenario độc lập.</li>
 *   <li>Test data-driven (nhiều bộ dữ liệu) dùng {@link DataProvider} + {@link DataUtils}.</li>
 *   <li>Test độc lập (không phụ thuộc thứ tự) – có thể chạy parallel an toàn.</li>
 *   <li>Annotated với Allure metadata để sinh report chuyên nghiệp.</li>
 * </ul>
 *
 * <p><b>Nguyên tắc viết test:</b>
 * <ul>
 *   <li>Test class KHÔNG chứa locator hay selector – chỉ gọi Page Object methods.</li>
 *   <li>Mỗi test có comment mô tả: Given / When / Then (BDD-style).</li>
 *   <li>Assert message rõ ràng để dễ debug khi fail.</li>
 * </ul>
 */
@Epic("Authentication")
@Feature("Login Module")
public class LoginTest extends BaseTest {

    /** Path đến file JSON test data trong classpath */
    private static final String LOGIN_DATA_JSON = "testdata/login_test_data.json";

    // ═══════════════════════════════════════════════════════════════
    //  DataProvider – Cung cấp dữ liệu cho các test method
    // ═══════════════════════════════════════════════════════════════

    /**
     * DataProvider đọc dữ liệu từ JSON và cung cấp cho @Test methods.
     *
     * <p>{@code parallel = true}: DataProvider chạy trên nhiều thread đồng thời
     * (kết hợp với parallel="methods" trong testng.xml).
     *
     * @return Object[][] mảng 2 chiều, mỗi hàng là một bộ dữ liệu test
     */
    @DataProvider(name = "loginData", parallel = true)
    public Object[][] provideLoginData() {
        LogUtils.info("📦 Đang tải test data từ: {}", LOGIN_DATA_JSON);
        return DataUtils.readJsonAsDataProvider(LOGIN_DATA_JSON, LoginTestData.class);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Test Cases
    // ═══════════════════════════════════════════════════════════════

    /**
     * TC_LOGIN_001 – Đăng nhập thành công với thông tin hợp lệ.
     *
     * <p><b>Given</b> Người dùng ở trang đăng nhập<br>
     * <b>When</b> Nhập username "NV2" và password hợp lệ rồi click Đăng nhập<br>
     * <b>Then</b> Hệ thống redirect về trang chủ (URL không chứa /login)
     */
    @Test(description = "TC_LOGIN_001 - Đăng nhập thành công",
          groups = {"smoke", "regression"})
    @Story("Happy Path - Valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Xác minh người dùng có thể đăng nhập thành công với username='NV2' và password hợp lệ, " +
                 "sau đó được redirect về trang chủ.")
    public void testLoginSuccess() {
        LogUtils.info("▶▶ [TC_LOGIN_001] Bắt đầu test đăng nhập thành công");

        // Given: Khởi tạo Page Object
        LoginPage loginPage = new LoginPage(getDriver());

        // When: Thực hiện đăng nhập với thông tin hợp lệ
        loginPage.login(config.getValidUsername(), config.getValidPassword());

        // Then: Xác minh redirect về trang chủ
        boolean isRedirected = loginPage.isRedirectedToHomePage();
        Assert.assertTrue(isRedirected,
                "❌ Kỳ vọng redirect về trang chủ sau khi đăng nhập, nhưng vẫn ở: "
                + loginPage.getCurrentUrl());

        LogUtils.info("✅ [TC_LOGIN_001] PASSED – URL hiện tại: {}", loginPage.getCurrentUrl());
    }

    /**
     * TC_LOGIN_002 – Đăng nhập thất bại với mật khẩu sai.
     *
     * <p><b>Given</b> Người dùng ở trang đăng nhập<br>
     * <b>When</b> Nhập username đúng nhưng mật khẩu sai rồi click Đăng nhập<br>
     * <b>Then</b> Hiển thị thông báo lỗi: "Tên đăng nhập hoặc mật khẩu không chính xác."
     */
    @Test(description = "TC_LOGIN_002 - Đăng nhập thất bại: mật khẩu sai",
          groups = {"regression"})
    @Story("Invalid Credentials - Wrong Password")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Xác minh thông báo lỗi chính xác khi người dùng nhập sai mật khẩu.")
    public void testLoginWithWrongPassword() {
        LogUtils.info("▶▶ [TC_LOGIN_002] Bắt đầu test đăng nhập với mật khẩu sai");

        // Arrange
        String expectedError = "Tên đăng nhập hoặc mật khẩu không chính xác.";
        LoginPage loginPage = new LoginPage(getDriver());

        // Act
        loginPage.login(config.getValidUsername(), "wrong_password_123");

        // Assert
        String actualError = loginPage.getGeneralErrorMessage();
        Assert.assertEquals(actualError, expectedError,
                "❌ Thông báo lỗi không đúng khi nhập sai mật khẩu:");

        LogUtils.info("✅ [TC_LOGIN_002] PASSED – Error message: '{}'", actualError);
    }

    /**
     * TC_LOGIN_003 / 004 – Đăng nhập với thông tin không hợp lệ (Data-Driven).
     *
     * <p>Test này nhận dữ liệu từ DataProvider "loginData",
     * chỉ xử lý các bản ghi có expectedResult = "invalid_credentials".
     */
    @Test(dataProvider = "loginData",
          description = "TC_LOGIN - Đăng nhập với thông tin không hợp lệ (data-driven)",
          groups = {"regression"})
    @Story("Invalid Credentials")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Data-driven test: xác minh thông báo lỗi khi đăng nhập với thông tin không hợp lệ.")
    public void testLoginWithInvalidCredentials(LoginTestData testData) {
        // Chỉ chạy cho các test case liên quan đến invalid credentials
        if (!"invalid_credentials".equals(testData.getExpectedResult())) {
            LogUtils.info("⏭️ Bỏ qua test case {} (expectedResult='{}')",
                    testData.getTestCaseId(), testData.getExpectedResult());
            return;
        }

        LogUtils.info("▶▶ [{}] {}", testData.getTestCaseId(), testData.getDescription());

        // Act
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(testData.getUsername(), testData.getPassword());

        // Assert: Kiểm tra thông báo lỗi chung
        String actualError = loginPage.getGeneralErrorMessage();
        Assert.assertEquals(actualError, testData.getExpectedErrorMessage(),
                String.format("❌ [%s] Thông báo lỗi không đúng:", testData.getTestCaseId()));

        // Assert: Đảm bảo vẫn ở trang login (không bị redirect)
        Assert.assertTrue(loginPage.getCurrentUrl().contains("login"),
                "❌ Kỳ vọng vẫn ở trang login sau khi đăng nhập sai.");

        LogUtils.info("✅ [{}] PASSED", testData.getTestCaseId());
    }

    /**
     * TC_LOGIN_005 – Validation: Để trống cả hai trường.
     *
     * <p><b>Given</b> Người dùng ở trang đăng nhập<br>
     * <b>When</b> Không nhập gì và click nút Đăng nhập<br>
     * <b>Then</b> Hiển thị 2 thông báo lỗi: một dưới username, một dưới password
     */
    @Test(description = "TC_LOGIN_005 - Validation: Để trống cả hai trường",
          groups = {"regression"})
    @Story("Form Validation - Both Fields Empty")
    @Severity(SeverityLevel.NORMAL)
    @Description("Xác minh thông báo validation xuất hiện đúng khi cả hai trường bị để trống.")
    public void testLoginValidationBothFieldsEmpty() {
        LogUtils.info("▶▶ [TC_LOGIN_005] Test validation: cả hai trường trống");

        // Act
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.clickLoginButton(); // Không nhập gì, click thẳng

        // Assert username error
        String usernameError = loginPage.getUsernameErrorMessage();
        Assert.assertEquals(usernameError, "Tên đăng nhập không được để trống.",
                "❌ Lỗi trường username không đúng:");

        // Assert password error
        String passwordError = loginPage.getPasswordErrorMessage();
        Assert.assertEquals(passwordError, "Mật khẩu không được để trống.",
                "❌ Lỗi trường password không đúng:");

        LogUtils.info("✅ [TC_LOGIN_005] PASSED – Cả hai lỗi validation đều hiển thị đúng");
    }

    /**
     * TC_LOGIN_006 – Validation: Có username, để trống password.
     */
    @Test(description = "TC_LOGIN_006 - Validation: Có username nhưng để trống password",
          groups = {"regression"})
    @Story("Form Validation - Password Empty")
    @Severity(SeverityLevel.NORMAL)
    @Description("Xác minh chỉ lỗi password xuất hiện khi username được nhập nhưng password bị để trống.")
    public void testLoginValidationPasswordEmpty() {
        LogUtils.info("▶▶ [TC_LOGIN_006] Test validation: password trống");

        // Act
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(config.getValidUsername()).clickLoginButton();

        // Assert: Không có lỗi username
        String usernameError = loginPage.getUsernameErrorMessage();
        Assert.assertTrue(usernameError.isEmpty(),
                "❌ Không kỳ vọng lỗi username nhưng xuất hiện: '" + usernameError + "'");

        // Assert: Có lỗi password
        String passwordError = loginPage.getPasswordErrorMessage();
        Assert.assertEquals(passwordError, "Mật khẩu không được để trống.",
                "❌ Lỗi trường password không đúng:");

        LogUtils.info("✅ [TC_LOGIN_006] PASSED");
    }

    /**
     * TC_LOGIN_007 – Validation: Để trống username, có password.
     */
    @Test(description = "TC_LOGIN_007 - Validation: Để trống username nhưng có password",
          groups = {"regression"})
    @Story("Form Validation - Username Empty")
    @Severity(SeverityLevel.NORMAL)
    @Description("Xác minh chỉ lỗi username xuất hiện khi username bị để trống nhưng password được nhập.")
    public void testLoginValidationUsernameEmpty() {
        LogUtils.info("▶▶ [TC_LOGIN_007] Test validation: username trống");

        // Act
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterPassword(config.getValidPassword()).clickLoginButton();

        // Assert: Có lỗi username
        String usernameError = loginPage.getUsernameErrorMessage();
        Assert.assertEquals(usernameError, "Tên đăng nhập không được để trống.",
                "❌ Lỗi trường username không đúng:");

        // Assert: Không có lỗi password
        String passwordError = loginPage.getPasswordErrorMessage();
        Assert.assertTrue(passwordError.isEmpty(),
                "❌ Không kỳ vọng lỗi password nhưng xuất hiện: '" + passwordError + "'");

        LogUtils.info("✅ [TC_LOGIN_007] PASSED");
    }

    /**
     * TC_LOGIN_UC03 – Toggle hiển thị mật khẩu.
     *
     * <p><b>Given</b> Người dùng ở trang đăng nhập<br>
     * <b>When</b> Click icon con mắt lần 1 → Click lần 2<br>
     * <b>Then</b> Field type thay đổi giữa "password" và "text"
     */
    @Test(description = "TC_LOGIN_UC03 - Toggle hiển thị/ẩn mật khẩu",
          groups = {"regression"})
    @Story("Password Visibility Toggle")
    @Severity(SeverityLevel.MINOR)
    @Description("Xác minh chức năng toggle hiển thị mật khẩu: click icon mắt để đổi qua lại giữa kiểu password và text.")
    public void testPasswordVisibilityToggle() {
        LogUtils.info("▶▶ [TC_LOGIN_UC03] Test toggle hiển thị mật khẩu");

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterPassword("TestPassword123");

        // Trạng thái ban đầu: type="password"
        String initialType = loginPage.getPasswordFieldType();
        Assert.assertEquals(initialType, "password",
                "❌ Mặc định trường mật khẩu phải có type='password':");

        // Click toggle lần 1: type phải đổi thành "text"
        loginPage.togglePasswordVisibility();
        String afterFirstToggle = loginPage.getPasswordFieldType();
        Assert.assertEquals(afterFirstToggle, "text",
                "❌ Sau lần click 1, type phải là 'text':");

        // Click toggle lần 2: type phải trở về "password"
        loginPage.togglePasswordVisibility();
        String afterSecondToggle = loginPage.getPasswordFieldType();
        Assert.assertEquals(afterSecondToggle, "password",
                "❌ Sau lần click 2, type phải trở lại 'password':");

        LogUtils.info("✅ [TC_LOGIN_UC03] PASSED – Toggle hoạt động đúng");
    }

    /**
     * TC_LOGIN_UC04 – Click link Quên mật khẩu.
     *
     * <p><b>Given</b> Người dùng ở trang đăng nhập<br>
     * <b>When</b> Click "Quên mật khẩu?"<br>
     * <b>Then</b> URL chứa "/forget-password"
     */
    @Test(description = "TC_LOGIN_UC04 - Chuyển hướng đến trang Quên mật khẩu",
          groups = {"regression"})
    @Story("Forgot Password Navigation")
    @Severity(SeverityLevel.MINOR)
    @Description("Xác minh link 'Quên mật khẩu?' dẫn người dùng đến trang /forget-password.")
    public void testForgotPasswordNavigation() {
        LogUtils.info("▶▶ [TC_LOGIN_UC04] Test navigation đến trang quên mật khẩu");

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.clickForgotPassword();

        // Assert: URL chứa "/forget-password"
        boolean isCorrectUrl = loginPage.isUrlContains("forget-password");
        Assert.assertTrue(isCorrectUrl,
                "❌ Kỳ vọng URL chứa '/forget-password' nhưng URL hiện tại là: "
                + loginPage.getCurrentUrl());

        LogUtils.info("✅ [TC_LOGIN_UC04] PASSED – URL: {}", loginPage.getCurrentUrl());
    }
}
