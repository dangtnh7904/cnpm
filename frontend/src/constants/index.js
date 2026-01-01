export const API_BASE_URL = process.env.REACT_APP_API_BASE || "http://localhost:8080/api";

export const ROLES = {
  ADMIN: "ADMIN",
  ACCOUNTANT: "ACCOUNTANT",
};

export const GENDER_OPTIONS = [
  { value: "Nam", label: "Nam" },
  { value: "Nữ", label: "Nữ" },
  { value: "Khác", label: "Khác" },
];

export const RELATIONSHIP_OPTIONS = [
  { value: "Chủ hộ", label: "Chủ hộ" },
  { value: "Vợ/Chồng", label: "Vợ/Chồng" },
  { value: "Con", label: "Con" },
  { value: "Người thân", label: "Người thân" },
  { value: "Thuê nhà", label: "Thuê nhà" },
];

// Quan hệ cho người tạm trú (không có Chủ hộ)
export const TAM_TRU_RELATIONSHIP_OPTIONS = [
  { value: "Họ hàng", label: "Họ hàng" },
  { value: "Bạn bè", label: "Bạn bè" },
  { value: "Thuê nhà", label: "Thuê nhà" },
  { value: "Người giúp việc", label: "Người giúp việc" },
  { value: "Khác", label: "Khác" },
];

export const RESIDENT_STATUS_OPTIONS = [
  { value: "Hoat dong", label: "Đang cư trú" },
  { value: "Tam tru", label: "Tạm trú" },
  { value: "Tam vang", label: "Tạm vắng" },
];

export const HOUSEHOLD_STATUS_OPTIONS = [
  { value: "Hoat dong", label: "Hoạt động" },
  { value: "Tam dung", label: "Tạm dừng" },
];

export const DATE_FORMAT = "YYYY-MM-DD";
