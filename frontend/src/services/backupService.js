import axiosClient from "./axiosClient";

const backupService = {
  createBackup: async () => {
    const response = await axiosClient.post("/backup/create");
    return response.data;
  },

  createBackupZip: async () => {
    const response = await axiosClient.post("/backup/create-zip", null, {
      responseType: "blob",
    });
    const url = window.URL.createObjectURL(response.data);
    const link = document.createElement("a");
    link.href = url;
    link.download = `backup-${new Date().toISOString()}.zip`;
    link.click();
    window.URL.revokeObjectURL(url);
  },

  listBackups: async () => {
    const response = await axiosClient.get("/backup/list");
    return response.data;
  },

  restoreBackup: async (fileName) => {
    const response = await axiosClient.post("/backup/restore", null, {
      params: { fileName },
    });
    return response.data;
  },

  deleteBackup: async (fileName) => {
    await axiosClient.delete(`/backup/${fileName}`);
  },
};

export default backupService;

