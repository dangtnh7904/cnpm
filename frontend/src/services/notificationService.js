import axiosClient from "./axiosClient";

const notificationService = {
  // ===== CRUD Thông báo =====
  
  /**
   * Lấy danh sách thông báo (phân quyền tự động theo role).
   */
  getAll: async (page = 0, size = 20) => {
    const response = await axiosClient.get("/notification", { params: { page, size } });
    return response.data;
  },

  /**
   * Tìm kiếm thông báo.
   */
  search: async ({ loaiThongBao, tieuDe, page = 0, size = 20 }) => {
    const response = await axiosClient.get("/notification/search", {
      params: { loaiThongBao, tieuDe, page, size },
    });
    return response.data;
  },

  /**
   * Lấy chi tiết thông báo theo ID.
   */
  getById: async (id) => {
    const response = await axiosClient.get(`/notification/${id}`);
    return response.data;
  },

  /**
   * Tạo thông báo hệ thống (Admin).
   */
  createThongBao: async (data) => {
    const response = await axiosClient.post("/notification/thong-bao", data);
    return response.data;
  },

  /**
   * Tạo thông báo cho tòa nhà (Manager).
   */
  createForBuilding: async (toaNhaId, data) => {
    const response = await axiosClient.post(`/notification/toa-nha/${toaNhaId}`, data);
    return response.data;
  },

  /**
   * Xóa thông báo.
   */
  delete: async (id) => {
    const response = await axiosClient.delete(`/notification/${id}`);
    return response.data;
  },

  // ===== Gửi nhắc hạn =====

  /**
   * Gửi thông báo nhắc hạn cho tòa nhà.
   * @param {number} toaNhaId - ID tòa nhà
   * @param {string} tenDotThu - Tên đợt thu (VD: "Tháng 01/2026")
   */
  sendPaymentReminder: async (toaNhaId, tenDotThu) => {
    const response = await axiosClient.post("/notification/nhac-han", null, {
      params: { toaNhaId, tenDotThu }
    });
    return response.data;
  },

  sendInvoiceByEmail: async (idHoaDon) => {
    const response = await axiosClient.post(`/notification/gui-hoa-don/${idHoaDon}`);
    return response.data;
  },
};

export default notificationService;

