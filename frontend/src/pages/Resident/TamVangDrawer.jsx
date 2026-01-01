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
  Empty,
  Popconfirm,
  Alert
} from "antd";
import { PlusOutlined, CheckCircleOutlined } from "@ant-design/icons";
import { tamVangService } from "../../services";
import { DATE_FORMAT } from "../../constants";
import dayjs from "dayjs";

/**
 * Drawer quản lý Tạm vắng theo hộ gia đình.
 * Props:
 * - open: boolean
 * - onClose: () => void
 * - hoGiaDinhId: number
 * - hoGiaDinhInfo: object { maHoGiaDinh, tenChuHo }
 * - residents: array - danh sách nhân khẩu của hộ (để chọn đăng ký tạm vắng)
 * - onResidentsUpdate: () => void - callback khi cần refresh danh sách nhân khẩu
 */
export default function TamVangDrawer({ 
  open, 
  onClose, 
  hoGiaDinhId, 
  hoGiaDinhInfo,
  residents = [],
  onResidentsUpdate
}) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [form] = Form.useForm();

  // Lọc nhân khẩu "Đang ở" để có thể đăng ký tạm vắng
  const eligibleResidents = residents.filter(r => 
    r.trangThai === "Đang ở" || 
    r.trangThai === "Hoat dong" || 
    r.trangThai === "Thường trú" ||
    r.trangThai === "Hoạt động"
  );

  // Fetch danh sách tạm vắng theo hộ
  const fetchRecords = useCallback(async () => {
    if (!hoGiaDinhId) return;
    try {
      setLoading(true);
      const data = await tamVangService.getByHoGiaDinh(hoGiaDinhId);
      setRecords(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error fetching tam vang:", error);
      message.error("Không thể tải danh sách tạm vắng");
    } finally {
      setLoading(false);
    }
  }, [hoGiaDinhId]);

  useEffect(() => {
    if (open && hoGiaDinhId) {
      fetchRecords();
    }
  }, [open, hoGiaDinhId, fetchRecords]);

  // Đăng ký tạm vắng
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      const payload = {
        nhanKhauId: values.nhanKhauId,
        ngayDi: values.ngayDi?.format(DATE_FORMAT),
        ngayVe: values.ngayVe?.format(DATE_FORMAT) || null,
        lyDo: values.lyDo,
        noiDen: values.noiDen,
        ghiChu: values.ghiChu || null,
      };

      await tamVangService.create(payload);
      message.success("Đăng ký tạm vắng thành công");
      form.resetFields();
      setFormOpen(false);
      fetchRecords();
      // Refresh danh sách nhân khẩu vì trạng thái đã thay đổi
      if (onResidentsUpdate) onResidentsUpdate();
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

  // Kết thúc tạm vắng
  const handleKetThuc = async (id) => {
    try {
      await tamVangService.ketThucTamVang(id);
      message.success("Đã kết thúc tạm vắng - người này đã quay về");
      fetchRecords();
      // Refresh danh sách nhân khẩu vì trạng thái đã thay đổi
      if (onResidentsUpdate) onResidentsUpdate();
    } catch (error) {
      message.error(error.response?.data?.message || "Kết thúc tạm vắng thất bại");
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
      title: "Ngày đi",
      dataIndex: "ngayDi",
      render: (v) => v ? dayjs(v).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Ngày về (dự kiến)",
      dataIndex: "ngayVe",
      render: (v) => v ? dayjs(v).format("DD/MM/YYYY") : "Chưa xác định",
    },
    {
      title: "Nơi đến",
      dataIndex: "noiDen",
      ellipsis: true,
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Popconfirm
          title="Kết thúc tạm vắng?"
          description="Đánh dấu người này đã quay về"
          onConfirm={() => handleKetThuc(record.id)}
          okText="Xác nhận"
          cancelText="Đóng"
        >
          <Button size="small" type="primary" ghost icon={<CheckCircleOutlined />}>
            Đã về
          </Button>
        </Popconfirm>
      ),
    },
  ];

  // Options cho select nhân khẩu
  const residentOptions = eligibleResidents.map(r => ({
    label: `${r.hoTen} - ${r.soCCCD || 'Chưa có CCCD'}`,
    value: r.id,
  }));

  return (
    <>
      <Drawer
        title={`Quản lý tạm vắng - ${hoGiaDinhInfo?.maHoGiaDinh || ''}`}
        placement="right"
        width={850}
        onClose={onClose}
        open={open}
        extra={
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => setFormOpen(true)}
            disabled={eligibleResidents.length === 0}
          >
            Đăng ký tạm vắng
          </Button>
        }
      >
        {eligibleResidents.length === 0 && (
          <Alert
            type="info"
            message="Không có nhân khẩu nào có thể đăng ký tạm vắng"
            description="Tất cả nhân khẩu đang ở trạng thái không được phép (Tạm vắng, Tạm trú, Đã chuyển đi...)"
            style={{ marginBottom: 16 }}
          />
        )}
        
        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          locale={{
            emptyText: <Empty description="Chưa có ai đang tạm vắng" />,
          }}
        />
      </Drawer>

      {/* Modal đăng ký tạm vắng */}
      <Modal
        title="Đăng ký tạm vắng"
        open={formOpen}
        onOk={handleSubmit}
        onCancel={() => { form.resetFields(); setFormOpen(false); }}
        width={600}
        okText="Đăng ký"
        cancelText="Hủy"
      >
        <Form layout="vertical" form={form}>
          <Alert
            type="info"
            message="Tạm vắng là khi nhân khẩu đang cư trú tại hộ đi vắng tạm thời"
            style={{ marginBottom: 16 }}
          />

          <Form.Item 
            name="nhanKhauId" 
            label="Chọn nhân khẩu" 
            rules={[{ required: true, message: "Vui lòng chọn nhân khẩu" }]}
          >
            <Select 
              options={residentOptions} 
              placeholder="Chọn nhân khẩu đang ở"
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="ngayDi" label="Ngày đi" rules={[{ required: true }]}>
                <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="ngayVe" label="Ngày về (dự kiến)">
                <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="noiDen" label="Nơi đến" rules={[{ required: true }]}>
            <Input placeholder="Địa chỉ nơi đến tạm vắng" />
          </Form.Item>

          <Form.Item name="lyDo" label="Lý do tạm vắng" rules={[{ required: true }]}>
            <Input.TextArea rows={2} placeholder="Công tác, du lịch, thăm thân..." />
          </Form.Item>

          <Form.Item name="ghiChu" label="Ghi chú">
            <Input.TextArea rows={2} placeholder="Ghi chú thêm (nếu có)" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
