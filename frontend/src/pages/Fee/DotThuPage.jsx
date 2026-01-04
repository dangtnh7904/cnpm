import React, { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { 
  Button, 
  Modal, 
  Form, 
  Input, 
  DatePicker, 
  Select, 
  Space, 
  Tag,
  Popconfirm,
  App 
} from "antd";
import { 
  PlusOutlined, 
  CalendarOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined
} from "@ant-design/icons";
import dayjs from "dayjs";
import { ContentCard, DataTable } from "../../components";
import { dotThuService, buildingService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

/**
 * Trang Quản lý Đợt Thu.
 * 
 * CHỨC NĂNG:
 * - Xem danh sách đợt thu (theo tòa nhà)
 * - Tạo đợt thu mới (bắt buộc chọn tòa nhà)
 * - Sửa đợt thu
 * - Xóa đợt thu
 * - Xem chi tiết đợt thu
 */
export default function DotThuPage() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const { data: dotThus, loading, refetch } = useFetch(dotThuService.getAllForDropdown);
  const { data: buildings } = useFetch(buildingService.getAllForDropdown);
  
  const modal = useModal({
    tenDotThu: "",
    loaiDotThu: "PhiSinhHoat",
    toaNhaId: null,
    thang: dayjs().subtract(1, 'month').month() + 1, // Tháng trước (1-12)
    nam: dayjs().subtract(1, 'month').year(),
    ngayBatDau: null,
    ngayKetThuc: null,
  });

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      toaNhaId: record.toaNha?.id || null,
      thang: record.thang || dayjs().subtract(1, 'month').month() + 1,
      nam: record.nam || dayjs().subtract(1, 'month').year(),
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
      thang: values.thang,
      nam: values.nam,
      ngayBatDau: values.ngayBatDau?.format("YYYY-MM-DD"),
      ngayKetThuc: values.ngayKetThuc?.format("YYYY-MM-DD"),
      toaNha: { id: values.toaNhaId },
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
      render: (text, record) => (
        <Button 
          type="link" 
          style={{ padding: 0, fontWeight: 600 }}
          onClick={() => navigate(`/fee/dot-thu/${record.id}`)}
        >
          {text}
        </Button>
      ),
    },
    {
      title: "Tòa nhà",
      dataIndex: ["toaNha", "tenToaNha"],
      key: "toaNha",
      width: 120,
      render: (value) => value || <span style={{ color: "#999" }}>Chưa gán</span>,
    },
    {
      title: "Loại",
      dataIndex: "loaiDotThu",
      key: "loaiDotThu",
      width: 130,
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
      title: "Kỳ thu",
      key: "kyThu",
      width: 100,
      render: (_, record) => {
        if (record.thang && record.nam) {
          return `${String(record.thang).padStart(2, '0')}/${record.nam}`;
        }
        return <span style={{ color: "#999" }}>-</span>;
      },
    },
    {
      title: "Ngày bắt đầu",
      dataIndex: "ngayBatDau",
      key: "ngayBatDau",
      width: 110,
      render: (value) => value ? dayjs(value).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Ngày kết thúc",
      dataIndex: "ngayKetThuc",
      key: "ngayKetThuc",
      width: 110,
      render: (value) => value ? dayjs(value).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Trạng thái",
      key: "status",
      width: 110,
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
      title: "Thao tác",
      key: "actions",
      width: 130,
      render: (_, record) => (
        <Space>
          <Button 
            type="text" 
            size="small" 
            icon={<EyeOutlined />}
            onClick={() => navigate(`/fee/dot-thu/${record.id}`)}
            title="Xem chi tiết"
          />
          <Button 
            type="text" 
            size="small" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            title="Sửa"
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

      <DotThuFormModal modal={modal} onSubmit={handleSubmit} buildings={buildings} />
    </ContentCard>
  );
}

/**
 * Modal Form tạo/sửa Đợt Thu.
 */
function DotThuFormModal({ modal, onSubmit, buildings = [] }) {
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

  // Gợi ý tên đợt thu theo tháng/năm được chọn
  const suggestName = () => {
    const thang = form.getFieldValue("thang");
    const nam = form.getFieldValue("nam");
    if (thang && nam) {
      return `Tháng ${String(thang).padStart(2, '0')}/${nam}`;
    }
    const now = dayjs().subtract(1, 'month');
    return `Tháng ${now.format("MM/YYYY")}`;
  };

  // Khi thay đổi tháng hoặc năm thì auto update gợi ý tên
  const handleMonthYearChange = () => {
    const thang = form.getFieldValue("thang");
    const nam = form.getFieldValue("nam");
    if (thang && nam) {
      form.setFieldValue("tenDotThu", `Tháng ${String(thang).padStart(2, '0')}/${nam}`);
    }
  };

  // Danh sách tháng (1-12)
  const months = Array.from({ length: 12 }, (_, i) => i + 1);
  
  // Danh sách năm (năm hiện tại - 1 đến năm hiện tại + 1)
  const currentYear = dayjs().year();
  const years = [currentYear - 1, currentYear, currentYear + 1];

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
      destroyOnClose
    >
      <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
        <Form.Item
          name="toaNhaId"
          label="Tòa nhà"
          rules={[{ required: true, message: "Vui lòng chọn tòa nhà" }]}
          tooltip="Mỗi đợt thu thuộc về một tòa nhà cụ thể"
        >
          <Select 
            placeholder="Chọn tòa nhà" 
            disabled={isEditing}
            showSearch
            optionFilterProp="children"
          >
            {buildings.map((b) => (
              <Option key={b.id} value={b.id}>{b.tenToaNha}</Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item label="Kỳ thu phí (Tháng/Năm)" required>
          <Space>
            <Form.Item
              name="thang"
              noStyle
              rules={[{ required: true, message: "Chọn tháng" }]}
            >
              <Select 
                style={{ width: 100 }} 
                placeholder="Tháng"
                onChange={handleMonthYearChange}
              >
                {months.map((m) => (
                  <Option key={m} value={m}>Tháng {String(m).padStart(2, '0')}</Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              name="nam"
              noStyle
              rules={[{ required: true, message: "Chọn năm" }]}
            >
              <Select 
                style={{ width: 100 }} 
                placeholder="Năm"
                onChange={handleMonthYearChange}
              >
                {years.map((y) => (
                  <Option key={y} value={y}>{y}</Option>
                ))}
              </Select>
            </Form.Item>
          </Space>
        </Form.Item>

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
