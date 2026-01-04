import React, { useEffect, useCallback, useState } from "react";
import { Button, App, Input, Select, Modal, Form, Card, Space, Table, Tag, Popconfirm, Empty } from "antd";
import { PlusOutlined, SendOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { notificationService, buildingService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { useAuthContext } from "../../contexts";
import dayjs from "dayjs";

const { Option } = Select;
const { TextArea } = Input;

export default function NotificationPage() {
  const { message } = App.useApp();
  const { user } = useAuthContext();
  const isAdmin = user?.role === "ADMIN";
  
  // Cho gửi nhắc hạn đơn giản
  const [selectedToaNhaReminder, setSelectedToaNhaReminder] = useState(null);
  const [reminderDotThuName, setReminderDotThuName] = useState("");
  const [sendingReminder, setSendingReminder] = useState(false);
  
  // Danh sách thông báo
  const [notifications, setNotifications] = useState([]);
  const [loadingNotifications, setLoadingNotifications] = useState(false);
  const [filterToaNha, setFilterToaNha] = useState(null);
  
  const { data: buildings, refetch: fetchBuildings } = useFetch(buildingService.getAllForDropdown, false);
  
  const notificationModal = useModal({
    tieuDe: "",
    noiDung: "",
    loaiThongBao: "Tin tức",
    nguoiTao: "",
    toaNhaId: null,
  });

  // Load danh sách thông báo
  const loadNotifications = useCallback(async () => {
    setLoadingNotifications(true);
    try {
      const data = await notificationService.getAll(0, 100);
      let list = data?.content || data || [];
      
      // Lọc theo tòa nhà nếu có chọn
      if (filterToaNha) {
        list = list.filter(n => n.toaNha?.id === filterToaNha || !n.toaNha);
      }
      
      setNotifications(list);
    } catch (error) {
      console.error("Error loading notifications:", error);
      setNotifications([]);
    } finally {
      setLoadingNotifications(false);
    }
  }, [filterToaNha]);

  useEffect(() => {
    fetchBuildings();
    loadNotifications();
  }, [fetchBuildings, loadNotifications]);

  const handleCreateThongBao = useCallback(async (values, editingId) => {
    try {
      if (values.toaNhaId) {
        await notificationService.createForBuilding(values.toaNhaId, {
          tieuDe: values.tieuDe,
          noiDung: values.noiDung,
          loaiThongBao: values.loaiThongBao,
        });
      } else {
        await notificationService.createThongBao(values);
      }
      message.success("Tạo thông báo thành công");
      notificationModal.closeModal();
      loadNotifications(); // Reload danh sách
    } catch (error) {
      message.error("Tạo thông báo thất bại: " + (error.response?.data?.message || error.message));
    }
  }, [notificationModal, message, loadNotifications]);

  // Xóa thông báo
  const handleDeleteNotification = useCallback(async (id) => {
    try {
      await notificationService.delete(id);
      message.success("Đã xóa thông báo");
      loadNotifications();
    } catch (error) {
      message.error("Xóa thất bại: " + (error.response?.data?.message || error.message));
    }
  }, [message, loadNotifications]);

  // Gửi nhắc hạn đơn giản
  const handleSendReminder = useCallback(async () => {
    if (!selectedToaNhaReminder) {
      message.warning("Vui lòng chọn tòa nhà");
      return;
    }
    if (!reminderDotThuName.trim()) {
      message.warning("Vui lòng nhập tên đợt thu");
      return;
    }
    
    setSendingReminder(true);
    try {
      await notificationService.sendPaymentReminder(selectedToaNhaReminder, reminderDotThuName.trim());
      message.success("Đã gửi thông báo nhắc hạn cho tòa nhà");
      setReminderDotThuName("");
      loadNotifications(); // Reload danh sách
    } catch (error) {
      message.error("Lỗi gửi thông báo: " + (error.response?.data?.message || error.message));
    } finally {
      setSendingReminder(false);
    }
  }, [selectedToaNhaReminder, reminderDotThuName, message, loadNotifications]);

  // Columns cho bảng thông báo
  const columns = [
    {
      title: "Tiêu đề",
      dataIndex: "tieuDe",
      key: "tieuDe",
      width: 250,
      ellipsis: true,
    },
    {
      title: "Loại",
      dataIndex: "loaiThongBao",
      key: "loaiThongBao",
      width: 100,
      render: (type) => {
        const colors = {
          "Tin tức": "blue",
          "Cảnh báo": "orange",
          "Phí": "green",
          "Nhắc hạn": "red",
        };
        return <Tag color={colors[type] || "default"}>{type}</Tag>;
      },
    },
    {
      title: "Tòa nhà",
      dataIndex: ["toaNha", "tenToaNha"],
      key: "toaNha",
      width: 120,
      render: (text) => text || <Tag>Hệ thống</Tag>,
    },
    {
      title: "Người tạo",
      dataIndex: "nguoiTao",
      key: "nguoiTao",
      width: 120,
    },
    {
      title: "Ngày tạo",
      dataIndex: "ngayTao",
      key: "ngayTao",
      width: 150,
      render: (date) => date ? dayjs(date).format("DD/MM/YYYY HH:mm") : "-",
    },
    {
      title: "Thao tác",
      key: "actions",
      width: 80,
      render: (_, record) => (
        <Popconfirm
          title="Xóa thông báo này?"
          onConfirm={() => handleDeleteNotification(record.id)}
          okText="Xóa"
          cancelText="Hủy"
        >
          <Button type="text" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý thông báo"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => notificationModal.openModal()}>
          Tạo thông báo
        </Button>
      }
    >
      {/* Gửi nhắc hạn đơn giản */}
      <Card title="Gửi nhắc hạn thanh toán" size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select
            style={{ width: 200 }}
            placeholder="Chọn tòa nhà"
            onChange={setSelectedToaNhaReminder}
            value={selectedToaNhaReminder}
          >
            {buildings?.map((b) => (
              <Option key={b.id} value={b.id}>
                {b.tenToaNha}
              </Option>
            ))}
          </Select>
          <Input
            style={{ width: 250 }}
            placeholder="Tên đợt thu (VD: Tháng 01/2026)"
            value={reminderDotThuName}
            onChange={(e) => setReminderDotThuName(e.target.value)}
          />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={handleSendReminder}
            loading={sendingReminder}
            disabled={!selectedToaNhaReminder || !reminderDotThuName.trim()}
          >
            Gửi nhắc hạn
          </Button>
        </Space>
      </Card>

      {/* Danh sách thông báo */}
      <Card 
        title="Danh sách thông báo" 
        size="small"
        extra={
          <Space>
            <Select
              style={{ width: 180 }}
              placeholder="Lọc theo tòa nhà"
              allowClear
              onChange={(value) => setFilterToaNha(value)}
              value={filterToaNha}
            >
              {buildings?.map((b) => (
                <Option key={b.id} value={b.id}>
                  {b.tenToaNha}
                </Option>
              ))}
            </Select>
            <Button icon={<ReloadOutlined />} onClick={loadNotifications}>
              Làm mới
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={notifications}
          rowKey="id"
          loading={loadingNotifications}
          pagination={{ pageSize: 10 }}
          size="small"
          locale={{ emptyText: <Empty description="Chưa có thông báo" /> }}
          expandable={{
            expandedRowRender: (record) => (
              <div style={{ padding: "8px 0", whiteSpace: "pre-wrap" }}>
                {record.noiDung}
              </div>
            ),
          }}
        />
      </Card>

      <NotificationFormModal 
        modal={notificationModal} 
        onSubmit={handleCreateThongBao}
        buildings={buildings || []}
        isAdmin={isAdmin}
      />
    </ContentCard>
  );
}

function NotificationFormModal({ modal, onSubmit, buildings, isAdmin }) {
  const { form, open, closeModal, handleSubmit, loading } = modal;
  const { user } = useAuthContext();

  useEffect(() => {
    if (open) {
      form.setFieldsValue({ nguoiTao: user?.username || "" });
      // Manager: auto-select tòa nhà đầu tiên nếu chỉ có 1
      if (!isAdmin && buildings?.length === 1) {
        form.setFieldsValue({ toaNhaId: buildings[0].id });
      }
    }
  }, [open, form, user, buildings, isAdmin]);

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
        {/* Manager phải chọn tòa nhà, Admin có thể bỏ qua để gửi thông báo hệ thống */}
        <Form.Item
          name="toaNhaId"
          label="Tòa nhà"
          rules={isAdmin ? [] : [{ required: true, message: "Vui lòng chọn tòa nhà" }]}
          extra={isAdmin ? "Bỏ trống để gửi thông báo hệ thống cho tất cả" : undefined}
        >
          <Select 
            placeholder={isAdmin ? "Chọn tòa nhà (hoặc bỏ trống cho thông báo hệ thống)" : "Chọn tòa nhà"}
            allowClear={isAdmin}
          >
            {buildings?.map((b) => (
              <Option key={b.id} value={b.id}>{b.tenToaNha}</Option>
            ))}
          </Select>
        </Form.Item>
        
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

