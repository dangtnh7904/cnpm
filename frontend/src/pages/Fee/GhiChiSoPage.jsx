import React, { useState, useEffect, useCallback } from "react";
import { 
  Select, 
  Button, 
  InputNumber, 
  message, 
  Space, 
  Alert,
  Divider,
  Typography,
  Card,
  Row,
  Col,
  Spin,
  Tag,
  Tooltip,
  Statistic
} from "antd";
import { 
  SaveOutlined, 
  ReloadOutlined, 
  ThunderboltOutlined,
  DropboxOutlined,
  BankOutlined,
  CalculatorOutlined,
  WarningOutlined
} from "@ant-design/icons";
import { ContentCard, DataTable } from "../../components";
import { dotThuService, dienNuocService, buildingService } from "../../services";

const { Option } = Select;
const { Text } = Typography;

/**
 * Trang Ghi Chỉ Số Điện Nước.
 * 
 * LUỒNG XỬ LÝ:
 * 1. Chọn Đợt thu → Loại phí (Điện/Nước) → Tòa nhà
 * 2. Load danh sách căn hộ với chỉ số cũ
 * 3. Nhập chỉ số mới → Tự động tính tiêu thụ và thành tiền
 * 4. Lưu & Tính toán → Sinh hóa đơn
 */
export default function GhiChiSoPage() {
  // ===== FILTER STATE =====
  const [dotThus, setDotThus] = useState([]);
  const [loaiPhis, setLoaiPhis] = useState([]);
  const [buildings, setBuildings] = useState([]);
  
  const [selectedDotThu, setSelectedDotThu] = useState(null);
  const [selectedLoaiPhi, setSelectedLoaiPhi] = useState(null);
  const [selectedToaNha, setSelectedToaNha] = useState(null);
  
  // ===== DATA STATE =====
  const [chiSoData, setChiSoData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [loadingFilters, setLoadingFilters] = useState(true);
  
  // ===== STATISTICS =====
  const totalHouseholds = chiSoData.length;
  const enteredCount = chiSoData.filter(item => item.chiSoMoi !== null && item.chiSoMoi !== undefined).length;
  const invalidCount = chiSoData.filter(item => 
    item.chiSoMoi !== null && item.chiSoMoi !== undefined && item.chiSoMoi < (item.chiSoCu || 0)
  ).length;
  const totalAmount = chiSoData.reduce((sum, item) => sum + (item.thanhTien || 0), 0);

  // ===== LOAD FILTER DATA =====
  useEffect(() => {
    const loadFilters = async () => {
      setLoadingFilters(true);
      try {
        const [dotThuData, loaiPhiData, buildingData] = await Promise.all([
          dotThuService.getAllForDropdown(),
          dienNuocService.getLoaiPhiBienDoi(),
          buildingService.getAllForDropdown(),
        ]);
        
        setDotThus(dotThuData);
        setLoaiPhis(loaiPhiData);
        setBuildings(buildingData);
        
        // Auto-select nếu chỉ có 1 option
        if (dotThuData.length === 1) setSelectedDotThu(dotThuData[0].id);
        if (loaiPhiData.length === 1) setSelectedLoaiPhi(loaiPhiData[0].id);
        if (buildingData.length === 1) setSelectedToaNha(buildingData[0].id);
      } catch (error) {
        message.error("Không thể tải dữ liệu bộ lọc");
      } finally {
        setLoadingFilters(false);
      }
    };
    loadFilters();
  }, []);

  // ===== LOAD DATA KHI ĐỦ FILTER =====
  useEffect(() => {
    if (selectedDotThu && selectedLoaiPhi && selectedToaNha) {
      loadChiSoData();
    } else {
      setChiSoData([]);
    }
  }, [selectedDotThu, selectedLoaiPhi, selectedToaNha]);

  const loadChiSoData = async () => {
    setLoading(true);
    try {
      const data = await dienNuocService.getDanhSachGhiChiSo(
        selectedDotThu, 
        selectedLoaiPhi, 
        selectedToaNha
      );
      
      // Map data với key và tính toán
      const mappedData = data.map((item, index) => ({
        ...item,
        key: item.hoGiaDinhId || index,
        tieuThu: calculateTieuThu(item.chiSoCu, item.chiSoMoi),
        thanhTien: calculateThanhTien(item.chiSoCu, item.chiSoMoi, item.donGia),
      }));
      
      setChiSoData(mappedData);
    } catch (error) {
      message.error("Không thể tải dữ liệu: " + (error.response?.data?.message || error.message || "Lỗi không xác định"));
      setChiSoData([]);
    } finally {
      setLoading(false);
    }
  };

  // ===== HELPER FUNCTIONS =====
  const calculateTieuThu = (chiSoCu, chiSoMoi) => {
    if (chiSoMoi === null || chiSoMoi === undefined) return null;
    return Math.max(0, (chiSoMoi || 0) - (chiSoCu || 0));
  };

  const calculateThanhTien = (chiSoCu, chiSoMoi, donGia) => {
    const tieuThu = calculateTieuThu(chiSoCu, chiSoMoi);
    if (tieuThu === null) return 0;
    return tieuThu * (donGia || 0);
  };

  // ===== HANDLE CHANGE CHỈ SỐ MỚI =====
  const handleChiSoMoiChange = useCallback((hoGiaDinhId, value) => {
    setChiSoData(prev => 
      prev.map(item => {
        if (item.hoGiaDinhId !== hoGiaDinhId) return item;
        
        const chiSoMoi = value;
        const tieuThu = calculateTieuThu(item.chiSoCu, chiSoMoi);
        const thanhTien = calculateThanhTien(item.chiSoCu, chiSoMoi, item.donGia);
        
        return { ...item, chiSoMoi, tieuThu, thanhTien };
      })
    );
  }, []);

  // ===== SAVE & CALCULATE =====
  const handleSaveAndCalculate = async () => {
    // Validate
    const dataToSave = chiSoData.filter(item => 
      item.chiSoMoi !== null && item.chiSoMoi !== undefined
    );
    
    if (dataToSave.length === 0) {
      message.warning("Chưa có chỉ số nào được nhập");
      return;
    }
    
    // Check invalid entries
    const invalidEntries = dataToSave.filter(item => item.chiSoMoi < (item.chiSoCu || 0));
    if (invalidEntries.length > 0) {
      message.error(`Có ${invalidEntries.length} chỉ số mới nhỏ hơn chỉ số cũ. Vui lòng kiểm tra lại.`);
      return;
    }
    
    setSaving(true);
    try {
      const danhSachChiSo = dataToSave.map(item => ({
        hoGiaDinhId: item.hoGiaDinhId,
        chiSoCu: item.chiSoCu || 0,
        chiSoMoi: item.chiSoMoi,
      }));
      
      const result = await dienNuocService.saveAndCalculate(
        selectedDotThu, 
        selectedLoaiPhi, 
        danhSachChiSo
      );
      
      message.success(result.message || `Đã lưu ${dataToSave.length} chỉ số và cập nhật hóa đơn thành công!`);
      
      // Reload data
      await loadChiSoData();
    } catch (error) {
      message.error("Lưu thất bại: " + (error.response?.data?.message || error.message || "Lỗi không xác định"));
    } finally {
      setSaving(false);
    }
  };

  // ===== GET LOẠI PHÍ ICON =====
  const getLoaiPhiIcon = () => {
    const loaiPhi = loaiPhis.find(lp => lp.id === selectedLoaiPhi);
    if (!loaiPhi) return <CalculatorOutlined />;
    
    if (loaiPhi.tenLoaiPhi?.toLowerCase().includes("điện")) {
      return <ThunderboltOutlined style={{ color: "#faad14" }} />;
    }
    if (loaiPhi.tenLoaiPhi?.toLowerCase().includes("nước")) {
      return <DropboxOutlined style={{ color: "#1890ff" }} />;
    }
    return <CalculatorOutlined />;
  };

  // ===== COLUMNS =====
  const columns = [
    {
      title: "Mã căn hộ",
      dataIndex: "maHoGiaDinh",
      key: "maHoGiaDinh",
      width: 120,
      render: (text) => <Text strong>{text}</Text>,
    },
    {
      title: "Chủ hộ",
      dataIndex: "tenChuHo",
      key: "tenChuHo",
      width: 180,
    },
    {
      title: "Chỉ số cũ",
      dataIndex: "chiSoCu",
      key: "chiSoCu",
      width: 120,
      align: "right",
      render: (value) => (
        <Text type="secondary">
          {new Intl.NumberFormat("vi-VN").format(value || 0)}
        </Text>
      ),
    },
    {
      title: "Chỉ số mới",
      key: "chiSoMoi",
      width: 150,
      render: (_, record) => {
        const isInvalid = record.chiSoMoi !== null && 
                          record.chiSoMoi !== undefined && 
                          record.chiSoMoi < (record.chiSoCu || 0);
        
        return (
          <InputNumber
            value={record.chiSoMoi}
            onChange={(value) => handleChiSoMoiChange(record.hoGiaDinhId, value)}
            min={0}
            style={{ 
              width: 120,
              borderColor: isInvalid ? "#ff4d4f" : undefined,
            }}
            status={isInvalid ? "error" : undefined}
            placeholder="Nhập..."
          />
        );
      },
    },
    {
      title: "Tiêu thụ",
      key: "tieuThu",
      width: 100,
      align: "right",
      render: (_, record) => {
        if (record.tieuThu === null) return <Text type="secondary">-</Text>;
        
        const isNegative = record.tieuThu < 0;
        return (
          <Text strong style={{ color: isNegative ? "#ff4d4f" : "#52c41a" }}>
            {new Intl.NumberFormat("vi-VN").format(record.tieuThu)}
            {isNegative && (
              <Tooltip title="Chỉ số mới nhỏ hơn chỉ số cũ!">
                <WarningOutlined style={{ marginLeft: 4, color: "#ff4d4f" }} />
              </Tooltip>
            )}
          </Text>
        );
      },
    },
    {
      title: "Đơn giá",
      dataIndex: "donGia",
      key: "donGia",
      width: 120,
      align: "right",
      render: (value) => (
        <Text type="secondary">
          {new Intl.NumberFormat("vi-VN").format(value || 0)} đ
        </Text>
      ),
    },
    {
      title: "Thành tiền (tạm tính)",
      key: "thanhTien",
      width: 150,
      align: "right",
      render: (_, record) => {
        if (!record.thanhTien) return <Text type="secondary">-</Text>;
        return (
          <Text strong style={{ color: "#1890ff" }}>
            {new Intl.NumberFormat("vi-VN").format(record.thanhTien)} đ
          </Text>
        );
      },
    },
  ];

  // ===== CHECK IF FILTERS COMPLETE =====
  const isFilterComplete = selectedDotThu && selectedLoaiPhi && selectedToaNha;

  // ===== RENDER =====
  return (
    <ContentCard
      title={
        <Space>
          {getLoaiPhiIcon()}
          <span>Ghi Chỉ Số Điện Nước</span>
        </Space>
      }
    >
      {/* FILTER BAR */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col xs={24} sm={8}>
            <Space direction="vertical" size={4} style={{ width: "100%" }}>
              <Text type="secondary">Đợt thu:</Text>
              <Select
                placeholder="Chọn đợt thu"
                value={selectedDotThu}
                onChange={setSelectedDotThu}
                style={{ width: "100%" }}
                loading={loadingFilters}
                showSearch
                optionFilterProp="children"
              >
                {dotThus.map((dt) => (
                  <Option key={dt.id} value={dt.id}>
                    {dt.tenDotThu}
                  </Option>
                ))}
              </Select>
            </Space>
          </Col>
          <Col xs={24} sm={8}>
            <Space direction="vertical" size={4} style={{ width: "100%" }}>
              <Text type="secondary">Loại phí:</Text>
              <Select
                placeholder="Chọn loại phí (Điện/Nước)"
                value={selectedLoaiPhi}
                onChange={setSelectedLoaiPhi}
                style={{ width: "100%" }}
                loading={loadingFilters}
              >
                {loaiPhis.map((lp) => (
                  <Option key={lp.id} value={lp.id}>
                    {lp.tenLoaiPhi?.toLowerCase().includes("điện") && (
                      <ThunderboltOutlined style={{ marginRight: 8, color: "#faad14" }} />
                    )}
                    {lp.tenLoaiPhi?.toLowerCase().includes("nước") && (
                      <DropboxOutlined style={{ marginRight: 8, color: "#1890ff" }} />
                    )}
                    {lp.tenLoaiPhi}
                  </Option>
                ))}
              </Select>
            </Space>
          </Col>
          <Col xs={24} sm={8}>
            <Space direction="vertical" size={4} style={{ width: "100%" }}>
              <Text type="secondary">Tòa nhà:</Text>
              <Select
                placeholder="Chọn tòa nhà"
                value={selectedToaNha}
                onChange={setSelectedToaNha}
                style={{ width: "100%" }}
                loading={loadingFilters}
                showSearch
                optionFilterProp="children"
              >
                {buildings.map((b) => (
                  <Option key={b.id} value={b.id}>
                    <BankOutlined style={{ marginRight: 8 }} />
                    {b.tenToaNha}
                  </Option>
                ))}
              </Select>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* CONTENT */}
      {!isFilterComplete ? (
        <Alert
          message="Vui lòng chọn đầy đủ bộ lọc"
          description="Chọn Đợt thu, Loại phí và Tòa nhà để xem danh sách căn hộ cần ghi chỉ số."
          type="info"
          showIcon
          style={{ marginTop: 20 }}
        />
      ) : loading ? (
        <Spin size="large" tip="Đang tải danh sách...">
          <div style={{ textAlign: "center", padding: 60 }} />
        </Spin>
      ) : (
        <>
          {/* STATISTICS */}
          <Card size="small" style={{ marginBottom: 16 }}>
            <Row gutter={24}>
              <Col span={6}>
                <Statistic 
                  title="Tổng căn hộ" 
                  value={totalHouseholds} 
                  valueStyle={{ fontSize: 18 }}
                />
              </Col>
              <Col span={6}>
                <Statistic 
                  title="Đã nhập" 
                  value={enteredCount}
                  suffix={`/ ${totalHouseholds}`}
                  valueStyle={{ fontSize: 18, color: "#52c41a" }}
                />
              </Col>
              <Col span={6}>
                <Statistic 
                  title="Lỗi" 
                  value={invalidCount}
                  valueStyle={{ fontSize: 18, color: invalidCount > 0 ? "#ff4d4f" : undefined }}
                />
              </Col>
              <Col span={6}>
                <Statistic 
                  title="Tổng tiền (tạm tính)" 
                  value={totalAmount}
                  formatter={(value) => `${new Intl.NumberFormat("vi-VN").format(value)} đ`}
                  valueStyle={{ fontSize: 18, color: "#1890ff" }}
                />
              </Col>
            </Row>
          </Card>

          {/* HƯỚNG DẪN */}
          <Alert
            message="Hướng dẫn"
            description={
              <span>
                Nhập <b>Chỉ số mới</b> cho từng căn hộ. Hệ thống sẽ tự động tính tiêu thụ và thành tiền.
                <b> Chỉ số mới phải lớn hơn hoặc bằng Chỉ số cũ.</b>
              </span>
            }
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />

          {/* DATA TABLE */}
          <DataTable
            columns={columns}
            dataSource={chiSoData}
            loading={loading}
            pagination={false}
            rowKey="hoGiaDinhId"
            scroll={{ y: 400 }}
            rowClassName={(record) => {
              if (record.chiSoMoi !== null && record.chiSoMoi !== undefined) {
                if (record.chiSoMoi < (record.chiSoCu || 0)) {
                  return "row-error";
                }
                return "row-entered";
              }
              return "";
            }}
          />

          {/* FOOTER ACTIONS */}
          <Divider />
          <Row justify="space-between" align="middle">
            <Col>
              <Button
                icon={<ReloadOutlined />}
                onClick={loadChiSoData}
                disabled={loading}
              >
                Làm mới
              </Button>
            </Col>
            <Col>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={handleSaveAndCalculate}
                loading={saving}
                disabled={enteredCount === 0 || invalidCount > 0}
                size="large"
              >
                Lưu & Tính toán ({enteredCount} căn hộ)
              </Button>
            </Col>
          </Row>

          <style>{`
            .row-entered {
              background-color: rgba(82, 196, 26, 0.1) !important;
            }
            .row-entered:hover > td {
              background-color: rgba(82, 196, 26, 0.2) !important;
            }
            .row-error {
              background-color: rgba(255, 77, 79, 0.1) !important;
            }
            .row-error:hover > td {
              background-color: rgba(255, 77, 79, 0.2) !important;
            }
          `}</style>
        </>
      )}
    </ContentCard>
  );
}
