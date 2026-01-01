import React, { useEffect, useCallback, useState } from "react";
import { Button, message, Input, Select, Modal, Form, Card } from "antd";
import { PlusOutlined, SendOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { notificationService, feeService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { useAuthContext } from "../../contexts";

const { Option } = Select;
const { TextArea } = Input;

export default function NotificationPage() {
  const [selectedDotThu, setSelectedDotThu] = useState(null);
  const { data: dotThus, refetch: fetchDotThus } = useFetch(feeService.getAllDotThu, false);
  const notificationModal = useModal({
    tieuDe: "",
    noiDung: "",
    loaiThongBao: "Tin tức",
    nguoiTao: "",
  });

  useEffect(() => {
    fetchDotThus();
  }, [fetchDotThus]);

  const handleCreateThongBao = useCallback(async (values, editingId) => {
    try {
      await notificationService.createThongBao(values);
      message.success("Tạo thông báo thành công");
      notificationModal.closeModal();
    } catch (error) {
      message.error("Tạo thông báo thất bại");
    }
  }, [notificationModal]);

  const handleSendReminder = useCallback(async (idHoaDon) => {
    try {
      await notificationService.sendPaymentReminder(idHoaDon);
      message.success("Đã gửi thông báo nhắc hạn");
    } catch (error) {
      message.error("Lỗi gửi thông báo: " + (error.response?.data?.message || error.message));
    }
  }, []);

  const handleSendBulkReminder = useCallback(async () => {
    if (!selectedDotThu) {
      message.warning("Vui lòng chọn đợt thu");
      return;
    }
    try {
      const result = await notificationService.sendBulkPaymentReminder(selectedDotThu);
      message.success(`Đã gửi thông báo cho ${result.sentCount} hộ gia đình`);
    } catch (error) {
      message.error("Lỗi gửi thông báo hàng loạt");
    }
  }, [selectedDotThu]);

  return (
    <ContentCard
      title="Quản lý thông báo"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => notificationModal.openModal()}>
          Tạo thông báo
        </Button>
      }
    >
      <div style={{ marginBottom: 24 }}>
        <Select
          style={{ width: 300, marginRight: 8 }}
          placeholder="Chọn đợt thu"
          onChange={setSelectedDotThu}
          value={selectedDotThu}
        >
          {dotThus?.map((dt) => (
            <Option key={dt.id} value={dt.id}>
              {dt.tenDotThu}
            </Option>
          ))}
        </Select>
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleSendBulkReminder}
          disabled={!selectedDotThu}
        >
          Gửi nhắc hạn hàng loạt
        </Button>
      </div>

      <Card>
        <h3>Hướng dẫn sử dụng</h3>
        <ul>
          <li>Tạo thông báo mới để gửi cho tất cả cư dân</li>
          <li>Chọn đợt thu và nhấn "Gửi nhắc hạn hàng loạt" để gửi thông báo cho các hộ chưa đóng phí</li>
          <li>Thông báo sẽ được gửi qua email nếu đã cấu hình</li>
        </ul>
      </Card>

      <NotificationFormModal modal={notificationModal} onSubmit={handleCreateThongBao} />
    </ContentCard>
  );
}

function NotificationFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, loading } = modal;
  const { user } = useAuthContext();

  useEffect(() => {
    if (open) {
      form.setFieldsValue({ nguoiTao: user?.username || "" });
    }
  }, [open, form, user]);

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, "Tạo thông báo thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title="Tạo thông báo"
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="tieuDe"
          label="Tiêu đề"
          rules={[{ required: true, message: "Vui lòng nhập tiêu đề" }]}
        >
          <Input placeholder="Tiêu đề thông báo" />
        </Form.Item>

        <Form.Item
          name="noiDung"
          label="Nội dung"
          rules={[{ required: true, message: "Vui lòng nhập nội dung" }]}
        >
          <TextArea rows={6} placeholder="Nội dung thông báo" />
        </Form.Item>

        <Form.Item
          name="loaiThongBao"
          label="Loại thông báo"
          rules={[{ required: true, message: "Vui lòng chọn loại thông báo" }]}
        >
          <Select>
            <Option value="Tin tức">Tin tức</Option>
            <Option value="Cảnh báo">Cảnh báo</Option>
            <Option value="Phí">Phí</Option>
          </Select>
        </Form.Item>

        <Form.Item name="nguoiTao" label="Người tạo" hidden>
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
}

