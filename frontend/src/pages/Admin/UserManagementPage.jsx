import React, { useCallback, useState } from "react";
import { Button, App, Input, Select, Modal, Form, Table, Tag, Space, Popconfirm, Tooltip } from "antd";
import { SearchOutlined, EditOutlined, DeleteOutlined, KeyOutlined, MailOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { authService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

export default function UserManagementPage() {
  const { message } = App.useApp();
  const [searchText, setSearchText] = useState("");

  const { data: users, loading, refetch } = useFetch(
    authService.getAllUsers,
    true
  );

  const modal = useModal({
    username: "",
    fullName: "",
    email: "",
    role: "ACCOUNTANT",
  });

  // Lọc users theo search text
  const filteredUsers = users?.filter(user => {
    const search = searchText.toLowerCase();
    return (
      user.username?.toLowerCase().includes(search) ||
      user.fullName?.toLowerCase().includes(search) ||
      user.email?.toLowerCase().includes(search) ||
      user.role?.toLowerCase().includes(search)
    );
  }) || [];

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await authService.deleteUser(id);
      message.success("Đã xóa tài khoản");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại: " + (error.response?.data?.message || "Lỗi không xác định"));
    }
  }, [refetch, message]);

  const handleResetPassword = useCallback(async (record) => {
    try {
      await authService.resetPassword(record.id);
      message.success(`Đã gửi mật khẩu mới về email ${record.email}`);
    } catch (error) {
      message.error("Reset mật khẩu thất bại: " + (error.response?.data?.message || "Lỗi không xác định"));
    }
  }, [message]);

  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      if (editingId) {
        await authService.updateUser(editingId, {
          fullName: values.fullName,
          email: values.email,
          role: values.role
        });
        message.success("Cập nhật tài khoản thành công");
      }
      refetch();
      modal.closeModal();
    } catch (error) {
      message.error("Thao tác thất bại: " + (error.response?.data?.message || "Lỗi không xác định"));
    }
  }, [refetch, modal, message]);

  const columns = [
    {
      title: "Username",
      dataIndex: "username",
      sorter: (a, b) => a.username?.localeCompare(b.username),
    },
    {
      title: "Họ tên",
      dataIndex: "fullName",
      sorter: (a, b) => a.fullName?.localeCompare(b.fullName),
    },
    {
      title: "Email",
      dataIndex: "email"
    },
    {
      title: "Vai trò",
      dataIndex: "role",
      filters: [
        { text: "Admin", value: "ADMIN" },
        { text: "Manager", value: "MANAGER" },
        { text: "Accountant", value: "ACCOUNTANT" },
        { text: "Resident", value: "RESIDENT" },
      ],
      onFilter: (value, record) => record.role === value,
      render: (role) => {
        const colorMap = {
          ADMIN: "red",
          MANAGER: "purple",
          ACCOUNTANT: "blue",
          RESIDENT: "green",
        };
        return <Tag color={colorMap[role] || "default"}>{role}</Tag>;
      }
    },
    {
      title: "Thao tác",
      width: 180,
      render: (_, record) => (
        <Space>
          <Tooltip title="Sửa thông tin">
            <Button
              type="link"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>

          <Popconfirm
            title="Reset mật khẩu?"
            description={`Gửi mật khẩu mới về ${record.email}`}
            onConfirm={() => handleResetPassword(record)}
            okText="Reset"
            cancelText="Hủy"
          >
            <Tooltip title="Reset mật khẩu">
              <Button type="link" icon={<KeyOutlined />} />
            </Tooltip>
          </Popconfirm>

          <Popconfirm
            title="Xóa tài khoản này?"
            description="Thao tác này không thể hoàn tác"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
          >
            <Tooltip title="Xóa tài khoản">
              <Button type="link" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý tài khoản"
      extra={
        <Input
          placeholder="Tìm kiếm theo username, họ tên, email..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
          allowClear
        />
      }
    >
      <Table
        columns={columns}
        dataSource={filteredUsers}
        loading={loading}
        rowKey="id"
        pagination={{ pageSize: 10, showSizeChanger: true }}
      />

      <UserFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}

function UserFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, isEditing, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, "Cập nhật thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title="Sửa thông tin tài khoản"
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={500}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="username"
          label="Username"
        >
          <Input disabled />
        </Form.Item>

        <Form.Item
          name="fullName"
          label="Họ tên"
          rules={[{ required: true, message: "Vui lòng nhập họ tên" }]}
        >
          <Input placeholder="Họ và tên" />
        </Form.Item>

        <Form.Item
          name="email"
          label="Email"
          rules={[
            { required: true, message: "Vui lòng nhập email" },
            { type: "email", message: "Email không hợp lệ" }
          ]}
        >
          <Input placeholder="email@example.com" />
        </Form.Item>

        <Form.Item
          name="role"
          label="Vai trò"
          rules={[{ required: true, message: "Vui lòng chọn vai trò" }]}
        >
          <Select>
            <Option value="ADMIN">Quản trị viên</Option>
            <Option value="MANAGER">Quản lý tòa nhà</Option>
            <Option value="ACCOUNTANT">Kế toán</Option>
            <Option value="RESIDENT">Cư dân</Option>
          </Select>
        </Form.Item>

        <div style={{ color: "#888", fontSize: 12, marginTop: 8 }}>
          <MailOutlined /> Để reset mật khẩu, sử dụng nút "Reset mật khẩu" trong danh sách
        </div>
      </Form>
    </Modal>
  );
}
