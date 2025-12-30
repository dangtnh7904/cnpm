import axiosClient from "./axiosClient";

const tamVangService = {
  getAll: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/tam-vang", { params: { page, size } });
    return response.data.content || response.data;
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
