export const API_BASE_URL = process.env.REACT_APP_API_BASE || "http://localhost:8080/api";

export const ROLES = {
  ADMIN: "ADMIN",
  ACCOUNTANT: "ACCOUNTANT",
};

export const GENDER_OPTIONS = [
  { value: "Nam", label: "Nam" },
  { value: "Nu", label: "Nữ" },
  { value: "Khac", label: "Khác" },
];

export const RELATIONSHIP_OPTIONS = [
  { value: "Chu ho", label: "Chủ hộ" },
  { value: "Vo/Chong", label: "Vợ/Chồng" },
  { value: "Con", label: "Con" },
  { value: "Nguoi than", label: "Người thân" },
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
