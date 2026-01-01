import axiosClient from "./axiosClient";

const tamTruService = {
  getAll: async (params = {}) => {
    // params: { hoTen, page, size }
    const response = await axiosClient.get("/tam-tru", { params });
    return response.data.content || response.data;
  },

  getTotalCount: async () => {
    try {
      const response = await axiosClient.get("/tam-tru", { params: { page: 0, size: 1 } });
      let data = response.data;
      // Handle potential string response although less likely here based on getAll
      if (typeof data === 'string') {
        data = JSON.parse(data);
      }
      if (data && typeof data.totalElements === 'number') {
        return data.totalElements;
      }
      return 0;
    } catch (error) {
      console.error("Error fetching total tam tru count:", error);
      return 0;
    }
  },

  search: async (hoTen, page = 0, size = 50) => {
    const response = await axiosClient.get("/tam-tru", { params: { hoTen, page, size } });
    return response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/tam-tru/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosClient.post("/tam-tru", data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosClient.put(`/tam-tru/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/tam-tru/${id}`);
  },
};

export default tamTruService;
