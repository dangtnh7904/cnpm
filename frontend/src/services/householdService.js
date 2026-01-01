import axiosClient from "./axiosClient";

const householdService = {
  getAll: async (page = 0, size = 50) => {
    try {
      const response = await axiosClient.get("/ho-gia-dinh", { params: { page, size } });

      // Fix: Parse JSON string if response.data is a string
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
      // Fallback: if data is already an array
      if (Array.isArray(data)) {
        return data;
      }
      // If data is a single object, wrap it in array
      if (data && typeof data === 'object' && data !== null) {
        return [data];
      }
      return [];
    } catch (error) {
      console.error("Error fetching households:", error);
      console.error("Error response:", error.response?.data);
      return []; // Return empty array on error
    }
  },

  getTotalCount: async () => {
    try {
      // Fetch 1 item just to get the totalElements metadata
      const response = await axiosClient.get("/ho-gia-dinh", { params: { page: 0, size: 1 } });
      let data = response.data;
      if (typeof data === 'string') {
        data = JSON.parse(data);
      }
      if (data && typeof data.totalElements === 'number') {
        return data.totalElements;
      }
      return 0;
    } catch (error) {
      console.error("Error fetching total household count:", error);
      return 0;
    }
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
    try {
      const data = await householdService.getAll(0, 100);
      if (Array.isArray(data)) {
        return data.map((h) => ({
          label: `${h.maHoGiaDinh} - ${h.tenChuHo || 'Chưa có chủ hộ'}`,
          value: h.id
        }));
      }
      return [];
    } catch (error) {
      console.error("Error fetching household options:", error);
      return [];
    }
  },
};

export default householdService;
