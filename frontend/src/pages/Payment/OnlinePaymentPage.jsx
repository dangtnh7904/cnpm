import React, { useState, useCallback, useEffect, useMemo } from "react";
import { 
  Button, App, Select, Card, Tag, Descriptions, Spin, Empty, Alert, Space, Divider 
} from "antd";
import { 
  CreditCardOutlined, 
  CheckCircleOutlined, 
  InfoCircleOutlined,
  CalendarOutlined,
  HomeOutlined,
  BankOutlined
} from "@ant-design/icons";
import { ContentCard } from "../../components";
import { paymentService, householdService, dotThuService, buildingService } from "../../services";

const { Option } = Select;

/**
 * Trang Thanh Toán Online - UX cải tiến.
 * 
 * Luồng:
 * 1. Chọn Tòa nhà
 * 2. Chọn Hộ gia đình (lọc theo tòa nhà)
 * 3. Chọn Đợt thu
 * 4. Hệ thống tự động tìm hóa đơn của hộ đó trong đợt đó
 * 5. Hiển thị chi tiết và nút thanh toán (nếu còn nợ)
 */
export default function OnlinePaymentPage() {
  const { message } = App.useApp();
  
  // State
  const [buildings, setBuildings] = useState([]);
  const [households, setHouseholds] = useState([]);
  const [dotThus, setDotThus] = useState([]);
  const [selectedToaNha, setSelectedToaNha] = useState(null);
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = useState(null);
  const [selectedDotThu, setSelectedDotThu] = useState(null);
  const [hoaDon, setHoaDon] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingHoaDon, setLoadingHoaDon] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);

  // Load danh sách tòa nhà
  useEffect(() => {
    buildingService.getAllForDropdown()
      .then(data => setBuildings(Array.isArray(data) ? data : []))
      .catch(err => {
        console.error("Error loading buildings:", err);
      });
  }, []);

  // Load danh sách hộ gia đình
  useEffect(() => {
    householdService.getAll()
      .then(data => setHouseholds(Array.isArray(data) ? data : []))
      .catch(err => {
        console.error("Error loading households:", err);
        message.error("Không thể tải danh sách hộ gia đình");
      });
  }, [message]);

  // Lọc hộ gia đình theo tòa nhà đã chọn
  const filteredHouseholds = useMemo(() => {
    if (!selectedToaNha) return [];
    return households.filter(h => 
      (h.toaNha?.id === selectedToaNha) || (h.idToaNha === selectedToaNha)
    );
  }, [households, selectedToaNha]);

  // Load danh sách đợt thu khi chọn tòa nhà
  useEffect(() => {
    if (selectedToaNha) {
      setLoading(true);
      dotThuService.getAllForDropdown()
        .then(data => {
          // Lọc đợt thu theo tòa nhà
          const filtered = data.filter(dt => dt.toaNha?.id === selectedToaNha);
          setDotThus(filtered);
        })
        .catch(err => {
          console.error("Error loading dot thu:", err);
          message.error("Không thể tải danh sách đợt thu");
        })
        .finally(() => setLoading(false));
    } else {
      setDotThus([]);
    }
  }, [selectedToaNha, message]);

  // Load hóa đơn khi chọn đợt thu
  useEffect(() => {
    if (selectedHoGiaDinh && selectedDotThu) {
      setLoadingHoaDon(true);
      setHoaDon(null);
      
      paymentService.getHoaDonByDotThu(selectedHoGiaDinh, selectedDotThu)
        .then(data => setHoaDon(data))
        .catch(err => {
          console.error("Error loading hoa don:", err);
          setHoaDon(null);
        })
        .finally(() => setLoadingHoaDon(false));
    } else {
      setHoaDon(null);
    }
  }, [selectedHoGiaDinh, selectedDotThu]);

  // Xử lý thanh toán
  const handlePayment = useCallback(async () => {
    if (!hoaDon) {
      message.warning("Không có hóa đơn để thanh toán");
      return;
    }

    const conNo = (hoaDon.tongTienPhaiThu || 0) - (hoaDon.soTienDaDong || 0);
    if (conNo <= 0) {
      message.warning("Hóa đơn đã được thanh toán đầy đủ");
      return;
    }

    setPaymentLoading(true);
    try {
      const paymentUrl = await paymentService.createVnPayUrl(hoaDon.id, conNo);
      // Redirect sang VNPAY
      window.location.href = paymentUrl;
    } catch (error) {
      message.error("Lỗi tạo link thanh toán: " + (error.response?.data?.message || error.message));
      setPaymentLoading(false);
    }
  }, [hoaDon, message]);

  // Tính số tiền còn nợ
  const conNo = hoaDon 
    ? (hoaDon.tongTienPhaiThu || 0) - (hoaDon.soTienDaDong || 0)
    : 0;

  // Lấy thông tin hộ gia đình đã chọn
  const selectedHousehold = households.find(h => h.id === selectedHoGiaDinh);
  const selectedDotThuData = dotThus.find(dt => dt.id === selectedDotThu);
  const selectedBuilding = buildings.find(b => b.id === selectedToaNha);

  return (
    <ContentCard 
      title={
        <Space>
          <CreditCardOutlined />
          Thanh toán trực tuyến
        </Space>
      }
    >
      {/* Bước 1: Chọn Tòa nhà */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: "100%" }}>
          <div>
            <BankOutlined style={{ marginRight: 8 }} />
            <strong>Bước 1:</strong> Chọn tòa nhà
          </div>
          <Select
            style={{ width: "100%", maxWidth: 400 }}
            placeholder="Chọn tòa nhà..."
            showSearch
            optionFilterProp="children"
            value={selectedToaNha}
            onChange={(value) => {
              setSelectedToaNha(value);
              setSelectedHoGiaDinh(null);
              setSelectedDotThu(null);
              setHoaDon(null);
            }}
          >
            {buildings.map((b) => (
              <Option key={b.id} value={b.id}>
                {b.tenToaNha}
              </Option>
            ))}
          </Select>
        </Space>
      </Card>

      {/* Bước 2: Chọn Hộ gia đình */}
      {selectedToaNha && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Space direction="vertical" style={{ width: "100%" }}>
            <div>
              <HomeOutlined style={{ marginRight: 8 }} />
              <strong>Bước 2:</strong> Chọn hộ gia đình
            </div>
            <Select
              style={{ width: "100%", maxWidth: 400 }}
              placeholder="Tìm và chọn hộ gia đình..."
              showSearch
              optionFilterProp="children"
              value={selectedHoGiaDinh}
              onChange={(value) => {
                setSelectedHoGiaDinh(value);
                setSelectedDotThu(null);
                setHoaDon(null);
              }}
              filterOption={(input, option) =>
                option?.children?.toLowerCase().includes(input.toLowerCase())
              }
              notFoundContent={filteredHouseholds.length === 0 ? "Không có hộ gia đình trong tòa nhà này" : null}
            >
              {filteredHouseholds.map((h) => (
                <Option key={h.id} value={h.id}>
                  {h.maHoGiaDinh} - {h.tenChuHo || "Chưa có chủ hộ"} (Phòng {h.soCanHo || "N/A"})
                </Option>
              ))}
            </Select>
          </Space>
        </Card>
      )}

      {/* Bước 3: Chọn Đợt thu */}
      {selectedHoGiaDinh && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Space direction="vertical" style={{ width: "100%" }}>
            <div>
              <CalendarOutlined style={{ marginRight: 8 }} />
              <strong>Bước 3:</strong> Chọn đợt thu phí
            </div>
            <Select
              style={{ width: "100%", maxWidth: 400 }}
              placeholder="Chọn đợt thu..."
              loading={loading}
              value={selectedDotThu}
              onChange={setSelectedDotThu}
              notFoundContent={loading ? <Spin size="small" /> : "Không có đợt thu nào"}
            >
              {dotThus.map((dt) => (
                <Option key={dt.id} value={dt.id}>
                  {dt.tenDotThu} 
                  {dt.trangThai && (
                    <Tag 
                      color={dt.trangThai === "Đang diễn ra" ? "green" : "default"} 
                      style={{ marginLeft: 8 }}
                    >
                      {dt.trangThai}
                    </Tag>
                  )}
                </Option>
              ))}
            </Select>
          </Space>
        </Card>
      )}

      {/* Hiển thị kết quả */}
      {selectedHoGiaDinh && selectedDotThu && (
        <Card>
          {loadingHoaDon ? (
            <div style={{ textAlign: "center", padding: 40 }}>
              <Spin tip="Đang tải thông tin hóa đơn..." />
            </div>
          ) : hoaDon ? (
            <>
              {/* Có hóa đơn */}
              <Descriptions 
                title={
                  <Space>
                    <InfoCircleOutlined />
                    Thông tin hóa đơn
                  </Space>
                }
                bordered 
                column={{ xs: 1, sm: 2 }}
              >
                <Descriptions.Item label="Mã hóa đơn">
                  <strong>HD{String(hoaDon.id).padStart(6, "0")}</strong>
                </Descriptions.Item>
                <Descriptions.Item label="Đợt thu">
                  {selectedDotThuData?.tenDotThu}
                </Descriptions.Item>
                <Descriptions.Item label="Hộ gia đình">
                  {selectedHousehold?.maHoGiaDinh} - {selectedHousehold?.tenChuHo}
                </Descriptions.Item>
                <Descriptions.Item label="Tòa nhà">
                  {selectedHousehold?.toaNha?.tenToaNha}
                </Descriptions.Item>
                <Descriptions.Item label="Tổng phải thu">
                  <strong style={{ fontSize: 16 }}>
                    {new Intl.NumberFormat("vi-VN").format(hoaDon.tongTienPhaiThu || 0)} đ
                  </strong>
                </Descriptions.Item>
                <Descriptions.Item label="Đã thanh toán">
                  <span style={{ color: "#52c41a" }}>
                    {new Intl.NumberFormat("vi-VN").format(hoaDon.soTienDaDong || 0)} đ
                  </span>
                </Descriptions.Item>
                <Descriptions.Item label="Còn nợ" span={2}>
                  <Tag color={conNo > 0 ? "red" : "green"} style={{ fontSize: 16, padding: "4px 12px" }}>
                    {new Intl.NumberFormat("vi-VN").format(conNo)} đ
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Trạng thái" span={2}>
                  <Tag color={
                    hoaDon.trangThai === "Đã thanh toán" ? "green" : 
                    hoaDon.trangThai === "Đang đóng" ? "orange" : "red"
                  }>
                    {hoaDon.trangThai}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>

              <Divider />

              {/* Nút thanh toán hoặc thông báo đã hoàn thành */}
              {conNo > 0 ? (
                <div style={{ textAlign: "center" }}>
                  <Button
                    type="primary"
                    size="large"
                    icon={<CreditCardOutlined />}
                    onClick={handlePayment}
                    loading={paymentLoading}
                    style={{ height: 50, fontSize: 16, paddingInline: 40 }}
                  >
                    Thanh toán {new Intl.NumberFormat("vi-VN").format(conNo)} đ qua VNPAY
                  </Button>
                  <div style={{ marginTop: 16, color: "#8c8c8c" }}>
                    <p>Bạn sẽ được chuyển đến trang thanh toán VNPAY</p>
                    <p>Hỗ trợ: ATM nội địa, Visa, MasterCard, QR Code</p>
                  </div>
                </div>
              ) : (
                <Alert
                  type="success"
                  showIcon
                  icon={<CheckCircleOutlined />}
                  message="Hóa đơn đã được thanh toán đầy đủ"
                  description="Cảm ơn bạn đã hoàn thành nghĩa vụ tài chính!"
                  style={{ textAlign: "center" }}
                />
              )}
            </>
          ) : (
            /* Chưa có hóa đơn */
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description={
                <span>
                  Chưa có hóa đơn cho đợt thu <strong>{selectedDotThuData?.tenDotThu}</strong>
                  <br />
                  <span style={{ color: "#8c8c8c" }}>
                    Hóa đơn sẽ được tạo khi Ban quản lý tính phí cho đợt thu này
                  </span>
                </span>
              }
            />
          )}
        </Card>
      )}

      {/* Hướng dẫn ban đầu */}
      {!selectedToaNha && (
        <Alert
          type="info"
          showIcon
          message="Hướng dẫn thanh toán"
          description={
            <ol style={{ marginBottom: 0, paddingLeft: 20 }}>
              <li>Chọn tòa nhà</li>
              <li>Chọn hộ gia đình của bạn</li>
              <li>Chọn đợt thu phí cần thanh toán</li>
              <li>Xem chi tiết hóa đơn và bấm "Thanh toán qua VNPAY"</li>
              <li>Hoàn tất thanh toán trên trang VNPAY</li>
            </ol>
          }
        />
      )}
    </ContentCard>
  );
}

