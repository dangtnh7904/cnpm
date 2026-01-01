import axiosClient from "./axiosClient";

const tamVangService = {
  getAll: async (params = {}) => {
    // params: { noiDen, page, size }
    const response = await axiosClient.get("/tam-vang", { params });
    return response.data.content || response.data;
  },

  getTotalCount: async () => {
    try {
      const response = await axiosClient.get("/tam-vang", { params: { page: 0, size: 1 } });
      let data = response.data;
      if (typeof data === 'string') {
        data = JSON.parse(data);
      }
      if (data && typeof data.totalElements === 'number') {
        return data.totalElements;
      }
      return 0;
    } catch (error) {
      console.error("Error fetching total tam vang count:", error);
      return 0;
    }
  },

  search: async (noiDen, page = 0, size = 50) => {
    const response = await axiosClient.get("/tam-vang", { params: { noiDen, page, size } });
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/tam-vang/${id}`);
    return response.data;
  },

  // Lấy danh sách tạm vắng theo hộ gia đình
  getByHoGiaDinh: async (hoGiaDinhId, page = 0, size = 50) => {
    const response = await axiosClient.get(`/tam-vang/ho-gia-dinh/${hoGiaDinhId}`, {
      params: { page, size }
    });
    return response.data.content || response.data;
  },

  // Đăng ký tạm vắng - gọi đúng endpoint /dang-ky
  create: async (data) => {
    const response = await axiosClient.post("/tam-vang/dang-ky", data);
    return response.data;
  },

  // Alias cho create
  dangKyTamVang: async (data) => {
    const response = await axiosClient.post("/tam-vang/dang-ky", data);
    return response.data;
  },

  // Kết thúc tạm vắng (người đã quay về)
  ketThucTamVang: async (id) => {
    const response = await axiosClient.post(`/tam-vang/${id}/ket-thuc`);
    return response.data;
  },

  // Legacy - không còn dùng
  update: async (id, data) => {
    console.warn("tamVangService.update is deprecated. Use specific APIs instead.");
    const response = await axiosClient.put(`/tam-vang/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/tam-vang/${id}`);
  },
};

export default tamVangService;
