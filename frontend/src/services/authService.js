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

  signup: async (username, password, fullName, email, role) => {
    const response = await axiosClient.post("/auth/signup", {
      username,
      password,
      fullName,
      email,
      role,
    });
    return response.data;
  },

  getAllUsers: async () => {
    const response = await axiosClient.get("/users");
    return response.data;
  },

  updateUser: async (id, data) => {
    const response = await axiosClient.put(`/users/${id}`, data);
    return response.data;
  },

  deleteUser: async (id) => {
    const response = await axiosClient.delete(`/users/${id}`);
    return response.data;
  },
};

export default authService;
