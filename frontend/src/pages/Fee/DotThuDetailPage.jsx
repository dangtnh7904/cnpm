import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Tabs,
  Table,
  Modal,
  Select,
  Popconfirm,
  Statistic,
  Row,
  Col,
  Alert,
  Spin,
  Empty,
  App,
} from "antd";
import {
  ArrowLeftOutlined,
  PlusOutlined,
  DeleteOutlined,
  ThunderboltOutlined,
  DropboxOutlined,
  LockOutlined,
  CalculatorOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  PrinterOutlined,
  ReloadOutlined,
  DownloadOutlined,
  FileExcelOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import { ContentCard } from "../../components";
import { dotThuService, feeService } from "../../services";

const { Option } = Select;

/**
 * Trang Chi tiết Đợt Thu.
 * 
 * CHỨC NĂNG:
 * - Xem thông tin đợt thu
 * - Cấu hình phí trong đợt thu (thêm/xóa loại phí)
 * - Tính tiền điện/nước (chốt sổ từ bảng ChiSoDienNuoc theo Tháng/Năm)
 */
export default function DotThuDetailPage() {
  const { message } = App.useApp();
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [dotThu, setDotThu] = useState(null);
  const [configuredFees, setConfiguredFees] = useState([]);
  const [hasUtilityFee, setHasUtilityFee] = useState(false);
  const [activeTab, setActiveTab] = useState("fees");
  
  // Modal thêm phí
  const [addFeeModalOpen, setAddFeeModalOpen] = useState(false);
  const [availableFees, setAvailableFees] = useState([]);
  const [selectedFeeToAdd, setSelectedFeeToAdd] = useState(null);
  const [addingFee, setAddingFee] = useState(false);
  
  // Modal tính tiền điện/nước
  const [calculateModalOpen, setCalculateModalOpen] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [calculateResult, setCalculateResult] = useState(null);
  
  // Bảng kê
  const [bangKeData, setBangKeData] = useState(null);
  const [loadingBangKe, setLoadingBangKe] = useState(false);

  // Load dữ liệu ban đầu
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [dotThuData, feesData, hasUtility] = await Promise.all([
        dotThuService.getById(id),
        dotThuService.getFeesInPeriod(id),
        dotThuService.hasUtilityFee(id),
      ]);
      
      setDotThu(dotThuData);
      setConfiguredFees(feesData);
      setHasUtilityFee(hasUtility);
    } catch (error) {
      message.error("Không thể tải thông tin đợt thu");
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id, message]);

  useEffect(() => {
    loadData();
  }, [loadData]);
  
  // Load bảng kê
  const loadBangKe = async () => {
    setLoadingBangKe(true);
    try {
      const data = await dotThuService.getBangKe(id);
      setBangKeData(data);
    } catch (error) {
      message.error("Không thể tải bảng kê");
      console.error(error);
    } finally {
      setLoadingBangKe(false);
    }
  };

  // Export Excel - Gọi API Backend để xuất file
  const handleExportExcel = async () => {
    try {
      message.loading({ content: "Đang xuất file...", key: "export" });
      
      const blob = await dotThuService.exportExcel(id);
      
      // Tạo và download file
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `BangKe_${dotThu?.tenDotThu?.replace(/\s+/g, "_") || "DotThu"}_${dayjs().format("YYYYMMDD")}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      
      message.success({ content: "Đã xuất file Excel thành công!", key: "export" });
    } catch (error) {
      console.error("Export error:", error);
      message.error({ content: "Không thể xuất file Excel", key: "export" });
    }
  };

  // Load danh sách loại phí có thể thêm
  const loadAvailableFees = async () => {
    try {
      const allFees = await feeService.searchLoaiPhi({ dangHoatDong: true });
      const configuredIds = configuredFees.map(f => f.loaiPhi.id);
      const available = (allFees || []).filter(f => !configuredIds.includes(f.id));
      setAvailableFees(available);
    } catch (error) {
      console.error("Error loading fees:", error);
      setAvailableFees([]);
    }
  };

  // Mở modal thêm phí
  const openAddFeeModal = () => {
    loadAvailableFees();
    setAddFeeModalOpen(true);
    setSelectedFeeToAdd(null);
  };

  // Thêm phí vào đợt thu
  const handleAddFee = async () => {
    if (!selectedFeeToAdd) {
      message.warning("Vui lòng chọn loại phí");
      return;
    }
    
    setAddingFee(true);
    try {
      const result = await dotThuService.addFeeToPeriod(id, selectedFeeToAdd);
      message.success("Đã thêm loại phí vào đợt thu");
      setAddFeeModalOpen(false);
      
      // Cập nhật state
      setHasUtilityFee(result.hasUtilityFee);
      await loadData(); // Reload để cập nhật danh sách
    } catch (error) {
      message.error(error.response?.data?.message || "Không thể thêm loại phí");
    } finally {
      setAddingFee(false);
    }
  };

  // Xóa phí khỏi đợt thu
  const handleRemoveFee = async (loaiPhiId) => {
    try {
      const result = await dotThuService.removeFeeFromPeriod(id, loaiPhiId);
      message.success("Đã xóa loại phí khỏi đợt thu");
      
      setHasUtilityFee(result.hasUtilityFee);
      await loadData();
    } catch (error) {
      message.error(error.response?.data?.message || "Không thể xóa loại phí");
    }
  };

  // Xử lý tính tiền điện/nước - sử dụng thang/nam đã lưu trong đợt thu
  const handleCalculateInvoices = async () => {
    setCalculating(true);
    try {
      const result = await dotThuService.calculateInvoices(id);
      setCalculateResult(result);
      message.success(`Đã tính tiền thành công! Tạo ${result.soHoaDonTao} hóa đơn.`);
      await loadData(); // Reload để cập nhật
    } catch (error) {
      message.error(error.response?.data?.message || "Tính tiền thất bại");
    } finally {
      setCalculating(false);
    }
  };

  // Cột bảng phí đã cấu hình
  const feeColumns = [
    {
      title: "Loại phí",
      dataIndex: ["loaiPhi", "tenLoaiPhi"],
      key: "tenLoaiPhi",
      render: (text, record) => (
        <Space>
          {text}
          {["Điện", "Nước"].includes(text) && (
            <Tag color="blue">Biến đổi</Tag>
          )}
        </Space>
      ),
    },
    {
      title: "Đơn giá",
      dataIndex: "donGiaApDung",
      key: "donGiaApDung",
      width: 150,
      render: (value, record) => {
        // Format giá tiền chuẩn
        const price = typeof value === 'number' ? value : (parseFloat(value) || 0);
        const formattedPrice = price.toLocaleString("vi-VN") + " đ";
        
        // Hiển thị icon nếu dùng giá riêng theo tòa nhà
        if (record.nguonGia === "BangGiaDichVu") {
          return (
            <span title="Giá riêng theo tòa nhà">
              {formattedPrice} <span style={{ color: '#1890ff', fontSize: 12 }}>★</span>
            </span>
          );
        }
        return formattedPrice;
      },
    },
    {
      title: "Đơn vị",
      dataIndex: ["loaiPhi", "donViTinh"],
      key: "donViTinh",
      width: 100,
    },
    {
      title: "Loại thu",
      dataIndex: ["loaiPhi", "loaiThu"],
      key: "loaiThu",
      width: 120,
      render: (value) => (
        <Tag color={value === "BatBuoc" ? "red" : "green"}>
          {value === "BatBuoc" ? "Bắt buộc" : "Tự nguyện"}
        </Tag>
      ),
    },
    {
      title: "Thao tác",
      key: "actions",
      width: 80,
      render: (_, record) => (
        // Cho phép xóa TẤT CẢ loại phí (không lock Điện/Nước nữa)
        <Popconfirm
          title="Xóa loại phí này khỏi đợt thu?"
          onConfirm={() => handleRemoveFee(record.loaiPhi.id)}
          okText="Xóa"
          cancelText="Hủy"
        >
          <Button type="text" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  // Tabs configuration
  const tabItems = [
    {
      key: "fees",
      label: "Cấu hình phí",
      children: (
        <div>
          <div style={{ marginBottom: 16, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openAddFeeModal}>
              Thêm loại phí
            </Button>
            
            {/* Nút Tạo hóa đơn - cho phép tạo hóa đơn cho TẤT CẢ loại phí đã cấu hình */}
            {configuredFees.length > 0 && (
              <Button
                type="primary"
                icon={<CalculatorOutlined />}
                onClick={() => setCalculateModalOpen(true)}
                style={{ backgroundColor: "#52c41a", borderColor: "#52c41a" }}
                disabled={!dotThu?.thang || !dotThu?.nam}
              >
                Tạo hóa đơn ({configuredFees.length} loại phí)
              </Button>
            )}
          </div>
          
          {configuredFees.length > 0 && (!dotThu?.thang || !dotThu?.nam) && (
            <Alert
              message="Chưa cấu hình Tháng/Năm cho đợt thu"
              description="Vui lòng cập nhật Tháng và Năm của đợt thu để có thể tạo hóa đơn."
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
          
          <Table
            columns={feeColumns}
            dataSource={configuredFees}
            rowKey={(record) => record.loaiPhi.id}
            pagination={false}
            size="middle"
          />
        </div>
      ),
    },
  ];

  // Thêm tab Tính tiền Điện/Nước nếu có phí biến đổi
  if (hasUtilityFee) {
    tabItems.push({
      key: "calculate",
      label: (
        <Space>
          <CalculatorOutlined />
          Tính tiền Điện/Nước
        </Space>
      ),
      children: (
        <div>
          <Alert
            message="Hướng dẫn tính tiền điện/nước"
            description={
              <div>
                <p>1. Đảm bảo đã ghi chỉ số điện/nước cho tháng cần tính (trong menu <b>Ghi Chỉ Số Điện Nước</b>).</p>
                <p>2. Chọn Tháng/Năm tương ứng với kỳ chỉ số đã ghi.</p>
                <p>3. Nhấn <b>Tính tiền</b> để hệ thống tự động tạo hóa đơn dựa trên tiêu thụ và đơn giá theo tòa nhà.</p>
              </div>
            }
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />
          
          <Card title="Chốt sổ tính tiền" size="small">
            <Row gutter={16} align="middle">
              <Col span={8}>
                <Space direction="vertical" size={4} style={{ width: "100%" }}>
                  <span>Kỳ thu phí:</span>
                  <Tag color="blue" style={{ fontSize: 16, padding: "4px 12px" }}>
                    Tháng {String(dotThu?.thang || '').padStart(2, '0')}/{dotThu?.nam || '---'}
                  </Tag>
                </Space>
              </Col>
              <Col span={8}>
                <Button
                  type="primary"
                  size="large"
                  icon={<CalculatorOutlined />}
                  onClick={() => setCalculateModalOpen(true)}
                  disabled={!dotThu?.thang || !dotThu?.nam}
                >
                  Tính tiền Điện/Nước
                </Button>
              </Col>
            </Row>
            
            {/* Hiển thị kết quả nếu có */}
            {calculateResult && (
              <div style={{ marginTop: 24 }}>
                <Card size="small" style={{ background: "#f6ffed", borderColor: "#b7eb8f" }}>
                  <Row gutter={24}>
                    <Col span={8}>
                      <Statistic 
                        title="Số hóa đơn đã tạo" 
                        value={calculateResult.soHoaDonTao}
                        prefix={<CheckCircleOutlined style={{ color: "#52c41a" }} />}
                        valueStyle={{ color: "#52c41a" }}
                      />
                    </Col>
                    <Col span={8}>
                      <Statistic 
                        title="Số hộ thiếu chỉ số" 
                        value={calculateResult.soHoThieuChiSo}
                        valueStyle={{ color: calculateResult.soHoThieuChiSo > 0 ? "#faad14" : undefined }}
                      />
                    </Col>
                  </Row>
                  
                  {calculateResult.soHoThieuChiSo > 0 && (
                    <Alert
                      message="Các hộ thiếu chỉ số"
                      description={calculateResult.danhSachThieuChiSo?.join(", ")}
                      type="warning"
                      showIcon
                      style={{ marginTop: 16 }}
                    />
                  )}
                </Card>
              </div>
            )}
          </Card>
        </div>
      ),
    });
  }
  
  // Tab Bảng Kê & Thông Báo - Luôn hiển thị
  tabItems.push({
    key: "bangke",
    label: (
      <Space>
        <FileTextOutlined />
        Bảng Kê & Thông Báo
      </Space>
    ),
    children: (
      <div>
        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button 
              type="primary" 
              icon={<ReloadOutlined />} 
              onClick={loadBangKe}
              loading={loadingBangKe}
            >
              Tải bảng kê
            </Button>
            
            <Button
              type="default"
              icon={<FileExcelOutlined />}
              onClick={() => handleExportExcel()}
              style={{ background: '#52c41a', borderColor: '#52c41a', color: 'white' }}
            >
              Xuất Excel
            </Button>
          </Space>
        </div>
        
        {!bangKeData && !loadingBangKe && (
          <Alert
            message="Chưa có dữ liệu bảng kê"
            description="Nhấn 'Tải bảng kê' để xem chi tiết các khoản phí của từng hộ gia đình. Hoặc nhấn 'Xuất Excel' để tải file trực tiếp."
            type="info"
            showIcon
          />
        )}
        
        {loadingBangKe && (
          <div style={{ textAlign: "center", padding: 40 }}>
            <Spin tip="Đang tải bảng kê..." />
          </div>
        )}
        
        {bangKeData && !loadingBangKe && (
          <div>
            {/* Thống kê tổng quan */}
            <Card size="small" style={{ marginBottom: 16 }}>
              <Row gutter={24}>
                <Col span={8}>
                  <Statistic 
                    title="Số hóa đơn" 
                    value={bangKeData.soHoaDon}
                    valueStyle={{ color: "#1890ff" }}
                  />
                </Col>
                <Col span={8}>
                  <Statistic 
                    title="Tổng tiền phải thu" 
                    value={bangKeData.tongCong}
                    valueStyle={{ color: "#52c41a" }}
                    suffix="đ"
                    formatter={(value) => value?.toLocaleString("vi-VN")}
                  />
                </Col>
                <Col span={8}>
                  <Statistic 
                    title="Tòa nhà" 
                    value={bangKeData.toaNha}
                  />
                </Col>
              </Row>
            </Card>
            
            {/* Bảng chi tiết */}
            <Table
              columns={[
                {
                  title: "Mã hộ",
                  dataIndex: "maHoGiaDinh",
                  key: "maHoGiaDinh",
                  width: 100,
                  fixed: "left",
                  render: (text) => <strong>{text}</strong>,
                },
                {
                  title: "Căn hộ",
                  dataIndex: "soCanHo",
                  key: "soCanHo",
                  width: 80,
                },
                {
                  title: "Chủ hộ",
                  dataIndex: "chuHo",
                  key: "chuHo",
                  width: 150,
                },
                {
                  title: "Chi tiết phí",
                  dataIndex: "chiTiet",
                  key: "chiTiet",
                  render: (chiTiet) => (
                    <div>
                      {chiTiet?.map((ct, idx) => (
                        <div key={idx} style={{ marginBottom: 4 }}>
                          <Tag color="blue">{ct.tenLoaiPhi}</Tag>
                          <span>
                            {ct.soLuong} × {ct.donGia?.toLocaleString("vi-VN")}đ = <strong>{ct.thanhTien?.toLocaleString("vi-VN")}đ</strong>
                          </span>
                        </div>
                      ))}
                    </div>
                  ),
                },
                {
                  title: "Tổng tiền",
                  dataIndex: "tongTien",
                  key: "tongTien",
                  width: 120,
                  align: "right",
                  render: (value) => (
                    <strong style={{ color: "#1890ff" }}>
                      {value?.toLocaleString("vi-VN")}đ
                    </strong>
                  ),
                },
                {
                  title: "Đã đóng",
                  dataIndex: "daDong",
                  key: "daDong",
                  width: 120,
                  align: "right",
                  render: (value) => (
                    <span style={{ color: "#52c41a" }}>
                      {value?.toLocaleString("vi-VN")}đ
                    </span>
                  ),
                },
                {
                  title: "Còn nợ",
                  dataIndex: "conNo",
                  key: "conNo",
                  width: 120,
                  align: "right",
                  render: (value) => (
                    <span style={{ color: value > 0 ? "#ff4d4f" : "#52c41a" }}>
                      {value?.toLocaleString("vi-VN")}đ
                    </span>
                  ),
                },
                {
                  title: "Trạng thái",
                  dataIndex: "trangThai",
                  key: "trangThai",
                  width: 130,
                  render: (value) => {
                    let color = "default";
                    let text = value;
                    if (value === "DaThanhToan") {
                      color = "green";
                      text = "Đã thanh toán";
                    } else if (value === "ThanhToanMotPhan") {
                      color = "orange";
                      text = "Thanh toán một phần";
                    } else if (value === "ChuaThanhToan") {
                      color = "red";
                      text = "Chưa thanh toán";
                    }
                    return <Tag color={color}>{text}</Tag>;
                  },
                },
              ]}
              dataSource={bangKeData.danhSach || []}
              rowKey="hoaDonId"
              pagination={{ pageSize: 20 }}
              scroll={{ x: 1100 }}
              size="middle"
              bordered
              summary={() => (
                <Table.Summary fixed>
                  <Table.Summary.Row>
                    <Table.Summary.Cell index={0} colSpan={4}>
                      <strong>TỔNG CỘNG</strong>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell index={1} align="right">
                      <strong style={{ color: "#1890ff", fontSize: 16 }}>
                        {bangKeData.tongCong?.toLocaleString("vi-VN")}đ
                      </strong>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell index={2} />
                    <Table.Summary.Cell index={3} />
                    <Table.Summary.Cell index={4} />
                  </Table.Summary.Row>
                </Table.Summary>
              )}
            />
          </div>
        )}
      </div>
    ),
  });

  if (loading) {
    return (
      <Spin size="large" tip="Đang tải...">
        <div style={{ minHeight: 400 }} />
      </Spin>
    );
  }

  if (!dotThu) {
    return (
      <ContentCard>
        <Empty description="Không tìm thấy đợt thu" />
        <div style={{ textAlign: "center", marginTop: 16 }}>
          <Button onClick={() => navigate("/fee/dot-thu")}>
            <ArrowLeftOutlined /> Quay lại
          </Button>
        </div>
      </ContentCard>
    );
  }

  // Xác định trạng thái đợt thu
  const getStatus = () => {
    const now = dayjs();
    const start = dayjs(dotThu.ngayBatDau);
    const end = dayjs(dotThu.ngayKetThuc);
    
    if (now.isBefore(start)) return { color: "default", text: "Chưa bắt đầu" };
    if (now.isAfter(end)) return { color: "red", text: "Đã kết thúc" };
    return { color: "green", text: "Đang diễn ra" };
  };
  
  const status = getStatus();

  return (
    <ContentCard
      title={
        <Space>
          <Button 
            type="text" 
            icon={<ArrowLeftOutlined />} 
            onClick={() => navigate("/fee/dot-thu")}
          />
          Chi tiết đợt thu: {dotThu.tenDotThu}
        </Space>
      }
    >
      {/* Thông tin đợt thu */}
      <Card size="small" style={{ marginBottom: 24 }}>
        <Descriptions column={3} size="small">
          <Descriptions.Item label="Tên đợt thu">
            <strong>{dotThu.tenDotThu}</strong>
          </Descriptions.Item>
          <Descriptions.Item label="Tòa nhà">
            <Tag color="blue">{dotThu.toaNha?.tenToaNha || "Chưa gán"}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Loại">
            <Tag color={dotThu.loaiDotThu === "PhiSinhHoat" ? "blue" : "green"}>
              {dotThu.loaiDotThu === "PhiSinhHoat" ? "Phí sinh hoạt" : "Đóng góp"}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Ngày bắt đầu">
            {dayjs(dotThu.ngayBatDau).format("DD/MM/YYYY")}
          </Descriptions.Item>
          <Descriptions.Item label="Ngày kết thúc">
            {dayjs(dotThu.ngayKetThuc).format("DD/MM/YYYY")}
          </Descriptions.Item>
          <Descriptions.Item label="Trạng thái">
            <Tag color={status.color}>{status.text}</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Tabs */}
      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

      {/* Modal thêm phí */}
      <Modal
        title="Thêm loại phí vào đợt thu"
        open={addFeeModalOpen}
        onCancel={() => setAddFeeModalOpen(false)}
        onOk={handleAddFee}
        confirmLoading={addingFee}
        okText="Thêm"
        cancelText="Hủy"
      >
        <div style={{ marginTop: 16 }}>
          <Select
            style={{ width: "100%" }}
            placeholder="Chọn loại phí"
            value={selectedFeeToAdd}
            onChange={setSelectedFeeToAdd}
            showSearch
            optionFilterProp="children"
          >
            {availableFees.map((fee) => (
              <Option key={fee.id} value={fee.id}>
                {fee.tenLoaiPhi} - {fee.donGia?.toLocaleString("vi-VN")}đ/{fee.donViTinh}
              </Option>
            ))}
          </Select>
          
          {availableFees.length === 0 && (
            <Alert
              message="Không có loại phí nào có thể thêm"
              description="Tất cả các loại phí đã được cấu hình trong đợt thu này."
              type="info"
              showIcon
              style={{ marginTop: 16 }}
            />
          )}
        </div>
      </Modal>
      
      {/* Modal xác nhận tạo hóa đơn */}
      <Modal
        title="Xác nhận tạo hóa đơn"
        open={calculateModalOpen}
        onCancel={() => setCalculateModalOpen(false)}
        onOk={handleCalculateInvoices}
        confirmLoading={calculating}
        okText="Tạo hóa đơn"
        cancelText="Hủy"
      >
        <Alert
          message="Tạo hóa đơn cho đợt thu"
          description={
            <div>
              <p>Hệ thống sẽ tạo hóa đơn cho <b>Tháng {String(dotThu?.thang || '').padStart(2, '0')}/{dotThu?.nam}</b>:</p>
              <ul>
                <li><b>Phí cố định</b> (Gửi xe, Quản lý...): Tính theo định mức hoặc số lượng = 1</li>
                <li><b>Phí biến đổi</b> (Điện, Nước): Tính theo chỉ số đã ghi</li>
                <li>Áp dụng đơn giá theo tòa nhà (BangGiaDichVu)</li>
                <li>Tạo/Cập nhật hóa đơn cho từng hộ gia đình</li>
              </ul>
              {hasUtilityFee && (
                <p style={{ color: "#faad14" }}>
                  <b>Lưu ý: Các hộ chưa có chỉ số điện/nước sẽ không được tính phí điện/nước.</b>
                </p>
              )}
            </div>
          }
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
        
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="Đợt thu">
            {dotThu?.tenDotThu}
          </Descriptions.Item>
          <Descriptions.Item label="Tòa nhà">
            {dotThu?.toaNha?.tenToaNha}
          </Descriptions.Item>
          <Descriptions.Item label="Kỳ thu (Tháng/Năm)">
            {dotThu?.thang && dotThu?.nam 
              ? `${String(dotThu.thang).padStart(2, '0')}/${dotThu.nam}` 
              : <span style={{color: 'red'}}>Chưa cấu hình</span>}
          </Descriptions.Item>
          <Descriptions.Item label="Các loại phí">
            {configuredFees.map(f => f.loaiPhi.tenLoaiPhi).join(", ")}
          </Descriptions.Item>
        </Descriptions>
      </Modal>
    </ContentCard>
  );
}
