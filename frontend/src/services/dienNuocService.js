import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Chỉ số Điện Nước.
 * 
 * CHỨC NĂNG:
 * - Lấy danh sách căn hộ cần ghi chỉ số theo đợt thu, loại phí, tòa nhà
 * - Ghi chỉ số mới
 * - Tính toán và sinh hóa đơn
 * 
 * API Endpoints:
 * - GET /api/chi-so/prepare-input  : Lấy danh sách nhập liệu
 * - POST /api/chi-so/save-all      : Lưu chỉ số hàng loạt
 * - GET /api/chi-so/statistics     : Lấy thống kê
 */

const BASE_URL = "/chi-so";

/**
 * Lấy danh sách căn hộ cần ghi chỉ số.
 * Tự động điền chỉ số cũ từ đợt thu trước.
 * 
 * @param {number} dotThuId - ID đợt thu
 * @param {number} loaiPhiId - ID loại phí (Điện hoặc Nước)
 * @param {number} toaNhaId - ID tòa nhà (optional)
 * @returns {Array} Danh sách ChiSoInputDTO
 * 
 * Response:
 * [
 *   {
 *     hoGiaDinhId: 1,
 *     maHoGiaDinh: "A101",
 *     tenChuHo: "Nguyễn Văn A",
 *     soCanHo: "101",
 *     chiSoCu: 1000,   // Chỉ số mới của tháng trước
 *     chiSoMoi: null,  // Chưa ghi hoặc đã ghi
 *     trangThai: "Chưa nhập" | "Đã chốt",
 *     donGia: 3500,    // Đơn giá áp dụng
 *     tieuThu: 50,     // chiSoMoi - chiSoCu (nếu đã ghi)
 *     thanhTien: 175000, // tieuThu * donGia
 *   }
 * ]
 */
export const getDanhSachGhiChiSo = async (dotThuId, loaiPhiId, toaNhaId) => {
  const params = { dotThuId, loaiPhiId };
  if (toaNhaId) {
    params.toaNhaId = toaNhaId;
  }
  
  const response = await axiosClient.get(`${BASE_URL}/prepare-input`, { params });
  return response.data || [];
};

/**
 * Lưu chỉ số hàng loạt và tự động cập nhật hóa đơn.
 * 
 * @param {number} dotThuId - ID đợt thu
 * @param {number} loaiPhiId - ID loại phí
 * @param {Array} danhSachChiSo - Danh sách [{ hoGiaDinhId, chiSoCu, chiSoMoi }]
 * @returns {Object} { success, message, savedCount }
 */
export const saveAndCalculate = async (dotThuId, loaiPhiId, danhSachChiSo) => {
  const response = await axiosClient.post(`${BASE_URL}/save-all`, {
    dotThuId,
    loaiPhiId,
    danhSachChiSo,
  });
  return response.data;
};

/**
 * Lấy thống kê nhập chỉ số trong đợt thu.
 * 
 * @param {number} dotThuId - ID đợt thu
 * @param {number} loaiPhiId - ID loại phí
 * @returns {Object} { tongSo, daNhap, chuaNhap, phanTramHoanThanh }
 */
export const getStatistics = async (dotThuId, loaiPhiId) => {
  const response = await axiosClient.get(`${BASE_URL}/statistics`, {
    params: { dotThuId, loaiPhiId },
  });
  return response.data;
};

/**
 * Lấy danh sách loại phí biến đổi (có đồng hồ: Điện, Nước).
 * Đây là loại phí tính theo tiêu thụ.
 */
export const getLoaiPhiBienDoi = async () => {
  const response = await axiosClient.get("/loai-phi/active");
  const allActive = response.data || [];
  // Filter những loại phí biến đổi (Điện, Nước)
  return allActive.filter(lp => 
    lp.tenLoaiPhi?.toLowerCase().includes("điện") ||
    lp.tenLoaiPhi?.toLowerCase().includes("nước")
  );
};

// Export default object
const dienNuocService = {
  getDanhSachGhiChiSo,
  saveAndCalculate,
  getStatistics,
  getLoaiPhiBienDoi,
};

export default dienNuocService;
