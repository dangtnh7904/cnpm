import React, { useState, useCallback } from "react";
import { Button, message, Select, Card, Tag, Descriptions, Spin } from "antd";
import { DollarOutlined, CreditCardOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { paymentService, householdService } from "../../services";
import { useFetch } from "../../hooks";

const { Option } = Select;

export default function OnlinePaymentPage() {
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = useState(null);
  const [selectedHoaDon, setSelectedHoaDon] = useState(null);
  const [loading, setLoading] = useState(false);
  const { data: households, refetch: fetchHouseholds } = useFetch(householdService.getAll, false);
  const { data: hoaDons, refetch: fetchHoaDons } = useFetch(
    () => selectedHoGiaDinh ? paymentService.getHoaDonByHoGiaDinh(selectedHoGiaDinh) : Promise.resolve([]),
    false
  );

  React.useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  React.useEffect(() => {
    if (selectedHoGiaDinh) {
      fetchHoaDons();
    }
  }, [selectedHoGiaDinh, fetchHoaDons]);

  const handlePayment = useCallback(async () => {
    if (!selectedHoaDon) {
      message.warning("Vui lòng chọn hóa đơn cần thanh toán");
      return;
    }

    const hoaDon = hoaDons?.find(hd => hd.id === selectedHoaDon);
    const conNo = (hoaDon?.tongTienPhaiThu || 0) - (hoaDon?.soTienDaDong || 0);
    
    if (conNo <= 0) {
      message.warning("Hóa đơn đã được thanh toán đủ");
      return;
    }

    setLoading(true);
    try {
      const paymentUrl = await paymentService.createVnPayUrl(selectedHoaDon);
      // Mở cửa sổ thanh toán
      window.open(paymentUrl, '_blank');
      message.success("Đang chuyển đến trang thanh toán VNPay");
    } catch (error) {
      message.error("Lỗi tạo link thanh toán: " + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  }, [selectedHoaDon, hoaDons]);

  const selectedHoaDonData = hoaDons?.find(hd => hd.id === selectedHoaDon);
  const conNo = selectedHoaDonData 
    ? (selectedHoaDonData.tongTienPhaiThu || 0) - (selectedHoaDonData.soTienDaDong || 0)
    : 0;

  return (
    <ContentCard title="Thanh toán trực tuyến">
      <div style={{ marginBottom: 24 }}>
        <Select
          style={{ width: 300, marginBottom: 16 }}
          placeholder="Chọn hộ gia đình"
          onChange={(value) => {
            setSelectedHoGiaDinh(value);
            setSelectedHoaDon(null);
          }}
          value={selectedHoGiaDinh}
        >
          {(Array.isArray(households) ? households : []).map((h) => (
            <Option key={h.id} value={h.id}>
              {h.maHoGiaDinh} - {h.tenChuHo}
            </Option>
          ))}
        </Select>
      </div>

      {selectedHoGiaDinh && (
        <div style={{ marginBottom: 24 }}>
          <Select
            style={{ width: 400 }}
            placeholder="Chọn hóa đơn cần thanh toán"
            onChange={setSelectedHoaDon}
            value={selectedHoaDon}
          >
            {hoaDons
              ?.filter(hd => {
                const no = (hd.tongTienPhaiThu || 0) - (hd.soTienDaDong || 0);
                return no > 0;
              })
              ?.map((hd) => (
                <Option key={hd.id} value={hd.id}>
                  HD{String(hd.id).padStart(6, '0')} - {hd.dotThu?.tenDotThu} - 
                  Còn nợ: {new Intl.NumberFormat('vi-VN').format(
                    (hd.tongTienPhaiThu || 0) - (hd.soTienDaDong || 0)
                  )} đ
                </Option>
              ))}
          </Select>
        </div>
      )}

      {selectedHoaDonData && (
        <Card>
          <Descriptions title="Thông tin hóa đơn" bordered column={2}>
            <Descriptions.Item label="Mã hóa đơn">
              HD{String(selectedHoaDonData.id).padStart(6, '0')}
            </Descriptions.Item>
            <Descriptions.Item label="Đợt thu">
              {selectedHoaDonData.dotThu?.tenDotThu}
            </Descriptions.Item>
            <Descriptions.Item label="Hộ gia đình">
              {selectedHoaDonData.hoGiaDinh?.maHoGiaDinh} - {selectedHoaDonData.hoGiaDinh?.tenChuHo}
            </Descriptions.Item>
            <Descriptions.Item label="Tổng phải thu">
              {new Intl.NumberFormat('vi-VN').format(selectedHoaDonData.tongTienPhaiThu || 0)} đ
            </Descriptions.Item>
            <Descriptions.Item label="Đã đóng">
              {new Intl.NumberFormat('vi-VN').format(selectedHoaDonData.soTienDaDong || 0)} đ
            </Descriptions.Item>
            <Descriptions.Item label="Còn nợ">
              <Tag color={conNo > 0 ? "red" : "green"}>
                {new Intl.NumberFormat('vi-VN').format(conNo)} đ
              </Tag>
            </Descriptions.Item>
          </Descriptions>

          <div style={{ marginTop: 24, textAlign: "center" }}>
            <Button
              type="primary"
              size="large"
              icon={<CreditCardOutlined />}
              onClick={handlePayment}
              loading={loading}
              disabled={conNo <= 0}
            >
              Thanh toán qua VNPay
            </Button>
          </div>

          <div style={{ marginTop: 16, textAlign: "center", color: "#8c8c8c" }}>
            <p>Bạn sẽ được chuyển đến trang thanh toán VNPay</p>
            <p>Sau khi thanh toán thành công, hệ thống sẽ tự động cập nhật trạng thái</p>
          </div>
        </Card>
      )}
    </ContentCard>
  );
}

