import React from "react";
import { Modal, Form, Input } from "antd";

const { TextArea } = Input;

export default function BuildingFormModal({ modal, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật tòa nhà" : "Thêm tòa nhà"}
      open={open}
      onOk={() => handleSubmit(onSubmit)}
      onCancel={closeModal}
      confirmLoading={loading}
      okText="Lưu"
      cancelText="Hủy"
      width={500}
    >
      <Form layout="vertical" form={form}>
        <Form.Item 
          name="tenToaNha" 
          label="Tên tòa nhà" 
          rules={[{ required: true, message: "Nhập tên tòa nhà" }]}
        >
          <Input placeholder="Ví dụ: Tòa A, CT1, S1..." />
        </Form.Item>

        <Form.Item 
          name="moTa" 
          label="Mô tả"
        >
          <TextArea 
            rows={3} 
            placeholder="Mô tả về tòa nhà (không bắt buộc)" 
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
