import React, { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { Result, Button, Card, Descriptions, Spin, Tag } from "antd";
import { 
  CheckCircleOutlined, 
  CloseCircleOutlined, 
  ExclamationCircleOutlined,
  HomeOutlined,
  ReloadOutlined,
  FileTextOutlined
} from "@ant-design/icons";
import { ContentCard } from "../../components";
import { paymentService } from "../../services";

/**
 * Trang hiển thị kết quả thanh toán VNPAY.
 * 
 * Query params:
 * - status: 'success' | 'failed' | 'error'
 * - hoaDonId: ID hóa đơn (nếu có)
 * - message: Thông báo từ server
 * - code: Mã lỗi VNPAY (nếu failed)
 */
export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [hoaDon, setHoaDon] = useState(null);
  const [loading, setLoading] = useState(false);

  const status = searchParams.get("status");
  const hoaDonId = searchParams.get("hoaDonId");
  const message = searchParams.get("message") || "";
  const responseCode = searchParams.get("code");

  // Fetch thông tin hóa đơn sau khi thanh toán thành công
  useEffect(() => {
    if (status === "success" && hoaDonId) {
      setLoading(true);
      paymentService.getHoaDonById(hoaDonId)
        .then(data => setHoaDon(data))
        .catch(err => console.error("Error fetching invoice:", err))
        .finally(() => setLoading(false));
    }
  }, [status, hoaDonId]);

  // Render nội dung theo status
  const renderContent = () => {
    switch (status) {
      case "success":
        return (
          <Result
            icon={<CheckCircleOutlined style={{ color: "#52c41a" }} />}
            status="success"
            title="Thanh toán thành công!"
            subTitle={decodeURIComponent(message)}
            extra={[
              <Button 
                type="primary" 
                key="home" 
                icon={<HomeOutlined />}
                onClick={() => navigate("/resident")}
              >
                Về trang chủ
              </Button>,
              hoaDonId && (
                <Button 
                  key="detail" 
                  icon={<FileTextOutlined />}
                  onClick={() => navigate(`/resident/invoices`)}
                >
                  Xem hóa đơn
                </Button>
              ),
            ]}
          >
            {loading ? (
              <Spin tip="Đang tải thông tin hóa đơn..." />
            ) : hoaDon && (
              <Card style={{ maxWidth: 500, margin: "0 auto", marginTop: 24 }}>
                <Descriptions title="Chi tiết hóa đơn" column={1} bordered size="small">
                  <Descriptions.Item label="Mã hóa đơn">
                    HD{String(hoaDon.id).padStart(6, "0")}
                  </Descriptions.Item>
                  <Descriptions.Item label="Đợt thu">
                    {hoaDon.dotThu?.tenDotThu}
                  </Descriptions.Item>
                  <Descriptions.Item label="Tổng phải thu">
                    {new Intl.NumberFormat("vi-VN").format(hoaDon.tongTienPhaiThu || 0)} đ
                  </Descriptions.Item>
                  <Descriptions.Item label="Đã thanh toán">
                    {new Intl.NumberFormat("vi-VN").format(hoaDon.soTienDaDong || 0)} đ
                  </Descriptions.Item>
                  <Descriptions.Item label="Trạng thái">
                    <Tag color={hoaDon.trangThai === "Đã thanh toán" ? "green" : "orange"}>
                      {hoaDon.trangThai}
                    </Tag>
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            )}
          </Result>
        );

      case "failed":
        return (
          <Result
            icon={<CloseCircleOutlined style={{ color: "#ff4d4f" }} />}
            status="error"
            title="Giao dịch thất bại"
            subTitle={getFailureMessage(responseCode, message)}
            extra={[
              <Button 
                type="primary" 
                key="retry" 
                icon={<ReloadOutlined />}
                onClick={() => navigate("/resident/online-payment")}
              >
                Thử lại
              </Button>,
              <Button 
                key="home" 
                icon={<HomeOutlined />}
                onClick={() => navigate("/resident")}
              >
                Về trang chủ
              </Button>,
            ]}
          >
            {responseCode && (
              <Card style={{ maxWidth: 400, margin: "0 auto", marginTop: 24, textAlign: "left" }}>
                <p><strong>Mã lỗi:</strong> {responseCode}</p>
                <p><strong>Chi tiết:</strong> {decodeURIComponent(message)}</p>
                <p style={{ marginTop: 16, color: "#8c8c8c" }}>
                  Nếu bạn đã bị trừ tiền nhưng giao dịch thất bại, vui lòng liên hệ ban quản lý chung cư để được hỗ trợ.
                </p>
              </Card>
            )}
          </Result>
        );

      case "error":
        return (
          <Result
            icon={<ExclamationCircleOutlined style={{ color: "#faad14" }} />}
            status="warning"
            title="Có lỗi xảy ra"
            subTitle={decodeURIComponent(message) || "Không thể xử lý kết quả thanh toán. Vui lòng kiểm tra lại hóa đơn."}
            extra={[
              <Button 
                type="primary" 
                key="home" 
                icon={<HomeOutlined />}
                onClick={() => navigate("/resident")}
              >
                Về trang chủ
              </Button>,
            ]}
          />
        );

      default:
        return (
          <Result
            status="info"
            title="Không có thông tin thanh toán"
            subTitle="Không tìm thấy thông tin về giao dịch thanh toán."
            extra={[
              <Button 
                type="primary" 
                key="home" 
                icon={<HomeOutlined />}
                onClick={() => navigate("/resident")}
              >
                Về trang chủ
              </Button>,
            ]}
          />
        );
    }
  };

  return (
    <ContentCard title="Kết quả thanh toán">
      {renderContent()}
    </ContentCard>
  );
}

/**
 * Lấy thông báo lỗi thân thiện dựa trên mã lỗi VNPAY.
 */
function getFailureMessage(responseCode, serverMessage) {
  const errorMessages = {
    "07": "Giao dịch bị nghi ngờ gian lận. Vui lòng liên hệ ngân hàng.",
    "09": "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking.",
    "10": "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.",
    "11": "Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch.",
    "12": "Thẻ/Tài khoản của bạn bị khóa.",
    "13": "Bạn nhập sai mật khẩu xác thực giao dịch (OTP).",
    "24": "Bạn đã hủy giao dịch thanh toán.",
    "51": "Tài khoản của bạn không đủ số dư để thực hiện giao dịch.",
    "65": "Tài khoản của bạn đã vượt quá hạn mức giao dịch trong ngày.",
    "75": "Ngân hàng thanh toán đang bảo trì. Vui lòng thử lại sau.",
    "79": "Bạn nhập sai mật khẩu thanh toán quá số lần quy định.",
    "99": "Có lỗi xảy ra. Vui lòng thử lại sau.",
    "CHECKSUM_FAILED": "Chữ ký bảo mật không hợp lệ. Giao dịch bị từ chối vì lý do bảo mật.",
  };

  if (responseCode && errorMessages[responseCode]) {
    return errorMessages[responseCode];
  }

  if (serverMessage) {
    return decodeURIComponent(serverMessage);
  }

  return "Giao dịch không thành công. Vui lòng thử lại sau.";
}
