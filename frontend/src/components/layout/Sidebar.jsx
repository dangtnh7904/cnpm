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
} from "@ant-design/icons";
import { useAuthContext } from "../../contexts";

const { Sider: AntSider } = Layout;

export default function Sidebar({ collapsed, onCollapse }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAdmin, isAccountant, isResident, logout } = useAuthContext();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const menuItems = [
    (isAdmin || isAccountant || isResident) && { key: "/", icon: <DashboardOutlined />, label: "Trang chủ" },
    isAdmin && { key: "/buildings", icon: <BankOutlined />, label: "Tòa nhà" },
    isAdmin && { key: "/households", icon: <HomeOutlined />, label: "Hộ gia đình" },
    // Menu Quản lý Phí (Submenu)
    (isAdmin || isAccountant) && {
      key: "fee-management",
      icon: <DollarOutlined />,
      label: "Quản lý Phí",
      children: [
        { key: "/loai-phi", label: "Loại phí" },
        { key: "/dinh-muc-thu", icon: <SettingOutlined />, label: "Cấu hình Bảng giá" },
      ],
    },
    (isAdmin || isAccountant) && { key: "/payment/update", icon: <DollarOutlined />, label: "Cập nhật thanh toán" },
    (isAdmin || isAccountant) && { key: "/payment/online", icon: <DollarOutlined />, label: "Thanh toán online" },
    (isAdmin || isAccountant) && { key: "/report", icon: <DollarOutlined />, label: "Báo cáo" },
    (isAdmin || isAccountant) && { key: "/invoice", icon: <DollarOutlined />, label: "Hóa đơn" },
    isResident && { key: "/resident/payment-history", icon: <DollarOutlined />, label: "Lịch sử thanh toán" },
    isResident && { key: "/resident/feedback", icon: <UserSwitchOutlined />, label: "Phản ánh" },
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
