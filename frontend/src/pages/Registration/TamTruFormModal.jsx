import React from "react";
import { Modal, Form, Input, Select, DatePicker } from "antd";
import { DATE_FORMAT } from "../../constants";

export default function TamTruFormModal({ modal, householdOptions, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật tạm trú" : "Thêm tạm trú"}
      open={open}
      onOk={() => handleSubmit(onSubmit)}
      onCancel={closeModal}
      confirmLoading={loading}
      okText="Lưu"
      cancelText="Hủy"
      width={640}
    >
      <Form layout="vertical" form={form}>
        <Form.Item name="hoTen" label="Họ tên" rules={[{ required: true, message: "Nhập họ tên" }]}>
          <Input placeholder="Nguyễn Văn A" />
        </Form.Item>

        <Form.Item name="soCCCD" label="CCCD" rules={[{ required: true, message: "Nhập CCCD" }]}>
          <Input placeholder="001234567890" />
        </Form.Item>

        <Form.Item name="soDienThoai" label="Số điện thoại" rules={[{ required: true, message: "Nhập số điện thoại" }]}>
          <Input placeholder="0912345678" />
        </Form.Item>

        <Form.Item name="ngayBatDau" label="Ngày bắt đầu" rules={[{ required: true, message: "Chọn ngày" }]}>
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item name="ngayKetThuc" label="Ngày kết thúc" rules={[{ required: true, message: "Chọn ngày" }]}>
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item name="lyDo" label="Lý do" rules={[{ required: true, message: "Nhập lý do" }]}>
          <Input.TextArea rows={2} maxLength={200} placeholder="Mục đích tạm trú" />
        </Form.Item>

        <Form.Item name="idHoGiaDinh" label="Thuộc hộ" rules={[{ required: true, message: "Chọn hộ" }]}>
          <Select options={householdOptions} placeholder="Chọn hộ" showSearch optionFilterProp="label" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
