import React, { useCallback } from "react";
import { 
  Button, 
  message, 
  Modal, 
  Form, 
  Input, 
  DatePicker, 
  Select, 
  Space, 
  Tag,
  Popconfirm 
} from "antd";
import { 
  PlusOutlined, 
  CalendarOutlined,
  EditOutlined,
  DeleteOutlined 
} from "@ant-design/icons";
import dayjs from "dayjs";
import { ContentCard, DataTable } from "../../components";
import { dotThuService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * Trang Quản lý Đợt Thu.
 * 
 * CHỨC NĂNG:
 * - Xem danh sách đợt thu
 * - Tạo đợt thu mới
 * - Sửa đợt thu
 * - Xóa đợt thu
 */
export default function DotThuPage() {
  const { data: dotThus, loading, refetch } = useFetch(dotThuService.getAllForDropdown);
  
  const modal = useModal({
    tenDotThu: "",
    loaiDotThu: "PhiSinhHoat",
    ngayBatDau: null,
    ngayKetThuc: null,
  });

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      ngayBatDau: record.ngayBatDau ? dayjs(record.ngayBatDau) : null,
      ngayKetThuc: record.ngayKetThuc ? dayjs(record.ngayKetThuc) : null,
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await dotThuService.remove(id);
      message.success("Đã xóa đợt thu");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại: " + (error.message || "Lỗi không xác định"));
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    const payload = {
      tenDotThu: values.tenDotThu,
      loaiDotThu: values.loaiDotThu,
      ngayBatDau: values.ngayBatDau?.format("YYYY-MM-DD"),
      ngayKetThuc: values.ngayKetThuc?.format("YYYY-MM-DD"),
    };

    if (editingId) {
      await dotThuService.update(editingId, payload);
    } else {
      await dotThuService.create(payload);
    }
    refetch();
  }, [refetch]);

  const columns = [
    {
      title: "Tên đợt thu",
      dataIndex: "tenDotThu",
      key: "tenDotThu",
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: "Loại",
      dataIndex: "loaiDotThu",
      key: "loaiDotThu",
      width: 150,
      render: (value) => {
        const config = {
          PhiSinhHoat: { color: "blue", text: "Phí sinh hoạt" },
          DongGop: { color: "green", text: "Đóng góp" },
        };
        const { color, text } = config[value] || { color: "default", text: value };
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: "Ngày bắt đầu",
      dataIndex: "ngayBatDau",
      key: "ngayBatDau",
      width: 130,
      render: (value) => value ? dayjs(value).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Ngày kết thúc",
      dataIndex: "ngayKetThuc",
      key: "ngayKetThuc",
      width: 130,
      render: (value) => value ? dayjs(value).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Trạng thái",
      key: "status",
      width: 120,
      render: (_, record) => {
        const now = dayjs();
        const start = record.ngayBatDau ? dayjs(record.ngayBatDau) : null;
        const end = record.ngayKetThuc ? dayjs(record.ngayKetThuc) : null;
        
        if (!start || !end) return <Tag>Chưa xác định</Tag>;
        
        if (now.isBefore(start)) {
          return <Tag color="default">Chưa bắt đầu</Tag>;
        }
        if (now.isAfter(end)) {
          return <Tag color="red">Đã kết thúc</Tag>;
        }
        return <Tag color="green">Đang diễn ra</Tag>;
      },
    },
    {
      title: "Ngày tạo",
      dataIndex: "ngayTao",
      key: "ngayTao",
      width: 150,
      render: (value) => value ? dayjs(value).format("DD/MM/YYYY HH:mm") : "-",
    },
    {
      title: "Thao tác",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Space>
          <Button 
            type="text" 
            size="small" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="Xóa đợt thu này?"
            description="Thao tác này không thể hoàn tác. Các hóa đơn liên quan cũng sẽ bị ảnh hưởng."
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
          >
            <Button 
              type="text" 
              size="small" 
              danger
              icon={<DeleteOutlined />}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <ContentCard
      title={
        <Space>
          <CalendarOutlined />
          <span>Quản lý Đợt Thu</span>
        </Space>
      }
      extra={
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => modal.openModal()}
        >
          Tạo đợt thu mới
        </Button>
      }
    >
      <DataTable 
        columns={columns} 
        dataSource={dotThus} 
        loading={loading}
        rowKey="id"
      />

      <DotThuFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}

/**
 * Modal Form tạo/sửa Đợt Thu.
 */
function DotThuFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, isEditing, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(
      onSubmit, 
      isEditing ? "Cập nhật đợt thu thành công" : "Tạo đợt thu thành công"
    );
    if (success) {
      closeModal();
    }
  };

  // Gợi ý tên đợt thu theo tháng hiện tại
  const suggestName = () => {
    const now = dayjs();
    return `Tháng ${now.format("MM/YYYY")}`;
  };

  return (
    <Modal
      title={isEditing ? "Sửa đợt thu" : "Tạo đợt thu mới"}
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={500}
      okText={isEditing ? "Cập nhật" : "Tạo đợt thu"}
      cancelText="Hủy"
    >
      <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
        <Form.Item
          name="tenDotThu"
          label="Tên đợt thu"
          rules={[
            { required: true, message: "Vui lòng nhập tên đợt thu" },
            { max: 100, message: "Tên đợt thu không vượt quá 100 ký tự" },
          ]}
        >
          <Input 
            placeholder={`VD: ${suggestName()}`}
            suffix={
              <Button 
                type="link" 
                size="small"
                onClick={() => form.setFieldValue("tenDotThu", suggestName())}
                style={{ padding: 0 }}
              >
                Gợi ý
              </Button>
            }
          />
        </Form.Item>

        <Form.Item
          name="loaiDotThu"
          label="Loại đợt thu"
          rules={[{ required: true, message: "Vui lòng chọn loại đợt thu" }]}
          initialValue="PhiSinhHoat"
        >
          <Select placeholder="Chọn loại đợt thu">
            <Option value="PhiSinhHoat">Phí sinh hoạt</Option>
            <Option value="DongGop">Đóng góp</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="ngayBatDau"
          label="Ngày bắt đầu"
          rules={[{ required: true, message: "Vui lòng chọn ngày bắt đầu" }]}
        >
          <DatePicker 
            style={{ width: "100%" }} 
            format="DD/MM/YYYY"
            placeholder="Chọn ngày bắt đầu"
          />
        </Form.Item>

        <Form.Item
          name="ngayKetThuc"
          label="Ngày kết thúc"
          rules={[
            { required: true, message: "Vui lòng chọn ngày kết thúc" },
            ({ getFieldValue }) => ({
              validator(_, value) {
                const start = getFieldValue("ngayBatDau");
                if (!value || !start || value.isAfter(start)) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error("Ngày kết thúc phải sau ngày bắt đầu"));
              },
            }),
          ]}
        >
          <DatePicker 
            style={{ width: "100%" }} 
            format="DD/MM/YYYY"
            placeholder="Chọn ngày kết thúc"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
