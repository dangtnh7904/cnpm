import React, { useState, useEffect } from "react";
import { Card, Select, Row, Col, Statistic, Table, Tag } from "antd";
import { DollarOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { reportService, feeService } from "../../services";
import { useFetch } from "../../hooks";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

const { Option } = Select;

export default function ReportDashboard() {
  const [selectedDotThu, setSelectedDotThu] = useState(null);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [stats, setStats] = useState(null);
  const [monthlyStats, setMonthlyStats] = useState([]);
  const { data: dotThus, refetch: fetchDotThus } = useFetch(feeService.getAllDotThu, false);

  useEffect(() => {
    fetchDotThus();
  }, [fetchDotThus]);

  useEffect(() => {
    if (selectedDotThu) {
      loadStatistics();
    }
  }, [selectedDotThu]);

  useEffect(() => {
    loadMonthlyStatistics();
  }, [year]);

  const loadStatistics = async () => {
    try {
      const data = await reportService.getStatisticsByDotThu(selectedDotThu);
      setStats(data);
    } catch (error) {
      console.error("Lỗi tải thống kê:", error);
    }
  };

  const loadMonthlyStatistics = async () => {
    try {
      const months = [];
      for (let m = 1; m <= 12; m++) {
        const data = await reportService.getStatisticsByMonth(year, m);
        months.push({ ...data, month: m });
      }
      setMonthlyStats(months);
    } catch (error) {
      console.error("Lỗi tải thống kê tháng:", error);
    }
  };

  const chartData = monthlyStats.map(m => ({
    name: `Tháng ${m.month}`,
    "Tổng thu": m.tongPhaiThu || 0,
    "Đã thu": m.tongDaThu || 0,
    "Còn nợ": m.tongConNo || 0,
  }));

  return (
    <ContentCard title="Báo cáo tài chính">
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={8}>
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
        <Col span={8}>
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn năm"
            value={year}
            onChange={setYear}
          >
            {[2023, 2024, 2025, 2026].map((y) => (
              <Option key={y} value={y}>{y}</Option>
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

      <Card title="Biểu đồ thống kê theo tháng" style={{ marginTop: 24 }}>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip formatter={(value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"} />
            <Legend />
            <Bar dataKey="Tổng thu" fill="#1890ff" />
            <Bar dataKey="Đã thu" fill="#52c41a" />
            <Bar dataKey="Còn nợ" fill="#ff4d4f" />
          </BarChart>
        </ResponsiveContainer>
      </Card>

      <Card title="Xu hướng thu phí" style={{ marginTop: 24 }}>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip formatter={(value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"} />
            <Legend />
            <Line type="monotone" dataKey="Tổng thu" stroke="#1890ff" />
            <Line type="monotone" dataKey="Đã thu" stroke="#52c41a" />
            <Line type="monotone" dataKey="Còn nợ" stroke="#ff4d4f" />
          </LineChart>
        </ResponsiveContainer>
      </Card>
    </ContentCard>
  );
}

