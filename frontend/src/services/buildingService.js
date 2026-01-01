import axiosClient from "./axiosClient";

const buildingService = {
  getAll: async (page = 0, size = 50) => {
    try {
      const response = await axiosClient.get("/toa-nha", { params: { page, size } });
      
      let data = response.data;
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch (parseError) {
          console.error("Error parsing JSON string:", parseError);
          return [];
        }
      }

      // Backend returns Page object with 'content' property containing the array
      if (data && data.content && Array.isArray(data.content)) {
        return data.content;
      }
      if (Array.isArray(data)) {
        return data;
      }
      if (data && typeof data === 'object' && data !== null) {
        return [data];
      }
      return [];
    } catch (error) {
      console.error("Error fetching buildings:", error);
      return [];
    }
  },

  // Get all buildings for dropdown (no pagination)
  getAllForDropdown: async () => {
    try {
      const response = await axiosClient.get("/toa-nha/all");
      return response.data || [];
    } catch (error) {
      console.error("Error fetching buildings for dropdown:", error);
      return [];
    }
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/toa-nha/${id}`);
    return response.data;
  },

  create: async (data) => {
    const response = await axiosClient.post("/toa-nha", data);
    return response.data;
  },

  update: async (id, data) => {
    const response = await axiosClient.put(`/toa-nha/${id}`, data);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/toa-nha/${id}`);
  },

  getOptions: async () => {
    try {
      const data = await buildingService.getAllForDropdown();
      if (Array.isArray(data)) {
        return data.map((b) => ({
          label: b.tenToaNha,
          value: b.id,
        }));
      }
      return [];
    } catch (error) {
      console.error("Error getting building options:", error);
      return [];
    }
  },
};

export default buildingService;
