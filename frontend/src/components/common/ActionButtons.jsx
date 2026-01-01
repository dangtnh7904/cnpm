import React from "react";
import { Button, Space, Popconfirm } from "antd";
import { EditOutlined, DeleteOutlined } from "@ant-design/icons";

export default function ActionButtons({ onEdit, onDelete, deleteTitle = "Xác nhận xóa?" }) {
  return (
    <Space>
      <Button type="link" icon={<EditOutlined />} onClick={onEdit}>
        Sửa
      </Button>
      <Popconfirm title={deleteTitle} onConfirm={onDelete} okText="Xóa" cancelText="Hủy">
        <Button danger type="link" icon={<DeleteOutlined />}>
          Xóa
        </Button>
      </Popconfirm>
    </Space>
  );
}
