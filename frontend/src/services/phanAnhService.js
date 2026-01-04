import axiosClient from "./axiosClient";

const phanAnhService = {
  /**
   * Lấy danh sách phản ánh (phân quyền tự động).
   * - ADMIN: Xem tất cả
   * - MANAGER: Xem phản ánh của tòa nhà mình
   * - RESIDENT: Xem phản ánh của mình
   */
  getAll: async (page = 0, size = 20) => {
    const response = await axiosClient.get("/phan-anh", { params: { page, size } });
    return response.data;
  },

  /**
   * Lấy phản ánh của user hiện tại.
   */
  getMyPhanAnh: async (page = 0, size = 20) => {
    const response = await axiosClient.get("/phan-anh/my", { params: { page, size } });
    return response.data;
  },

  /**
   * Lấy phản ánh theo tòa nhà (cho Manager).
   */
  getByToaNha: async (toaNhaId, page = 0, size = 20) => {
    const response = await axiosClient.get(`/phan-anh/toa-nha/${toaNhaId}`, { 
      params: { page, size } 
    });
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/phan-anh/${id}`);
    return response.data;
  },

  /**
   * Tạo phản ánh mới.
   * @param {Object} data - { toaNhaId, tieuDe, noiDung }
   */
  create: async (data) => {
    const response = await axiosClient.post("/phan-anh", data);
    return response.data;
  },

  getPhanHoi: async (idPhanAnh) => {
    try {
      const response = await axiosClient.get(`/phan-anh/${idPhanAnh}/phan-hoi`);
      const data = response.data;
      return Array.isArray(data) ? data : (data ? [data] : []);
    } catch (error) {
      console.error("Error fetching phan hoi:", error);
      return [];
    }
  },

  addPhanHoi: async (idPhanAnh, data) => {
    const response = await axiosClient.post(`/phan-anh/${idPhanAnh}/phan-hoi`, data);
    return response.data;
  },

  updateTrangThai: async (id, trangThai) => {
    const response = await axiosClient.put(`/phan-anh/${id}/trang-thai`, { trangThai });
    return response.data;
  },

  /**
   * Tìm kiếm phản ánh.
   * @param {Object} params - { toaNhaId, trangThai, tieuDe, page, size }
   */
  search: async ({ toaNhaId, trangThai, tieuDe, page = 0, size = 20 }) => {
    const response = await axiosClient.get("/phan-anh/search", {
      params: { toaNhaId, trangThai, tieuDe, page, size },
    });
    return response.data;
  },
};

export default phanAnhService;

