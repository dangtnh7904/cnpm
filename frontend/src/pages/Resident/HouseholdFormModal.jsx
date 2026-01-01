import React from "react";
import { Modal, Form, Input, InputNumber, Select } from "antd";
import { HOUSEHOLD_STATUS_OPTIONS } from "../../constants";

export default function HouseholdFormModal({ modal, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật hộ gia đình" : "Thêm hộ gia đình"}
      open={open}
      onOk={() => handleSubmit(onSubmit)}
      onCancel={closeModal}
      confirmLoading={loading}
      okText="Lưu"
      cancelText="Hủy"
    >
      <Form layout="vertical" form={form}>
        <Form.Item name="maHoGiaDinh" label="Mã hộ" rules={[{ required: true, message: "Nhập mã hộ" }]}>
          <Input placeholder="HO001" />
        </Form.Item>

        <Form.Item name="tenChuHo" label="Chủ hộ" rules={[{ required: true, message: "Nhập tên chủ hộ" }]}>
          <Input placeholder="Nguyễn Văn A" />
        </Form.Item>

        <Form.Item name="soCanHo" label="Số căn hộ" rules={[{ required: true, message: "Nhập số căn hộ" }]}>
          <Input placeholder="101" />
        </Form.Item>

        <Form.Item name="soTang" label="Tầng" rules={[{ required: true, message: "Nhập tầng" }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item name="dienTich" label="Diện tích (m²)" rules={[{ required: true, message: "Nhập diện tích" }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item name="soDienThoaiLienHe" label="SĐT liên hệ" rules={[{ required: true, message: "Nhập số điện thoại" }]}>
          <Input placeholder="0912345678" />
        </Form.Item>

        <Form.Item name="emailLienHe" label="Email liên hệ" rules={[{ type: "email", message: "Email không hợp lệ" }]}>
          <Input placeholder="contact@example.com" />
        </Form.Item>

        <Form.Item name="trangThai" label="Trạng thái">
          <Select options={HOUSEHOLD_STATUS_OPTIONS} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
