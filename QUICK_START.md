# ⚡ HƯỚNG DẪN CHẠY NHANH

## 🚀 CHẠY TRONG 3 BƯỚC

### Bước 1: Database
```sql
-- Chạy file: database/sql_base.sql
-- Hoặc tạo database: QuanLyChungCuDB
```

### Bước 2: Backend
```bash
cd backend/quanlychungcu
mvn clean install
mvn spring-boot:run
```
✅ Backend chạy tại: 

### Bước 3: Frontend
```bash
cd frontend
npm install
npm start
```
✅ Frontend chạy tại: http://localhost:3000

---

## 🔐 ĐĂNG NHẬP

- **Admin:** `admin` / `Admin@123`
- **Kế toán:** `accountant` / `Accountant@123`

---

## ⚠️ NẾU GẶP LỖI

### Backend không chạy:
1. Kiểm tra SQL Server đã chạy chưa
2. Tạo file `.env` trong `backend/quanlychungcu/`:
```env
DB_HOST=localhost
DB_PORT=1433
DB_NAME=QuanLyChungCuDB
DB_USERNAME=sa
DB_PASSWORD=YourPassword
```

### Frontend không chạy:
```bash
rm -rf node_modules
npm install --legacy-peer-deps
npm start
```

---

## 📚 XEM CHI TIẾT

Đọc file `HUONG_DAN_CHAY_DU_AN.md` để biết chi tiết hơn.

