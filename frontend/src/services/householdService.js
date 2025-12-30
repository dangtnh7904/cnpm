import axiosClient from "./axiosClient";

const householdService = {
  getAll: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/ho-gia-dinh", { params: { page, size } });
    return response.data.content || response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/ho-gia-dinh/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosClient.post("/ho-gia-dinh", data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosClient.put(`/ho-gia-dinh/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/ho-gia-dinh/${id}`);
  },

  getOptions: async () => {
    const data = await householdService.getAll(0, 100);
    return data.map((h) => ({ label: h.maHoGiaDinh, value: h.id }));
  },
};

export default householdService;
