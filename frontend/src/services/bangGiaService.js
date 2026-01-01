import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Bảng giá dịch vụ theo tòa nhà.
 * 
 * CHỨC NĂNG:
 * - Cấu hình giá riêng cho từng loại phí tại từng tòa nhà.
 * - Lấy bảng giá của tòa nhà.
 * - Lấy đơn giá áp dụng với logic ưu tiên.
 */

const BASE_URL = "/bang-gia";

// ===== Cấu hình giá =====

/**
 * Cấu hình giá hàng loạt cho một tòa nhà.
 * 
 * @param {number} toaNhaId - ID tòa nhà
 * @param {Array} danhSachGia - Danh sách [{ loaiPhiId, donGiaRieng, ghiChu }]
 */
export const cauHinhGia = async (toaNhaId, danhSachGia) => {
  const response = await axiosClient.post(`${BASE_URL}/cau-hinh`, {
    toaNhaId,
    danhSachGia,
  });
  return response.data;
};

/**
 * Upsert một bảng giá đơn lẻ.
 * 
 * @param {number} toaNhaId - ID tòa nhà
 * @param {number} loaiPhiId - ID loại phí
 * @param {number} donGiaRieng - Đơn giá riêng
 * @param {string} ghiChu - Ghi chú (optional)
 */
export const upsertBangGia = async (toaNhaId, loaiPhiId, donGiaRieng, ghiChu = null) => {
  const response = await axiosClient.post(`${BASE_URL}/upsert`, {
    loaiPhiId,
    donGiaRieng,
    ghiChu,
  }, {
    params: { toaNhaId }
  });
  return response.data;
};

// ===== Lấy bảng giá =====

/**
 * Lấy tất cả bảng giá.
 */
export const getAll = async () => {
  const response = await axiosClient.get(BASE_URL);
  return response.data;
};

/**
 * Lấy tất cả bảng giá của một tòa nhà (bao gồm loại phí chưa có giá riêng).
 * 
 * @param {number} toaNhaId - ID tòa nhà
 * @returns {Array} Danh sách bảng giá với giá mặc định và giá riêng
 */
export const getByToaNhaFull = async (toaNhaId) => {
  const response = await axiosClient.get(`${BASE_URL}/toa-nha/${toaNhaId}`);
  return response.data;
};

/**
 * Lấy bảng giá đã cấu hình của một tòa nhà.
 * 
 * @param {number} toaNhaId - ID tòa nhà
 */
export const getByToaNhaConfigured = async (toaNhaId) => {
  const response = await axiosClient.get(`${BASE_URL}/toa-nha/${toaNhaId}/configured`);
  return response.data;
};

/**
 * Lấy bảng giá của một loại phí (tại tất cả tòa nhà).
 * 
 * @param {number} loaiPhiId - ID loại phí
 */
export const getByLoaiPhi = async (loaiPhiId) => {
  const response = await axiosClient.get(`${BASE_URL}/loai-phi/${loaiPhiId}`);
  return response.data;
};

/**
 * Lấy đơn giá áp dụng (với logic ưu tiên).
 * 
 * LOGIC:
 * 1. Nếu có giá riêng trong BangGiaDichVu -> trả về giá riêng.
 * 2. Nếu không có -> trả về giá mặc định từ LoaiPhi.
 * 
 * @param {number} loaiPhiId - ID loại phí
 * @param {number} toaNhaId - ID tòa nhà
 * @returns {Object} { loaiPhiId, toaNhaId, donGia, isCustomPrice }
 */
export const getDonGiaApDung = async (loaiPhiId, toaNhaId) => {
  const response = await axiosClient.get(`${BASE_URL}/don-gia`, {
    params: { loaiPhiId, toaNhaId }
  });
  return response.data;
};

// ===== Xóa bảng giá =====

/**
 * Xóa một bảng giá theo ID.
 * 
 * @param {number} id - ID bảng giá
 */
export const deleteById = async (id) => {
  const response = await axiosClient.delete(`${BASE_URL}/${id}`);
  return response.data;
};

/**
 * Xóa bảng giá theo loại phí và tòa nhà.
 * 
 * @param {number} toaNhaId - ID tòa nhà
 * @param {number} loaiPhiId - ID loại phí
 */
export const deleteByLoaiPhiAndToaNha = async (toaNhaId, loaiPhiId) => {
  const response = await axiosClient.delete(`${BASE_URL}/toa-nha/${toaNhaId}/loai-phi/${loaiPhiId}`);
  return response.data;
};

/**
 * Reset tất cả bảng giá của một tòa nhà về giá mặc định.
 * 
 * @param {number} toaNhaId - ID tòa nhà
 */
export const resetToaNha = async (toaNhaId) => {
  const response = await axiosClient.delete(`${BASE_URL}/toa-nha/${toaNhaId}/reset`);
  return response.data;
};

// Export default object
const bangGiaService = {
  cauHinhGia,
  upsertBangGia,
  getAll,
  getByToaNhaFull,
  getByToaNhaConfigured,
  getByLoaiPhi,
  getDonGiaApDung,
  deleteById,
  deleteByLoaiPhiAndToaNha,
  resetToaNha,
};

export default bangGiaService;
