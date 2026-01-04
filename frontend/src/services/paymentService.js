import axiosClient from "./axiosClient";

const paymentService = {
  // Hóa đơn
  getAllHoaDon: async (page = 0, size = 50) => {
    const response = await axiosClient.get("/hoa-don", { params: { page, size } });
    return response.data.content || response.data;
  },

  getHoaDonById: async (id) => {
    const response = await axiosClient.get(`/hoa-don/${id}`);
    return response.data;
  },

  createHoaDon: async (idHoGiaDinh, idDotThu) => {
    const response = await axiosClient.post(`/hoa-don/tao-cho-ho/${idHoGiaDinh}/dot-thu/${idDotThu}`);
    return response.data;
  },

  getHoaDonByHoGiaDinh: async (idHoGiaDinh, page = 0, size = 50) => {
    const response = await axiosClient.get(`/hoa-don/ho-gia-dinh/${idHoGiaDinh}`, {
      params: { page, size },
    });
    return response.data.content || response.data;
  },

  /**
   * Lấy hóa đơn của một hộ trong một đợt thu cụ thể.
   * @returns {Object|null} Hóa đơn hoặc {found: false} nếu chưa có
   */
  getHoaDonByDotThu: async (idHoGiaDinh, idDotThu) => {
    const response = await axiosClient.get(`/hoa-don/ho-gia-dinh/${idHoGiaDinh}/dot-thu/${idDotThu}`);
    // Backend trả về HoaDon hoặc {found: false, message: "..."}
    if (response.data.found === false) {
      return null;
    }
    return response.data;
  },

  getLichSuThanhToan: async (idHoaDon) => {
    const response = await axiosClient.get(`/hoa-don/${idHoaDon}/lich-su-thanh-toan`);
    return response.data;
  },

  addPayment: async (idHoaDon, data) => {
    const response = await axiosClient.post(`/hoa-don/${idHoaDon}/thanh-toan`, data);
    return response.data;
  },

  // VNPay
  createVnPayUrl: async (idHoaDon, amount = null) => {
    const params = amount ? { amount } : {};
    const response = await axiosClient.post(`/payment/vnpay/create/${idHoaDon}`, null, { params });
    return response.data.paymentUrl;
  },
};

export default paymentService;

