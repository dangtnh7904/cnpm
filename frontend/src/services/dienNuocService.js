import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Chỉ số Điện Nước.
 * 
 * CHỨC NĂNG:
 * - Lấy danh sách căn hộ cần ghi chỉ số theo đợt thu, loại phí, tòa nhà
 * - Ghi chỉ số mới
 * - Tính toán và sinh hóa đơn
 * 
 * GHI CHÚ:
 * - Nếu Backend chưa có API này, cần tạo endpoint tương ứng.
 * - API giả định: /api/chi-so-dien-nuoc
 */

const BASE_URL = "/chi-so-dien-nuoc";

/**
 * Lấy danh sách căn hộ cần ghi chỉ số.
 * 
 * @param {number} dotThuId - ID đợt thu
 * @param {number} loaiPhiId - ID loại phí (Điện hoặc Nước)
 * @param {number} toaNhaId - ID tòa nhà
 * @returns {Array} Danh sách căn hộ với chỉ số cũ
 * 
 * Response mẫu:
 * [
 *   {
 *     hoGiaDinhId: 1,
 *     maHoGiaDinh: "A101",
 *     tenChuHo: "Nguyễn Văn A",
 *     chiSoCu: 1000,  // Chỉ số mới của tháng trước
 *     chiSoMoi: null, // Chưa ghi
 *     donGia: 3500,   // Đơn giá áp dụng
 *   }
 * ]
 */
export const getDanhSachGhiChiSo = async (dotThuId, loaiPhiId, toaNhaId) => {
  const response = await axiosClient.get(`${BASE_URL}/danh-sach`, {
    params: { dotThuId, loaiPhiId, toaNhaId },
  });
  return response.data || [];
};

/**
 * Lưu chỉ số điện nước cho một căn hộ.
 * 
 * @param {Object} data - { dotThuId, loaiPhiId, hoGiaDinhId, chiSoCu, chiSoMoi }
 */
export const saveChiSo = async (data) => {
  const response = await axiosClient.post(BASE_URL, data);
  return response.data;
};

/**
 * Lưu chỉ số hàng loạt và tính toán hóa đơn.
 * 
 * @param {number} dotThuId - ID đợt thu
 * @param {number} loaiPhiId - ID loại phí
 * @param {Array} danhSachChiSo - Danh sách [{ hoGiaDinhId, chiSoCu, chiSoMoi }]
 * @returns {Object} Kết quả tính toán và số hóa đơn được tạo
 */
export const saveAndCalculate = async (dotThuId, loaiPhiId, danhSachChiSo) => {
  const response = await axiosClient.post(`${BASE_URL}/save-calculate`, {
    dotThuId,
    loaiPhiId,
    danhSachChiSo,
  });
  return response.data;
};

/**
 * Lấy lịch sử chỉ số của một căn hộ.
 * 
 * @param {number} hoGiaDinhId - ID hộ gia đình
 * @param {number} loaiPhiId - ID loại phí
 * @param {number} limit - Số bản ghi tối đa
 */
export const getLichSuChiSo = async (hoGiaDinhId, loaiPhiId, limit = 12) => {
  const response = await axiosClient.get(`${BASE_URL}/lich-su`, {
    params: { hoGiaDinhId, loaiPhiId, limit },
  });
  return response.data || [];
};

/**
 * Lấy danh sách loại phí biến đổi (có đồng hồ: Điện, Nước).
 * Đây là loại phí tính theo tiêu thụ.
 */
export const getLoaiPhiBienDoi = async () => {
  const response = await axiosClient.get("/loai-phi/active");
  const allActive = response.data || [];
  // Filter những loại phí biến đổi (có thể dựa trên loaiThu hoặc đặc điểm khác)
  // Giả sử loại thu "BienDoi" hoặc tên chứa "Điện", "Nước"
  return allActive.filter(lp => 
    lp.loaiThu === "BienDoi" || 
    lp.tenLoaiPhi?.toLowerCase().includes("điện") ||
    lp.tenLoaiPhi?.toLowerCase().includes("nước")
  );
};

// Export default object
const dienNuocService = {
  getDanhSachGhiChiSo,
  saveChiSo,
  saveAndCalculate,
  getLichSuChiSo,
  getLoaiPhiBienDoi,
};

export default dienNuocService;
