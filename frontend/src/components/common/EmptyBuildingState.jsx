import React from "react";
import { Card, Empty, Button, Typography } from "antd";
import { BankOutlined, PlusOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

const { Title, Text } = Typography;

/**
 * Component hiển thị khi Manager/Admin chưa có tòa nhà nào
 * Sử dụng trong HomePage và BuildingListPage
 */
export default function EmptyBuildingState({ 
  onCreateBuilding,
  showCard = true,
  title = "Chưa có tòa nhà",
  description = "Bạn chưa có tòa nhà nào. Hãy tạo tòa nhà đầu tiên để bắt đầu quản lý!"
}) {
  const navigate = useNavigate();

  const handleCreate = () => {
    if (onCreateBuilding) {
      onCreateBuilding();
    } else {
      navigate("/buildings");
    }
  };

  const content = (
    <div style={{ textAlign: "center", padding: showCard ? 0 : 40 }}>
      <Empty
        image={
          <BankOutlined 
            style={{ 
              fontSize: 80, 
              color: "#3b82f6",
              opacity: 0.6
            }} 
          />
        }
        imageStyle={{ height: 100 }}
        description={null}
      >
        <Title level={4} style={{ color: "#e2e8f0", marginBottom: 8 }}>
          {title}
        </Title>
        <Text style={{ color: "#94a3b8", display: "block", marginBottom: 24 }}>
          {description}
        </Text>
        <Button 
          type="primary" 
          size="large" 
          icon={<PlusOutlined />} 
          onClick={handleCreate}
        >
          Tạo tòa nhà đầu tiên
        </Button>
      </Empty>
    </div>
  );

  if (!showCard) {
    return content;
  }

  return (
    <Card
      style={{
        background: "rgba(255, 255, 255, 0.02)",
        border: "1px solid rgba(255, 255, 255, 0.1)",
        borderRadius: 12,
        padding: 40,
      }}
    >
      {content}
    </Card>
  );
}
