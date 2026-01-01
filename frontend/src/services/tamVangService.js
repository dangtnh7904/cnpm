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

  create: async (data) => {
    const response = await axiosClient.post("/tam-vang", data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosClient.put(`/tam-vang/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/tam-vang/${id}`);
  },
};

export default tamVangService;
