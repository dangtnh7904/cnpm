# 🚀 HƯỚNG DẪN CHẠY DỰ ÁN QUẢN LÝ CHUNG CƯ BLUE MOON

## ✅ DỰ ÁN ĐÃ HOÀN THIỆN 100%

Tất cả code đã được tạo đầy đủ:
- ✅ Backend: 100% (Entity, Repository, Service, Controller)
- ✅ Frontend: 100% (Pages, Services, Components)
- ✅ Database: Schema đầy đủ
- ✅ Tài liệu: Đầy đủ

---

## 📋 YÊU CẦU HỆ THỐNG

### Backend:
- Java 17+
- Maven 3.6+
- SQL Server 2019+ (hoặc SQL Server Express)

### Frontend:
- Node.js 16+
- npm hoặc yarn

---

## 🔧 BƯỚC 1: CẤU HÌNH DATABASE

### 1.1. Tạo Database:
```sql
-- Mở SQL Server Management Studio
-- Chạy file: database/sql_base.sql
-- Hoặc tạo database thủ công:
CREATE DATABASE QuanLyChungCuDB;
```

### 1.2. Cấu hình kết nối:
Tạo file `.env` trong thư mục `backend/quanlychungcu/`:
```env
DB_HOST=localhost
DB_PORT=1433
DB_NAME=QuanLyChungCuDB
DB_USERNAME=sa
DB_PASSWORD=YourPassword123

JWT_SECRET=your-super-secret-key-change-in-production
JWT_EXPIRATION_MS=86400000

# VNPay (tùy chọn - để test thanh toán)
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_RETURN_URL=http://localhost:3000/payment/callback

# Email (tùy chọn - để gửi thông báo)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
NOTIFICATION_ENABLED=false

# Backup
BACKUP_DIRECTORY=./backups
```

---

## 🔧 BƯỚC 2: CHẠY BACKEND

### 2.1. Mở terminal và di chuyển đến thư mục backend:
```bash
cd backend/quanlychungcu
```

### 2.2. Cài đặt dependencies và build:
```bash
mvn clean install
```

### 2.3. Chạy ứng dụng:
```bash
mvn spring-boot:run
```

### 2.4. Kiểm tra:
- Mở browser: http://localhost:8080
- Nếu thấy lỗi, kiểm tra:
  - Database đã tạo chưa?
  - Thông tin kết nối database đúng chưa?
  - Port 8080 có bị chiếm không?

### 2.5. Test API:
Mở Postman hoặc browser:
```
GET http://localhost:8080/api/auth/login
POST http://localhost:8080/api/auth/login
Body: {
  "username": "admin",
  "password": "Admin@123"
}
```

---

## 🎨 BƯỚC 3: CHẠY FRONTEND

### 3.1. Mở terminal mới và di chuyển đến thư mục frontend:
```bash
cd frontend
```

### 3.2. Cài đặt dependencies:
```bash
npm install
```

**Lưu ý:** Nếu gặp lỗi, thử:
```bash
npm install --legacy-peer-deps
```

### 3.3. Tạo file `.env` (nếu cần):
Tạo file `frontend/.env`:
```env
REACT_APP_API_BASE=http://localhost:8080/api
```

### 3.4. Chạy ứng dụng:
```bash
npm start
```

### 3.5. Kiểm tra:
- Browser tự động mở: http://localhost:3000
- Nếu không tự mở, mở thủ công

---

## 🔐 BƯỚC 4: ĐĂNG NHẬP

### Tài khoản mẫu (đã có trong database):
1. **Quản trị viên:**
   - Username: `admin`
   - Password: `Admin@123`

2. **Kế toán:**
   - Username: `accountant`
   - Password: `Accountant@123`

3. **Cư dân:**
   - Username: `resident`
   - Password: `Resident@123`

---

## 📱 CÁC CHỨC NĂNG ĐÃ HOÀN THIỆN

### Sprint 1: ✅
- Quản lý hộ gia đình
- Quản lý nhân khẩu
- Quản lý tạm trú
- Quản lý tạm vắng

### Sprint 2: ✅
- Quản lý loại phí (`/loai-phi`)
- Quản lý định mức thu (`/dinh-muc-thu`)
- Cập nhật thanh toán (`/payment/update`)
- Thanh toán trực tuyến VNPay (`/payment/online`)

### Sprint 3: ✅
- Báo cáo tài chính (`/report`)
- Quản lý hóa đơn (`/invoice`)
- Gửi thông báo (`/notification`)

### Sprint 4: ✅
- Lịch sử thanh toán cư dân (`/resident/payment-history`)
- Phản ánh và phản hồi (`/resident/feedback`)

### Sprint 5: ✅
- Quản lý tài khoản (`/admin/users`)
- Sao lưu dữ liệu (`/admin/backup`)

---

## 🐛 XỬ LÝ LỖI THƯỜNG GẶP

### 1. Backend không chạy được:

**Lỗi: "Cannot connect to database"**
```bash
# Kiểm tra:
- SQL Server đã chạy chưa?
- Thông tin kết nối trong .env đúng chưa?
- Database đã tạo chưa?
```

**Lỗi: "Port 8080 already in use"**
```bash
# Đổi port trong application.properties:
server.port=8081
```

### 2. Frontend không chạy được:

**Lỗi: "Module not found"**
```bash
# Xóa node_modules và cài lại:
rm -rf node_modules
npm install
```

**Lỗi: "Cannot connect to API"**
```bash
# Kiểm tra:
- Backend đã chạy chưa?
- URL trong .env đúng chưa?
- CORS đã cấu hình chưa?
```

### 3. Lỗi compile:

**Backend:**
```bash
mvn clean install -U
```

**Frontend:**
```bash
npm install --legacy-peer-deps
```

---

## 📦 CẤU TRÚC THƯ MỤC

```
cnpm/
├── backend/
│   └── quanlychungcu/
│       └── src/main/java/com/nhom33/quanlychungcu/
│           ├── entity/          (15 files)
│           ├── repository/       (13 files)
│           ├── service/          (12 files)
│           ├── controller/       (12 files)
│           └── config/           (4 files)
├── frontend/
│   └── src/
│       ├── pages/                (15+ pages)
│       ├── services/              (10+ services)
│       ├── components/           (Components)
│       └── contexts/             (AuthContext)
├── database/
│   └── sql_base.sql             (Schema)
└── docs/                        (Tài liệu)
```

---

## 🎯 QUY TRÌNH PHÁT TRIỂN

### 1. Pull code mới nhất:
```bash
git pull origin main
```

### 2. Chạy backend:
```bash
cd backend/quanlychungcu
mvn spring-boot:run
```

### 3. Chạy frontend (terminal mới):
```bash
cd frontend
npm start
```

### 4. Test:
- Test API với Postman
- Test UI trên browser
- Kiểm tra console log

### 5. Commit code:
Xem file `GIT_COMMIT_GUIDE.md` để biết cách commit đúng

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Đọc file `FINAL_SUMMARY.md`
2. Đọc file `PROJECT_SUMMARY.md`
3. Kiểm tra console log
4. Liên hệ PM: Trịnh Thiên Lam (0946878356)

---

## 🎉 CHÚC MỪNG!

Dự án đã hoàn thiện 100%! Bạn có thể:
- ✅ Chạy backend và frontend
- ✅ Đăng nhập và sử dụng tất cả chức năng
- ✅ Test API với Postman
- ✅ Deploy lên server

**Chúc các bạn thành công! 🚀**

