import axiosClient from "./axiosClient";

/**
 * Service: Quản lý Khoản thu & Định mức.
 * 
 * CHỨC NĂNG:
 * - Quản lý Loại phí (CRUD, soft delete)
 * - Quản lý Đợt thu
 * - Quản lý Định mức thu (với tính toán giá)
 */

const feeService = {
  // ===== Loại phí =====

  getAllLoaiPhi: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/loai-phi", { params: { page, size } });
    return response.data.content || response.data;
  },

  getAllLoaiPhiList: async () => {
    const response = await axiosClient.get("/loai-phi/all");
    return response.data;
  },

  getActiveLoaiPhi: async () => {
    const response = await axiosClient.get("/loai-phi/active");
    return response.data;
  },

  getLoaiPhiById: async (id) => {
    const response = await axiosClient.get(`/loai-phi/${id}`);
    return response.data;
  },

  createLoaiPhi: async (data) => {
    const response = await axiosClient.post("/loai-phi", data);
    return response.data;
  },

  createLoaiPhiDTO: async (data) => {
    const response = await axiosClient.post("/loai-phi/dto", data);
    return response.data;
  },

  updateLoaiPhi: async (id, data) => {
    const response = await axiosClient.put(`/loai-phi/${id}`, data);
    return response.data;
  },

  updateLoaiPhiDTO: async (id, data) => {
    const response = await axiosClient.put(`/loai-phi/${id}/dto`, data);
    return response.data;
  },

  updateDonGia: async (id, donGia) => {
    const response = await axiosClient.patch(`/loai-phi/${id}/don-gia`, null, {
      params: { donGia }
    });
    return response.data;
  },

  deleteLoaiPhi: async (id) => {
    await axiosClient.delete(`/loai-phi/${id}`);
  },

  /**
   * Soft delete - Vô hiệu hóa loại phí.
   */
  disableLoaiPhi: async (id) => {
    const response = await axiosClient.patch(`/loai-phi/${id}/disable`);
    return response.data;
  },

  /**
   * Khôi phục loại phí đã vô hiệu hóa.
   */
  restoreLoaiPhi: async (id) => {
    const response = await axiosClient.patch(`/loai-phi/${id}/restore`);
    return response.data;
  },

  searchLoaiPhi: async (params = {}) => {
    try {
      const { tenLoaiPhi, loaiThu, dangHoatDong, page = 0, size = 50 } = params;
      const response = await axiosClient.get("/loai-phi/search", {
        params: { tenLoaiPhi, loaiThu, dangHoatDong, page, size },
      });
      // API trả về dạng phân trang { content: [...] } hoặc mảng trực tiếp
      const result = response.data?.content || response.data || [];
      return Array.isArray(result) ? result : [];
    } catch (error) {
      console.error('searchLoaiPhi error:', error);
      return [];
    }
  },

  // ===== Đợt thu =====

  getAllDotThu: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/dot-thu", { params: { page, size } });
    return response.data.content || response.data;
  },

  createDotThu: async (data) => {
    const response = await axiosClient.post("/dot-thu", data);
    return response.data;
  },

  updateDotThu: async (id, data) => {
    const response = await axiosClient.put(`/dot-thu/${id}`, data);
    return response.data;
  },

  deleteDotThu: async (id) => {
    await axiosClient.delete(`/dot-thu/${id}`);
  },

  // ===== Định mức thu =====

  getDinhMucByHoGiaDinh: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/dinh-muc-thu/ho-gia-dinh/${idHoGiaDinh}`);
    return response.data;
  },

  /**
   * Lấy định mức kèm giá đã tính.
   * Response: [{ dinhMuc, donGiaApDung, thanhTien, isCustomPrice }]
   */
  getDinhMucWithPrice: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/dinh-muc-thu/ho-gia-dinh/${idHoGiaDinh}/with-price`);
    return response.data;
  },

  /**
   * Lấy tổng tiền của hộ gia đình.
   */
  getTongTien: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/dinh-muc-thu/ho-gia-dinh/${idHoGiaDinh}/tong-tien`);
    return response.data;
  },

  createDinhMuc: async (data) => {
    const response = await axiosClient.post("/dinh-muc-thu", data);
    return response.data;
  },

  /**
   * Tạo định mức hàng loạt cho tất cả hộ gia đình trong tòa nhà.
   */
  createDinhMucBulk: async (toaNhaId, loaiPhiId, soLuong = 1) => {
    const response = await axiosClient.post(`/dinh-muc-thu/bulk/toa-nha/${toaNhaId}`, null, {
      params: { loaiPhiId, soLuong }
    });
    return response.data;
  },

  updateDinhMuc: async (id, data) => {
    const response = await axiosClient.put(`/dinh-muc-thu/${id}`, data);
    return response.data;
  },

  updateSoLuong: async (id, soLuong) => {
    const response = await axiosClient.patch(`/dinh-muc-thu/${id}/so-luong`, null, {
      params: { soLuong }
    });
    return response.data;
  },

  /**
   * Cập nhật số lượng hàng loạt cho tất cả hộ gia đình trong tòa nhà.
   */
  updateDinhMucBulk: async (toaNhaId, loaiPhiId, soLuong) => {
    const response = await axiosClient.patch(`/dinh-muc-thu/bulk/toa-nha/${toaNhaId}`, null, {
      params: { loaiPhiId, soLuong }
    });
    return response.data;
  },

  deleteDinhMuc: async (id) => {
    await axiosClient.delete(`/dinh-muc-thu/${id}`);
  },
};

export default feeService;


