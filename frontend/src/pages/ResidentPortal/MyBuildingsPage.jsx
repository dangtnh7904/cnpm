import React, { useState, useEffect } from "react";
import {
  Card,
  List,
  Button,
  Tag,
  Typography,
  Empty,
  Popconfirm,
  message,
  Space,
} from "antd";
import {
  BankOutlined,
  LogoutOutlined,
  CalendarOutlined,
} from "@ant-design/icons";
import { userToaNhaService } from "../../services";

const { Title, Text } = Typography;

/**
 * Trang hiển thị danh sách tòa nhà mà cư dân được gắn vào.
 * 
 * CHỨC NĂNG:
 * - Xem danh sách tòa nhà đang tham gia
 * - Tự thoát khỏi tòa nhà
 */
export default function MyBuildingsPage() {
  const [buildings, setBuildings] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadBuildings = async () => {
    setLoading(true);
    try {
      const data = await userToaNhaService.getMyBuildings();
      setBuildings(data || []);
    } catch (error) {
      message.error("Không thể tải danh sách tòa nhà");
      setBuildings([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBuildings();
  }, []);

  const handleLeaveBuilding = async (toaNhaId, toaNhaName) => {
    try {
      await userToaNhaService.leaveBuilding(toaNhaId);
      message.success(`Đã thoát khỏi tòa nhà "${toaNhaName}"`);
      loadBuildings();
    } catch (error) {
      const errorMsg = error.response?.data?.message || "Không thể thoát khỏi tòa nhà";
      message.error(errorMsg);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Title level={4} style={{ marginBottom: 24 }}>
          <BankOutlined /> Tòa nhà của tôi
        </Title>
        
        <Text type="secondary" style={{ display: "block", marginBottom: 24 }}>
          Danh sách các tòa nhà bạn đang tham gia. Bạn có thể xem thông báo và nộp tiền cho các hộ trong tòa.
        </Text>

        {buildings.length === 0 ? (
          <Empty
            description="Bạn chưa được thêm vào tòa nhà nào"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Text type="secondary">
              Liên hệ quản lý tòa nhà để được thêm vào.
            </Text>
          </Empty>
        ) : (
          <List
            loading={loading}
            dataSource={buildings}
            renderItem={(item) => (
              <List.Item
                actions={[
                  <Popconfirm
                    title="Thoát khỏi tòa nhà?"
                    description="Bạn sẽ không thể xem thông báo và nộp tiền cho tòa nhà này nữa."
                    onConfirm={() => handleLeaveBuilding(item.toaNha?.id, item.toaNha?.tenToaNha)}
                    okText="Thoát"
                    cancelText="Hủy"
                    okButtonProps={{ danger: true }}
                  >
                    <Button type="link" danger icon={<LogoutOutlined />}>
                      Thoát
                    </Button>
                  </Popconfirm>
                ]}
              >
                <List.Item.Meta
                  avatar={
                    <div style={{
                      width: 48,
                      height: 48,
                      background: "#1890ff",
                      borderRadius: 8,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center"
                    }}>
                      <BankOutlined style={{ fontSize: 24, color: "#fff" }} />
                    </div>
                  }
                  title={
                    <Space>
                      <Text strong>{item.toaNha?.tenToaNha || "Tòa nhà"}</Text>
                      <Tag color="blue">{item.toaNha?.diaChi || ""}</Tag>
                    </Space>
                  }
                  description={
                    <Space>
                      <CalendarOutlined />
                      <Text type="secondary">
                        Tham gia: {item.ngayThem
                          ? new Date(item.ngayThem).toLocaleDateString("vi-VN")
                          : "N/A"}
                      </Text>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </div>
  );
}
