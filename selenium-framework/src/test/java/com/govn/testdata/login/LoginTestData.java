package com.govn.testdata.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginTestData – POJO class đại diện cho một bản ghi test data trong file JSON.
 *
 * <p>Được Jackson ObjectMapper deserialize từ {@code testdata/login_test_data.json}.
 *
 * <p>Sử dụng Lombok để tự sinh getter/setter, giảm boilerplate code.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)}: bỏ qua các field JSON
 * không có trong class để tránh lỗi khi data file được thêm field mới.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginTestData {

    /** Mã test case (ví dụ: TC_LOGIN_001) – dùng để hiển thị trong report */
    private String testCaseId;

    /** Mô tả ngắn gọn của test case */
    private String description;

    /** Tên đăng nhập dùng trong test */
    private String username;

    /** Mật khẩu dùng trong test */
    private String password;

    /**
     * Kết quả mong đợi sau khi thực hiện login.
     * Các giá trị hợp lệ:
     * <ul>
     *   <li>{@code "success"}: đăng nhập thành công, redirect về trang chủ</li>
     *   <li>{@code "invalid_credentials"}: sai thông tin, hiện lỗi chung</li>
     *   <li>{@code "validation_error"}: để trống trường, hiện lỗi validation dưới field</li>
     * </ul>
     */
    private String expectedResult;

    /** URL kỳ vọng sau khi đăng nhập thành công */
    private String expectedUrl;

    /** Text lỗi chung kỳ vọng (khi sai thông tin đăng nhập) */
    private String expectedErrorMessage;

    /** Text lỗi validation kỳ vọng dưới trường Username */
    private String expectedUsernameError;

    /** Text lỗi validation kỳ vọng dưới trường Password */
    private String expectedPasswordError;

    /**
     * Override toString() để hiển thị đẹp trong TestNG report và log.
     * Mật khẩu được mask để bảo mật.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s (user='%s')", testCaseId, description,
                username.isEmpty() ? "(rỗng)" : username);
    }
}
