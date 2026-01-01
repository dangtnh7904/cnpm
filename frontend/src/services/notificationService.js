import axiosClient from "./axiosClient";

const notificationService = {
  createThongBao: async (data) => {
    const response = await axiosClient.post("/notification/thong-bao", data);
    return response.data;
  },

  sendPaymentReminder: async (idHoaDon) => {
    const response = await axiosClient.post(`/notification/nhac-han/${idHoaDon}`);
    return response.data;
  },

  sendBulkPaymentReminder: async (idDotThu) => {
    const response = await axiosClient.post(`/notification/nhac-han-hang-loat/${idDotThu}`);
    return response.data;
  },

  sendInvoiceByEmail: async (idHoaDon) => {
    const response = await axiosClient.post(`/notification/gui-hoa-don/${idHoaDon}`);
    return response.data;
  },
};

export default notificationService;

