import axiosClient from "./axiosClient";

const authService = {
  login: async (username, password) => {
    const response = await axiosClient.post("/auth/login", { username, password });
    return response.data;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
  },

  getCurrentUser: () => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    const username = localStorage.getItem("username");
    if (token && role && username) {
      return { token, role, username };
    }
    return null;
  },

  saveUser: (userData) => {
    localStorage.setItem("token", userData.token);
    localStorage.setItem("role", userData.role);
    localStorage.setItem("username", userData.username);
  },

  isAuthenticated: () => {
    return !!localStorage.getItem("token");
  },

  isAdmin: () => {
    return localStorage.getItem("role") === "ADMIN";
  },

  isAccountant: () => {
    return localStorage.getItem("role") === "ACCOUNTANT";
  },
};

export default authService;
