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
  message,
  Popconfirm,
  Statistic,
  Row,
  Col,
  InputNumber,
  Alert,
  Spin,
  Empty,
  Divider,
} from "antd";
import {
  ArrowLeftOutlined,
  PlusOutlined,
  DeleteOutlined,
  ThunderboltOutlined,
  DropboxOutlined,
  SaveOutlined,
  ReloadOutlined,
  LockOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import { ContentCard, DataTable } from "../../components";
import { dotThuService, feeService, dienNuocService } from "../../services";

const { Option } = Select;

/**
 * Trang Chi tiết Đợt Thu.
 * 
 * CHỨC NĂNG:
 * - Xem thông tin đợt thu
 * - Cấu hình phí trong đợt thu (thêm/xóa loại phí)
 * - Tab Ghi Chỉ Số Điện Nước (chỉ hiện khi có phí Điện/Nước)
 */
export default function DotThuDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [dotThu, setDotThu] = useState(null);
  const [configuredFees, setConfiguredFees] = useState([]);
  const [hasUtilityFee, setHasUtilityFee] = useState(false);
  const [utilityFees, setUtilityFees] = useState([]); // List of Điện/Nước fees
  const [activeTab, setActiveTab] = useState("fees");
  
  // Modal thêm phí
  const [addFeeModalOpen, setAddFeeModalOpen] = useState(false);
  const [availableFees, setAvailableFees] = useState([]);
  const [selectedFeeToAdd, setSelectedFeeToAdd] = useState(null);
  const [addingFee, setAddingFee] = useState(false);

  // Ghi chỉ số
  const [selectedUtilityFee, setSelectedUtilityFee] = useState(null);
  const [chiSoData, setChiSoData] = useState([]);
  const [chiSoLoading, setChiSoLoading] = useState(false);
  const [statistics, setStatistics] = useState({ tongSo: 0, daNhap: 0, chuaNhap: 0 });
  const [hasChanges, setHasChanges] = useState(false);
  const [saving, setSaving] = useState(false);

  // Load dữ liệu ban đầu
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [dotThuData, feesData, hasUtility, utilityList] = await Promise.all([
        dotThuService.getById(id),
        dotThuService.getFeesInPeriod(id),
        dotThuService.hasUtilityFee(id),
        dotThuService.getUtilityFees(id),
      ]);
      
      setDotThu(dotThuData);
      setConfiguredFees(feesData);
      setHasUtilityFee(hasUtility);
      setUtilityFees(utilityList);
      
      // Tự động chọn phí biến đổi đầu tiên
      if (utilityList.length > 0) {
        setSelectedUtilityFee(utilityList[0].loaiPhi.id);
      }
    } catch (error) {
      message.error("Không thể tải thông tin đợt thu");
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

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

  // Load dữ liệu ghi chỉ số
  const loadChiSoData = useCallback(async () => {
    if (!selectedUtilityFee) return;
    
    setChiSoLoading(true);
    try {
      const [inputData, stats] = await Promise.all([
        dienNuocService.prepareInput(id, selectedUtilityFee),
        dienNuocService.getStatistics(id, selectedUtilityFee),
      ]);
      
      setChiSoData(inputData || []);
      setStatistics(stats);
      setHasChanges(false);
    } catch (error) {
      message.error("Không thể tải danh sách ghi chỉ số");
      console.error(error);
    } finally {
      setChiSoLoading(false);
    }
  }, [id, selectedUtilityFee]);

  useEffect(() => {
    if (activeTab === "chiSo" && selectedUtilityFee) {
      loadChiSoData();
    }
  }, [activeTab, selectedUtilityFee, loadChiSoData]);

  // Xử lý thay đổi chỉ số mới
  const handleChiSoChange = (hoGiaDinhId, value) => {
    setChiSoData(prev => 
      prev.map(item => 
        item.hoGiaDinhId === hoGiaDinhId 
          ? { ...item, chiSoMoi: value }
          : item
      )
    );
    setHasChanges(true);
  };

  // Lưu chỉ số
  const handleSaveChiSo = async () => {
    const dataToSave = chiSoData
      .filter(item => item.chiSoMoi !== null && item.chiSoMoi !== undefined)
      .map(item => ({
        hoGiaDinhId: item.hoGiaDinhId,
        chiSoCu: item.chiSoCu,
        chiSoMoi: item.chiSoMoi,
      }));

    if (dataToSave.length === 0) {
      message.warning("Chưa có chỉ số nào được nhập");
      return;
    }

    setSaving(true);
    try {
      await dienNuocService.saveAll({
        dotThuId: parseInt(id),
        loaiPhiId: selectedUtilityFee,
        danhSachChiSo: dataToSave,
      });
      
      message.success(`Đã lưu ${dataToSave.length} chỉ số thành công`);
      setHasChanges(false);
      await loadChiSoData();
    } catch (error) {
      message.error(error.response?.data?.message || "Lưu chỉ số thất bại");
    } finally {
      setSaving(false);
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
      title: "Đơn giá mặc định",
      dataIndex: ["loaiPhi", "donGia"],
      key: "donGia",
      width: 150,
      render: (value) => value?.toLocaleString("vi-VN") + " đ",
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
      render: (_, record) => {
        const isMandatory = ["Điện", "Nước"].includes(record.loaiPhi.tenLoaiPhi);
        return isMandatory ? (
          <Button type="text" disabled icon={<LockOutlined />} title="Phí bắt buộc" />
        ) : (
          <Popconfirm
            title="Xóa loại phí này?"
            onConfirm={() => handleRemoveFee(record.loaiPhi.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        );
      },
    },
  ];

  // Cột bảng ghi chỉ số
  const chiSoColumns = [
    {
      title: "Mã hộ",
      dataIndex: "maHoGiaDinh",
      key: "maHoGiaDinh",
      width: 100,
    },
    {
      title: "Chủ hộ",
      dataIndex: "tenChuHo",
      key: "tenChuHo",
    },
    {
      title: "Căn hộ",
      dataIndex: "soCanHo",
      key: "soCanHo",
      width: 100,
    },
    {
      title: "Chỉ số cũ",
      dataIndex: "chiSoCu",
      key: "chiSoCu",
      width: 120,
      render: (value) => value?.toLocaleString("vi-VN") || 0,
    },
    {
      title: "Chỉ số mới",
      dataIndex: "chiSoMoi",
      key: "chiSoMoi",
      width: 150,
      render: (value, record) => (
        <InputNumber
          value={value}
          min={record.chiSoCu || 0}
          onChange={(val) => handleChiSoChange(record.hoGiaDinhId, val)}
          style={{ width: "100%" }}
          placeholder="Nhập chỉ số"
        />
      ),
    },
    {
      title: "Tiêu thụ",
      key: "tieuThu",
      width: 100,
      render: (_, record) => {
        const tieuThu = (record.chiSoMoi || 0) - (record.chiSoCu || 0);
        return tieuThu > 0 ? tieuThu.toLocaleString("vi-VN") : "-";
      },
    },
    {
      title: "Đơn giá",
      dataIndex: "donGia",
      key: "donGia",
      width: 120,
      render: (value) => value?.toLocaleString("vi-VN") + " đ",
    },
    {
      title: "Thành tiền",
      key: "thanhTien",
      width: 130,
      render: (_, record) => {
        const tieuThu = (record.chiSoMoi || 0) - (record.chiSoCu || 0);
        const thanhTien = tieuThu * (record.donGia || 0);
        return thanhTien > 0 
          ? <strong style={{ color: "#1890ff" }}>{thanhTien.toLocaleString("vi-VN")} đ</strong>
          : "-";
      },
    },
  ];

  // Tabs configuration
  const tabItems = [
    {
      key: "fees",
      label: "Cấu hình phí",
      children: (
        <div>
          <div style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openAddFeeModal}>
              Thêm loại phí
            </Button>
          </div>
          
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

  // Thêm tab Ghi Chỉ Số nếu có phí biến đổi
  if (hasUtilityFee) {
    tabItems.push({
      key: "chiSo",
      label: (
        <Space>
          <ThunderboltOutlined />
          Ghi Chỉ Số Điện/Nước
        </Space>
      ),
      children: (
        <div>
          {/* Chọn loại phí */}
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={8}>
              <Select
                value={selectedUtilityFee}
                onChange={setSelectedUtilityFee}
                style={{ width: "100%" }}
                placeholder="Chọn loại phí"
              >
                {utilityFees.map((f) => (
                  <Option key={f.loaiPhi.id} value={f.loaiPhi.id}>
                    {f.loaiPhi.tenLoaiPhi === "Điện" ? <ThunderboltOutlined /> : <DropboxOutlined />}
                    {" "}{f.loaiPhi.tenLoaiPhi}
                  </Option>
                ))}
              </Select>
            </Col>
            <Col span={4}>
              <Button icon={<ReloadOutlined />} onClick={loadChiSoData} loading={chiSoLoading}>
                Làm mới
              </Button>
            </Col>
          </Row>

          {/* Thống kê */}
          <Card size="small" style={{ marginBottom: 16 }}>
            <Row gutter={24}>
              <Col span={6}>
                <Statistic title="Tổng căn hộ" value={statistics.tongSo} />
              </Col>
              <Col span={6}>
                <Statistic title="Đã nhập" value={statistics.daNhap} valueStyle={{ color: "#52c41a" }} />
              </Col>
              <Col span={6}>
                <Statistic title="Chưa nhập" value={statistics.chuaNhap} valueStyle={{ color: "#faad14" }} />
              </Col>
              <Col span={6}>
                <Statistic 
                  title="Hoàn thành" 
                  value={statistics.phanTramHoanThanh || 0} 
                  suffix="%" 
                  valueStyle={{ color: "#1890ff" }}
                />
              </Col>
            </Row>
          </Card>

          {/* Alert nếu có thay đổi */}
          {hasChanges && (
            <Alert
              message="Có thay đổi chưa lưu"
              description="Nhấn nút Lưu để cập nhật chỉ số vào hệ thống."
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
              action={
                <Button type="primary" icon={<SaveOutlined />} onClick={handleSaveChiSo} loading={saving}>
                  Lưu chỉ số
                </Button>
              }
            />
          )}

          {/* Bảng ghi chỉ số */}
          <Spin spinning={chiSoLoading}>
            <DataTable
              columns={chiSoColumns}
              dataSource={chiSoData}
              rowKey="hoGiaDinhId"
              pagination={{ pageSize: 20 }}
              scroll={{ x: 1000 }}
            />
          </Spin>

          {/* Nút lưu */}
          <Divider />
          <Row justify="end">
            <Button 
              type="primary" 
              size="large"
              icon={<SaveOutlined />} 
              onClick={handleSaveChiSo}
              loading={saving}
              disabled={!hasChanges}
            >
              Lưu chỉ số ({chiSoData.filter(c => c.chiSoMoi != null).length} bản ghi)
            </Button>
          </Row>
        </div>
      ),
    });
  }

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
    </ContentCard>
  );
}
