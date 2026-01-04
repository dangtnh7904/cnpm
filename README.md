# HE THONG QUAN LY CHUNG CU BLUE MOON

## Gioi thieu

He thong quan ly va thu phi chung cu Blue Moon - Du an mon Cong nghe phan mem, Nhom 33, Dai hoc Bach Khoa Ha Noi.

**Cong nghe su dung:**
- Backend: Spring Boot 3.2.0 (Java 17)
- Frontend: React 18.2.0 + Ant Design 5
- Database: SQL Server
- Authentication: JWT

---

## Huong dan cai dat va chay du an

### Buoc 1: Cai dat cac cong cu can thiet

- Java JDK 17 tro len
- Node.js 18 tro len
- SQL Server 2019 tro len
- Maven 3.8 tro len
- Git

### Buoc 2: Clone du an

```bash
git clone <repository-url>
cd cnpm
```

### Buoc 3: Tao Database

1. Mo SQL Server Management Studio (SSMS)
2. Tao database moi ten: `QuanLyChungCuDB`
3. Chay file SQL de tao bang va du lieu mau:

```sql
-- Mo va chay file: database/sql_base.sql
```

### Buoc 4: Cau hinh Backend

1. Vao thu muc backend:
```bash
cd backend/quanlychungcu
```

2. Tao file `.env` tu file mau:
```bash
copy .env.example .env
```

3. Mo file `.env` va sua lai thong tin ket noi database:
```
DB_HOST=localhost
DB_PORT=1433
DB_NAME=QuanLyChungCuDB
DB_USERNAME=sa
DB_PASSWORD=<mat_khau_sql_server_cua_ban>
```

4. Chay backend:
```bash
mvn spring-boot:run
```

Backend se chay tai: http://localhost:8080

### Buoc 5: Cau hinh Frontend

1. Mo terminal moi, vao thu muc frontend:
```bash
cd frontend
```

2. Cai dat dependencies:
```bash
npm install
```

3. Chay frontend:
```bash
npm start
```

Frontend se chay tai: http://localhost:3000

---

## Tai khoan dang nhap thu nghiem

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@123 |
| Manager | manager | Manager@123 |
| Accountant | accountant | Accountant@123 |
| Resident | resident | Resident@123 |

---

## Cau truc thu muc

```
cnpm/
├── backend/
│   └── quanlychungcu/
│       ├── src/main/java/...    # Source code Java
│       ├── src/main/resources/  # Config files
│       ├── pom.xml              # Maven dependencies
│       └── .env                 # Bien moi truong (tu tao)
├── frontend/
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── pages/               # Cac trang
│   │   ├── services/            # API services
│   │   └── contexts/            # React contexts
│   └── package.json             # NPM dependencies
├── database/
│   ├── sql_base.sql             # Script tao database
│   └── ...                      # Cac file migration khac
└── docs/                        # Tai lieu
```

---

## Cac chuc nang chinh

### Quan ly nhan khau
- Quan ly ho gia dinh
- Quan ly nhan khau trong ho
- Dang ky tam tru, tam vang

### Quan ly phi
- Tao va quan ly cac loai phi (dien, nuoc, gui xe, dich vu...)
- Cau hinh bang gia theo dinh muc
- Tao dot thu phi
- Ghi chi so dien nuoc

### Thanh toan
- Cap nhat thanh toan (tien mat)
- Thanh toan truc tuyen qua VNPay
- Xem lich su thanh toan

### Thong bao va phan anh
- Gui thong bao den cu dan
- Cu dan gui phan anh
- Ban quan ly phan hoi phan anh

### Bao cao
- Bao cao doanh thu
- Thong ke thanh toan
- Xuat bao cao PDF

### Quan tri he thong
- Quan ly tai khoan nguoi dung
- Phan quyen (Admin, Manager, Accountant, Resident)
- Sao luu du lieu

---

## Xu ly loi thuong gap

### Loi ket noi database
- Kiem tra SQL Server da chay chua
- Kiem tra thong tin trong file `.env` dung chua
- Kiem tra database `QuanLyChungCuDB` da ton tai chua

### Loi port bi chiem
- Backend: Kiem tra port 8080 co bi chiem khong
- Frontend: Kiem tra port 3000 co bi chiem khong

### Loi npm install
- Xoa thu muc `node_modules` va file `package-lock.json`
- Chay lai `npm install`

---

## Thanh vien nhom 33

- Trinh Thien Lam - Project Manager
- Doan Thanh Hai - UI/UX Designer & QA
- Nguyen Thi Tuyet Mai - Backend Developer
- Duong Anh Quan - Frontend Developer
- Vu Thuy Duong - Full-stack Developer

---

## Lien he

- PM: Trinh Thien Lam (0946878356)
