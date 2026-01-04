import axiosClient from "./axiosClient";

const BASE_URL = "/user-toa-nha";

/**
 * Service quản lý liên kết User với Tòa nhà.
 * 
 * - Manager gắn user vào tòa nhà để xem thông báo & nộp tiền
 * - User tự thoát khỏi tòa nhà
 */
const userToaNhaService = {
  /**
   * Gắn user vào tòa nhà (bằng username).
   * POST /api/user-toa-nha
   */
  addUserToBuilding: async (username, toaNhaId) => {
    const res = await axiosClient.post(BASE_URL, { username, toaNhaId });
    return res.data;
  },

  /**
   * Xóa user khỏi tòa nhà (Manager hoặc Admin).
   * DELETE /api/user-toa-nha/{userId}/{toaNhaId}
   */
  removeUserFromBuilding: async (userId, toaNhaId) => {
    const res = await axiosClient.delete(`${BASE_URL}/${userId}/${toaNhaId}`);
    return res.data;
  },

  /**
   * Lấy danh sách user trong tòa nhà.
   * GET /api/user-toa-nha/toa-nha/{toaNhaId}
   */
  getUsersInBuilding: async (toaNhaId) => {
    const res = await axiosClient.get(`${BASE_URL}/toa-nha/${toaNhaId}`);
    return res.data;
  },

  /**
   * Tìm user trong tòa nhà theo username.
   * GET /api/user-toa-nha/toa-nha/{toaNhaId}/search?username=abc
   */
  searchUsersInBuilding: async (toaNhaId, username = "") => {
    const res = await axiosClient.get(`${BASE_URL}/toa-nha/${toaNhaId}/search`, {
      params: { username },
    });
    return res.data;
  },

  /**
   * Lấy danh sách tòa nhà của user hiện tại.
   * GET /api/user-toa-nha/my-buildings
   */
  getMyBuildings: async () => {
    const res = await axiosClient.get(`${BASE_URL}/my-buildings`);
    return res.data;
  },

  /**
   * User tự thoát khỏi tòa nhà.
   * DELETE /api/user-toa-nha/leave/{toaNhaId}
   */
  leaveBuilding: async (toaNhaId) => {
    const res = await axiosClient.delete(`${BASE_URL}/leave/${toaNhaId}`);
    return res.data;
  },
};

export default userToaNhaService;
