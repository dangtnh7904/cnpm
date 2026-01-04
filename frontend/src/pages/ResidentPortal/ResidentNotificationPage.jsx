import React, { useEffect, useState, useCallback } from "react";
import { Table, Tag, Card, Descriptions, Select, Empty, Spin, App } from "antd";
import { EyeOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { notificationService } from "../../services";
import dayjs from "dayjs";

const { Option } = Select;

/**
 * Trang hiển thị thông báo cho Resident.
 * Resident xem thông báo hệ thống + thông báo của tòa nhà mình thuộc (via UserToaNha).
 */
export default function ResidentNotificationPage() {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filterLoai, setFilterLoai] = useState(null);
  const [selectedNotification, setSelectedNotification] = useState(null);

  const fetchNotifications = useCallback(async (page = 0, size = 10, loaiThongBao = null) => {
    setLoading(true);
    try {
      let response;
      if (loaiThongBao) {
        response = await notificationService.search({ loaiThongBao, page, size });
      } else {
        response = await notificationService.getAll(page, size);
      }
      
      const data = response.content || response;
      setNotifications(Array.isArray(data) ? data : []);
      setPagination(prev => ({
        ...prev,
        current: page + 1,
        total: response.totalElements || data.length,
      }));
    } catch (error) {
      message.error("Lỗi tải thông báo");
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [message]);

  useEffect(() => {
    fetchNotifications(0, pagination.pageSize, filterLoai);
  }, [filterLoai]);

  const handleTableChange = (paginationConfig) => {
    fetchNotifications(paginationConfig.current - 1, paginationConfig.pageSize, filterLoai);
  };

  const getLoaiTag = (loai) => {
    const colorMap = {
      "Tin tức": "blue",
      "Cảnh báo": "orange",
      "Phí": "green",
      "Thông báo chung": "purple",
    };
    return <Tag color={colorMap[loai] || "default"}>{loai}</Tag>;
  };

  const columns = [
    { 
      title: "Tiêu đề", 
      dataIndex: "tieuDe",
      width: "35%",
    },
    { 
      title: "Loại", 
      dataIndex: "loaiThongBao",
      width: "15%",
      render: getLoaiTag,
    },
    { 
      title: "Người tạo", 
      dataIndex: "nguoiTao",
      width: "15%",
    },
    { 
      title: "Ngày tạo", 
      dataIndex: "ngayTao",
      width: "20%",
      render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm"),
    },
    {
      title: "",
      width: "10%",
      render: (_, record) => (
        <a onClick={() => setSelectedNotification(record)}>
          <EyeOutlined /> Xem
        </a>
      ),
    },
  ];

  return (
    <ContentCard
      title="Thông báo"
      extra={
        <Select
          style={{ width: 160 }}
          placeholder="Lọc theo loại"
          allowClear
          onChange={setFilterLoai}
          value={filterLoai}
        >
          <Option value="Tin tức">Tin tức</Option>
          <Option value="Cảnh báo">Cảnh báo</Option>
          <Option value="Phí">Phí</Option>
          <Option value="Thông báo chung">Thông báo chung</Option>
        </Select>
      }
    >
      <Spin spinning={loading}>
        {notifications.length === 0 && !loading ? (
          <Empty description="Không có thông báo nào" />
        ) : (
          <Table
            columns={columns}
            dataSource={notifications}
            rowKey="id"
            pagination={{
              ...pagination,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} thông báo`,
            }}
            onChange={handleTableChange}
          />
        )}
      </Spin>

      {selectedNotification && (
        <Card 
          title="Chi tiết thông báo" 
          style={{ marginTop: 24 }}
          extra={<a onClick={() => setSelectedNotification(null)}>Đóng</a>}
        >
          <Descriptions bordered column={1}>
            <Descriptions.Item label="Tiêu đề">
              {selectedNotification.tieuDe}
            </Descriptions.Item>
            <Descriptions.Item label="Loại thông báo">
              {getLoaiTag(selectedNotification.loaiThongBao)}
            </Descriptions.Item>
            <Descriptions.Item label="Người tạo">
              {selectedNotification.nguoiTao}
            </Descriptions.Item>
            <Descriptions.Item label="Ngày tạo">
              {dayjs(selectedNotification.ngayTao).format("DD/MM/YYYY HH:mm")}
            </Descriptions.Item>
            <Descriptions.Item label="Nội dung">
              <div style={{ whiteSpace: "pre-wrap" }}>
                {selectedNotification.noiDung}
              </div>
            </Descriptions.Item>
            {selectedNotification.toaNha && (
              <Descriptions.Item label="Tòa nhà">
                {selectedNotification.toaNha.tenToaNha}
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>
      )}
    </ContentCard>
  );
}
