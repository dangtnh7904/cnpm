import React from "react";
import { Modal, Form, Input, Select, DatePicker } from "antd";
import { DATE_FORMAT } from "../../constants";

export default function TamVangFormModal({ modal, residentOptions, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật tạm vắng" : "Thêm tạm vắng"}
      open={open}
      onOk={() => handleSubmit(onSubmit)}
      onCancel={closeModal}
      confirmLoading={loading}
      okText="Lưu"
      cancelText="Hủy"
      width={640}
    >
      <Form layout="vertical" form={form}>
        <Form.Item name="idNhanKhau" label="Nhân khẩu" rules={[{ required: true, message: "Chọn nhân khẩu" }]}>
          <Select options={residentOptions} showSearch placeholder="Chọn nhân khẩu" optionFilterProp="label" />
        </Form.Item>

        <Form.Item name="noiDen" label="Nơi đến" rules={[{ required: true, message: "Nhập nơi đến" }]}>
          <Input placeholder="Địa chỉ" />
        </Form.Item>

        <Form.Item name="lyDo" label="Lý do" rules={[{ required: true, message: "Nhập lý do" }]}>
          <Input.TextArea rows={2} maxLength={200} placeholder="Lý do tạm vắng" />
        </Form.Item>

        <Form.Item name="ngayBatDau" label="Ngày bắt đầu" rules={[{ required: true, message: "Chọn ngày" }]}>
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item name="ngayKetThuc" label="Ngày kết thúc" rules={[{ required: true, message: "Chọn ngày" }]}>
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
