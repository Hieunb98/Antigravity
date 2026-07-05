package com.govn.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader – Singleton utility để đọc file config.properties.
 *
 * <p>Nguyên lý áp dụng:
 * <ul>
 *   <li><b>SRP</b>: Class chỉ có một nhiệm vụ duy nhất – đọc và cung cấp config.</li>
 *   <li><b>Thread-safe Singleton</b>: Sử dụng pattern "Initialization-on-demand holder"
 *       (lazy loading, không cần synchronized overhead).</li>
 * </ul>
 *
 * <p>File config được đặt tại: {@code src/test/resources/config/config.properties}
 */
public final class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE_PATH = "config/config.properties";

    private final Properties properties;

    // ─── Private constructor: chỉ được gọi một lần bởi Holder ───
    private ConfigReader() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Inner static Holder class – JVM đảm bảo thread-safe lazy initialization.
     * INSTANCE chỉ được tạo khi getInstance() được gọi lần đầu tiên.
     */
    private static class Holder {
        private static final ConfigReader INSTANCE = new ConfigReader();
    }

    /**
     * Trả về Singleton instance của ConfigReader.
     *
     * @return ConfigReader instance duy nhất
     */
    public static ConfigReader getInstance() {
        return Holder.INSTANCE;
    }

    // ─── Load properties từ classpath ───
    private void loadProperties() {
        // ClassLoader của thread hiện tại để tương thích tốt nhất với Maven
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE_PATH)) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "Không tìm thấy file config: " + CONFIG_FILE_PATH
                        + ". Vui lòng kiểm tra src/test/resources/config/config.properties");
            }

            properties.load(inputStream);
            log.info("✅ Đã tải thành công config từ: {}", CONFIG_FILE_PATH);

        } catch (IOException e) {
            log.error("❌ Lỗi khi đọc file config: {}", CONFIG_FILE_PATH, e);
            throw new RuntimeException("Không thể đọc file cấu hình.", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Public getter methods – mỗi method wrap một property key cụ thể
    //  → Tránh hard-code String key ở nhiều nơi (DRY principle)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy giá trị String của một property bất kỳ.
     *
     * @param key  key trong file properties
     * @return     giá trị tương ứng, hoặc ném RuntimeException nếu không tồn tại
     */
    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Property '" + key + "' không tồn tại hoặc rỗng trong config.");
        }
        return value.trim();
    }

    /** @return Base URL của AUT (ví dụ: https://gon-demo.pirago.work) */
    public String getBaseUrl() {
        return get("base.url");
    }

    /** @return URL trang Login */
    public String getLoginUrl() {
        return get("login.url");
    }

    /**
     * Lấy tên browser từ config, ưu tiên System property (dùng trong CI).
     * Ví dụ: mvn test -Dbrowser=firefox
     *
     * @return tên browser (chrome / firefox / edge), chữ thường
     */
    public String getBrowser() {
        // System property được set từ CLI / CI pipeline có độ ưu tiên cao hơn
        String browserFromSystem = System.getProperty("browser");
        if (browserFromSystem != null && !browserFromSystem.isBlank()) {
            return browserFromSystem.trim().toLowerCase();
        }
        return get("browser").toLowerCase();
    }

    /**
     * Kiểm tra headless mode, ưu tiên System property.
     * Ví dụ: mvn test -Dheadless=true
     *
     * @return true nếu chạy headless
     */
    public boolean isHeadless() {
        String headlessFromSystem = System.getProperty("headless");
        if (headlessFromSystem != null && !headlessFromSystem.isBlank()) {
            return Boolean.parseBoolean(headlessFromSystem.trim());
        }
        return Boolean.parseBoolean(get("headless"));
    }

    /** @return Explicit wait timeout tính bằng giây */
    public int getExplicitWaitTimeout() {
        return Integer.parseInt(get("explicit.wait.timeout"));
    }

    /** @return Page load timeout tính bằng giây */
    public int getPageLoadTimeout() {
        return Integer.parseInt(get("page.load.timeout"));
    }

    /** @return Username hợp lệ mặc định */
    public String getValidUsername() {
        return get("valid.username");
    }

    /** @return Password hợp lệ mặc định */
    public String getValidPassword() {
        return get("valid.password");
    }
}
