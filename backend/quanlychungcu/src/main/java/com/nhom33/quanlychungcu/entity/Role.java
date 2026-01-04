package com.nhom33.quanlychungcu.entity;

/**
 * Các vai trò trong hệ thống quản lý chung cư (Multi-tenancy)
 * 
 * - ADMIN: Quản trị viên hệ thống (thấy tất cả)
 * - MANAGER: Người quản lý tòa nhà (chỉ thấy tòa nhà của mình)
 * - ACCOUNTANT: Kế toán (xử lý phí, hóa đơn)
 * - RESIDENT: Cư dân (xem thông tin hộ của mình)
 */
public enum Role {
    ADMIN,
    MANAGER,
    ACCOUNTANT,
    RESIDENT
}
