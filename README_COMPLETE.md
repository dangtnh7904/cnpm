# 🎯 HƯỚNG DẪN HOÀN THIỆN DỰ ÁN QUẢN LÝ CHUNG CƯ BLUE MOON

## 📊 TỔNG QUAN

Dự án đã được bổ sung code cơ bản cho **Sprint 2** (Quản lý phí và thanh toán). Các file đã được tạo:

### ✅ ĐÃ TẠO:

#### Backend:
1. **Entity:**
   - ✅ LoaiPhi.java
   - ✅ DotThu.java
   - ✅ DinhMucThu.java
   - ✅ HoaDon.java
   - ✅ ChiTietHoaDon.java
   - ✅ LichSuThanhToan.java
   - ✅ PhanAnh.java
   - ✅ PhanHoi.java
   - ✅ ThongBao.java

2. **Repository:**
   - ✅ LoaiPhiRepository.java
   - ✅ DotThuRepository.java
   - ✅ DinhMucThuRepository.java
   - ✅ HoaDonRepository.java
   - ✅ ChiTietHoaDonRepository.java
   - ✅ LichSuThanhToanRepository.java
   - ✅ PhanAnhRepository.java
   - ✅ PhanHoiRepository.java
   - ✅ ThongBaoRepository.java

3. **Service:**
   - ✅ LoaiPhiService.java
   - ✅ DotThuService.java
   - ✅ DinhMucThuService.java
   - ✅ HoaDonService.java

4. **Controller:**
   - ✅ LoaiPhiController.java
   - ✅ DotThuController.java
   - ✅ DinhMucThuController.java
   - ✅ HoaDonController.java

#### Tài liệu:
- ✅ GIT_COMMIT_GUIDE.md - Hướng dẫn commit cho từng thành viên
- ✅ PROJECT_SUMMARY.md - Tóm tắt dự án và checklist
- ✅ README_COMPLETE.md - File này

---

## 🚀 CÁCH SỬ DỤNG

### 1. Kiểm tra code đã tạo:

```bash
# Backend
cd backend/quanlychungcu
mvn clean compile

# Nếu có lỗi, sửa và chạy lại
mvn spring-boot:run
```

### 2. Test API với Postman:

#### Loại phí:
- `GET /api/loai-phi` - Lấy danh sách
- `POST /api/loai-phi` - Tạo mới
- `PUT /api/loai-phi/{id}` - Cập nhật
- `DELETE /api/loai-phi/{id}` - Xóa

#### Đợt thu:
- `GET /api/dot-thu` - Lấy danh sách
- `POST /api/dot-thu` - Tạo mới
- `PUT /api/dot-thu/{id}` - Cập nhật

#### Định mức thu:
- `GET /api/dinh-muc-thu/ho-gia-dinh/{id}` - Lấy theo hộ
- `POST /api/dinh-muc-thu` - Tạo mới

#### Hóa đơn:
- `POST /api/hoa-don/tao-cho-ho/{idHo}/dot-thu/{idDot}` - Tạo hóa đơn
- `POST /api/hoa-don/{id}/thanh-toan` - Thêm thanh toán
- `GET /api/hoa-don/{id}/lich-su-thanh-toan` - Lịch sử thanh toán

---

## 📝 CẦN BỔ SUNG TIẾP

### 1. Tích hợp VNPay (Ưu tiên cao):

Tạo file: `backend/.../config/VnPayConfig.java`
```java
@Configuration
public class VnPayConfig {
    // Cấu hình VNPay
    // Tạo URL thanh toán
    // Xử lý callback
}
```

Tạo file: `backend/.../service/PaymentService.java`
```java
@Service
public class PaymentService {
    // Tạo payment URL
    // Xử lý callback từ VNPay
    // Cập nhật trạng thái thanh toán
}
```

### 2. Frontend cho quản lý phí:

Tạo các file:
- `frontend/src/pages/Fee/LoaiPhiPage.jsx`
- `frontend/src/pages/Fee/DinhMucThuPage.jsx`
- `frontend/src/pages/Payment/PaymentUpdatePage.jsx`
- `frontend/src/pages/Payment/OnlinePaymentPage.jsx`

### 3. Báo cáo và hóa đơn (Sprint 3):

- ReportService.java - Thống kê thu, nợ
- InvoiceService.java - Sinh PDF
- NotificationService.java - Gửi email

### 4. Chức năng cư dân (Sprint 4):

- PhanAnhService.java
- ResidentPortalService.java
- Frontend trang cư dân

---

## 📋 CHECKLIST CHO TỪNG NGƯỜI

### Trịnh Thiên Lam:
- [ ] Tích hợp VNPay/Momo
- [ ] API báo cáo tài chính
- [ ] API sao lưu dữ liệu

### Đoàn Thanh Hải:
- [ ] Wireframe các giao diện còn thiếu
- [ ] Test cases cho các chức năng mới

### Nguyễn Thị Tuyết Mai:
- [ ] Hoàn thiện các Service còn thiếu
- [ ] API gửi email thông báo
- [ ] API sinh PDF hóa đơn

### Dương Anh Quân:
- [ ] Frontend quản lý phí
- [ ] Frontend thanh toán
- [ ] Frontend báo cáo

### Vũ Thùy Dương:
- [ ] Frontend thanh toán trực tuyến
- [ ] API cập nhật thanh toán
- [ ] Frontend trang cư dân

---

## 🔗 TÀI LIỆU THAM KHẢO

1. **VNPay Integration:**
   - https://sandbox.vnpayment.vn/apis/docs/

2. **Spring Boot Email:**
   - https://spring.io/guides/gs/serving-web-content/

3. **iTextPDF:**
   - https://itextpdf.com/en/resources/guides/itext-7

4. **React Charts:**
   - https://recharts.org/

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Không commit file nhạy cảm:**
   - `.env`
   - `application.properties` có password
   - API keys

2. **Luôn test trước khi commit:**
   - Chạy backend: `mvn spring-boot:run`
   - Chạy frontend: `npm start`
   - Test API với Postman

3. **Theo dõi file GIT_COMMIT_GUIDE.md:**
   - Biết chính xác file nào cần commit
   - Message commit đúng format

4. **Pull trước khi push:**
   ```bash
   git pull origin main
   git add .
   git commit -m "feat: ..."
   git push origin main
   ```

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Đọc file `PROJECT_SUMMARY.md` để biết còn thiếu gì
2. Đọc file `GIT_COMMIT_GUIDE.md` để biết cách commit
3. Liên hệ PM: Trịnh Thiên Lam (0946878356)

---

## 🎉 CHÚC CÁC BẠN HOÀN THÀNH TỐT DỰ ÁN!

