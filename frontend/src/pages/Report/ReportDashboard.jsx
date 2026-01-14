import React, { useState, useEffect } from "react";
import { Card, Select, Row, Col, Statistic, Empty, App, Alert } from "antd";
import { DollarOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined, BankOutlined, HomeOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { reportService, feeService, buildingService } from "../../services";
import { useFetch } from "../../hooks";
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts";

const { Option } = Select;

// Màu sắc cho chart
const COLORS_HOUSEHOLD = ["#52c41a", "#faad14", "#ff4d4f"]; // Đã đóng (xanh), Thanh toán 1 phần (vàng), Chưa đóng (đỏ)
const COLORS_AMOUNT = ["#52c41a", "#ff4d4f"]; // Đã thu, Còn nợ

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
    : [];

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
      message.error("Không thể tải thống kê");
    }
  };

  // Reset đợt thu khi đổi tòa nhà
  const handleToaNhaChange = (value) => {
    setSelectedToaNha(value);
    setSelectedDotThu(null);
    setStats(null);
  };

  // Dữ liệu cho chart số hộ (3 trạng thái: Đã đóng, Thanh toán 1 phần, Chưa đóng)
  const soHoDaDong = stats?.soHoDaDong || 0;
  const soHoThanhToanMotPhan = stats?.soHoDangNo || 0;
  const soHoChuaDong = stats?.soHoChuaDong || 0;
  const householdChartData = stats ? [
    { name: "Đã đóng", value: soHoDaDong, color: "#52c41a" },
    { name: "Thanh toán 1 phần", value: soHoThanhToanMotPhan, color: "#faad14" },
    { name: "Chưa đóng", value: soHoChuaDong, color: "#ff4d4f" },
  ].filter(item => item.value > 0) : [];

  // Dữ liệu cho chart số tiền
  const amountChartData = stats ? [
    { name: "Đã thu", value: Number(stats.tongDaThu) || 0 },
    { name: "Còn nợ", value: Number(stats.tongConNo) || 0 },
  ].filter(item => item.value > 0) : [];

  // Format tiền VND
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN').format(value) + " đ";
  };

  // Custom label cho PieChart
  const renderCustomLabel = ({ name, percent }) => {
    return `${name}: ${(percent * 100).toFixed(0)}%`;
  };

  return (
    <ContentCard title="Báo cáo tài chính">
      {/* Chọn tòa nhà (bắt buộc) */}
      <Alert
        message="Vui lòng chọn tòa nhà để xem báo cáo"
        type="info"
        showIcon
        icon={<BankOutlined />}
        style={{ marginBottom: 16, display: selectedToaNha ? "none" : "flex" }}
      />

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12}>
          <label style={{ display: "block", marginBottom: 8, color: "#94a3b8" }}>
            <BankOutlined /> Tòa nhà <span style={{ color: "#ff4d4f" }}>*</span>
          </label>
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn tòa nhà"
            onChange={handleToaNhaChange}
            value={selectedToaNha}
            size="large"
          >
            {buildings?.map((b) => (
              <Option key={b.id} value={b.id}>
                {b.tenToaNha}
              </Option>
            ))}
          </Select>
        </Col>
        <Col xs={24} sm={12}>
          <label style={{ display: "block", marginBottom: 8, color: "#94a3b8" }}>
            <HomeOutlined /> Đợt thu
          </label>
          <Select
            style={{ width: "100%" }}
            placeholder={selectedToaNha ? "Chọn đợt thu" : "Chọn tòa nhà trước"}
            onChange={setSelectedDotThu}
            value={selectedDotThu}
            disabled={!selectedToaNha}
            size="large"
          >
            {dotThus?.map((dt) => (
              <Option key={dt.id} value={dt.id}>
                {dt.tenDotThu}
              </Option>
            ))}
          </Select>
        </Col>
      </Row>

      {/* Hiển thị khi chưa chọn tòa nhà */}
      {!selectedToaNha && (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="Chọn tòa nhà để xem báo cáo"
          style={{ padding: 60 }}
        />
      )}

      {/* Hiển thị khi đã chọn tòa nhà nhưng chưa chọn đợt thu */}
      {selectedToaNha && !selectedDotThu && (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={dotThus.length > 0 ? "Chọn đợt thu để xem thống kê chi tiết" : "Tòa nhà này chưa có đợt thu nào"}
          style={{ padding: 60 }}
        />
      )}

      {/* Thống kê tổng quan */}
      {stats && (
        <>
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={6}>
              <Card>
                <Statistic
                  title="Tổng phải thu"
                  value={stats.tongPhaiThu || 0}
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: "#1890ff" }}
                  formatter={(value) => formatCurrency(value)}
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
                  formatter={(value) => formatCurrency(value)}
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
                  formatter={(value) => formatCurrency(value)}
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

          {/* Charts */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} lg={12}>
              <Card title="Thống kê số hộ theo trạng thái">
                {householdChartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={householdChartData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={renderCustomLabel}
                        outerRadius={100}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {householdChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <Empty description="Không có dữ liệu" />
                )}
                <Row gutter={16} style={{ marginTop: 16 }}>
                  <Col span={8}>
                    <Statistic title="Đã đóng" value={soHoDaDong} valueStyle={{ color: "#52c41a" }} suffix="hộ" />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Thanh toán 1 phần" value={soHoThanhToanMotPhan} valueStyle={{ color: "#faad14" }} suffix="hộ" />
                  </Col>
                  <Col span={8}>
                    <Statistic title="Chưa đóng" value={soHoChuaDong} valueStyle={{ color: "#ff4d4f" }} suffix="hộ" />
                  </Col>
                </Row>
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="Thống kê số tiền thu">
                {amountChartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={amountChartData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={renderCustomLabel}
                        outerRadius={100}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {amountChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS_AMOUNT[index % COLORS_AMOUNT.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => formatCurrency(value)} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                ) : (
                  <Empty description="Không có dữ liệu" />
                )}
                <Row gutter={16} style={{ marginTop: 16 }}>
                  <Col span={12}>
                    <Statistic
                      title="Đã thu"
                      value={stats.tongDaThu || 0}
                      valueStyle={{ color: "#52c41a" }}
                      formatter={(value) => formatCurrency(value)}
                    />
                  </Col>
                  <Col span={12}>
                    <Statistic
                      title="Còn nợ"
                      value={stats.tongConNo || 0}
                      valueStyle={{ color: "#ff4d4f" }}
                      formatter={(value) => formatCurrency(value)}
                    />
                  </Col>
                </Row>
              </Card>
            </Col>
          </Row>
        </>
      )}
    </ContentCard>
  );
}
