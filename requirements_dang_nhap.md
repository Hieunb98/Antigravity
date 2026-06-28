# Tài liệu đặc tả yêu cầu chức năng: Màn đăng nhập

Tài liệu này đặc tả yêu cầu chức năng và quy tắc nghiệp vụ cho màn hình Đăng nhập của hệ thống GoVN tại URL: https://gon-demo.pirago.work/login.

## 1. Tổng quan (Overview)

Module Đăng nhập cung cấp giao diện để người dùng đăng nhập vào hệ thống. Người dùng cần cung cấp tên đăng nhập và mật khẩu để được xác thực. Sau khi đăng nhập thành công, hệ thống sẽ thiết lập phiên làm việc và chuyển hướng người dùng về Trang chủ.

## 2. Yêu cầu chức năng (Functional Requirements)

### Use Case 01: Đăng nhập vào hệ thống
- Mô tả: Cho phép người dùng nhập thông tin tài khoản để đăng nhập vào hệ thống.
- Các bước thực hiện:
  1. Người dùng truy cập URL: https://gon-demo.pirago.work/login.
  2. Người dùng nhập Tên đăng nhập.
  3. Người dùng nhập Mật khẩu.
  4. Người dùng nhấn nút Đăng nhập.
- Tiêu chí chấp nhận:
  - Nếu thông tin chính xác (ví dụ tài khoản thử nghiệm: Tên đăng nhập là "NV2" và Mật khẩu là "12345678@Hn"), hệ thống sẽ thiết lập phiên làm việc và chuyển hướng người dùng đến Trang chủ tại địa chỉ https://gon-demo.pirago.work/.
  - Khi chuyển hướng thành công:
    * Tiêu đề của trang đổi thành "Trang chủ".
    * Sidebar bên trái hiển thị Logo GoVN, các liên kết "Trang chủ", "Quản lý công việc" (kèm theo huy hiệu hiển thị số lượng công việc hiện tại, ví dụ: 4), và nút "Đăng xuất" ở góc dưới sidebar.
    * Header phía trên hiển thị nút menu (Hamburger menu toggle icon), biểu tượng thông báo "Công việc" (kèm số lượng thông báo chưa đọc, ví dụ: 1), thông tin người dùng đăng nhập (Tên hiển thị: "Nv2", Vai trò: "nvdaf").
    * Vùng nội dung chính của Trang chủ hiển thị thông điệp mặc định: "Hiện không có sẵn nội dung nào để hiển thị."

### Use Case 02: Kiểm tra lỗi nhập liệu (Validation)
- Mô tả: Hệ thống kiểm tra tính hợp lệ của dữ liệu người dùng nhập trước khi gửi yêu cầu xác thực.
- Tiêu chí chấp nhận:
  - Nếu để trống cả hai trường Tên đăng nhập và Mật khẩu:
    * Dưới trường Tên đăng nhập xuất hiện thông báo lỗi màu đỏ: "Tên đăng nhập không được để trống."
    * Dưới trường Mật khẩu xuất hiện thông báo lỗi màu đỏ: "Mật khẩu không được để trống."
  - Nếu nhập Tên đăng nhập nhưng để trống Mật khẩu:
    * Trường Tên đăng nhập không hiển thị lỗi.
    * Dưới trường Mật khẩu xuất hiện lỗi: "Mật khẩu không được để trống."
  - Nếu để trống Tên đăng nhập nhưng nhập Mật khẩu:
    * Dưới trường Tên đăng nhập xuất hiện lỗi: "Tên đăng nhập không được để trống."
    * Trường Mật khẩu không hiển thị lỗi.
  - Nếu nhập sai thông tin tài khoản (Tên đăng nhập hoặc Mật khẩu hoặc cả hai không hợp lệ):
    * Không hiển thị thông báo lỗi ở dưới chân từng trường nhập liệu.
    * Xuất hiện thông báo lỗi chung bằng chữ màu đỏ nằm ở phía trên biểu mẫu nhập liệu và dưới tiêu đề Đăng nhập: "Tên đăng nhập hoặc mật khẩu không chính xác."

### Use Case 03: Ẩn hoặc hiển thị mật khẩu
- Mô tả: Cho phép người dùng thay đổi chế độ ẩn hoặc hiển thị nội dung trường Mật khẩu.
- Tiêu chí chấp nhận:
  - Mặc định khi mở trang, trường Mật khẩu được ẩn (kiểu password) và biểu tượng con mắt hiển thị ở trạng thái mắt đóng (class biểu tượng là visibility_off).
  - Khi người dùng click vào biểu tượng con mắt ở phía bên phải ô nhập mật khẩu:
    * Kiểu nhập liệu của trường đổi thành hiển thị văn bản thường (kiểu text) để người dùng đọc được mật khẩu đã nhập.
    * Biểu tượng đổi sang trạng thái mắt mở (class biểu tượng là visibility).
  - Khi click lại vào biểu tượng con mắt lần nữa, trường quay lại trạng thái ẩn mặc định và biểu tượng đổi về visibility_off.

### Use Case 04: Chuyển hướng đến màn hình Quên mật khẩu
- Mô tả: Cho phép người dùng đi đến màn hình khôi phục mật khẩu.
- Tiêu chí chấp nhận:
  - Khi click vào liên kết "Quên mật khẩu?" bên dưới form đăng nhập, hệ thống chuyển hướng người dùng đến URL phù hợp (trang /forget-password).

## 3. Quy tắc trường dữ liệu (Field Specifications)

Dưới đây là đặc tả chi tiết về các trường dữ liệu của màn hình Đăng nhập được thu thập trực tiếp từ DOM và kiểm thử thực tế:

| Tên Trường (Label) | Loại UI | Thuộc tính HTML chính | Bắt buộc | Validation Rules | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| Tên đăng nhập * | Textbox | class: q-field__native q-placeholder input-login; placeholder: Tên đăng nhập; id: động (ví dụ: f_7fc7c26a...) | Có | Không được để trống. Hiển thị lỗi "Tên đăng nhập không được để trống." dưới trường nếu vi phạm. | ID của trường thay đổi động mỗi khi tải lại trang (được tạo tự động bởi Quasar Framework). Nên dùng placeholder hoặc class input-login để xác định phần tử trong kiểm thử tự động. |
| Mật khẩu * | Password Textbox | class: q-field__native q-placeholder input-login; placeholder: Mật khẩu; id: động (ví dụ: f_d37e6054...); type: password/text | Có | Không được để trống. Hiển thị lỗi "Mật khẩu không được để trống." dưới trường nếu vi phạm. | ID thay đổi động. Có biểu tượng con mắt (class q-icon cursor-pointer) bên phải ô nhập để thay đổi kiểu trường giữa password và text. |
| Đăng nhập | Button | class: q-btn q-btn-item bg-primary text-white full-width; type: button; thẻ span con có văn bản "Đăng nhập" | Không áp dụng | Kích hoạt sự kiện submit form đăng nhập và gửi yêu cầu đến máy chủ. | Chiếm toàn bộ chiều rộng của biểu mẫu (full-width). |
| Quên mật khẩu? | Link | class: text-black; href: /forget-password; thẻ a | Không áp dụng | Chuyển hướng tới trang /forget-password khi được click. | Màu chữ đen. |

## 4. Luồng xử lý và Quy tắc nghiệp vụ (Business & User Flows)

### Luồng Đăng nhập thành công (Happy Path)
1. Người dùng truy cập màn hình đăng nhập.
2. Người dùng nhập Tên đăng nhập hợp lệ (NV2).
3. Người dùng nhập Mật khẩu hợp lệ (12345678@Hn).
4. Người dùng bấm nút Đăng nhập.
5. Hệ thống gửi thông tin xác thực lên server.
6. Server kiểm tra thông tin đúng, thiết lập session cho người dùng.
7. Trình duyệt nhận phản hồi thành công, chuyển hướng về Trang chủ (/).

### Luồng Đăng nhập thất bại do thông tin không chính xác (Alternate Path 1)
1. Người dùng nhập Tên đăng nhập và Mật khẩu nhưng thông tin không khớp với dữ liệu người dùng hợp lệ trên hệ thống.
2. Người dùng bấm nút Đăng nhập.
3. Hệ thống gửi thông tin lên server và nhận phản hồi xác thực thất bại.
4. Hệ thống giữ nguyên tại trang đăng nhập và hiển thị thông báo lỗi chung phía trên form nhập: "Tên đăng nhập hoặc mật khẩu không chính xác."
5. Các trường nhập liệu không bị reset và không hiển thị thông báo lỗi dưới chân trường.

### Luồng Đăng nhập thất bại do để trống trường bắt buộc (Alternate Path 2)
1. Người dùng để trống một hoặc cả hai trường bắt buộc (Tên đăng nhập, Mật khẩu).
2. Người dùng bấm nút Đăng nhập.
3. Hệ thống chặn hành động gửi yêu cầu xác thực và lập tức hiển thị thông báo lỗi phù hợp dưới chân các trường đang để trống:
   - Nếu Tên đăng nhập trống: Hiển thị lỗi "Tên đăng nhập không được để trống."
   - Nếu Mật khẩu trống: Hiển thị lỗi "Mật khẩu không được để trống."
4. Hệ thống giữ nguyên trạng thái để cho người dùng bổ sung thông tin thiếu.
