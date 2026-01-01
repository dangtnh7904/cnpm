import React, { useEffect, useCallback, useState } from "react";
import { Button, message, Select, Table, Tag, Modal, Descriptions, Spin } from "antd";
import { FilePdfOutlined, MailOutlined, EyeOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { paymentService, householdService, invoiceService, notificationService, feeService } from "../../services";
import { useFetch } from "../../hooks";

const { Option } = Select;

export default function InvoiceManagementPage() {
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = useState(null);
  const [previewModal, setPreviewModal] = useState({ open: false, idHoaDon: null });
  const { data: households, refetch: fetchHouseholds } = useFetch(householdService.getAll, false);
  const { data: hoaDons, loading, refetch } = useFetch(
    () => selectedHoGiaDinh ? paymentService.getHoaDonByHoGiaDinh(selectedHoGiaDinh) : Promise.resolve([]),
    false
  );

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  useEffect(() => {
    if (selectedHoGiaDinh) {
      refetch();
    }
  }, [selectedHoGiaDinh, refetch]);

  const handleDownloadPdf = useCallback(async (idHoaDon) => {
    try {
      await invoiceService.downloadInvoice(idHoaDon);
      message.success("Đã tải hóa đơn PDF");
    } catch (error) {
      message.error("Lỗi tải hóa đơn");
    }
  }, []);

  const handleSendEmail = useCallback(async (idHoaDon) => {
    try {
      await notificationService.sendInvoiceByEmail(idHoaDon);
      message.success("Đã gửi hóa đơn qua email");
    } catch (error) {
      message.error("Lỗi gửi email: " + (error.response?.data?.message || error.message));
    }
  }, []);

  const handlePreview = useCallback(async (idHoaDon) => {
    setPreviewModal({ open: true, idHoaDon });
  }, []);

  const columns = [
    { title: "Mã hóa đơn", render: (record) => `HD${String(record.id).padStart(6, '0')}` },
    { title: "Đợt thu", render: (record) => record?.dotThu?.tenDotThu || "" },
    { 
      title: "Tổng tiền", 
      render: (record) => new Intl.NumberFormat('vi-VN').format(record.tongTienPhaiThu || 0) + " đ"
    },
    { 
      title: "Trạng thái", 
      dataIndex: "trangThai",
      render: (status) => {
        const color = status === "Đã đóng" ? "green" : status === "Đang nợ" ? "orange" : "red";
        return <Tag color={color}>{status}</Tag>;
      }
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record.id)}
          >
            Xem
          </Button>
          <Button
            type="link"
            icon={<FilePdfOutlined />}
            onClick={() => handleDownloadPdf(record.id)}
          >
            PDF
          </Button>
          <Button
            type="link"
            icon={<MailOutlined />}
            onClick={() => handleSendEmail(record.id)}
          >
            Gửi email
          </Button>
        </>
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý hóa đơn"
      extra={
        <Select
          style={{ width: 300 }}
          placeholder="Chọn hộ gia đình"
          onChange={setSelectedHoGiaDinh}
          value={selectedHoGiaDinh}
        >
          {(Array.isArray(households) ? households : []).map((h) => (
            <Option key={h.id} value={h.id}>
              {h.maHoGiaDinh} - {h.tenChuHo}
            </Option>
          ))}
        </Select>
      }
    >
      <Table
        columns={columns}
        dataSource={hoaDons}
        loading={loading}
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />

      <InvoicePreviewModal
        open={previewModal.open}
        idHoaDon={previewModal.idHoaDon}
        onClose={() => setPreviewModal({ open: false, idHoaDon: null })}
      />
    </ContentCard>
  );
}

function InvoicePreviewModal({ open, idHoaDon, onClose }) {
  const [htmlContent, setHtmlContent] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open && idHoaDon) {
      loadInvoice();
    }
  }, [open, idHoaDon]);

  const loadInvoice = async () => {
    setLoading(true);
    try {
      const html = await invoiceService.getInvoiceHtml(idHoaDon);
      setHtmlContent(html);
    } catch (error) {
      message.error("Lỗi tải hóa đơn");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Xem trước hóa đơn"
      open={open}
      onCancel={onClose}
      footer={null}
      width={800}
    >
      {loading ? (
        <div style={{ textAlign: "center", padding: 50 }}>
          <Spin size="large" />
        </div>
      ) : (
        <div dangerouslySetInnerHTML={{ __html: htmlContent }} />
      )}
    </Modal>
  );
}

