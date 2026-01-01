import React, { useCallback } from "react";
import { Button, message, Input, Select, Switch, Modal, Form, Popconfirm, Space, Tag } from "antd";
import { PlusOutlined, CheckCircleOutlined, StopOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { feeService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

export default function LoaiPhiPage() {
  const { data: loaiPhis, loading, refetch } = useFetch(feeService.searchLoaiPhi);
  const modal = useModal({
    tenLoaiPhi: "",
    donGia: 0,
    donViTinh: "",
    loaiThu: "BatBuoc",
    moTa: "",
    dangHoatDong: true,
  });

  const handleSearch = useCallback((value) => {
    refetch({ tenLoaiPhi: value });
  }, [refetch]);


  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      donGia: record.donGia?.toString() || "0",
    });
  }, [modal]);

  /**
   * Toggle trạng thái hoạt động (Soft Delete / Restore)
   * Thay vì xóa cứng, dùng API disable/restore để giữ dữ liệu lịch sử.
   */
  const handleToggleStatus = useCallback(async (record) => {
    try {
      if (record.dangHoatDong) {
        await feeService.disableLoaiPhi(record.id);
        message.success(`Đã ngừng thu loại phí "${record.tenLoaiPhi}"`);
      } else {
        await feeService.restoreLoaiPhi(record.id);
        message.success(`Đã kích hoạt lại loại phí "${record.tenLoaiPhi}"`);
      }
      refetch();
    } catch (error) {
      message.error("Thao tác thất bại: " + (error.message || "Lỗi không xác định"));
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    const payload = {
      ...values,
      donGia: parseFloat(values.donGia) || 0,
    };

    if (editingId) {
      await feeService.updateLoaiPhi(editingId, payload);
    } else {
      await feeService.createLoaiPhi(payload);
    }
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Tên loại phí", dataIndex: "tenLoaiPhi" },
    {
      title: "Đơn giá mặc định",
      dataIndex: "donGia",
      render: (value) => value ? new Intl.NumberFormat('vi-VN').format(value) + " đ" : "0 đ"
    },
    { title: "Đơn vị tính", dataIndex: "donViTinh" },
    { title: "Loại thu", dataIndex: "loaiThu" },
    {
      title: "Trạng thái",
      dataIndex: "dangHoatDong",
      render: (value) => value 
        ? <Tag icon={<CheckCircleOutlined />} color="success">Đang thu</Tag>
        : <Tag icon={<StopOutlined />} color="default">Ngừng thu</Tag>
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            Sửa
          </Button>
          <Popconfirm
            title={record.dangHoatDong 
              ? `Ngừng thu "${record.tenLoaiPhi}"?` 
              : `Kích hoạt lại "${record.tenLoaiPhi}"?`}
            description={record.dangHoatDong 
              ? "Loại phí sẽ không được thu nữa nhưng vẫn giữ dữ liệu lịch sử."
              : "Loại phí sẽ được kích hoạt lại và có thể thu."}
            onConfirm={() => handleToggleStatus(record)}
            okText="Xác nhận"
            cancelText="Hủy"
          >
            <Button 
              type="link" 
              size="small"
              danger={record.dangHoatDong}
              style={{ color: !record.dangHoatDong ? '#52c41a' : undefined }}
            >
              {record.dangHoatDong ? "Ngừng thu" : "Kích hoạt"}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý loại phí"
      extra={
        <div style={{ display: 'flex', gap: 10 }}>
          <Input.Search
            placeholder="Tìm theo tên loại phí..."
            onSearch={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
            Thêm loại phí
          </Button>
        </div>
      }
    >
      <DataTable columns={columns} dataSource={loaiPhis} loading={loading} />

      <LoaiPhiFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}

function LoaiPhiFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, isEditing, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, isEditing ? "Cập nhật thành công" : "Thêm thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title={isEditing ? "Sửa loại phí" : "Thêm loại phí"}
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="tenLoaiPhi"
          label="Tên loại phí"
          rules={[{ required: true, message: "Vui lòng nhập tên loại phí" }]}
        >
          <Input placeholder="Ví dụ: Phí dịch vụ, Phí gửi xe" />
        </Form.Item>

        <Form.Item
          name="donGia"
          label="Đơn giá (VNĐ)"
          rules={[{ required: true, message: "Vui lòng nhập đơn giá" }]}
        >
          <Input type="number" placeholder="0" />
        </Form.Item>

        <Form.Item name="donViTinh" label="Đơn vị tính">
          <Input placeholder="Ví dụ: m², người, hộ, xe" />
        </Form.Item>

        <Form.Item
          name="loaiThu"
          label="Loại thu"
          rules={[{ required: true, message: "Vui lòng chọn loại thu" }]}
        >
          <Select>
            <Option value="BatBuoc">Bắt buộc</Option>
            <Option value="TuNguyen">Tự nguyện</Option>
          </Select>
        </Form.Item>

        <Form.Item name="moTa" label="Mô tả">
          <Input.TextArea rows={3} placeholder="Mô tả về loại phí" />
        </Form.Item>

        <Form.Item name="dangHoatDong" valuePropName="checked" label="Đang hoạt động">
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  );
}

