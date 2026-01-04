import React, { useState, useEffect, useCallback } from "react";
import { 
  Select, 
  Button, 
  InputNumber, 
  Space, 
  Alert,
  Divider,
  Typography,
  Card,
  Row,
  Col,
  Spin,
  Tooltip,
  Statistic,
  App
} from "antd";
import { 
  SaveOutlined, 
  ReloadOutlined, 
  ThunderboltOutlined,
  DropboxOutlined,
  BankOutlined,
  CalculatorOutlined,
  WarningOutlined,
  CalendarOutlined
} from "@ant-design/icons";
import { ContentCard, DataTable } from "../../components";
import { dienNuocService, buildingService } from "../../services";

const { Option } = Select;
const { Text } = Typography;

/**
 * Trang Ghi Chỉ Số Điện Nước.
 * 
 * LUỒNG XỬ LÝ MỚI (tách rời ghi số và thu tiền):
 * 1. Chọn Tháng/Năm → Loại phí (Điện/Nước) → Tòa nhà
 * 2. Load danh sách căn hộ với chỉ số tháng trước
 * 3. Nhập chỉ số mới → Tự động tính tiêu thụ (chỉ hiển thị, không tính tiền)
 * 4. Lưu chỉ số → Việc tính tiền sẽ thực hiện khi tạo Đợt thu
 */
export default function GhiChiSoPage() {
  const { message } = App.useApp();
  
  // ===== FILTER STATE =====
  const [loaiPhis, setLoaiPhis] = useState([]);
  const [buildings, setBuildings] = useState([]);
  
  // Tháng/Năm mặc định là tháng hiện tại - CHỈ TÍNH 1 LẦN KHI MOUNT
  const [selectedThang, setSelectedThang] = useState(() => new Date().getMonth() + 1);
  const [selectedNam, setSelectedNam] = useState(() => new Date().getFullYear());
  const [selectedLoaiPhi, setSelectedLoaiPhi] = useState(null);
  const [selectedToaNha, setSelectedToaNha] = useState(null);
  
  // Reference date for display (không thay đổi)
  const currentYear = new Date().getFullYear();
  
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
  const totalTieuThu = chiSoData.reduce((sum, item) => sum + (item.tieuThu || 0), 0);
  
  // ===== GENERATE OPTIONS =====
  const thangOptions = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: `Tháng ${i + 1}`,
  }));
  
  const namOptions = Array.from({ length: 10 }, (_, i) => ({
    value: currentYear - 5 + i,
    label: `${currentYear - 5 + i}`,
  }));

  // ===== LOAD FILTER DATA =====
  useEffect(() => {
    const loadFilters = async () => {
      setLoadingFilters(true);
      try {
        const [loaiPhiData, buildingData] = await Promise.all([
          dienNuocService.getLoaiPhiBienDoi(),
          buildingService.getAllForDropdown(),
        ]);
        
        setLoaiPhis(loaiPhiData);
        setBuildings(buildingData);
        
        // Auto-select nếu chỉ có 1 option
        if (loaiPhiData.length === 1) setSelectedLoaiPhi(loaiPhiData[0].id);
        if (buildingData.length === 1) setSelectedToaNha(buildingData[0].id);
      } catch (error) {
        message.error("Không thể tải dữ liệu bộ lọc");
      } finally {
        setLoadingFilters(false);
      }
    };
    loadFilters();
  }, [message]);

  // ===== LOAD DATA KHI ĐỦ FILTER =====
  useEffect(() => {
    const loadData = async () => {
      if (!selectedThang || !selectedNam || !selectedLoaiPhi || !selectedToaNha) {
        setChiSoData([]);
        return;
      }
      
      setLoading(true);
      try {
        const data = await dienNuocService.getDanhSachGhiChiSo(
          selectedThang, 
          selectedNam, 
          selectedToaNha,
          selectedLoaiPhi
        );
        
        // Map data với key và tính toán tiêu thụ (không tính tiền)
        const mappedData = data.map((item, index) => ({
          ...item,
          key: item.hoGiaDinhId || index,
          tieuThu: item.chiSoMoi !== null && item.chiSoMoi !== undefined 
            ? Math.max(0, (item.chiSoMoi || 0) - (item.chiSoCu || 0))
            : null,
        }));
        
        setChiSoData(mappedData);
      } catch (error) {
        message.error("Không thể tải dữ liệu: " + (error.response?.data?.message || error.message || "Lỗi không xác định"));
        setChiSoData([]);
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, [selectedThang, selectedNam, selectedLoaiPhi, selectedToaNha, message]);

  // Hàm reload data thủ công (cho nút Làm mới)
  const loadChiSoData = useCallback(async () => {
    if (!selectedThang || !selectedNam || !selectedLoaiPhi || !selectedToaNha) {
      return;
    }
    
    setLoading(true);
    try {
      const data = await dienNuocService.getDanhSachGhiChiSo(
        selectedThang, 
        selectedNam, 
        selectedToaNha,
        selectedLoaiPhi
      );
      
      // Map data với key và tính toán tiêu thụ (không tính tiền)
      const mappedData = data.map((item, index) => ({
        ...item,
        key: item.hoGiaDinhId || index,
        tieuThu: item.chiSoMoi !== null && item.chiSoMoi !== undefined 
          ? Math.max(0, (item.chiSoMoi || 0) - (item.chiSoCu || 0))
          : null,
      }));
      
      setChiSoData(mappedData);
    } catch (error) {
      message.error("Không thể tải dữ liệu: " + (error.response?.data?.message || error.message || "Lỗi không xác định"));
      setChiSoData([]);
    } finally {
      setLoading(false);
    }
  }, [selectedThang, selectedNam, selectedLoaiPhi, selectedToaNha, message]);

  // ===== HELPER FUNCTIONS =====
  const calculateTieuThu = (chiSoCu, chiSoMoi) => {
    if (chiSoMoi === null || chiSoMoi === undefined) return null;
    return Math.max(0, (chiSoMoi || 0) - (chiSoCu || 0));
  };

  // ===== HANDLE CHANGE CHỈ SỐ MỚI =====
  const handleChiSoMoiChange = useCallback((hoGiaDinhId, value) => {
    setChiSoData(prev => 
      prev.map(item => {
        if (item.hoGiaDinhId !== hoGiaDinhId) return item;
        
        const chiSoMoi = value;
        const tieuThu = calculateTieuThu(item.chiSoCu, chiSoMoi);
        
        return { ...item, chiSoMoi, tieuThu };
      })
    );
  }, []);

  // ===== SAVE CHỈ SỐ =====
  const handleSave = useCallback(async () => {
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
        chiSoMoi: item.chiSoMoi,
      }));
      
      const result = await dienNuocService.saveChiSo(
        selectedThang, 
        selectedNam, 
        selectedToaNha,
        selectedLoaiPhi,
        danhSachChiSo
      );
      
      message.success(result.message || `Đã lưu ${dataToSave.length} chỉ số thành công!`);
      
      // QUAN TRỌNG: KHÔNG reload data sau khi save để giữ nguyên state tháng/năm
      // Chỉ hiển thị thông báo thành công, dữ liệu đã được lưu
      // Người dùng có thể nhấn "Làm mới" nếu muốn reload
    } catch (error) {
      message.error("Lưu thất bại: " + (error.response?.data?.message || error.message || "Lỗi không xác định"));
    } finally {
      setSaving(false);
    }
  }, [chiSoData, selectedThang, selectedNam, selectedToaNha, selectedLoaiPhi, message]);

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

  // ===== COLUMNS - Đã bỏ Đơn giá và Thành tiền =====
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
      width: 200,
    },
    {
      title: `Chỉ số T${selectedThang > 1 ? selectedThang - 1 : 12}/${selectedThang > 1 ? selectedNam : selectedNam - 1}`,
      dataIndex: "chiSoCu",
      key: "chiSoCu",
      width: 150,
      align: "right",
      render: (value) => (
        <Text type="secondary">
          {new Intl.NumberFormat("vi-VN").format(value || 0)}
        </Text>
      ),
    },
    {
      title: `Chỉ số T${selectedThang}/${selectedNam}`,
      key: "chiSoMoi",
      width: 180,
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
              width: 140,
              borderColor: isInvalid ? "#ff4d4f" : undefined,
            }}
            status={isInvalid ? "error" : undefined}
            placeholder="Nhập chỉ số..."
          />
        );
      },
    },
    {
      title: "Tiêu thụ",
      key: "tieuThu",
      width: 120,
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
  ];

  // ===== CHECK IF FILTERS COMPLETE =====
  const isFilterComplete = selectedThang && selectedNam && selectedLoaiPhi && selectedToaNha;

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
          {/* Tháng */}
          <Col xs={12} sm={6}>
            <Space direction="vertical" size={4} style={{ width: "100%" }}>
              <Text type="secondary">
                <CalendarOutlined style={{ marginRight: 4 }} />
                Tháng:
              </Text>
              <Select
                placeholder="Chọn tháng"
                value={selectedThang}
                onChange={setSelectedThang}
                style={{ width: "100%" }}
                options={thangOptions}
              />
            </Space>
          </Col>
          {/* Năm */}
          <Col xs={12} sm={6}>
            <Space direction="vertical" size={4} style={{ width: "100%" }}>
              <Text type="secondary">Năm:</Text>
              <Select
                placeholder="Chọn năm"
                value={selectedNam}
                onChange={setSelectedNam}
                style={{ width: "100%" }}
                options={namOptions}
              />
            </Space>
          </Col>
          {/* Loại phí */}
          <Col xs={24} sm={6}>
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
          {/* Tòa nhà */}
          <Col xs={24} sm={6}>
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
          description="Chọn Tháng, Năm, Loại phí và Tòa nhà để xem danh sách căn hộ cần ghi chỉ số."
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
                  title="Tổng tiêu thụ (tạm tính)" 
                  value={totalTieuThu}
                  formatter={(value) => new Intl.NumberFormat("vi-VN").format(value)}
                  valueStyle={{ fontSize: 18, color: "#1890ff" }}
                />
              </Col>
            </Row>
          </Card>

          {/* HƯỚNG DẪN */}
          <Alert
            message="Hướng dẫn ghi chỉ số"
            description={
              <span>
                Nhập <b>Chỉ số mới</b> cho từng căn hộ. Hệ thống sẽ tự động tính tiêu thụ dựa trên chỉ số tháng trước.
                <b> Chỉ số mới phải lớn hơn hoặc bằng Chỉ số cũ.</b>
                <br />
                <Text type="secondary" style={{ marginTop: 8, display: 'block' }}>
                  Lưu ý: Việc tính tiền điện/nước sẽ được thực hiện khi tạo Đợt thu (trong phần Quản lý Đợt thu).
                </Text>
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
                onClick={handleSave}
                loading={saving}
                disabled={enteredCount === 0 || invalidCount > 0}
                size="large"
              >
                Lưu chỉ số ({enteredCount} căn hộ)
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
