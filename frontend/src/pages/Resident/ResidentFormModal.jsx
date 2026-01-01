import React from "react";
import { Modal, Form, Input, Select, DatePicker, Alert } from "antd";
import { HomeOutlined } from "@ant-design/icons";
import { GENDER_OPTIONS, RELATIONSHIP_OPTIONS, DATE_FORMAT } from "../../constants";

/**
 * ResidentFormModal - Modal for adding/editing residents
 * 
 * Props:
 * - modal: Modal state from useModal hook
 * - householdOptions: Array of household options (for standalone usage)
 * - onSubmit: Callback for form submission
 * - apartmentId: If provided, the apartment selector is hidden (used in ApartmentDetailPage)
 * - apartmentInfo: Apartment details to display when apartmentId is provided
 */
export default function ResidentFormModal({ 
  modal, 
  householdOptions, 
  onSubmit, 
  apartmentId = null,
  apartmentInfo = null 
}) {
  const { form, open, isEditing, loading, closeModal, handleSubmit } = modal;
  
  // Determine if we're in "locked apartment" mode (called from ApartmentDetailPage)
  const isApartmentLocked = apartmentId !== null;

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
        {/* Show apartment info when locked (from ApartmentDetailPage) */}
        {isApartmentLocked && apartmentInfo && (
          <Alert
            message={
              <span>
                <HomeOutlined style={{ marginRight: 8 }} />
                Thêm nhân khẩu vào căn hộ: <strong>{apartmentInfo.soCanHo}</strong> - Tầng {apartmentInfo.soTang}
                {apartmentInfo.tenChuHo && ` (Chủ hộ: ${apartmentInfo.tenChuHo})`}
              </span>
            }
            type="info"
            showIcon={false}
            style={{ marginBottom: 16 }}
          />
        )}

        <Form.Item 
          name="hoTen" 
          label="Họ tên" 
          rules={[{ required: true, message: "Nhập họ tên" }]}
        >
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

        <Form.Item 
          name="ngaySinh" 
          label="Ngày sinh" 
          rules={[{ required: true, message: "Chọn ngày sinh" }]}
        >
          <DatePicker style={{ width: "100%" }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item name="gioiTinh" label="Giới tính">
          <Select options={GENDER_OPTIONS} />
        </Form.Item>

        <Form.Item 
          name="quanHeVoiChuHo" 
          label="Quan hệ với chủ hộ" 
          rules={[{ required: !isApartmentLocked, message: "Chọn quan hệ" }]}
          tooltip={isApartmentLocked ? "Nếu hộ chưa có ai, hệ thống sẽ tự động đặt là Chủ hộ" : undefined}
        >
          <Select 
            options={RELATIONSHIP_OPTIONS} 
            placeholder={isApartmentLocked ? "Để trống nếu là chủ hộ đầu tiên" : "Chọn quan hệ"}
            allowClear={isApartmentLocked}
          />
        </Form.Item>

        {/* TRẠNG THÁI: Chỉ hiển thị khi chỉnh sửa (disabled, không thể thay đổi)
            Trạng thái được quản lý bởi hệ thống qua API nghiệp vụ (Tạm vắng/Tạm trú) */}
        {isEditing && (
          <Form.Item name="trangThai" label="Trạng thái">
            <Input 
              disabled 
              placeholder="Trạng thái được quản lý bởi hệ thống" 
              style={{ backgroundColor: '#f5f5f5', color: '#888' }}
            />
          </Form.Item>
        )}

        <Form.Item name="soDienThoai" label="Số điện thoại">
          <Input placeholder="0912345678" />
        </Form.Item>

        <Form.Item 
          name="email" 
          label="Email" 
          rules={[{ type: "email", message: "Email không hợp lệ" }]}
        >
          <Input placeholder="email@example.com" />
        </Form.Item>

        {/* Only show apartment selector when NOT locked (standalone usage) */}
        {!isApartmentLocked && (
          <Form.Item 
            name="idHoGiaDinh" 
            label="Thuộc hộ" 
            rules={[{ required: true, message: "Chọn hộ" }]}
          >
            <Select 
              options={Array.isArray(householdOptions) ? householdOptions : []} 
              placeholder="Chọn hộ" 
              showSearch 
              optionFilterProp="label" 
            />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
}
