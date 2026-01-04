import React from "react";
import { Layout, Menu } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  HomeOutlined,
  UserSwitchOutlined,
  LogoutOutlined,
  DashboardOutlined,
  DollarOutlined,
  BankOutlined,
  SettingOutlined,
  CalendarOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  FileTextOutlined,
  BellOutlined,
  SafetyOutlined,
  CreditCardOutlined,
  CommentOutlined,
} from "@ant-design/icons";
import { useAuthContext } from "../../contexts";

const { Sider: AntSider } = Layout;

export default function Sidebar({ collapsed, onCollapse }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAdmin, isAccountant, isResident, isManager, logout } = useAuthContext();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  // Manager có quyền giống Admin nhưng chỉ thấy tòa nhà của mình (lọc ở backend)
  const canManage = isAdmin || isManager;

  const menuItems = [
    // Trang chủ - Dashboard (Admin/Manager only)
    canManage && { key: "/", icon: <DashboardOutlined />, label: "Trang chủ" },
    
    // Quản lý Tòa nhà & Hộ gia đình (Admin/Manager)
    canManage && { key: "/buildings", icon: <BankOutlined />, label: "Tòa nhà" },
    canManage && { key: "/households", icon: <HomeOutlined />, label: "Hộ gia đình" },
    
    // Quản lý User trong tòa nhà (Admin/Manager) - để user xem thông báo & nộp tiền
    canManage && { key: "/building-users", icon: <UserSwitchOutlined />, label: "Cư dân tòa nhà" },
    
    // Nhân khẩu - chỉ ADMIN (quản lý thông tin nhân khẩu chi tiết)
    isAdmin && { key: "/residents", icon: <TeamOutlined />, label: "Nhân khẩu" },
    
    // Quản lý Đăng ký Tạm trú/Tạm vắng - chỉ ADMIN
    isAdmin && {
      key: "registration",
      icon: <FileTextOutlined />,
      label: "Đăng ký",
      children: [
        { key: "/tam-tru", label: "Tạm trú" },
        { key: "/tam-vang", label: "Tạm vắng" },
      ],
    },
    
    // Quản lý Phí (Admin/Manager only - Accountant không cần)
    canManage && {
      key: "fee-management",
      icon: <DollarOutlined />,
      label: "Quản lý Phí",
      children: [
        { key: "/loai-phi", label: "Loại phí" },
        { key: "/dinh-muc-thu", icon: <SettingOutlined />, label: "Cấu hình Bảng giá" },
        { key: "/dot-thu", icon: <CalendarOutlined />, label: "Đợt thu" },
        { key: "/ghi-chi-so", icon: <ThunderboltOutlined />, label: "Ghi chỉ số điện nước" },
      ],
    },
    
    // Thanh toán (Admin/Manager/Accountant)
    (canManage || isAccountant) && { key: "/payment/update", icon: <DollarOutlined />, label: "Cập nhật thanh toán" },
    (canManage || isAccountant) && { key: "/payment/online", icon: <CreditCardOutlined />, label: "Thanh toán online" },
    
    // Báo cáo (Admin/Manager only)
    canManage && { key: "/report", icon: <FileTextOutlined />, label: "Báo cáo" },
    
    // Thông báo (Admin/Manager)
    canManage && { key: "/notification", icon: <BellOutlined />, label: "Thông báo" },
    
    // Quản lý phản ánh (Admin/Manager)
    canManage && { key: "/feedback-management", icon: <CommentOutlined />, label: "Quản lý phản ánh" },
    
    // Gửi phản ánh cho Accountant (báo lỗi cho Manager)
    isAccountant && { key: "/accountant/feedback", icon: <CommentOutlined />, label: "Gửi phản ánh" },
    
    // Quản trị hệ thống (chỉ Admin hệ thống)
    isAdmin && { key: "/admin/users", icon: <SafetyOutlined />, label: "Quản lý Users" },
    isAdmin && { key: "/admin/backup", icon: <SettingOutlined />, label: "Backup" },
    
    // Menu Cư dân (Resident) - Tòa nhà, Thông báo, Phản ánh, Thanh toán
    isResident && { key: "/resident/my-buildings", icon: <BankOutlined />, label: "Tòa nhà của tôi" },
    isResident && { key: "/resident/notifications", icon: <BellOutlined />, label: "Thông báo" },
    isResident && { key: "/resident/feedback", icon: <CommentOutlined />, label: "Phản ánh" },
    isResident && { key: "/resident/payment-history", icon: <DollarOutlined />, label: "Lịch sử thanh toán" },
    isResident && { key: "/resident/online-payment", icon: <CreditCardOutlined />, label: "Thanh toán online" },
  ].filter(Boolean);

  const handleMenuClick = (info) => {
    if (info.key === "logout") {
      handleLogout();
    } else {
      navigate(info.key);
    }
  };

  return (
    <AntSider
      collapsible
      collapsed={collapsed}
      onCollapse={onCollapse}
      theme="dark"
      style={{
        background: "#111827",
      }}
    >
      <div
        style={{
          color: "#e2e8f0",
          padding: 16,
          fontWeight: 700,
          fontSize: collapsed ? 14 : 18,
          textAlign: "center",
          borderBottom: "1px solid rgba(255, 255, 255, 0.1)",
        }}
      >
        {collapsed ? "CC" : "Chung cư"}
      </div>
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[location.pathname]}
        items={[
          ...menuItems,
          { type: "divider" },
          { key: "logout", icon: <LogoutOutlined />, label: "Đăng xuất", danger: true },
        ]}
        onClick={handleMenuClick}
        style={{ background: "transparent" }}
      />
    </AntSider>
  );
}
