import axiosClient from "./axiosClient";

const feeService = {
  // Loại phí
  getAllLoaiPhi: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/loai-phi", { params: { page, size } });
    return response.data.content || response.data;
  },

  getActiveLoaiPhi: async () => {
    const response = await axiosClient.get("/loai-phi/active");
    return response.data;
  },

  createLoaiPhi: async (data) => {
    const response = await axiosClient.post("/loai-phi", data);
    return response.data;
  },

  updateLoaiPhi: async (id, data) => {
    const response = await axiosClient.put(`/loai-phi/${id}`, data);
    return response.data;
  },

  deleteLoaiPhi: async (id) => {
    await axiosClient.delete(`/loai-phi/${id}`);
  },

  searchLoaiPhi: async ({ tenLoaiPhi, loaiThu, dangHoatDong, page = 0, size = 50 }) => {
    const response = await axiosClient.get("/loai-phi/search", {
      params: { tenLoaiPhi, loaiThu, dangHoatDong, page, size },
    });
    return response.data;
  },

  // Đợt thu
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

  // Định mức thu
  getDinhMucByHoGiaDinh: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/dinh-muc-thu/ho-gia-dinh/${idHoGiaDinh}`);
    return response.data;
  },

  createDinhMuc: async (data) => {
    const response = await axiosClient.post("/dinh-muc-thu", data);
    return response.data;
  },

  updateDinhMuc: async (id, data) => {
    const response = await axiosClient.put(`/dinh-muc-thu/${id}`, data);
    return response.data;
  },

  deleteDinhMuc: async (id) => {
    await axiosClient.delete(`/dinh-muc-thu/${id}`);
  },
};

export default feeService;

