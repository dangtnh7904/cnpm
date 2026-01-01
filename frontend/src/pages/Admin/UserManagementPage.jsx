import React, { useCallback } from "react";
import { Button, message, Input, Select, Modal, Form, Table, Tag, Switch } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons } from "../../components";
import { authService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

export default function UserManagementPage() {
  // Note: Cần tạo API để lấy danh sách user
  const { data: users, loading, refetch } = useFetch(
    authService.getAllUsers,
    false
  );

  const modal = useModal({
    username: "",
    password: "",
    fullName: "",
    email: "",
    role: "ACCOUNTANT",
  });

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      password: "", // Không hiển thị password
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
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      if (editingId) {
        await authService.updateUser(editingId, {
          username: values.username, // Username thường không đổi, nhưng API có thể yêu cầu body
          password: values.password,
          fullName: values.fullName,
          email: values.email,
          role: values.role
        });
        message.success("Cập nhật tài khoản thành công");
      } else {
        await authService.signup(values.username, values.password, values.fullName, values.email, values.role);
        message.success("Tạo tài khoản thành công");
      }
      refetch();
      modal.closeModal();
    } catch (error) {
      message.error("Thao tác thất bại");
    }
  }, [refetch, modal]);

  const columns = [
    { title: "Username", dataIndex: "username" },
    { title: "Họ tên", dataIndex: "fullName" },
    { title: "Email", dataIndex: "email" },
    {
      title: "Vai trò",
      dataIndex: "role",
      render: (role) => {
        const color = role === "ADMIN" ? "red" : "blue";
        return <Tag color={color}>{role}</Tag>;
      }
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa tài khoản này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý tài khoản"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
          Thêm tài khoản
        </Button>
      }
    >
      <Table columns={columns} dataSource={users} loading={loading} rowKey="id" />

      <UserFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}

function UserFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, isEditing, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, isEditing ? "Cập nhật thành công" : "Tạo thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title={isEditing ? "Sửa tài khoản" : "Thêm tài khoản"}
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="username"
          label="Username"
          rules={[{ required: true, message: "Vui lòng nhập username" }]}
        >
          <Input disabled={isEditing} placeholder="Tên đăng nhập" />
        </Form.Item>

        <Form.Item
          name="password"
          label="Mật khẩu"
          rules={[{ required: !isEditing, message: "Vui lòng nhập mật khẩu" }]}
        >
          <Input.Password placeholder={isEditing ? "Để trống nếu không đổi" : "Mật khẩu"} />
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
            <Option value="ACCOUNTANT">Kế toán</Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
}

