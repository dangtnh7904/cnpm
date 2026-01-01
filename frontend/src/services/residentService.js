import axiosClient from "./axiosClient";

const residentService = {
  // Admin management methods
  getAll: async (page = 0, size = 50) => {
    try {
      const response = await axiosClient.get("/nhan-khau", { params: { page, size } });

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
      if (data) {
        return [data];
      }
      return [];
    } catch (error) {
      console.error("Error fetching residents:", error);
      return [];
    }
  },

  getTotalCount: async () => {
    try {
      const response = await axiosClient.get("/nhan-khau", { params: { page: 0, size: 1 } });
      let data = response.data;
      if (typeof data === 'string') {
        data = JSON.parse(data);
      }
      if (data && typeof data.totalElements === 'number') {
        return data.totalElements;
      }
      return 0;
    } catch (error) {
      console.error("Error fetching total resident count:", error);
      return 0;
    }
  },

  getById: async (id) => {
    const response = await axiosClient.get(`/nhan-khau/${id}`);
    return response.data;
  },

  create: async (data, hoGiaDinhId) => {
    // Gửi hoGiaDinhId trong body (theo NhanKhauRequestDTO)
    // Loại bỏ các field không cần thiết (trangThai, hoGiaDinh object)
    const payload = {
      hoGiaDinhId: hoGiaDinhId,
      hoTen: data.hoTen,
      soCCCD: data.soCCCD,
      ngaySinh: data.ngaySinh,
      gioiTinh: data.gioiTinh,
      soDienThoai: data.soDienThoai,
      email: data.email,
      quanHeVoiChuHo: data.quanHeVoiChuHo || "Chủ hộ",
      ngayChuyenDen: data.ngayChuyenDen
    };
    const response = await axiosClient.post("/nhan-khau", payload);
    return response.data;
  },

  update: async (id, data) => {
    // Gửi đúng format theo NhanKhauRequestDTO
    // Loại bỏ trangThai vì backend không cho phép thay đổi qua API này
    const payload = {
      hoGiaDinhId: data.hoGiaDinh?.id || data.idHoGiaDinh,
      hoTen: data.hoTen,
      soCCCD: data.soCCCD,
      ngaySinh: data.ngaySinh,
      gioiTinh: data.gioiTinh,
      soDienThoai: data.soDienThoai,
      email: data.email,
      quanHeVoiChuHo: data.quanHeVoiChuHo,
      ngayChuyenDen: data.ngayChuyenDen
    };
    const response = await axiosClient.put(`/nhan-khau/${id}`, payload);
    return response.data;
  },

  delete: async (id) => {
    await axiosClient.delete(`/nhan-khau/${id}`);
  },

  getOptions: async () => {
    const data = await residentService.getAll(0, 100);
    return data.map((r) => ({ label: `${r.hoTen} (${r.soCCCD})`, value: r.id }));
  },

  // Resident portal methods
  getPaymentHistory: async (idHoGiaDinh, page = 0, size = 50) => {
    try {
      const response = await axiosClient.get(`/resident/ho-gia-dinh/${idHoGiaDinh}/lich-su-thanh-toan`, {
        params: { page, size },
      });

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

      const content = data?.content || data;
      return Array.isArray(content) ? content : (content ? [content] : []);
    } catch (error) {
      console.error("Error fetching payment history:", error);
      return [];
    }
  },

  getPaymentDetails: async (idHoaDon) => {
    try {
      const response = await axiosClient.get(`/resident/hoa-don/${idHoaDon}/chi-tiet`);
      const data = response.data;
      return Array.isArray(data) ? data : (data ? [data] : []);
    } catch (error) {
      console.error("Error fetching payment details:", error);
      return [];
    }
  },

  getCurrentDebt: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/resident/ho-gia-dinh/${idHoGiaDinh}/cong-no`);
    return response.data;
  },

  getPhanAnh: async (idHoGiaDinh, page = 0, size = 50) => {
    const response = await axiosClient.get(`/resident/ho-gia-dinh/${idHoGiaDinh}/phan-anh`, {
      params: { page, size },
    });
    return response.data.content || response.data;
  },
};

export default residentService;
