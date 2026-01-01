import React from "react";
import { Modal, Form, Input, Select, DatePicker, Row, Col, Divider } from "antd";
import { DATE_FORMAT, GENDER_OPTIONS, TAM_TRU_RELATIONSHIP_OPTIONS } from "../../constants";

export default function TamTruFormModal({ modal, householdOptions, onSubmit }) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;

  return (
    <Modal
      title={isEditing ? "Cập nhật tạm trú" : "Đăng ký tạm trú"}
      open={open}
      onOk={() => handleSubmit(onSubmit)}
      onCancel={closeModal}
      confirmLoading={loading}
      okText="Lưu"
      cancelText="Hủy"
      width={720}
    >
      <Form layout="vertical" form={form}>
        <Divider orientation="left" plain>Thông tin cá nhân</Divider>
        
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="hoTen" label="Họ tên" rules={[{ required: true, message: "Nhập họ tên" }]}>
              <Input placeholder="Nguyễn Văn A" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item 
              name="soCCCD" 
              label="Số CCCD" 
              rules={[
                { required: true, message: "Nhập CCCD" },
                { pattern: /^[0-9]{12}$/, message: "CCCD phải có đúng 12 chữ số" }
              ]}
            >
              <Input placeholder="001234567890" maxLength={12} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item name="ngaySinh" label="Ngày sinh" rules={[{ required: true, message: "Chọn ngày sinh" }]}>
              <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} placeholder="Chọn ngày" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="gioiTinh" label="Giới tính" rules={[{ required: true, message: "Chọn giới tính" }]}>
              <Select options={GENDER_OPTIONS} placeholder="Chọn giới tính" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item name="soDienThoai" label="Số điện thoại">
              <Input placeholder="0912345678" />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="email" label="Email" rules={[{ type: "email", message: "Email không hợp lệ" }]}>
              <Input placeholder="email@example.com" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item 
              name="quanHeVoiChuHo" 
              label="Quan hệ với chủ hộ" 
              rules={[{ required: true, message: "Chọn quan hệ" }]}
            >
              <Select options={TAM_TRU_RELATIONSHIP_OPTIONS} placeholder="Chọn quan hệ" />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left" plain>Thông tin tạm trú</Divider>

        <Form.Item 
          name="hoGiaDinhId" 
          label="Tạm trú tại hộ" 
          rules={[{ required: true, message: "Chọn hộ gia đình" }]}
        >
          <Select options={householdOptions} placeholder="Chọn hộ gia đình" showSearch optionFilterProp="label" />
        </Form.Item>

        <Form.Item 
          name="diaChiThuongTru" 
          label="Địa chỉ thường trú" 
          rules={[{ required: true, message: "Nhập địa chỉ thường trú" }]}
        >
          <Input.TextArea rows={2} placeholder="Địa chỉ đăng ký hộ khẩu gốc" maxLength={200} />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="ngayBatDau" label="Ngày bắt đầu" rules={[{ required: true, message: "Chọn ngày" }]}>
              <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} placeholder="Chọn ngày" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="ngayKetThuc" label="Ngày kết thúc (dự kiến)">
              <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} placeholder="Để trống nếu chưa xác định" />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item 
          name="lyDo" 
          label="Lý do tạm trú" 
          rules={[{ required: true, message: "Nhập lý do tạm trú" }]}
        >
          <Input.TextArea rows={2} maxLength={500} placeholder="Mục đích tạm trú (làm việc, học tập, thuê nhà...)" />
        </Form.Item>

        <Form.Item name="maGiayTamTru" label="Mã giấy tạm trú (nếu có)">
          <Input placeholder="Mã do cơ quan cấp (nếu có)" maxLength={50} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
