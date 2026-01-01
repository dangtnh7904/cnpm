import axiosClient from "./axiosClient";

const invoiceService = {
  getInvoiceHtml: async (idHoaDon) => {
    const response = await axiosClient.get(`/invoice/${idHoaDon}/html`);
    return response.data;
  },

  getInvoicePdf: async (idHoaDon) => {
    const response = await axiosClient.get(`/invoice/${idHoaDon}/pdf`, {
      responseType: "blob",
    });
    return response.data;
  },

  downloadInvoice: async (idHoaDon) => {
    const blob = await invoiceService.getInvoicePdf(idHoaDon);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `hoa-don-${idHoaDon}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
  },
};

export default invoiceService;

