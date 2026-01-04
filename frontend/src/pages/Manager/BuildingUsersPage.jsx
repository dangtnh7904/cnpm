import React, { useState, useEffect, useCallback } from "react";
import {
  Card,
  Table,
  Button,
  Select,
  Modal,
  Form,
  Input,
  Space,
  Tag,
  message,
  Popconfirm,
  Typography,
  Empty,
} from "antd";
import {
  UserAddOutlined,
  DeleteOutlined,
  SearchOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import { buildingService, userToaNhaService } from "../../services";

const { Title, Text } = Typography;
const { Option } = Select;

/**
 * Trang quản lý Cư dân trong Tòa nhà.
 * 
 * CHỨC NĂNG:
 * - Manager chọn tòa nhà để xem danh sách user
 * - Thêm user vào tòa nhà (bằng username)
 * - Xóa user khỏi tòa nhà
 * 
 * LOGIC NGHIỆP VỤ:
 * - User được gắn vào tòa nhà có thể xem thông báo của tòa đó
 * - User có thể nộp tiền cho bất kỳ hộ nào trong tòa đó
 * - User tự thoát khỏi tòa nhà khi không muốn
 */
export default function BuildingUsersPage() {
  const [buildings, setBuildings] = useState([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [form] = Form.useForm();

  // Load danh sách tòa nhà
  useEffect(() => {
    const loadBuildings = async () => {
      try {
        const data = await buildingService.getAllForDropdown();
        setBuildings(data || []);
        if (data && data.length > 0) {
          setSelectedBuildingId(data[0].id);
        }
      } catch (error) {
        message.error("Không thể tải danh sách tòa nhà");
      }
    };
    loadBuildings();
  }, []);

  // Load danh sách user trong tòa nhà
  const loadUsers = useCallback(async () => {
    if (!selectedBuildingId) return;
    
    setLoading(true);
    try {
      const data = await userToaNhaService.getUsersInBuilding(selectedBuildingId);
      setUsers(data || []);
    } catch (error) {
      message.error("Không thể tải danh sách cư dân");
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [selectedBuildingId]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // Thêm user vào tòa nhà
  const handleAddUser = async (values) => {
    setAddLoading(true);
    try {
      await userToaNhaService.addUserToBuilding(values.username, selectedBuildingId);
      message.success(`Đã thêm "${values.username}" vào tòa nhà`);
      form.resetFields();
      setModalVisible(false);
      loadUsers();
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || "Không thể thêm user";
      message.error(errorMsg);
    } finally {
      setAddLoading(false);
    }
  };

  // Xóa user khỏi tòa nhà
  const handleRemoveUser = async (userId) => {
    try {
      await userToaNhaService.removeUserFromBuilding(userId, selectedBuildingId);
      message.success("Đã xóa cư dân khỏi tòa nhà");
      loadUsers();
    } catch (error) {
      message.error("Không thể xóa cư dân");
    }
  };

  const columns = [
    {
      title: "Username",
      dataIndex: ["user", "username"],
      key: "username",
      render: (text) => <Text strong>{text}</Text>,
    },
    {
      title: "Họ tên",
      dataIndex: ["user", "fullName"],
      key: "fullName",
      render: (text) => text || <Text type="secondary">Chưa cập nhật</Text>,
    },
    {
      title: "Email",
      dataIndex: ["user", "email"],
      key: "email",
      render: (text) => text || <Text type="secondary">-</Text>,
    },
    {
      title: "Role",
      dataIndex: ["user", "role"],
      key: "role",
      render: (role) => {
        const colors = {
          ADMIN: "red",
          MANAGER: "blue",
          ACCOUNTANT: "green",
          RESIDENT: "default",
        };
        return <Tag color={colors[role] || "default"}>{role}</Tag>;
      },
    },
    {
      title: "Ngày thêm",
      dataIndex: "ngayThem",
      key: "ngayThem",
      render: (date) =>
        date ? new Date(date).toLocaleDateString("vi-VN") : "-",
    },
    {
      title: "Thao tác",
      key: "action",
      width: 120,
      render: (_, record) => (
        <Popconfirm
          title="Xóa cư dân khỏi tòa nhà?"
          description="User sẽ không thể xem thông báo và nộp tiền cho tòa nhà này nữa."
          onConfirm={() => handleRemoveUser(record.user?.id)}
          okText="Xóa"
          cancelText="Hủy"
        >
          <Button type="link" danger icon={<DeleteOutlined />}>
            Xóa
          </Button>
        </Popconfirm>
      ),
    },
  ];

  const selectedBuilding = buildings.find((b) => b.id === selectedBuildingId);

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <div style={{ marginBottom: 24 }}>
          <Title level={4} style={{ marginBottom: 16 }}>
            <TeamOutlined /> Quản lý Cư dân Tòa nhà
          </Title>
          <Text type="secondary">
            Thêm user vào tòa nhà để họ có thể xem thông báo và nộp tiền cho các hộ trong tòa.
          </Text>
        </div>

        {/* Chọn tòa nhà */}
        <div style={{ marginBottom: 24 }}>
          <Space size="middle" wrap>
            <Select
              style={{ width: 300 }}
              placeholder="Chọn tòa nhà"
              value={selectedBuildingId}
              onChange={setSelectedBuildingId}
              showSearch
              optionFilterProp="children"
            >
              {buildings.map((b) => (
                <Option key={b.id} value={b.id}>
                  {b.tenToaNha}
                </Option>
              ))}
            </Select>

            <Button
              type="primary"
              icon={<UserAddOutlined />}
              onClick={() => setModalVisible(true)}
              disabled={!selectedBuildingId}
            >
              Thêm cư dân
            </Button>
          </Space>
        </div>

        {/* Thông tin tòa nhà */}
        {selectedBuilding && (
          <div style={{ marginBottom: 16 }}>
            <Tag color="blue">Tòa nhà: {selectedBuilding.tenToaNha}</Tag>
            <Tag color="green">Số cư dân: {users.length}</Tag>
          </div>
        )}

        {/* Bảng danh sách user */}
        {buildings.length === 0 ? (
          <Empty
            description="Chưa có tòa nhà nào"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        ) : (
          <Table
            columns={columns}
            dataSource={users}
            rowKey="id"
            loading={loading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} cư dân`,
            }}
            locale={{
              emptyText: (
                <Empty
                  description="Chưa có cư dân nào trong tòa nhà này"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                >
                  <Button
                    type="primary"
                    icon={<UserAddOutlined />}
                    onClick={() => setModalVisible(true)}
                  >
                    Thêm cư dân đầu tiên
                  </Button>
                </Empty>
              ),
            }}
          />
        )}
      </Card>

      {/* Modal thêm cư dân */}
      <Modal
        title="Thêm cư dân vào tòa nhà"
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleAddUser}
        >
          <Form.Item
            name="username"
            label="Username"
            rules={[
              { required: true, message: "Vui lòng nhập username" },
              { min: 3, message: "Username phải có ít nhất 3 ký tự" },
            ]}
          >
            <Input
              prefix={<SearchOutlined />}
              placeholder="Nhập username của cư dân"
              autoComplete="off"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: "right" }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={addLoading}>
                Thêm cư dân
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginTop: 16, padding: 12, background: "#f5f5f5", borderRadius: 6 }}>
          <Text type="secondary">
            <strong>Lưu ý:</strong> Cư dân được thêm sẽ có thể:
          </Text>
          <ul style={{ marginTop: 8, paddingLeft: 20 }}>
            <li>Xem thông báo của tòa nhà {selectedBuilding?.tenToaNha}</li>
            <li>Nộp tiền cho bất kỳ hộ gia đình nào trong tòa</li>
            <li>Tự rời khỏi tòa nhà khi không muốn</li>
          </ul>
        </div>
      </Modal>
    </div>
  );
}
