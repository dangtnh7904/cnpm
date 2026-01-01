import React, { useState, useEffect, useCallback } from "react";
import { 
  Drawer, 
  Table, 
  Button, 
  Space, 
  Tag, 
  message, 
  Modal, 
  Form, 
  Input, 
  Select, 
  DatePicker,
  Row,
  Col,
  Divider,
  Empty,
  Popconfirm
} from "antd";
import { PlusOutlined, StopOutlined } from "@ant-design/icons";
import { tamTruService } from "../../services";
import { DATE_FORMAT, GENDER_OPTIONS, TAM_TRU_RELATIONSHIP_OPTIONS } from "../../constants";
import dayjs from "dayjs";

/**
 * Drawer quản lý Tạm trú theo hộ gia đình.
 * Props:
 * - open: boolean
 * - onClose: () => void
 * - hoGiaDinhId: number
 * - hoGiaDinhInfo: object { maHoGiaDinh, tenChuHo }
 */
export default function TamTruDrawer({ open, onClose, hoGiaDinhId, hoGiaDinhInfo }) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [form] = Form.useForm();

  // Fetch danh sách tạm trú theo hộ
  const fetchRecords = useCallback(async () => {
    if (!hoGiaDinhId) return;
    try {
      setLoading(true);
      const data = await tamTruService.getByHoGiaDinh(hoGiaDinhId);
      setRecords(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error fetching tam tru:", error);
      message.error("Không thể tải danh sách tạm trú");
    } finally {
      setLoading(false);
    }
  }, [hoGiaDinhId]);

  useEffect(() => {
    if (open && hoGiaDinhId) {
      fetchRecords();
    }
  }, [open, hoGiaDinhId, fetchRecords]);

  // Đăng ký tạm trú
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      const payload = {
        hoGiaDinhId: hoGiaDinhId,
        hoTen: values.hoTen,
        soCCCD: values.soCCCD,
        ngaySinh: values.ngaySinh?.format(DATE_FORMAT),
        gioiTinh: values.gioiTinh,
        soDienThoai: values.soDienThoai || null,
        email: values.email || null,
        quanHeVoiChuHo: values.quanHeVoiChuHo,
        diaChiThuongTru: values.diaChiThuongTru,
        maGiayTamTru: values.maGiayTamTru || null,
        ngayBatDau: values.ngayBatDau?.format(DATE_FORMAT),
        ngayKetThuc: values.ngayKetThuc?.format(DATE_FORMAT) || null,
        lyDo: values.lyDo,
      };

      await tamTruService.create(payload);
      message.success("Đăng ký tạm trú thành công");
      form.resetFields();
      setFormOpen(false);
      fetchRecords();
    } catch (error) {
      const errorData = error.response?.data;
      if (errorData?.errors && Array.isArray(errorData.errors)) {
        errorData.errors.forEach(err => message.error(err));
      } else if (errorData?.message) {
        message.error(errorData.message);
      } else if (!error.errorFields) {
        message.error("Có lỗi xảy ra");
      }
    }
  };

  // Hủy tạm trú
  const handleHuyTamTru = async (id) => {
    try {
      await tamTruService.huyTamTru(id);
      message.success("Đã hủy tạm trú");
      fetchRecords();
    } catch (error) {
      message.error(error.response?.data?.message || "Hủy tạm trú thất bại");
    }
  };

  const columns = [
    {
      title: "Họ tên",
      key: "hoTen",
      render: (_, record) => record?.nhanKhau?.hoTen || "-",
    },
    {
      title: "CCCD",
      key: "soCCCD",
      render: (_, record) => record?.nhanKhau?.soCCCD || "-",
    },
    {
      title: "Quan hệ",
      key: "quanHe",
      render: (_, record) => record?.nhanKhau?.quanHeVoiChuHo || "-",
    },
    {
      title: "Từ ngày",
      dataIndex: "ngayBatDau",
      render: (v) => v ? dayjs(v).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Đến ngày",
      dataIndex: "ngayKetThuc",
      render: (v) => v ? dayjs(v).format("DD/MM/YYYY") : "Chưa xác định",
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Popconfirm
          title="Hủy tạm trú?"
          description="Người này sẽ được đánh dấu là đã rời đi"
          onConfirm={() => handleHuyTamTru(record.id)}
          okText="Hủy tạm trú"
          cancelText="Đóng"
        >
          <Button size="small" danger icon={<StopOutlined />}>
            Hủy
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <>
      <Drawer
        title={`Quản lý tạm trú - ${hoGiaDinhInfo?.maHoGiaDinh || ''}`}
        placement="right"
        width={800}
        onClose={onClose}
        open={open}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setFormOpen(true)}>
            Đăng ký tạm trú
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          locale={{
            emptyText: <Empty description="Chưa có người tạm trú" />,
          }}
        />
      </Drawer>

      {/* Modal đăng ký tạm trú */}
      <Modal
        title="Đăng ký tạm trú"
        open={formOpen}
        onOk={handleSubmit}
        onCancel={() => { form.resetFields(); setFormOpen(false); }}
        width={720}
        okText="Đăng ký"
        cancelText="Hủy"
      >
        <Form layout="vertical" form={form}>
          <Divider orientation="left" plain>Thông tin cá nhân</Divider>
          
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="hoTen" label="Họ tên" rules={[{ required: true, message: "Nhập họ tên" }]}>
                <Input placeholder="Nguyễn Văn A" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item 
                name="soCCCD" 
                label="Số CCCD" 
                rules={[
                  { required: true, message: "Nhập CCCD" },
                  { pattern: /^[0-9]{12}$/, message: "CCCD phải có đúng 12 chữ số" }
                ]}
              >
                <Input placeholder="001234567890" maxLength={12} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="ngaySinh" label="Ngày sinh" rules={[{ required: true, message: "Chọn ngày" }]}>
                <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="gioiTinh" label="Giới tính" rules={[{ required: true, message: "Chọn" }]}>
                <Select options={GENDER_OPTIONS} placeholder="Chọn" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="soDienThoai" label="Số điện thoại">
                <Input placeholder="0912345678" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="email" label="Email">
                <Input placeholder="email@example.com" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="quanHeVoiChuHo" label="Quan hệ với chủ hộ" rules={[{ required: true }]}>
                <Select options={TAM_TRU_RELATIONSHIP_OPTIONS} placeholder="Chọn quan hệ" />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left" plain>Thông tin tạm trú</Divider>

          <Form.Item name="diaChiThuongTru" label="Địa chỉ thường trú" rules={[{ required: true }]}>
            <Input.TextArea rows={2} placeholder="Địa chỉ đăng ký hộ khẩu gốc" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="ngayBatDau" label="Ngày bắt đầu" rules={[{ required: true }]}>
                <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="ngayKetThuc" label="Ngày kết thúc (dự kiến)">
                <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="lyDo" label="Lý do tạm trú" rules={[{ required: true }]}>
            <Input.TextArea rows={2} placeholder="Thuê nhà, làm việc, học tập..." />
          </Form.Item>

          <Form.Item name="maGiayTamTru" label="Mã giấy tạm trú (nếu có)">
            <Input placeholder="Mã do cơ quan cấp" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
