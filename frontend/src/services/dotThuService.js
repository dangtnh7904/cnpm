import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Đợt Thu phí.
 * 
 * CHỨC NĂNG:
 * - CRUD đợt thu (mỗi đợt thuộc 1 tòa nhà)
 * - Tìm kiếm đợt thu theo điều kiện
 * - Quản lý loại phí trong đợt thu
 * - Kiểm tra phí biến đổi (Điện/Nước) để hiện Tab Ghi Chỉ Số
 */

const BASE_URL = "/dot-thu";

/**
 * Lấy danh sách đợt thu (phân trang).
 */
export const getAll = async (page = 0, size = 20) => {
  const response = await axiosClient.get(BASE_URL, {
    params: { page, size },
  });
  return response.data;
};

/**
 * Lấy danh sách đợt thu cho dropdown (không phân trang).
 */
export const getAllForDropdown = async () => {
  try {
    const response = await axiosClient.get(BASE_URL, {
      params: { page: 0, size: 100 },
    });
    // API trả về Page object, lấy content
    const result = response.data?.content || response.data || [];
    return Array.isArray(result) ? result : [];
  } catch (error) {
    console.error('getAllForDropdown error:', error);
    return [];
  }
};

/**
 * Lấy đợt thu theo ID.
 */
export const getById = async (id) => {
  const response = await axiosClient.get(`${BASE_URL}/${id}`);
  return response.data;
};

/**
 * Tạo đợt thu mới.
 * @param {Object} data - { tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc, toaNha: { id } }
 */
export const create = async (data) => {
  const response = await axiosClient.post(BASE_URL, data);
  return response.data;
};

/**
 * Cập nhật đợt thu.
 */
export const update = async (id, data) => {
  const response = await axiosClient.put(`${BASE_URL}/${id}`, data);
  return response.data;
};

/**
 * Xóa đợt thu.
 */
export const remove = async (id) => {
  const response = await axiosClient.delete(`${BASE_URL}/${id}`);
  return response.data;
};

/**
 * Tìm kiếm đợt thu.
 */
export const search = async (params = {}) => {
  const { tenDotThu, loaiDotThu, toaNhaId, ngayBatDau, ngayKetThuc, page = 0, size = 20 } = params;
  const response = await axiosClient.get(`${BASE_URL}/search`, {
    params: { tenDotThu, loaiDotThu, toaNhaId, ngayBatDau, ngayKetThuc, page, size },
  });
  return response.data?.content || response.data || [];
};

// ===== Quản lý loại phí trong đợt thu =====

/**
 * Lấy danh sách loại phí trong đợt thu.
 */
export const getFeesInPeriod = async (dotThuId) => {
  const response = await axiosClient.get(`${BASE_URL}/${dotThuId}/fees`);
  return response.data || [];
};

/**
 * Thêm loại phí vào đợt thu.
 */
export const addFeeToPeriod = async (dotThuId, loaiPhiId) => {
  const response = await axiosClient.post(`${BASE_URL}/${dotThuId}/fees/${loaiPhiId}`);
  return response.data;
};

/**
 * Xóa loại phí khỏi đợt thu.
 */
export const removeFeeFromPeriod = async (dotThuId, loaiPhiId) => {
  const response = await axiosClient.delete(`${BASE_URL}/${dotThuId}/fees/${loaiPhiId}`);
  return response.data;
};

/**
 * Kiểm tra loại phí có phải bắt buộc không.
 */
export const isMandatoryFee = async (loaiPhiId) => {
  const response = await axiosClient.get(`${BASE_URL}/fees/${loaiPhiId}/is-mandatory`);
  return response.data?.isMandatory || false;
};

/**
 * Kiểm tra đợt thu có chứa phí biến đổi (Điện/Nước) không.
 * Dùng để quyết định hiển thị Tab Ghi Chỉ Số.
 */
export const hasUtilityFee = async (dotThuId) => {
  try {
    const response = await axiosClient.get(`${BASE_URL}/${dotThuId}/has-utility-fee`);
    return response.data?.hasUtilityFee || false;
  } catch (error) {
    console.error('hasUtilityFee error:', error);
    return false;
  }
};

/**
 * Lấy danh sách phí biến đổi (Điện/Nước) trong đợt thu.
 */
export const getUtilityFees = async (dotThuId) => {
  try {
    const response = await axiosClient.get(`${BASE_URL}/${dotThuId}/utility-fees`);
    return response.data || [];
  } catch (error) {
    console.error('getUtilityFees error:', error);
    return [];
  }
};

// Export default object
const dotThuService = {
  getAll,
  getAllForDropdown,
  getById,
  create,
  update,
  remove,
  search,
  getFeesInPeriod,
  addFeeToPeriod,
  removeFeeFromPeriod,
  isMandatoryFee,
  hasUtilityFee,
  getUtilityFees,
};

export default dotThuService;
