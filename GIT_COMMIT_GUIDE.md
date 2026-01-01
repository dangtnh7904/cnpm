# HƯỚNG DẪN COMMIT CODE LÊN GIT THEO PHÂN CÔNG

## Quy tắc chung:
1. **Luôn pull trước khi push**: `git pull origin main` hoặc `git pull origin develop`
2. **Commit message rõ ràng**: Mô tả ngắn gọn những gì đã làm
3. **Không commit file nhạy cảm**: `.env`, `application.properties` có password, token
4. **Tạo branch riêng cho feature**: `git checkout -b feature/ten-feature`

---

## PHÂN CÔNG THEO SPRINT VÀ NGƯỜI THỰC HIỆN

### 🎯 **TRỊNH THIÊN LAM** (Project Manager)

#### Sprint 1:
- ✅ **Thiết kế CSDL** - File: `database/sql_base.sql`
  ```bash
  git add database/sql_base.sql
  git commit -m "feat: Thiết kế CSDL cho HoGiaDinh, NhanKhau, TamTru, TamVang"
  ```

- ✅ **API CRUD hộ dân, nhân khẩu** - Files:
  - `backend/.../controller/HoGiaDinhController.java`
  - `backend/.../controller/NhanKhauController.java`
  - `backend/.../service/HoGiaDinhService.java`
  - `backend/.../service/NhanKhauService.java`
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/HoGiaDinhController.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/NhanKhauController.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/HoGiaDinhService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/NhanKhauService.java
  git commit -m "feat: API CRUD hộ gia đình và nhân khẩu"
  ```

#### Sprint 2:
- ✅ **Thiết kế CSDL quản lý phí** - File: `database/sql_base.sql` (phần LoaiPhi, DotThu, DinhMucThu, HoaDon)
  ```bash
  git add database/sql_base.sql
  git commit -m "feat: Thiết kế CSDL cho quản lý phí và thanh toán"
  ```

- ✅ **Tích hợp VNPay/Momo** - Files:
  - `backend/.../config/VnPayConfig.java` (tạo mới)
  - `backend/.../service/PaymentService.java` (tạo mới)
  - `backend/.../controller/PaymentController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/config/VnPayConfig.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/PaymentService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/PaymentController.java
  git commit -m "feat: Tích hợp cổng thanh toán VNPay/Momo"
  ```

#### Sprint 3:
- ✅ **API báo cáo tài chính** - Files:
  - `backend/.../service/ReportService.java` (tạo mới)
  - `backend/.../controller/ReportController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/ReportService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/ReportController.java
  git commit -m "feat: API báo cáo tài chính (thu, nợ, tỷ lệ hoàn thành)"
  ```

#### Sprint 4:
- ✅ **Phân tích luồng phản ánh** - File: `docs/phan-anh-flow.md` (tạo mới)
  ```bash
  git add docs/phan-anh-flow.md
  git commit -m "docs: Phân tích luồng dữ liệu phản ánh - phản hồi"
  ```

- ✅ **API phản ánh** - Files:
  - `backend/.../service/PhanAnhService.java` (tạo mới)
  - `backend/.../controller/PhanAnhController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/PhanAnhService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/PhanAnhController.java
  git commit -m "feat: API quản lý phản ánh và phản hồi"
  ```

#### Sprint 5:
- ✅ **API sao lưu dữ liệu** - Files:
  - `backend/.../service/BackupService.java` (tạo mới)
  - `backend/.../controller/BackupController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/BackupService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/BackupController.java
  git commit -m "feat: API sao lưu và khôi phục dữ liệu tự động"
  ```

---

### 🎨 **ĐOÀN THANH HẢI** (UI/UX Designer & QA)

#### Sprint 1:
- ✅ **Wireframe giao diện** - Files:
  - `docs/wireframes/household-management.png` (tạo mới)
  - `docs/wireframes/tam-tru-tam-vang.png` (tạo mới)
  ```bash
  git add docs/wireframes/
  git commit -m "docs: Wireframe giao diện quản lý hộ khẩu, tạm trú, tạm vắng"
  ```

- ✅ **Kịch bản kiểm thử** - File: `docs/test-cases/sprint1-test-cases.md` (tạo mới)
  ```bash
  git add docs/test-cases/sprint1-test-cases.md
  git commit -m "docs: Kịch bản kiểm thử CRUD hộ dân, tạm trú, tạm vắng"
  ```

#### Sprint 2:
- ✅ **Wireframe quản lý phí** - Files:
  - `docs/wireframes/fee-management.png` (tạo mới)
  - `docs/wireframes/payment-online.png` (tạo mới)
  ```bash
  git add docs/wireframes/fee-management.png
  git add docs/wireframes/payment-online.png
  git commit -m "docs: Wireframe giao diện quản lý phí và thanh toán"
  ```

- ✅ **Kịch bản kiểm thử thanh toán** - File: `docs/test-cases/sprint2-payment-test.md` (tạo mới)
  ```bash
  git add docs/test-cases/sprint2-payment-test.md
  git commit -m "docs: Kịch bản kiểm thử chức năng thanh toán"
  ```

#### Sprint 3:
- ✅ **Wireframe báo cáo** - Files:
  - `docs/wireframes/report-dashboard.png` (tạo mới)
  - `docs/wireframes/invoice-generation.png` (tạo mới)
  ```bash
  git add docs/wireframes/report-dashboard.png
  git add docs/wireframes/invoice-generation.png
  git commit -m "docs: Wireframe giao diện báo cáo và sinh hóa đơn"
  ```

- ✅ **Kịch bản kiểm thử báo cáo** - File: `docs/test-cases/sprint3-report-test.md` (tạo mới)
  ```bash
  git add docs/test-cases/sprint3-report-test.md
  git commit -m "docs: Kịch bản kiểm thử báo cáo và thông báo"
  ```

#### Sprint 4:
- ✅ **Wireframe trang cư dân** - File: `docs/wireframes/resident-portal.png` (tạo mới)
  ```bash
  git add docs/wireframes/resident-portal.png
  git commit -m "docs: Wireframe trang thông tin và phản ánh cư dân"
  ```

#### Sprint 5:
- ✅ **Thiết kế CSDL phân quyền** - File: `database/sql_base.sql` (phần Users, Roles)
  ```bash
  git add database/sql_base.sql
  git commit -m "feat: Thiết kế CSDL cho quản lý tài khoản và phân quyền"
  ```

---

### 💻 **NGUYỄN THỊ TUYẾT MAI** (Backend Developer)

#### Sprint 1:
- ✅ **Models Entity** - Files:
  - `backend/.../entity/HoGiaDinh.java`
  - `backend/.../entity/NhanKhau.java`
  - `backend/.../entity/TamTru.java`
  - `backend/.../entity/TamVang.java`
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/
  git commit -m "feat: Tạo Entity models cho HoGiaDinh, NhanKhau, TamTru, TamVang"
  ```

#### Sprint 2:
- ✅ **Models quản lý phí** - Files:
  - `backend/.../entity/LoaiPhi.java`
  - `backend/.../entity/DotThu.java`
  - `backend/.../entity/DinhMucThu.java`
  - `backend/.../entity/HoaDon.java`
  - `backend/.../entity/ChiTietHoaDon.java`
  - `backend/.../entity/LichSuThanhToan.java`
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/LoaiPhi.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/DotThu.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/DinhMucThu.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/HoaDon.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/ChiTietHoaDon.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/entity/LichSuThanhToan.java
  git commit -m "feat: Tạo Entity models cho quản lý phí và thanh toán"
  ```

- ✅ **API loại phí và định mức thu** - Files:
  - `backend/.../service/LoaiPhiService.java`
  - `backend/.../service/DinhMucThuService.java` (tạo mới)
  - `backend/.../controller/LoaiPhiController.java`
  - `backend/.../controller/DinhMucThuController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/LoaiPhiService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/DinhMucThuService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/LoaiPhiController.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/DinhMucThuController.java
  git commit -m "feat: API CRUD loại phí và định mức thu"
  ```

#### Sprint 3:
- ✅ **API báo cáo và hóa đơn** - Files:
  - `backend/.../service/HoaDonService.java` (tạo mới)
  - `backend/.../service/InvoiceService.java` (tạo mới - sinh PDF)
  - `backend/.../controller/HoaDonController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/HoaDonService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/InvoiceService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/HoaDonController.java
  git commit -m "feat: API quản lý hóa đơn và sinh PDF"
  ```

- ✅ **API gửi thông báo email** - Files:
  - `backend/.../service/NotificationService.java` (tạo mới)
  - `backend/.../controller/NotificationController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/NotificationService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/NotificationController.java
  git commit -m "feat: API gửi thông báo email tự động"
  ```

#### Sprint 4:
- ✅ **API lịch sử thanh toán cư dân** - Files:
  - `backend/.../service/ResidentService.java` (tạo mới - cho cư dân)
  - `backend/.../controller/ResidentPortalController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/ResidentService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/ResidentPortalController.java
  git commit -m "feat: API cho cư dân xem lịch sử thanh toán"
  ```

#### Sprint 5:
- ✅ **API đăng ký, đăng nhập, phân quyền** - Files:
  - `backend/.../service/AuthService.java` (đã có, cần cập nhật)
  - `backend/.../controller/AuthController.java` (đã có, cần cập nhật)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/AuthService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/AuthController.java
  git commit -m "feat: Cập nhật API đăng nhập và phân quyền RBAC"
  ```

---

### 🎨 **DƯƠNG ANH QUÂN** (Frontend Developer)

#### Sprint 1:
- ✅ **Form thêm/sửa hộ khẩu, nhân khẩu** - Files:
  - `frontend/src/pages/Resident/HouseholdFormModal.jsx`
  - `frontend/src/pages/Resident/ResidentFormModal.jsx`
  ```bash
  git add frontend/src/pages/Resident/HouseholdFormModal.jsx
  git add frontend/src/pages/Resident/ResidentFormModal.jsx
  git commit -m "feat: Form thêm/sửa hộ khẩu và nhân khẩu với validation"
  ```

#### Sprint 2:
- ✅ **Giao diện cập nhật thanh toán** - Files:
  - `frontend/src/pages/Payment/PaymentUpdatePage.jsx` (tạo mới)
  - `frontend/src/services/paymentService.js` (tạo mới)
  ```bash
  git add frontend/src/pages/Payment/PaymentUpdatePage.jsx
  git add frontend/src/services/paymentService.js
  git commit -m "feat: Giao diện cập nhật thanh toán cho cư dân"
  ```

#### Sprint 3:
- ✅ **Giao diện quản lý và gửi hóa đơn** - Files:
  - `frontend/src/pages/Invoice/InvoiceManagementPage.jsx` (tạo mới)
  - `frontend/src/services/invoiceService.js` (tạo mới)
  ```bash
  git add frontend/src/pages/Invoice/InvoiceManagementPage.jsx
  git add frontend/src/services/invoiceService.js
  git commit -m "feat: Giao diện quản lý và gửi hóa đơn PDF"
  ```

- ✅ **Giao diện gửi thông báo** - Files:
  - `frontend/src/pages/Notification/NotificationPage.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/Notification/NotificationPage.jsx
  git commit -m "feat: Giao diện gửi thông báo và nhắc hạn"
  ```

#### Sprint 4:
- ✅ **Giao diện xem lịch sử thanh toán** - Files:
  - `frontend/src/pages/ResidentPortal/PaymentHistoryPage.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/ResidentPortal/PaymentHistoryPage.jsx
  git commit -m "feat: Giao diện cư dân xem lịch sử thanh toán"
  ```

- ✅ **Giao diện gửi phản ánh** - Files:
  - `frontend/src/pages/ResidentPortal/FeedbackPage.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/ResidentPortal/FeedbackPage.jsx
  git commit -m "feat: Giao diện cư dân gửi phản ánh và xem phản hồi"
  ```

#### Sprint 5:
- ✅ **Giao diện sao lưu dữ liệu** - Files:
  - `frontend/src/pages/Admin/BackupPage.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/Admin/BackupPage.jsx
  git commit -m "feat: Giao diện sao lưu và khôi phục dữ liệu"
  ```

---

### 💻 **VŨ THÙY DƯƠNG** (Full-stack Developer)

#### Sprint 1:
- ✅ **API tạm trú/tạm vắng** - Files:
  - `backend/.../service/TamTruService.java`
  - `backend/.../service/TamVangService.java`
  - `backend/.../controller/TamTruController.java`
  - `backend/.../controller/TamVangController.java`
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/TamTruService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/TamVangService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/TamTruController.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/TamVangController.java
  git commit -m "feat: API CRUD tạm trú và tạm vắng"
  ```

- ✅ **Form quản lý tạm trú/tạm vắng** - Files:
  - `frontend/src/pages/Registration/TamTruFormModal.jsx`
  - `frontend/src/pages/Registration/TamVangFormModal.jsx`
  ```bash
  git add frontend/src/pages/Registration/TamTruFormModal.jsx
  git add frontend/src/pages/Registration/TamVangFormModal.jsx
  git commit -m "feat: Form quản lý tạm trú và tạm vắng"
  ```

#### Sprint 2:
- ✅ **API cập nhật thanh toán** - Files:
  - `backend/.../service/ThanhToanService.java` (tạo mới)
  - `backend/.../controller/ThanhToanController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/ThanhToanService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/ThanhToanController.java
  git commit -m "feat: API cập nhật trạng thái thanh toán"
  ```

- ✅ **Giao diện thanh toán trực tuyến** - Files:
  - `frontend/src/pages/Payment/OnlinePaymentPage.jsx` (tạo mới)
  - `frontend/src/components/Payment/VnPayButton.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/Payment/OnlinePaymentPage.jsx
  git add frontend/src/components/Payment/VnPayButton.jsx
  git commit -m "feat: Giao diện thanh toán trực tuyến VNPay/Momo"
  ```

#### Sprint 3:
- ✅ **API sinh hóa đơn PDF** - Files:
  - `backend/.../service/InvoiceService.java` (tạo mới - phần PDF)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/InvoiceService.java
  git commit -m "feat: API sinh hóa đơn PDF tự động"
  ```

- ✅ **Kiểm thử API thống kê, hóa đơn** - File: `docs/test-results/sprint3-api-test.md` (tạo mới)
  ```bash
  git add docs/test-results/sprint3-api-test.md
  git commit -m "test: Kết quả kiểm thử API thống kê và hóa đơn"
  ```

#### Sprint 4:
- ✅ **API phản ánh và phản hồi** - Files:
  - `backend/.../service/PhanAnhService.java` (tạo mới)
  - `backend/.../controller/PhanAnhController.java` (tạo mới)
  ```bash
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/service/PhanAnhService.java
  git add backend/quanlychungcu/src/main/java/com/nhom33/quanlychungcu/controller/PhanAnhController.java
  git commit -m "feat: API cư dân gửi phản ánh và BQT phản hồi"
  ```

- ✅ **Kiểm thử phản ánh** - File: `docs/test-results/sprint4-feedback-test.md` (tạo mới)
  ```bash
  git add docs/test-results/sprint4-feedback-test.md
  git commit -m "test: Kết quả kiểm thử chức năng phản ánh"
  ```

#### Sprint 5:
- ✅ **Giao diện quản lý tài khoản** - Files:
  - `frontend/src/pages/Admin/UserManagementPage.jsx` (tạo mới)
  ```bash
  git add frontend/src/pages/Admin/UserManagementPage.jsx
  git commit -m "feat: Giao diện quản lý tài khoản và phân quyền"
  ```

---

## 📋 QUY TRÌNH COMMIT CHUNG:

### 1. Trước khi commit:
```bash
# Kiểm tra trạng thái
git status

# Xem thay đổi
git diff

# Pull code mới nhất
git pull origin main  # hoặc develop
```

### 2. Commit:
```bash
# Add files
git add <file1> <file2> ...

# Commit với message rõ ràng
git commit -m "feat: Mô tả ngắn gọn chức năng"
# hoặc
git commit -m "fix: Sửa lỗi..."
git commit -m "docs: Cập nhật tài liệu..."
git commit -m "test: Thêm test case..."
```

### 3. Push:
```bash
# Push lên branch của mình
git push origin feature/ten-feature

# Hoặc push lên main/develop (nếu có quyền)
git push origin main
```

---

## 🏷️ QUY ƯỚC COMMIT MESSAGE:

- `feat:` - Tính năng mới
- `fix:` - Sửa lỗi
- `docs:` - Tài liệu
- `style:` - Format code (không ảnh hưởng logic)
- `refactor:` - Refactor code
- `test:` - Test
- `chore:` - Công việc bảo trì

---

## ⚠️ LƯU Ý:

1. **KHÔNG commit file nhạy cảm:**
   - `.env`
   - `application.properties` có password
   - File chứa API key, token

2. **Luôn test trước khi commit:**
   - Chạy backend: `mvn spring-boot:run`
   - Chạy frontend: `npm start`
   - Test API với Postman

3. **Nếu có conflict:**
   ```bash
   git pull origin main
   # Giải quyết conflict trong file
   git add <file-conflict>
   git commit -m "fix: Resolve merge conflict"
   ```

---

## 📞 HỖ TRỢ:

Nếu gặp vấn đề, liên hệ:
- **Trịnh Thiên Lam** (PM): 0946878356
- Hoặc tạo issue trên Git repository

