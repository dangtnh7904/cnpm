import React from "react";
import { Modal, Form, Input, InputNumber, Select } from "antd";
import { HOUSEHOLD_STATUS_OPTIONS } from "../../constants";

export default function HouseholdFormModal({ modal, onSubmit, buildingOptions = [] }) {
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
        <Form.Item 
          name="idToaNha" 
          label="Tòa nhà" 
          rules={[{ required: true, message: "Vui lòng chọn tòa nhà" }]}
        >
          <Select 
            options={Array.isArray(buildingOptions) ? buildingOptions : []} 
            placeholder="Chọn tòa nhà (bắt buộc)" 
            showSearch 
            optionFilterProp="label"
          />
        </Form.Item>

        <Form.Item name="maHoGiaDinh" label="Mã hộ" rules={[{ required: true, message: "Nhập mã hộ" }]}>
          <Input placeholder="HO001" />
        </Form.Item>

        {/* Tên chủ hộ sẽ tự động cập nhật khi thêm nhân khẩu có QuanHeVoiChuHo = "Chủ hộ" */}

        <Form.Item name="soCanHo" label="Số căn hộ" rules={[{ required: true, message: "Nhập số căn hộ" }]}>
          <Input placeholder="101" />
        </Form.Item>

        <Form.Item name="soTang" label="Tầng" rules={[{ required: true, message: "Nhập tầng" }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item name="dienTich" label="Diện tích (m²)" rules={[{ required: true, message: "Nhập diện tích" }]}>
          <InputNumber style={{ width: "100%" }} min={0} />
        </Form.Item>

        <Form.Item name="soDienThoaiLienHe" label="SĐT liên hệ">
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
