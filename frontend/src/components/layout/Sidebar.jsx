import React from "react";
import { Layout, Menu } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  HomeOutlined,
  TeamOutlined,
  UserSwitchOutlined,
  LogoutOutlined,
  DashboardOutlined,
  DollarOutlined,
} from "@ant-design/icons";
import { useAuthContext } from "../../contexts";

const { Sider: AntSider } = Layout;

export default function Sidebar({ collapsed, onCollapse }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAdmin, isAccountant, logout } = useAuthContext();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const menuItems = [
    isAdmin && { key: "/", icon: <DashboardOutlined />, label: "Trang chủ" },
    isAdmin && { key: "/households", icon: <HomeOutlined />, label: "Hộ gia đình" },
    isAdmin && { key: "/residents", icon: <TeamOutlined />, label: "Nhân khẩu" },
    isAdmin && { key: "/tam-tru", icon: <UserSwitchOutlined />, label: "Tạm trú" },
    isAdmin && { key: "/tam-vang", icon: <UserSwitchOutlined style={{ transform: "rotate(180deg)" }} />, label: "Tạm vắng" },
    (isAdmin || isAccountant) && { key: "/fees", icon: <DollarOutlined />, label: "Quản lý phí" },
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
