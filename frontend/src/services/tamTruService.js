import axiosClient from "./axiosClient";

const tamTruService = {
  getAll: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/tam-tru", { params: { page, size } });
    return response.data.content || response.data;
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
