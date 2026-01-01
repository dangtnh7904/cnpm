import axiosClient from "./axiosClient";

const reportService = {
  getStatisticsByDotThu: async (idDotThu) => {
    const response = await axiosClient.get(`/report/dot-thu/${idDotThu}`);
    return response.data;
  },

  getCongNoByHoGiaDinh: async (idHoGiaDinh) => {
    const response = await axiosClient.get(`/report/ho-gia-dinh/${idHoGiaDinh}/cong-no`);
    return response.data;
  },

  getStatisticsByMonth: async (year, month) => {
    const response = await axiosClient.get("/report/thang", {
      params: { year, month },
    });
    return response.data;
  },
};

export default reportService;

