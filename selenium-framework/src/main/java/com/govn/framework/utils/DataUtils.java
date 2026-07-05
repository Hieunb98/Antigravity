package com.govn.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DataUtils – Utility class chuyên đọc dữ liệu test từ các nguồn bên ngoài.
 *
 * <p><b>Nguyên lý thiết kế (SRP):</b><br>
 * Class này CHỈ chịu trách nhiệm đọc và parse dữ liệu từ file.
 * Logic nghiệp vụ của test KHÔNG nằm ở đây.
 *
 * <p><b>Hỗ trợ 2 định dạng dữ liệu:</b>
 * <ol>
 *   <li><b>JSON</b>: Dùng Jackson ObjectMapper – phù hợp cho dữ liệu có cấu trúc phức tạp.</li>
 *   <li><b>Excel (.xlsx / .xls)</b>: Dùng Apache POI – phù hợp cho test data dạng bảng.</li>
 * </ol>
 *
 * <p><b>Cách dùng với TestNG DataProvider:</b>
 * <pre>{@code
 * @DataProvider(name = "loginData")
 * public Object[][] getLoginData() {
 *     List<LoginTestData> dataList = DataUtils.readJsonList("testdata/login_test_data.json",
 *                                                            LoginTestData.class);
 *     return dataList.stream()
 *         .map(data -> new Object[]{data})
 *         .toArray(Object[][]::new);
 * }
 * }</pre>
 */
public final class DataUtils {

    private static final Logger log = LogManager.getLogger(DataUtils.class);

    // Jackson ObjectMapper là thread-safe khi đã cấu hình xong
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Utility class – không cho phép khởi tạo
    private DataUtils() {}

    // ═══════════════════════════════════════════════════════════════
    //  1. JSON Reading Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Đọc file JSON và deserialize thành danh sách các object kiểu T.
     *
     * <p>File JSON phải có dạng array ở root level:
     * <pre>{@code
     * [
     *   { "username": "NV2", "password": "12345678@Hn", ... },
     *   { "username": "NV3", "password": "abc123", ... }
     * ]
     * }</pre>
     *
     * @param <T>           kiểu dữ liệu của từng phần tử
     * @param filePath      đường dẫn file JSON trong classpath (ví dụ: "testdata/login.json")
     * @param targetClass   class đích để deserialize
     * @return              danh sách các object đã được parse
     */
    public static <T> List<T> readJsonList(String filePath, Class<T> targetClass) {
        log.info("📂 Đọc JSON file: {}", filePath);
        try (InputStream inputStream = getResourceAsStream(filePath)) {
            List<T> dataList = OBJECT_MAPPER.readValue(
                    inputStream,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, targetClass)
            );
            log.info("✅ Đọc thành công {} bản ghi từ JSON: {}", dataList.size(), filePath);
            return dataList;

        } catch (IOException e) {
            log.error("❌ Lỗi khi đọc JSON file: {}", filePath, e);
            throw new RuntimeException("Không thể đọc JSON file: " + filePath, e);
        }
    }

    /**
     * Đọc file JSON và deserialize thành danh sách các {@code Map<String, Object>}.
     * Dùng khi không muốn tạo POJO class mà vẫn cần data-driven testing.
     *
     * @param filePath đường dẫn file JSON trong classpath
     * @return danh sách Map key-value
     */
    public static List<Map<String, Object>> readJsonAsMapList(String filePath) {
        log.info("📂 Đọc JSON file dạng Map: {}", filePath);
        try (InputStream inputStream = getResourceAsStream(filePath)) {
            List<Map<String, Object>> dataList = OBJECT_MAPPER.readValue(
                    inputStream, new TypeReference<List<Map<String, Object>>>() {}
            );
            log.info("✅ Đọc thành công {} bản ghi từ JSON: {}", dataList.size(), filePath);
            return dataList;

        } catch (IOException e) {
            log.error("❌ Lỗi khi đọc JSON file dạng Map: {}", filePath, e);
            throw new RuntimeException("Không thể đọc JSON Map file: " + filePath, e);
        }
    }

    /**
     * Chuyển đổi List JSON data thành format Object[][] của TestNG DataProvider.
     *
     * @param <T>         kiểu dữ liệu
     * @param filePath    đường dẫn file JSON
     * @param targetClass class đích
     * @return Object[][] dùng được trực tiếp trong @DataProvider
     */
    public static <T> Object[][] readJsonAsDataProvider(String filePath, Class<T> targetClass) {
        List<T> dataList = readJsonList(filePath, targetClass);
        return dataList.stream()
                .map(item -> new Object[]{item})
                .toArray(Object[][]::new);
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. Excel Reading Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Đọc một sheet trong file Excel và trả về dạng List of Map.
     * Dòng đầu tiên được coi là header (tên cột).
     *
     * <p>Ví dụ Excel:
     * <pre>
     * | username | password     | expectedResult |
     * | NV2      | 12345678@Hn  | success        |
     * | WRONG    | badpass      | invalid        |
     * </pre>
     *
     * @param filePath  đường dẫn file Excel trong classpath (ví dụ: "testdata/login.xlsx")
     * @param sheetName tên sheet cần đọc (ví dụ: "LoginData")
     * @return danh sách Map, mỗi Map là một dòng dữ liệu, key là tên cột
     */
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) {
        log.info("📊 Đọc Excel file: {} | Sheet: {}", filePath, sheetName);
        List<Map<String, String>> dataList = new ArrayList<>();

        try (InputStream inputStream = getResourceAsStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' không tồn tại trong file: " + filePath);
            }

            Iterator<Row> rowIterator = sheet.iterator();

            // Dòng đầu tiên là header
            if (!rowIterator.hasNext()) {
                log.warn("⚠️ Sheet '{}' rỗng.", sheetName);
                return dataList;
            }

            Row headerRow = rowIterator.next();
            List<String> headers = extractRowValues(headerRow);

            // Đọc các dòng data
            while (rowIterator.hasNext()) {
                Row dataRow = rowIterator.next();
                // Bỏ qua dòng rỗng hoàn toàn
                if (isRowEmpty(dataRow)) continue;

                List<String> rowValues = extractRowValues(dataRow);
                Map<String, String> rowMap = new HashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    String value = (i < rowValues.size()) ? rowValues.get(i) : "";
                    rowMap.put(headers.get(i), value);
                }
                dataList.add(rowMap);
            }

            log.info("✅ Đọc thành công {} dòng dữ liệu từ Excel sheet '{}'", dataList.size(), sheetName);
            return dataList;

        } catch (IOException e) {
            log.error("❌ Lỗi khi đọc Excel file: {}", filePath, e);
            throw new RuntimeException("Không thể đọc Excel file: " + filePath, e);
        }
    }

    /**
     * Chuyển đổi dữ liệu Excel thành format Object[][] của TestNG DataProvider.
     *
     * @param filePath  đường dẫn file Excel
     * @param sheetName tên sheet
     * @return Object[][] dùng được trong @DataProvider
     */
    public static Object[][] readExcelAsDataProvider(String filePath, String sheetName) {
        List<Map<String, String>> dataList = readExcel(filePath, sheetName);
        return dataList.stream()
                .map(row -> new Object[]{row})
                .toArray(Object[][]::new);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Private Helper Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy InputStream của resource file từ classpath.
     *
     * @param filePath đường dẫn file trong classpath
     * @return InputStream
     */
    private static InputStream getResourceAsStream(String filePath) {
        InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(filePath);
        if (stream == null) {
            throw new RuntimeException("Không tìm thấy file trong classpath: " + filePath);
        }
        return stream;
    }

    /**
     * Trích xuất các giá trị cell từ một Row Excel thành List String.
     */
    private static List<String> extractRowValues(Row row) {
        List<String> values = new ArrayList<>();
        for (Cell cell : row) {
            values.add(getCellValueAsString(cell));
        }
        return values;
    }

    /**
     * Chuyển đổi giá trị của Excel Cell sang String.
     * Xử lý tất cả các kiểu dữ liệu: STRING, NUMERIC (số và ngày), BOOLEAN, FORMULA.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue()); // Tránh ".0" trailing
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.NUMERIC
                    ? String.valueOf((long) cell.getNumericCellValue())
                    : cell.getStringCellValue();
            default -> "";
        };
    }

    /**
     * Kiểm tra dòng Excel có rỗng hoàn toàn không (tất cả cell đều trống).
     */
    private static boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
