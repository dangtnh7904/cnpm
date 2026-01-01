import React, { useEffect, useCallback } from "react";
import { Button, message, Input, Select, Modal, Form, InputNumber, Table, Tag } from "antd";
import { PlusOutlined, DollarOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { paymentService, householdService, feeService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

export default function PaymentUpdatePage() {
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = React.useState(null);
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

  const paymentModal = useModal({
    idHoaDon: undefined,
    soTien: 0,
    hinhThuc: "Tiền mặt",
    nguoiNop: "",
    ghiChu: "",
  });

  const handleAddPayment = useCallback((hoaDon) => {
    paymentModal.openModal({
      idHoaDon: hoaDon.id,
      nguoiNop: hoaDon.hoGiaDinh?.tenChuHo || "",
    });
  }, [paymentModal]);

  const handleSubmitPayment = useCallback(async (values, editingId) => {
    try {
      await paymentService.addPayment(values.idHoaDon, {
        soTien: values.soTien,
        hinhThuc: values.hinhThuc,
        nguoiNop: values.nguoiNop,
        ghiChu: values.ghiChu,
      });
      message.success("Cập nhật thanh toán thành công");
      refetch();
      paymentModal.closeModal();
    } catch (error) {
      message.error("Cập nhật thất bại");
    }
  }, [refetch, paymentModal]);

  const columns = [
    { title: "Mã hóa đơn", render: (record) => `HD${String(record.id).padStart(6, '0')}` },
    { title: "Đợt thu", render: (record) => record?.dotThu?.tenDotThu || "" },
    { 
      title: "Tổng phải thu", 
      render: (record) => new Intl.NumberFormat('vi-VN').format(record.tongTienPhaiThu || 0) + " đ"
    },
    { 
      title: "Đã đóng", 
      render: (record) => new Intl.NumberFormat('vi-VN').format(record.soTienDaDong || 0) + " đ"
    },
    { 
      title: "Còn nợ", 
      render: (record) => {
        const conNo = (record.tongTienPhaiThu || 0) - (record.soTienDaDong || 0);
        return <span style={{ color: conNo > 0 ? '#ff4d4f' : '#52c41a' }}>
          {new Intl.NumberFormat('vi-VN').format(conNo)} đ
        </span>;
      }
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
      render: (_, record) => {
        const conNo = (record.tongTienPhaiThu || 0) - (record.soTienDaDong || 0);
        return (
          <Button 
            type="primary" 
            icon={<DollarOutlined />}
            onClick={() => handleAddPayment(record)}
            disabled={conNo <= 0}
          >
            Thanh toán
          </Button>
        );
      },
    },
  ];

  return (
    <ContentCard
      title="Cập nhật thanh toán"
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
      
      <PaymentFormModal modal={paymentModal} onSubmit={handleSubmitPayment} />
    </ContentCard>
  );
}

function PaymentFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, loading } = modal;

  const onFinish = async () => {
    await handleSubmit(onSubmit, "Cập nhật thanh toán thành công");
  };

  return (
    <Modal
      title="Cập nhật thanh toán"
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="soTien"
          label="Số tiền (VNĐ)"
          rules={[{ required: true, message: "Vui lòng nhập số tiền" }]}
        >
          <InputNumber min={0} style={{ width: "100%" }} formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
        </Form.Item>

        <Form.Item
          name="hinhThuc"
          label="Hình thức thanh toán"
          rules={[{ required: true, message: "Vui lòng chọn hình thức" }]}
        >
          <Select>
            <Option value="Tiền mặt">Tiền mặt</Option>
            <Option value="Chuyển khoản">Chuyển khoản</Option>
            <Option value="VNPay">VNPay</Option>
            <Option value="Momo">Momo</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="nguoiNop"
          label="Người nộp"
          rules={[{ required: true, message: "Vui lòng nhập tên người nộp" }]}
        >
          <Input placeholder="Tên người nộp tiền" />
        </Form.Item>

        <Form.Item name="ghiChu" label="Ghi chú">
          <Input.TextArea rows={3} placeholder="Ghi chú về thanh toán" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

