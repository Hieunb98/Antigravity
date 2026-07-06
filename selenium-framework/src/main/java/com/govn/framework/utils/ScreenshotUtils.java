package com.govn.framework.utils;

import com.govn.framework.driver.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtils – Utility class chuyên chụp màn hình và lưu file.
 *
 * <p><b>SRP</b>: Class này CHỈ chịu trách nhiệm việc chụp và lưu screenshot.
 * Được gọi từ ExtentReportListener khi test fail để đính kèm bằng chứng.
 */
public final class ScreenshotUtils {

    private static final String SCREENSHOT_DIR = "target/screenshots/";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    // Utility class – không cho phép khởi tạo
    private ScreenshotUtils() {}

    /**
     * Chụp màn hình và lưu vào thư mục target/screenshots/.
     *
     * @param driver    WebDriver instance
     * @param testName  tên test (dùng để đặt tên file)
     * @return đường dẫn tuyệt đối đến file screenshot, hoặc null nếu thất bại
     */
    public static String captureAndSave(WebDriver driver, String testName) {
        try {
            // Tạo tên file có timestamp để tránh ghi đè
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String fileName = sanitizedTestName + "_" + timestamp + ".png";

            // Đảm bảo thư mục tồn tại
            Path dirPath = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dirPath);

            // Chụp màn hình
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destPath = dirPath.resolve(fileName);
            Files.copy(srcFile.toPath(), destPath);

            LogUtils.info("📸 Screenshot đã lưu tại: {}", destPath.toAbsolutePath());
            return destPath.toAbsolutePath().toString();

        } catch (IOException e) {
            LogUtils.error("❌ Không thể lưu screenshot cho test: {}", testName, e);
            return null;
        }
    }

    /**
     * Chụp màn hình và trả về byte array (dùng để embed vào ExtentReports / Allure).
     *
     * @param driver WebDriver instance
     * @return byte array của ảnh PNG, hoặc null nếu thất bại
     */
    public static byte[] captureAsBytes(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LogUtils.error("❌ Không thể chụp screenshot dạng bytes: {}", e.getMessage());
            return null;
        }
    }
}
