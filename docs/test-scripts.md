# 1. Kiểm thử chức năng Đăng nhập/Đăng ký

## TC-AUTH-001: Đăng nhập với tài khoản Admin

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Mở trình duyệt, truy cập trang login | Trang đăng nhập hiện ra |
| 2 | Điền username: admin | Điền được |
| 3 | Điền password: Admin@123 | Password bị che |
| 4 | Bấm Đăng nhập | Vào được Dashboard, menu hiện đủ các mục |

## TC-AUTH-002: Đăng nhập với tài khoản Kế toán

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Mở trang login | OK |
| 2 | Điền accountant / Accountant@123 | OK |
| 3 | Bấm Đăng nhập | Vào Dashboard, menu chỉ hiện các mục liên quan kế toán |

## TC-AUTH-003: Đăng nhập với tài khoản Cư dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Mở trang login | OK |
| 2 | Điền resident1 / Resident@123 | OK |
| 3 | Bấm Đăng nhập | Vào Portal cư dân |

## TC-AUTH-004: Đăng nhập sai mật khẩu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Điền admin / saimatkhau | OK |
| 2 | Bấm Đăng nhập | Báo lỗi đăng nhập thất bại |

## TC-AUTH-005: Để trống thông tin đăng nhập

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Không điền gì, bấm Đăng nhập | Báo lỗi yêu cầu nhập username và password |

## TC-AUTH-006: Đăng ký tài khoản

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm link Đăng ký | Chuyển sang trang đăng ký |
| 2 | Điền đầy đủ thông tin | OK |
| 3 | Bấm Đăng ký | Tạo tài khoản xong, quay về login |

## TC-AUTH-007: Đăng xuất

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login thành công | OK |
| 2 | Bấm Đăng xuất | Quay về trang login |
| 3 | Thử vào lại Dashboard bằng URL | Bị chặn, chuyển về login |

---

# 2. Kiểm thử Quản lý Hộ dân

## TC-HD-001: Xem danh sách hộ dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login admin | OK |
| 2 | Bấm menu Ho dan | Hiện bảng danh sách |
| 3 | Kiểm tra các cột | Co: Ma ho, Chu ho, So phong, So nhan khau, Trang thai |

## TC-HD-002: Thêm hộ dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | Hiện form |
| 2 | Điền thông tin: số phòng, chủ hộ, sdt, email | OK |
| 3 | Chọn trạng thái | Dropdown hoạt động |
| 4 | Bấm Lưu | Thêm thành công, hộ mới xuất hiện trong list |

## TC-HD-003: Thêm hộ dân - thiếu thông tin

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | OK |
| 2 | Để trống các trường bắt buộc | OK |
| 3 | Bấm Lưu | Báo lỗi validation |

## TC-HD-004: Sửa hộ dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn 1 hộ trong list | OK |
| 2 | Bấm Sửa | Form hiện lên với data cũ |
| 3 | Đổi số điện thoại | OK |
| 4 | Bấm Lưu | Cập nhật thành công |

## TC-HD-005: Xóa hộ dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn 1 hộ | OK |
| 2 | Bấm Xóa | Hiện popup xác nhận |
| 3 | Xác nhận | Hộ bị xóa khỏi list |

## TC-HD-006: Tìm kiếm hộ dân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Gõ từ khóa vào ô search | OK |
| 2 | Bấm Enter | List lọc theo từ khóa |
| 3 | Xóa từ khóa | List hiện lại đầy đủ |

---

# 3. Kiểm thử Quản lý Nhân khẩu

## TC-NK-001: Xem danh sách nhân khẩu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login admin, vào menu Nhân khẩu | Hiện list nhân khẩu |
| 2 | Kiểm tra các cột | Họ tên, Ngày sinh, CCCD, Giới tính, Thuộc hộ |

## TC-NK-002: Thêm nhân khẩu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | Form hiện ra |
| 2 | Điền: họ tên, ngày sinh, cccd, giới tính | OK |
| 3 | Chọn hộ dân thuộc về | Dropdown hiện list hộ |
| 4 | Chọn quan hệ với chủ hộ | Dropdown: Chủ hộ, Vợ/Chồng, Con, v.v. |
| 5 | Bấm Lưu | Thêm thành công |

## TC-NK-003: Sửa nhân khẩu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn 1 nhân khẩu, bấm Sửa | Form có data cũ |
| 2 | Đổi thông tin | OK |
| 3 | Bấm Lưu | Cập nhật OK |

## TC-NK-004: Xóa nhân khẩu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn, bấm Xóa | Popup xác nhận |
| 2 | Xác nhận | Xóa thành công |

---

# 4. Kiểm thử Tạm trú

## TC-TT-001: Xem danh sách tạm trú

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Tạm trú | Hiện list đăng ký tạm trú |
| 2 | Kiểm tra cột | Người tạm trú, Nơi thường trú, Từ ngày, Đến ngày, Lý do |

## TC-TT-002: Thêm đăng ký tạm trú

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | Form hiện |
| 2 | Chọn/nhập người tạm trú | OK |
| 3 | Điền các thông tin | OK |
| 4 | Bấm Lưu | Đăng ký thành công |

## TC-TT-003: Sửa tạm trú

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn, bấm Sửa | Form hiện |
| 2 | Đổi ngày kết thúc | OK |
| 3 | Bấm Lưu | Cập nhật OK |

## TC-TT-004: Xóa tạm trú

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn, bấm Xóa | Popup xác nhận |
| 2 | Xác nhận | Xóa OK |

---

# 5. Kiểm thử Tạm vắng

## TC-TV-001: Xem danh sách tạm vắng

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Tạm vắng | Hiện list |
| 2 | Kiểm tra cột | Người tạm vắng, Nơi đến, Từ ngày, Đến ngày, Lý do |

## TC-TV-002: Thêm tạm vắng

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | Form hiện |
| 2 | Chọn nhân khẩu | OK |
| 3 | Điền thông tin | OK |
| 4 | Bấm Lưu | Đăng ký OK |

## TC-TV-003: Sửa/Xóa tạm vắng

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Thao tác sửa hoặc xóa | OK |

---

# 6. Kiểm thử Quản lý Loại phí

## TC-LP-001: Xem danh sách loại phí

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Loại phí | Hiện list |
| 2 | Kiểm tra | Tên, Mô tả, Bắt buộc/Tự nguyện, Đơn giá |

## TC-LP-002: Thêm loại phí

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Thêm mới | Form hiện |
| 2 | Điền tên, mô tả, loại phí, đơn giá | OK |
| 3 | Bấm Lưu | Thêm thành công |

## TC-LP-003: Cấu hình bảng giá

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn loại phí, bấm Cấu hình giá | Form cấu hình |
| 2 | Thiết lập giá bậc thang hoặc cố định | OK |
| 3 | Bấm Lưu | Áp dụng thành công |

## TC-LP-004: Sửa/Xóa loại phí

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Thao tác sửa/xóa | OK |

---

# 7. Kiểm thử Đợt thu

## TC-DT-001: Xem danh sách đợt thu

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Đợt thu | Hiện list |
| 2 | Kiểm tra cột | Tên, Tháng/Năm, Trạng thái, Ngày bắt đầu, Hạn nộp |

## TC-DT-002: Tạo đợt thu mới

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Tạo đợt thu | Form hiện |
| 2 | Điền tên, chọn tháng/năm | OK |
| 3 | Chọn các loại phí áp dụng | Checkbox đa chọn |
| 4 | Chọn hạn nộp | OK |
| 5 | Bấm Lưu | Tạo thành công |

## TC-DT-003: Sinh hóa đơn

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn đợt thu | OK |
| 2 | Bấm Sinh hóa đơn | Popup xác nhận |
| 3 | Xác nhận | Hóa đơn được tạo cho các hộ |

---

# 8. Kiểm thử Hóa đơn

## TC-HĐ-001: Xem danh sách hóa đơn

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Hóa đơn | Hiện list hóa đơn |
| 2 | Kiểm tra cột | Mã HD, Hộ dân, Đợt thu, Tổng tiền, Trạng thái |

## TC-HĐ-002: Lọc hóa đơn

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn filter Chưa thanh toán | Chỉ hiện HD chưa TT |
| 2 | Chọn Đã thanh toán | Chỉ hiện HD đã TT |

## TC-HĐ-003: Xem chi tiết hóa đơn

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Click vào 1 hóa đơn | Hiện chi tiết các khoản phí |

## TC-HĐ-004: Xác nhận thanh toán

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn HD chưa thanh toán | OK |
| 2 | Bấm Xác nhận thanh toán | Form xác nhận |
| 3 | Chọn phương thức (tiền mặt/chuyển khoản) | OK |
| 4 | Xác nhận | Trạng thái đổi thành Đã thanh toán |

---

# 9. Kiểm thử Chỉ số điện nước

## TC-CS-001: Xem chỉ số

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Chỉ số điện nước | Hiện list |
| 2 | Kiểm tra | Hộ dân, Tháng, Chỉ số điện, Chỉ số nước |

## TC-CS-002: Nhập chỉ số

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Nhập chỉ số | Form hiện |
| 2 | Chọn hộ, tháng | OK |
| 3 | Nhập số điện, số nước | OK |
| 4 | Bấm Lưu | Lưu và tính tiền OK |

---

# 10. Kiểm thử Thông báo

## TC-TB-001: Xem list thông báo

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Thông báo | Hiện list |
| 2 | Kiểm tra | Tiêu đề, Ngày tạo, Người tạo, Trạng thái |

## TC-TB-002: Tạo thông báo

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Tạo thông báo | Form hiện |
| 2 | Điền tiêu đề, nội dung | OK |
| 3 | Chọn đối tượng nhận | OK |
| 4 | Bấm Gửi | Gửi thành công |

## TC-TB-003: Sửa/Xóa thông báo

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Sửa hoặc xóa | OK |

---

# 11. Kiểm thử Phản ánh (phía Admin)

## TC-PA-001: Xem list phản ánh

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Phản ánh | Hiện list phản ánh từ cư dân |
| 2 | Kiểm tra | Người gửi, Tiêu đề, Nội dung, Trạng thái |
| 3 | Lọc theo trạng thái | Chờ xử lý, Đang xử lý, Đã xử lý |

## TC-PA-002: Phản hồi phản ánh

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn 1 phản ánh | Xem chi tiết |
| 2 | Nhập nội dung phản hồi | OK |
| 3 | Đổi trạng thái | OK |
| 4 | Bấm Gửi phản hồi | Lưu thành công |

---

# 12. Kiểm thử Báo cáo

## TC-BC-001: Xem báo cáo tổng hợp

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Báo cáo | Trang báo cáo hiện |
| 2 | Xem thống kê tổng thu | Biểu đồ, số liệu hiện ra |
| 3 | Xem thống kê nợ | OK |
| 4 | Xem tỷ lệ hoàn thành | OK |

## TC-BC-002: Lọc theo thời gian

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn khoảng thời gian | OK |
| 2 | Bấm Xem | Data cập nhật theo khoảng đã chọn |

## TC-BC-003: Xuất báo cáo

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bấm Xuất Excel/PDF | File được tải về |
| 2 | Mở file | Data đúng |

---

# 13. Kiểm thử Portal Cư dân

## TC-CD-001: Xem thông tin cá nhân

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login bằng tài khoản cư dân | OK |
| 2 | Xem trang thông tin | Hiện họ tên, căn hộ, liên hệ |

## TC-CD-002: Xem lịch sử thanh toán

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào Lịch sử thanh toán | Hiện list hóa đơn của hộ |
| 2 | Kiểm tra | Đợt thu, Tổng tiền, Trạng thái |
| 3 | Lọc | Lọc được theo trạng thái |

## TC-CD-003: Thanh toán online

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn HD chưa thanh toán | OK |
| 2 | Bấm Thanh toán | Chuyển sang trang thanh toán |
| 3 | Chọn VNPay | Chuyển sang cổng VNPay |
| 4 | Hoàn tất trên VNPay | Quay về app, trạng thái đổi thành Đã thanh toán |

## TC-CD-004: Thanh toán thất bại

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Bắt đầu thanh toán | OK |
| 2 | Hủy trên VNPay | Quay về app, báo lỗi |
| 3 | Kiểm tra trạng thái HD | Vẫn là Chưa thanh toán |

## TC-CD-005: Gửi phản ánh

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Phản ánh | Hiện list phản ánh đã gửi |
| 2 | Bấm Gửi phản ánh mới | Form hiện |
| 3 | Điền tiêu đề, nội dung | OK |
| 4 | Chọn loại phản ánh | OK |
| 5 | Bấm Gửi | Gửi OK, xuất hiện trong list với trạng thái Chờ xử lý |

## TC-CD-006: Xem phản hồi từ BQT

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Chọn phản ánh đã có phản hồi | Xem chi tiết |
| 2 | Kiểm tra | Hiện nội dung phản hồi, thời gian |

## TC-CD-007: Xem thông báo

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Vào menu Thông báo | Hiện list thông báo |
| 2 | Click vào 1 thông báo | Xem nội dung chi tiết |
| 3 | Kiểm tra | Đánh dấu đã đọc |

---

# 14. Kiểm thử Phân quyền

## TC-PQ-001: Admin có full quyền

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login Admin | OK |
| 2 | Truy cập tất cả menu | Vào được hết |

## TC-PQ-002: Kế toán chỉ có quyền kế toán

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login Accountant | OK |
| 2 | Kiểm tra menu | Chỉ có: Loại phí, Đợt thu, Hóa đơn, Chỉ số, Báo cáo |
| 3 | Vào URL /ho-dan trực tiếp | Bị chặn hoặc báo lỗi không có quyền |

## TC-PQ-003: Cư dân chỉ vào được Portal

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Login Resident | OK |
| 2 | Kiểm tra menu | Chỉ có: Thông tin, Lịch sử TT, Phản ánh, Thông báo |
| 3 | Vào URL admin | Bị chặn |

---

# 15. Kiểm thử Giao diện

## TC-UI-001: Responsive

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Test trên Desktop 1920x1080 | OK |
| 2 | Test trên Tablet 768x1024 | Menu collapse, layout OK |
| 3 | Test trên Mobile 375x667 | Mobile friendly |

## TC-UI-002: Validation lỗi

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Submit form với data sai | Hiện lỗi dưới từng trường |

## TC-UI-003: Loading

| Bước | Thao tác | Kết quả mong muốn |
|------|----------|-------------------|
| 1 | Thao tác cần thời gian | Hiện loading |
| 2 | Xong | Loading mất, data hiện |

---

# Tổng kết

| Module | So TC |
|--------|-------|
| Dang nhap/Dang ky | 7 |
| Quan ly Ho dan | 6 |
| Quan ly Nhan khau | 4 |
| Tam tru | 4 |
| Tam vang | 3 |
| Loai phi | 4 |
| Dot thu | 3 |
| Hoa don | 4 |
| Chi so dien nuoc | 2 |
| Thong bao | 3 |
| Phan anh | 2 |
| Bao cao | 3 |
| Portal Cu dan | 7 |
| Phan quyen | 3 |
| Giao dien | 3 |
| Tong | 58 |

## Moi truong test

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Database: SQL Server
- Browser: Chrome, Firefox, hoac Edge ban moi nhat
