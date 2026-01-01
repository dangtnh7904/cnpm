import React from "react";
import { Modal, Form, Input, Select, DatePicker } from "antd";
import { GENDER_OPTIONS, RELATIONSHIP_OPTIONS, RESIDENT_STATUS_OPTIONS, DATE_FORMAT } from "../../constants";

export default function ResidentFormModal({ modal, householdOptions, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật nhân khẩu" : "Thêm nhân khẩu"}
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
          <Input placeholder="Nguyễn Văn B" />
        </Form.Item>

        <Form.Item
          name="soCCCD"
          label="CCCD"
          rules={[
            { required: true, message: "Nhập CCCD" },
            { len: 12, message: "CCCD phải có 12 số" },
          ]}
        >
          <Input placeholder="001234567890" />
        </Form.Item>

        <Form.Item name="ngaySinh" label="Ngày sinh" rules={[{ required: true, message: "Chọn ngày sinh" }]}>
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item name="gioiTinh" label="Giới tính">
          <Select options={GENDER_OPTIONS} />
        </Form.Item>

        <Form.Item name="quanHeVoiChuHo" label="Quan hệ với chủ hộ" rules={[{ required: true, message: "Chọn quan hệ" }]}>
          <Select options={RELATIONSHIP_OPTIONS} />
        </Form.Item>

        <Form.Item name="trangThai" label="Trạng thái">
          <Select options={RESIDENT_STATUS_OPTIONS} />
        </Form.Item>

        <Form.Item name="soDienThoai" label="Số điện thoại">
          <Input placeholder="0912345678" />
        </Form.Item>

        <Form.Item name="email" label="Email" rules={[{ type: "email", message: "Email không hợp lệ" }]}>
          <Input placeholder="email@example.com" />
        </Form.Item>

        <Form.Item name="idHoGiaDinh" label="Thuộc hộ" rules={[{ required: true, message: "Chọn hộ" }]}>
          <Select 
            options={Array.isArray(householdOptions) ? householdOptions : []} 
            placeholder="Chọn hộ" 
            showSearch 
            optionFilterProp="label" 
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
