import React from "react";
import { Layout, Typography } from "antd";
import { useAuthContext } from "../../contexts";

const { Header: AntHeader } = Layout;
const { Text } = Typography;

export default function Header() {
  const { user } = useAuthContext();

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
        <div style={{ color: "#e2e8f0" }}>
          <Text style={{ color: "#e2e8f0" }}>{user.username}</Text>
          <Text style={{ marginLeft: 8, fontSize: 12, opacity: 0.7, color: "#94a3b8" }}>
            ({user.role === "ADMIN" ? "Quản lý" : "Kế toán"})
          </Text>
        </div>
      )}
    </AntHeader>
  );
}
