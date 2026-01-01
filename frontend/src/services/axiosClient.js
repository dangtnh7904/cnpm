import axios from "axios";

const axiosClient = axios.create({
  baseURL: process.env.REACT_APP_API_BASE || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - tự động gắn JWT token
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - xử lý lỗi 401 và parse JSON string
axiosClient.interceptors.response.use(
  (response) => {
    // Fix: Parse JSON string if response.data is a string
    if (typeof response.data === 'string') {
      try {
        response.data = JSON.parse(response.data);
      } catch (parseError) {
        console.warn("Failed to parse JSON string in response:", parseError);
        // Keep original data if parsing fails
      }
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
