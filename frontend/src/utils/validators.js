export const validateCCCD = (value) => {
  if (!value) return Promise.reject("Vui lòng nhập CCCD");
  if (value.length !== 12) return Promise.reject("CCCD phải có 12 số");
  if (!/^\d+$/.test(value)) return Promise.reject("CCCD chỉ chứa số");
  return Promise.resolve();
};

export const validatePhone = (value) => {
  if (!value) return Promise.resolve();
  if (!/^0\d{9,10}$/.test(value)) return Promise.reject("Số điện thoại không hợp lệ");
  return Promise.resolve();
};

export const validateEmail = (value) => {
  if (!value) return Promise.resolve();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(value)) return Promise.reject("Email không hợp lệ");
  return Promise.resolve();
};
