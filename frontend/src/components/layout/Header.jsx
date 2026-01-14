import React from "react";
import { Layout, Typography, Button, Dropdown } from "antd";
import { UserOutlined, SettingOutlined, LogoutOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../../contexts";

const { Header: AntHeader } = Layout;
const { Text } = Typography;

// Map role code to display name
const ROLE_DISPLAY_NAMES = {
  ADMIN: "Quản trị viên",
  MANAGER: "Quản lý tòa nhà",
  ACCOUNTANT: "Kế toán",
  RESIDENT: "Cư dân",
};

export default function Header() {
  const { user, logout } = useAuthContext();
  const navigate = useNavigate();

  const getRoleDisplayName = (role) => {
    return ROLE_DISPLAY_NAMES[role] || role || "Không xác định";
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const menuItems = [
    {
      key: "profile",
      icon: <SettingOutlined />,
      label: "Cài đặt tài khoản",
      onClick: () => navigate("/profile"),
    },
    {
      type: "divider",
    },
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "Đăng xuất",
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <AntHeader
      style={{
        background: "#0b1224",
        padding: "0 24px",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        borderBottom: "1px solid rgba(255, 255, 255, 0.05)",
      }}
    >
      <Text strong style={{ color: "#e2e8f0", fontSize: 16 }}>
        Hệ thống quản lý chung cư
      </Text>
      {user && (
        <Dropdown menu={{ items: menuItems }} placement="bottomRight" trigger={["click"]}>
          <Button
            type="text"
            style={{
              color: "#e2e8f0",
              display: "flex",
              alignItems: "center",
              gap: 8,
              height: "auto",
              padding: "8px 12px",
            }}
          >
            <UserOutlined />
            <span>{user.fullName || user.username}</span>
            <Text style={{ fontSize: 12, opacity: 0.7, color: "#94a3b8" }}>
              ({getRoleDisplayName(user.role)})
            </Text>
          </Button>
        </Dropdown>
      )}
    </AntHeader>
  );
}

