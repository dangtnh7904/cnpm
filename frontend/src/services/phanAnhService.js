import axiosClient from "./axiosClient";

const phanAnhService = {
  getAll: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/phan-anh", { params: { page, size } });
    return response.data.content || response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/phan-anh/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosClient.post("/phan-anh", data);
    return response.data;
  },

  getByHoGiaDinh: async (idHoGiaDinh, page = 0, size = 50) => {
    try {
      const response = await axiosClient.get(`/phan-anh/ho-gia-dinh/${idHoGiaDinh}`, {
        params: { page, size },
      });
      const data = response.data?.content || response.data;
      return Array.isArray(data) ? data : (data ? [data] : []);
    } catch (error) {
      console.error("Error fetching phan anh by ho gia dinh:", error);
      return [];
    }
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

  search: async ({ idHoGiaDinh, trangThai, tieuDe, page = 0, size = 50 }) => {
    const response = await axiosClient.get("/phan-anh/search", {
      params: { idHoGiaDinh, trangThai, tieuDe, page, size },
    });
    return response.data;
  },
};

export default phanAnhService;

