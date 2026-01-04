import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Chỉ số Điện Nước.
 * 
 * LOGIC NGHIỆP VỤ MỚI (Tách rời ghi số và thu tiền):
 * - Ghi chỉ số theo Tháng/Năm, không phụ thuộc Đợt thu
 * - Khi tạo Đợt thu có phí Điện/Nước: Hệ thống query ChiSoDienNuoc để tính tiền
 * 
 * API Endpoints:
 * - GET /api/chi-so/prepare-input  : Lấy danh sách nhập liệu theo Tháng/Năm/Tòa nhà
 * - POST /api/chi-so/save-all      : Lưu chỉ số hàng loạt
 * - GET /api/chi-so/statistics     : Lấy thống kê
 */

const BASE_URL = "/chi-so";

/**
 * Lấy danh sách căn hộ cần ghi chỉ số cho tháng/năm.
 * Tự động điền chỉ số cũ từ tháng trước.
 * 
 * @param {number} thang - Tháng ghi sổ (1-12)
 * @param {number} nam - Năm ghi sổ
 * @param {number} toaNhaId - ID tòa nhà
 * @param {number} loaiPhiId - ID loại phí (Điện hoặc Nước)
 * @returns {Array} Danh sách ChiSoInputDTO
 * 
 * Response:
 * [
 *   {
 *     hoGiaDinhId: 1,
 *     maHoGiaDinh: "A101",
 *     tenChuHo: "Nguyễn Văn A",
 *     soCanHo: "101",
 *     chiSoCu: 1000,   // Chỉ số tháng trước
 *     chiSoMoi: null,  // Chưa ghi hoặc đã ghi
 *     trangThai: "Chưa nhập" | "Đã chốt"
 *   }
 * ]
 */
export const getDanhSachGhiChiSo = async (thang, nam, toaNhaId, loaiPhiId) => {
  const params = { thang, nam, toaNhaId, loaiPhiId };
  const response = await axiosClient.get(`${BASE_URL}/prepare-input`, { params });
  return response.data || [];
};

/**
 * Lưu chỉ số hàng loạt.
 * CHỈ LƯU CHỈ SỐ - KHÔNG TÍNH TIỀN.
 * Việc tính tiền sẽ thực hiện khi "Chốt sổ/Tính tiền" trong Đợt thu.
 * 
 * @param {number} thang - Tháng ghi sổ
 * @param {number} nam - Năm ghi sổ
 * @param {number} toaNhaId - ID tòa nhà
 * @param {number} loaiPhiId - ID loại phí
 * @param {Array} danhSachChiSo - Danh sách [{ hoGiaDinhId, chiSoMoi }]
 * @returns {Object} { success, message, savedCount }
 */
export const saveChiSo = async (thang, nam, toaNhaId, loaiPhiId, danhSachChiSo) => {
  const response = await axiosClient.post(`${BASE_URL}/save-all`, {
    thang,
    nam,
    toaNhaId,
    loaiPhiId,
    danhSachChiSo,
  });
  return response.data;
};

/**
 * Lấy thống kê nhập chỉ số trong tháng/năm.
 * 
 * @param {number} thang - Tháng
 * @param {number} nam - Năm
 * @param {number} toaNhaId - ID tòa nhà
 * @param {number} loaiPhiId - ID loại phí
 * @returns {Object} { thang, nam, tongSoHo, daNhap, chuaNhap, phanTramHoanThanh }
 */
export const getStatistics = async (thang, nam, toaNhaId, loaiPhiId) => {
  const response = await axiosClient.get(`${BASE_URL}/statistics`, {
    params: { thang, nam, toaNhaId, loaiPhiId },
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
  saveChiSo,
  getStatistics,
  getLoaiPhiBienDoi,
};

export default dienNuocService;
