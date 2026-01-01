import React, { useState } from "react";
import { Layout } from "antd";
import Header from "./Header";
import Sidebar from "./Sidebar";
import "./MainLayout.css";

const { Content } = Layout;

export default function MainLayout({ children }) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <Layout className="main-layout">
      <Sidebar collapsed={collapsed} onCollapse={setCollapsed} />
      <Layout>
        <Header />
        <Content className="main-content">{children}</Content>
      </Layout>
    </Layout>
  );
}
