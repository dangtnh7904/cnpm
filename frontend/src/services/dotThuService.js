import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Đợt Thu phí.
 * 
 * CHỨC NĂNG:
 * - CRUD đợt thu
 * - Tìm kiếm đợt thu theo điều kiện
 * - Lấy danh sách đợt thu cho dropdown
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
  const response = await axiosClient.get(BASE_URL, {
    params: { page: 0, size: 100 },
  });
  // API trả về Page object, lấy content
  return response.data?.content || response.data || [];
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
 * @param {Object} data - { tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc }
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
  const { tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc, page = 0, size = 20 } = params;
  const response = await axiosClient.get(`${BASE_URL}/search`, {
    params: { tenDotThu, loaiDotThu, ngayBatDau, ngayKetThuc, page, size },
  });
  return response.data?.content || response.data || [];
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
};

export default dotThuService;
