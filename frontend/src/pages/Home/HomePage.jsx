import React, { useEffect, useState } from "react";
import { Row, Col, Card, Statistic } from "antd";
import { TeamOutlined, HomeOutlined, UserSwitchOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { householdService, residentService, tamTruService, tamVangService } from "../../services";

export default function HomePage() {
  const [stats, setStats] = useState({
    households: 0,
    residents: 0,
    tamTru: 0,
    tamVang: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const promises = [
          householdService.getTotalCount().catch(() => 0),
          residentService.getTotalCount().catch(() => 0),
          tamTruService.getTotalCount().catch(() => 0),
          tamVangService.getTotalCount().catch(() => 0),
        ];

        const [households, residents, tamTru, tamVang] = await Promise.all(promises);

        setStats({
          households,
          residents,
          tamTru,
          tamVang,
        });
      } catch (error) {
        console.error("Failed to fetch stats:", error);
        setStats({ households: 0, residents: 0, tamTru: 0, tamVang: 0 });
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  const statCards = [
    {
      title: "Hộ gia đình",
      value: stats.households,
      icon: <HomeOutlined style={{ fontSize: 32, color: "#3b82f6" }} />,
      color: "#3b82f6",
    },
    {
      title: "Nhân khẩu",
      value: stats.residents,
      icon: <TeamOutlined style={{ fontSize: 32, color: "#10b981" }} />,
      color: "#10b981",
    },
    {
      title: "Tạm trú",
      value: stats.tamTru,
      icon: <UserSwitchOutlined style={{ fontSize: 32, color: "#f59e0b" }} />,
      color: "#f59e0b",
    },
    {
      title: "Tạm vắng",
      value: stats.tamVang,
      icon: <UserSwitchOutlined style={{ fontSize: 32, color: "#ef4444", transform: "rotate(180deg)" }} />,
      color: "#ef4444",
    },
  ];

  return (
    <ContentCard title="Tổng quan hệ thống">
      <Row gutter={[16, 16]}>
        {statCards.map((stat) => (
          <Col xs={24} sm={12} lg={6} key={stat.title}>
            <Card
              loading={loading}
              style={{
                background: "rgba(255, 255, 255, 0.02)",
                border: "1px solid rgba(255, 255, 255, 0.1)",
                borderRadius: 8,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                {stat.icon}
                <Statistic
                  title={<span style={{ color: "#94a3b8" }}>{stat.title}</span>}
                  value={stat.value}
                  valueStyle={{ color: stat.color }}
                />
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Card
        style={{
          marginTop: 24,
          background: "rgba(255, 255, 255, 0.02)",
          border: "1px solid rgba(255, 255, 255, 0.1)",
        }}
      >
        <h3 style={{ color: "#e2e8f0", marginTop: 0 }}>Chào mừng đến với Hệ thống Quản lý Chung cư</h3>
        <p style={{ color: "#94a3b8" }}>
          Sử dụng menu bên trái để điều hướng đến các chức năng quản lý hộ gia đình, nhân khẩu, tạm trú và tạm vắng.
        </p>
      </Card>
    </ContentCard>
  );
}
