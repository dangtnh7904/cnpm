import React, { useState, useEffect } from "react";
import { Card, Select, Row, Col, Statistic, Table, Tag, App } from "antd";
import { DollarOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined, BankOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { reportService, feeService, buildingService } from "../../services";
import { useFetch } from "../../hooks";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

const { Option } = Select;

export default function ReportDashboard() {
  const { message } = App.useApp();
  const [selectedToaNha, setSelectedToaNha] = useState(null);
  const [selectedDotThu, setSelectedDotThu] = useState(null);
  const [stats, setStats] = useState(null);
  const [buildings, setBuildings] = useState([]);
  const { data: allDotThus, refetch: fetchDotThus } = useFetch(feeService.getAllDotThu, false);
  
  // Lọc đợt thu theo tòa nhà
  const dotThus = selectedToaNha 
    ? (allDotThus || []).filter(dt => dt.toaNha?.id === selectedToaNha)
    : (allDotThus || []);

  // Load danh sách tòa nhà
  useEffect(() => {
    buildingService.getAllForDropdown()
      .then(data => setBuildings(Array.isArray(data) ? data : []))
      .catch(err => console.error("Error loading buildings:", err));
  }, []);

  useEffect(() => {
    fetchDotThus();
  }, [fetchDotThus]);

  useEffect(() => {
    if (selectedDotThu) {
      loadStatistics();
    }
  }, [selectedDotThu]);

  const loadStatistics = async () => {
    try {
      const data = await reportService.getStatisticsByDotThu(selectedDotThu);
      setStats(data);
    } catch (error) {
      console.error("Lỗi tải thống kê:", error);
    }
  };

  // Reset đợt thu khi đổi tòa nhà
  const handleToaNhaChange = (value) => {
    setSelectedToaNha(value);
    setSelectedDotThu(null);
    setStats(null);
  };

  return (
    <ContentCard title="Báo cáo tài chính">
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12}>
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn tòa nhà"
            onChange={handleToaNhaChange}
            value={selectedToaNha}
            allowClear
            suffixIcon={<BankOutlined />}
          >
            {buildings?.map((b) => (
              <Option key={b.id} value={b.id}>
                {b.tenToaNha}
              </Option>
            ))}
          </Select>
        </Col>
        <Col xs={24} sm={12}>
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn đợt thu"
            onChange={setSelectedDotThu}
            value={selectedDotThu}
          >
            {dotThus?.map((dt) => (
              <Option key={dt.id} value={dt.id}>
                {dt.tenDotThu}
              </Option>
            ))}
          </Select>
        </Col>
      </Row>

      {stats && (
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng phải thu"
                value={stats.tongPhaiThu || 0}
                prefix={<DollarOutlined />}
                valueStyle={{ color: "#1890ff" }}
                formatter={(value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng đã thu"
                value={stats.tongDaThu || 0}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: "#52c41a" }}
                formatter={(value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tổng còn nợ"
                value={stats.tongConNo || 0}
                prefix={<WarningOutlined />}
                valueStyle={{ color: "#ff4d4f" }}
                formatter={(value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="Tỷ lệ hoàn thành"
                value={stats.tyLeHoanThanh || 0}
                suffix="%"
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: "#52c41a" }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12}>
          <Card title="Thống kê theo hộ">
            {stats && (
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic title="Chưa đóng" value={stats.soHoChuaDong || 0} valueStyle={{ color: "#ff4d4f" }} />
                </Col>
                <Col span={8}>
                  <Statistic title="Đang nợ" value={stats.soHoDangNo || 0} valueStyle={{ color: "#faad14" }} />
                </Col>
                <Col span={8}>
                  <Statistic title="Đã đóng" value={stats.soHoDaDong || 0} valueStyle={{ color: "#52c41a" }} />
                </Col>
              </Row>
            )}
          </Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card title="Tổng số hộ">
            {stats && (
              <Statistic
                value={stats.tongSoHo || 0}
                valueStyle={{ fontSize: 32, color: "#1890ff" }}
              />
            )}
          </Card>
        </Col>
      </Row>


    </ContentCard>
  );
}

