import React, { useState, useEffect, useCallback } from "react";
import { 
  Select, 
  Button, 
  InputNumber, 
  message, 
  Space, 
  Tag, 
  Tooltip, 
  Spin, 
  Alert,
  Divider,
  Typography,
  Card,
  Statistic,
  Row,
  Col 
} from "antd";
import { 
  SaveOutlined, 
  ReloadOutlined, 
  InfoCircleOutlined,
  DollarOutlined,
  BankOutlined,
  CheckCircleOutlined
} from "@ant-design/icons";
import { ContentCard, DataTable } from "../../components";
import { buildingService, bangGiaService } from "../../services";

const { Option } = Select;
const { Text } = Typography;

/**
 * Trang Cấu hình Bảng Giá Dịch Vụ Theo Tòa Nhà.
 * 
 * LUỒNG XỬ LÝ:
 * 1. Chọn tòa nhà từ dropdown.
 * 2. Load bảng giá: API GET /api/bang-gia/toa-nha/{id}
 * 3. Hiển thị tất cả loại phí với giá gốc và giá riêng (editable).
 * 4. Lưu cấu hình: API POST /api/bang-gia/cau-hinh (Bulk Upsert).
 * 
 * LOGIC GIÁ ƯU TIÊN:
 * - Nếu có giá riêng (BangGiaDichVu) → Dùng giá riêng.
 * - Nếu không → Dùng giá gốc từ LoaiPhi.
 */
export default function DinhMucThuPage() {
  // ===== STATE =====
  const [buildings, setBuildings] = useState([]);
  const [selectedBuilding, setSelectedBuilding] = useState(null);
  const [selectedBuildingName, setSelectedBuildingName] = useState("");
  const [priceData, setPriceData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  // ===== STATISTICS =====
  const customPriceCount = priceData.filter(item => item.donGiaRieng !== null && item.donGiaRieng !== undefined).length;
  const totalFeeTypes = priceData.length;
  const changedCount = priceData.filter(item => item.editedPrice !== item.originalPrice).length;

  // ===== LOAD DANH SÁCH TÒA NHÀ =====
  useEffect(() => {
    const fetchBuildings = async () => {
      try {
        const data = await buildingService.getAllForDropdown();
        setBuildings(data);
        // Auto-select first building if available
        if (data.length > 0 && !selectedBuilding) {
          setSelectedBuilding(data[0].id);
          setSelectedBuildingName(data[0].tenToaNha);
        }
      } catch (error) {
        message.error("Không thể tải danh sách tòa nhà");
      }
    };
    fetchBuildings();
  }, []);

  // ===== LOAD BẢNG GIÁ KHI CHỌN TÒA NHÀ =====
  useEffect(() => {
    if (selectedBuilding) {
      loadPriceData(selectedBuilding);
    }
  }, [selectedBuilding]);

  const loadPriceData = async (toaNhaId) => {
    setLoading(true);
    try {
      const data = await bangGiaService.getByToaNhaFull(toaNhaId);
      // Map data để có editedPrice cho mỗi row
      const mappedData = data.map((item) => ({
        ...item,
        key: item.loaiPhiId,
        editedPrice: item.donGiaRieng ?? item.donGiaMacDinh, // Giá hiện tại
        originalPrice: item.donGiaRieng ?? item.donGiaMacDinh, // Để so sánh thay đổi
      }));
      setPriceData(mappedData);
      setHasChanges(false);
    } catch (error) {
      message.error("Không thể tải bảng giá. Vui lòng thử lại.");
      setPriceData([]);
    } finally {
      setLoading(false);
    }
  };

  // ===== XỬ LÝ THAY ĐỔI GIÁ =====
  const handlePriceChange = useCallback((loaiPhiId, newPrice) => {
    setPriceData((prev) =>
      prev.map((item) =>
        item.loaiPhiId === loaiPhiId
          ? { ...item, editedPrice: newPrice }
          : item
      )
    );
    setHasChanges(true);
  }, []);

  // ===== RESET VỀ GIÁ GỐC =====
  const handleResetPrice = useCallback((loaiPhiId) => {
    setPriceData((prev) =>
      prev.map((item) =>
        item.loaiPhiId === loaiPhiId
          ? { ...item, editedPrice: item.donGiaMacDinh }
          : item
      )
    );
    setHasChanges(true);
  }, []);

  // ===== CHỌN TÒA NHÀ =====
  const handleBuildingChange = (value) => {
    const building = buildings.find(b => b.id === value);
    setSelectedBuilding(value);
    setSelectedBuildingName(building?.tenToaNha || "");
    setHasChanges(false);
  };

  // ===== LƯU CẤU HÌNH =====
  const handleSave = async () => {
    if (!selectedBuilding) {
      message.warning("Vui lòng chọn tòa nhà");
      return;
    }

    // Lọc những item có giá khác giá gốc (có cấu hình riêng)
    const changedItems = priceData.filter(
      (item) => item.editedPrice !== item.donGiaMacDinh
    );

    if (changedItems.length === 0) {
      message.info("Không có giá riêng nào được cấu hình. Tòa nhà sẽ sử dụng giá mặc định.");
      return;
    }

    setSaving(true);
    try {
      const danhSachGia = changedItems.map((item) => ({
        loaiPhiId: item.loaiPhiId,
        donGiaRieng: item.editedPrice,
        ghiChu: `Giá riêng cho tòa ${selectedBuildingName}`,
      }));

      await bangGiaService.cauHinhGia(selectedBuilding, danhSachGia);

      message.success({
        content: (
          <span>
            <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
            Đã lưu giá và tự động cập nhật định mức cho toàn bộ cư dân tòa <b>{selectedBuildingName}</b>
          </span>
        ),
        duration: 5,
      });

      // Reload data
      await loadPriceData(selectedBuilding);
    } catch (error) {
      message.error("Lưu cấu hình thất bại: " + (error.message || "Lỗi không xác định"));
    } finally {
      setSaving(false);
    }
  };

  // ===== RESET TẤT CẢ VỀ GIÁ MẶC ĐỊNH =====
  const handleResetAll = async () => {
    if (!selectedBuilding) return;

    try {
      await bangGiaService.resetToaNha(selectedBuilding);
      message.success(`Đã reset tất cả về giá mặc định cho tòa ${selectedBuildingName}`);
      await loadPriceData(selectedBuilding);
    } catch (error) {
      message.error("Reset thất bại");
    }
  };

  // ===== COLUMNS =====
  const columns = [
    {
      title: "Loại phí",
      dataIndex: "tenLoaiPhi",
      key: "tenLoaiPhi",
      width: 220,
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{text}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.donViTinh}
          </Text>
        </Space>
      ),
    },
    {
      title: (
        <Tooltip title="Giá mặc định áp dụng cho tất cả tòa nhà khi chưa có cấu hình riêng">
          <Space>
            Đơn giá Gốc
            <InfoCircleOutlined style={{ color: "#8c8c8c" }} />
          </Space>
        </Tooltip>
      ),
      dataIndex: "donGiaMacDinh",
      key: "donGiaMacDinh",
      width: 150,
      align: "right",
      render: (value) => (
        <Text type="secondary" style={{ fontSize: 13 }}>
          {new Intl.NumberFormat("vi-VN").format(value || 0)} đ
        </Text>
      ),
    },
    {
      title: (
        <Tooltip title="Nhập giá riêng cho tòa nhà này. Để trống hoặc bằng giá gốc = dùng giá mặc định.">
          <Space>
            <span style={{ color: "#1890ff", fontWeight: 600 }}>Đơn giá Riêng</span>
            <InfoCircleOutlined style={{ color: "#1890ff" }} />
          </Space>
        </Tooltip>
      ),
      key: "editedPrice",
      width: 200,
      render: (_, record) => {
        const isModified = record.editedPrice !== record.donGiaMacDinh;
        const isChanged = record.editedPrice !== record.originalPrice;
        
        return (
          <Space>
            <InputNumber
              value={record.editedPrice}
              onChange={(value) => handlePriceChange(record.loaiPhiId, value)}
              formatter={(value) =>
                `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
              }
              parser={(value) => value.replace(/\$\s?|(,*)/g, "")}
              min={0}
              style={{ width: 150 }}
              suffix="đ"
            />
            {isModified && (
              <Tooltip title="Reset về giá gốc">
                <Button
                  type="text"
                  size="small"
                  icon={<ReloadOutlined />}
                  onClick={() => handleResetPrice(record.loaiPhiId)}
                  style={{ color: "#faad14" }}
                />
              </Tooltip>
            )}
          </Space>
        );
      },
    },
    {
      title: "Trạng thái",
      key: "status",
      width: 140,
      align: "center",
      render: (_, record) => {
        // Chỉ dựa trên giá đã lưu (donGiaRieng từ DB)
        const hasCustom = record.donGiaRieng !== null && record.donGiaRieng !== undefined;
        
        if (hasCustom) {
          return <Tag color="green">Giá riêng</Tag>;
        }
        return <Tag color="default">Giá mặc định</Tag>;
      },
    },
    {
      title: "Chênh lệch",
      key: "diff",
      width: 120,
      align: "right",
      render: (_, record) => {
        const diff = (record.editedPrice || 0) - (record.donGiaMacDinh || 0);
        if (diff === 0) return <Text type="secondary">-</Text>;
        return (
          <Text strong style={{ color: diff > 0 ? "#f5222d" : "#52c41a" }}>
            {diff > 0 ? "+" : ""}
            {new Intl.NumberFormat("vi-VN").format(diff)} đ
          </Text>
        );
      },
    },
  ];

  // ===== RENDER =====
  return (
    <ContentCard
      title={
        <Space>
          <DollarOutlined />
          <span>Cấu hình Bảng Giá Theo Tòa Nhà</span>
        </Space>
      }
    >
      {/* HEADER: Chọn Tòa Nhà */}
      <Card 
        size="small" 
        style={{ marginBottom: 16 }}
      >
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <Space size="large">
              <Space>
                <BankOutlined style={{ fontSize: 18, color: "#1890ff" }} />
                <Text strong>Tòa nhà:</Text>
                <Select
                  placeholder="Chọn tòa nhà để cấu hình giá"
                  value={selectedBuilding}
                  onChange={handleBuildingChange}
                  style={{ width: 280 }}
                  showSearch
                  optionFilterProp="children"
                  loading={buildings.length === 0}
                  size="large"
                >
                  {buildings.map((building) => (
                    <Option key={building.id} value={building.id}>
                      {building.tenToaNha}
                    </Option>
                  ))}
                </Select>
              </Space>
            </Space>
          </Col>
          <Col>
            <Space split={<Divider type="vertical" />}>
              <Statistic 
                title="Tổng loại phí" 
                value={totalFeeTypes} 
                valueStyle={{ fontSize: 16 }}
              />
              <Statistic 
                title="Có giá riêng" 
                value={customPriceCount} 
                valueStyle={{ fontSize: 16, color: "#52c41a" }}
              />
              {changedCount > 0 && (
                <Statistic 
                  title="Chưa lưu" 
                  value={changedCount} 
                  valueStyle={{ fontSize: 16, color: "#1890ff" }}
                />
              )}
            </Space>
          </Col>
        </Row>
      </Card>

      {/* BODY: Bảng giá */}
      {!selectedBuilding ? (
        <Alert
          message="Vui lòng chọn tòa nhà"
          description="Chọn tòa nhà từ dropdown ở trên để xem và cấu hình bảng giá dịch vụ."
          type="info"
          showIcon
          style={{ marginTop: 20 }}
        />
      ) : loading ? (
        <Spin size="large" tip="Đang tải bảng giá...">
          <div style={{ textAlign: "center", padding: 60 }} />
        </Spin>
      ) : (
        <>
          <Alert
            message="Hướng dẫn"
            description={
              <span>
                Nhập <b>Đơn giá Riêng</b> cho từng loại phí. Các loại phí không được cấu hình sẽ sử dụng <b>Đơn giá Gốc</b> (mặc định).
                Sau khi lưu, giá mới sẽ tự động áp dụng cho tất cả cư dân trong tòa.
              </span>
            }
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />
          
          <DataTable
            columns={columns}
            dataSource={priceData}
            loading={loading}
            pagination={false}
            rowKey="loaiPhiId"
            rowClassName={(record) => {
              // Chỉ highlight row có giá riêng đã lưu (từ DB)
              const hasCustom = record.donGiaRieng !== null && record.donGiaRieng !== undefined;
              return hasCustom ? "row-custom" : "";
            }}
          />

          {/* FOOTER: Actions */}
          <Divider />
          <Row justify="space-between" align="middle">
            <Col>
              <Button
                onClick={handleResetAll}
                disabled={!selectedBuilding || loading || customPriceCount === 0}
              >
                Reset tất cả về giá mặc định
              </Button>
            </Col>
            <Col>
              <Space>
                <Button
                  onClick={() => loadPriceData(selectedBuilding)}
                  disabled={loading}
                >
                  Làm mới
                </Button>
                <Button
                  type="primary"
                  icon={<SaveOutlined />}
                  onClick={handleSave}
                  loading={saving}
                  disabled={!hasChanges || !selectedBuilding}
                  size="large"
                >
                  Lưu & Áp Dụng
                </Button>
              </Space>
            </Col>
          </Row>

          <style>{`
            .row-custom {
              background-color: rgba(82, 196, 26, 0.1) !important;
            }
            .row-custom:hover > td {
              background-color: rgba(82, 196, 26, 0.2) !important;
            }
          `}</style>
        </>
      )}
    </ContentCard>
  );
}

