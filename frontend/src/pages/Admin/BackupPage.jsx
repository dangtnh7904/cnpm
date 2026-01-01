import React, { useState, useEffect } from "react";
import { Button, message, Table, Tag, Popconfirm, Card, Space } from "antd";
import { DownloadOutlined, DeleteOutlined, ReloadOutlined, FileZipOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { backupService } from "../../services";
import { useFetch } from "../../hooks";

export default function BackupPage() {
  const [loading, setLoading] = useState(false);
  const { data: backups, refetch } = useFetch(backupService.listBackups, false);

  useEffect(() => {
    refetch();
  }, [refetch]);

  const handleCreateBackup = async () => {
    setLoading(true);
    try {
      await backupService.createBackup();
      message.success("Tạo backup thành công");
      refetch();
    } catch (error) {
      message.error("Lỗi tạo backup");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBackupZip = async () => {
    setLoading(true);
    try {
      await backupService.createBackupZip();
      message.success("Tạo backup ZIP thành công");
      refetch();
    } catch (error) {
      message.error("Lỗi tạo backup ZIP");
    } finally {
      setLoading(false);
    }
  };

  const handleRestore = async (fileName) => {
    try {
      await backupService.restoreBackup(fileName);
      message.success("Khôi phục backup thành công");
    } catch (error) {
      message.error("Lỗi khôi phục backup");
    }
  };

  const handleDelete = async (fileName) => {
    try {
      await backupService.deleteBackup(fileName);
      message.success("Xóa backup thành công");
      refetch();
    } catch (error) {
      message.error("Lỗi xóa backup");
    }
  };

  const columns = [
    { title: "Tên file", dataIndex: "name", key: "name" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <Space>
          <Popconfirm
            title="Khôi phục backup này?"
            onConfirm={() => handleRestore(record.name)}
            okText="Có"
            cancelText="Không"
          >
            <Button type="link" icon={<ReloadOutlined />}>
              Khôi phục
            </Button>
          </Popconfirm>
          <Popconfirm
            title="Xóa backup này?"
            onConfirm={() => handleDelete(record.name)}
            okText="Có"
            cancelText="Không"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              Xóa
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <ContentCard title="Sao lưu và khôi phục dữ liệu">
      <Card style={{ marginBottom: 24 }}>
        <Space>
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleCreateBackup}
            loading={loading}
          >
            Tạo backup SQL
          </Button>
          <Button
            type="primary"
            icon={<FileZipOutlined />}
            onClick={handleCreateBackupZip}
            loading={loading}
          >
            Tạo backup ZIP
          </Button>
        </Space>
      </Card>

      <Table
        columns={columns}
        dataSource={backups?.map(name => ({ name, key: name }))}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Card style={{ marginTop: 24 }}>
        <h3>Lưu ý:</h3>
        <ul>
          <li>Backup sẽ được lưu trong thư mục <code>./backups</code></li>
          <li>Nên tạo backup định kỳ để đảm bảo an toàn dữ liệu</li>
          <li>Khôi phục backup sẽ ghi đè dữ liệu hiện tại</li>
        </ul>
      </Card>
    </ContentCard>
  );
}

