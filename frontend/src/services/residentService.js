import axiosClient from "./axiosClient";

const residentService = {
  getAll: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/nhan-khau", { params: { page, size } });
    return response.data.content || response.data;
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/nhan-khau/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosClient.post("/nhan-khau", data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosClient.put(`/nhan-khau/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/nhan-khau/${id}`);
  },

  getOptions: async () => {
    const data = await residentService.getAll(0, 100);
    return data.map((r) => ({ label: `${r.hoTen} (${r.soCCCD})`, value: r.id }));
  },
};

export default residentService;
