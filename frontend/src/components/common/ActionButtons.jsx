import React from "react";
import { Button, Space, Popconfirm } from "antd";
import { EditOutlined, DeleteOutlined } from "@ant-design/icons";

export default function ActionButtons({
  onEdit,
  onDelete,
  deleteTitle = "Xác nhận xóa?",
  deleteDescription,
  confirmDelete = true
}) {
  return (
    <Space>
      <Button type="link" icon={<EditOutlined />} onClick={onEdit}>
        Sửa
      </Button>
      {confirmDelete ? (
        <Popconfirm
          title={deleteTitle}
          description={deleteDescription}
          onConfirm={onDelete}
          okText="Xóa"
          cancelText="Hủy"
        >
          <Button danger type="link" icon={<DeleteOutlined />}>
            Xóa
          </Button>
        </Popconfirm>
      ) : (
        <Button danger type="link" icon={<DeleteOutlined />} onClick={onDelete}>
          Xóa
        </Button>
      )}
    </Space>
  );
}
