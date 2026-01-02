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
    if (typeof response.data === 'string' && response.data.length > 0) {
      try {
        response.data = JSON.parse(response.data);
      } catch (parseError) {
        console.warn("Failed to parse JSON string in response:", parseError);
        // Return empty object/array to prevent downstream errors
        response.data = {};
      }
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = "/login";
    }
    
    // Xử lý lỗi validation (400/422) - trích xuất message rõ ràng
    if (error.response?.status === 400 || error.response?.status === 422) {
      const data = error.response.data;
      
      // Nếu có nhiều lỗi validation, tạo danh sách
      if (data && typeof data === 'string' && data.includes('Validation failed')) {
        // Parse lỗi từ Spring validation
        const fieldErrors = [];
        const matches = data.matchAll(/\[Field error.*?default message \[([^\]]+)\]\]/g);
        for (const match of matches) {
          if (match[1]) fieldErrors.push(match[1]);
        }
        
        if (fieldErrors.length > 0) {
          error.response.data = {
            message: 'Lỗi validation:\n' + fieldErrors.map((e, i) => `${i + 1}. ${e}`).join('\n'),
            errors: fieldErrors
          };
        }
      }
      
      // Nếu data.message chứa nhiều lỗi Spring
      if (data?.message && data.message.includes('Validation failed')) {
        const fieldErrors = [];
        const matches = data.message.matchAll(/default message \[([^\]]+)\]/g);
        for (const match of matches) {
          // Bỏ qua tên trường, chỉ lấy message
          if (match[1] && !match[1].match(/^[a-z]+$/i)) {
            fieldErrors.push(match[1]);
          }
        }
        
        if (fieldErrors.length > 0) {
          error.response.data = {
            message: fieldErrors.join('\n'),
            errors: fieldErrors
          };
        }
      }
    }
    
    return Promise.reject(error);
  }
);

export default axiosClient;
