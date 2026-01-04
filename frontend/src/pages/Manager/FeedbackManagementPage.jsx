import React, { useState, useEffect, useCallback } from "react";
import { 
  Button, Input, Modal, Form, Table, Tag, Card, Select, App, Empty, 
  Descriptions, Space, Divider, List, Typography 
} from "antd";
import { SendOutlined, ReloadOutlined, EyeOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { phanAnhService, buildingService } from "../../services";
import { useAuthContext } from "../../contexts";
import dayjs from "dayjs";

const { TextArea } = Input;
const { Option } = Select;
const { Text } = Typography;

/**
 * Trang Quản lý Phản ánh cho Admin/Manager.
 * - Xem danh sách phản ánh của tòa nhà
 * - Phản hồi phản ánh
 * - Khi phản hồi -> trạng thái chuyển thành "Đã xử lý"
 */
export default function FeedbackManagementPage() {
  const { message } = App.useApp();
  const { user } = useAuthContext();
  const isAdmin = user?.role === "ADMIN";
  
  const [loading, setLoading] = useState(false);
  const [phanAnhs, setPhanAnhs] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [buildings, setBuildings] = useState([]);
  const [filterToaNha, setFilterToaNha] = useState(null);
  const [filterTrangThai, setFilterTrangThai] = useState(null);
  
  // Modal chi tiết và phản hồi
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedPhanAnh, setSelectedPhanAnh] = useState(null);
  const [phanHois, setPhanHois] = useState([]);
  const [replyContent, setReplyContent] = useState("");
  const [replying, setReplying] = useState(false);

  // Load tòa nhà
  useEffect(() => {
    const fetchBuildings = async () => {
      try {
        const data = await buildingService.getAllForDropdown();
        setBuildings(data || []);
      } catch (error) {
        console.error("Error fetching buildings:", error);
      }
    };
    fetchBuildings();
  }, []);

  // Load phản ánh
  const fetchPhanAnhs = useCallback(async (page = 0, size = 10) => {
    setLoading(true);
    try {
      let response;
      if (filterToaNha) {
        response = await phanAnhService.getByToaNha(filterToaNha, page, size);
      } else {
        response = await phanAnhService.getAll(page, size);
      }
      
      let data = response?.content || response || [];
      
      // Lọc theo trạng thái nếu có
      if (filterTrangThai) {
        data = data.filter(p => p.trangThai === filterTrangThai);
      }
      
      setPhanAnhs(Array.isArray(data) ? data : []);
      setPagination(prev => ({
        ...prev,
        current: page + 1,
        total: response?.totalElements || (Array.isArray(data) ? data.length : 0),
      }));
    } catch (error) {
      message.error("Lỗi tải danh sách phản ánh");
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [message, filterToaNha, filterTrangThai]);

  useEffect(() => {
    fetchPhanAnhs();
  }, [fetchPhanAnhs]);

  // Load chi tiết phản ánh
  const loadPhanAnhDetail = async (id) => {
    try {
      const phanAnh = await phanAnhService.getById(id);
      setSelectedPhanAnh(phanAnh);
      
      const hois = await phanAnhService.getPhanHoi(id);
      setPhanHois(hois || []);
      
      setDetailModalOpen(true);
      setReplyContent("");
    } catch (error) {
      message.error("Lỗi tải chi tiết phản ánh");
    }
  };

  // Gửi phản hồi
  const handleReply = async () => {
    if (!replyContent.trim()) {
      message.warning("Vui lòng nhập nội dung phản hồi");
      return;
    }
    
    setReplying(true);
    try {
      await phanAnhService.addPhanHoi(selectedPhanAnh.id, {
        noiDung: replyContent.trim(),
        nguoiTraLoi: user?.username || "Ban quản lý",
      });
      
      message.success("Đã gửi phản hồi - Trạng thái chuyển thành 'Đã xử lý'");
      setReplyContent("");
      
      // Reload chi tiết và danh sách
      await loadPhanAnhDetail(selectedPhanAnh.id);
      fetchPhanAnhs(pagination.current - 1, pagination.pageSize);
    } catch (error) {
      message.error("Lỗi gửi phản hồi: " + (error.response?.data?.message || error.message));
    } finally {
      setReplying(false);
    }
  };

  const handleTableChange = (paginationConfig) => {
    fetchPhanAnhs(paginationConfig.current - 1, paginationConfig.pageSize);
  };

  const getStatusTag = (status) => {
    const colorMap = {
      "Đã xử lý": "green",
      "Đang xử lý": "orange",
      "Chờ xử lý": "red",
    };
    return <Tag color={colorMap[status] || "default"}>{status}</Tag>;
  };

  const columns = [
    { 
      title: "Tiêu đề", 
      dataIndex: "tieuDe",
      width: "25%",
      ellipsis: true,
    },
    { 
      title: "Người gửi", 
      dataIndex: ["user", "username"],
      width: "12%",
    },
    { 
      title: "Tòa nhà", 
      dataIndex: ["toaNha", "tenToaNha"],
      width: "12%",
    },
    { 
      title: "Ngày gửi", 
      dataIndex: "ngayGui",
      width: "15%",
      render: (date) => date ? dayjs(date).format("DD/MM/YYYY HH:mm") : "-",
    },
    { 
      title: "Trạng thái", 
      dataIndex: "trangThai",
      width: "12%",
      render: getStatusTag,
    },
    {
      title: "Thao tác",
      width: "14%",
      render: (_, record) => (
        <Button 
          type="primary" 
          size="small" 
          icon={<EyeOutlined />}
          onClick={() => loadPhanAnhDetail(record.id)}
        >
          Xem & Phản hồi
        </Button>
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý phản ánh cư dân"
      extra={
        <Button icon={<ReloadOutlined />} onClick={() => fetchPhanAnhs()}>
          Làm mới
        </Button>
      }
    >
      {/* Bộ lọc */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select
            style={{ width: 200 }}
            placeholder="Lọc theo tòa nhà"
            allowClear
            onChange={setFilterToaNha}
            value={filterToaNha}
          >
            {buildings.map(b => (
              <Option key={b.id} value={b.id}>{b.tenToaNha}</Option>
            ))}
          </Select>
          <Select
            style={{ width: 150 }}
            placeholder="Trạng thái"
            allowClear
            onChange={setFilterTrangThai}
            value={filterTrangThai}
          >
            <Option value="Chờ xử lý">Chờ xử lý</Option>
            <Option value="Đang xử lý">Đang xử lý</Option>
            <Option value="Đã xử lý">Đã xử lý</Option>
          </Select>
        </Space>
      </Card>

      {/* Bảng danh sách */}
      <Table
        columns={columns}
        dataSource={phanAnhs}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        locale={{ emptyText: <Empty description="Chưa có phản ánh" /> }}
      />

      {/* Modal chi tiết và phản hồi */}
      <Modal
        title="Chi tiết phản ánh"
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={700}
      >
        {selectedPhanAnh && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="Tiêu đề" span={2}>
                <strong>{selectedPhanAnh.tieuDe}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="Người gửi">
                {selectedPhanAnh.user?.username}
              </Descriptions.Item>
              <Descriptions.Item label="Tòa nhà">
                {selectedPhanAnh.toaNha?.tenToaNha}
              </Descriptions.Item>
              <Descriptions.Item label="Ngày gửi">
                {dayjs(selectedPhanAnh.ngayGui).format("DD/MM/YYYY HH:mm")}
              </Descriptions.Item>
              <Descriptions.Item label="Trạng thái">
                {getStatusTag(selectedPhanAnh.trangThai)}
              </Descriptions.Item>
              <Descriptions.Item label="Nội dung" span={2}>
                <div style={{ whiteSpace: "pre-wrap" }}>{selectedPhanAnh.noiDung}</div>
              </Descriptions.Item>
            </Descriptions>

            <Divider>Phản hồi từ Ban quản lý</Divider>

            {phanHois.length > 0 ? (
              <List
                dataSource={phanHois}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space>
                          <Text strong>{item.nguoiTraLoi}</Text>
                          <Text type="secondary">
                            {dayjs(item.ngayTraLoi).format("DD/MM/YYYY HH:mm")}
                          </Text>
                        </Space>
                      }
                      description={
                        <div style={{ whiteSpace: "pre-wrap", marginTop: 8 }}>
                          {item.noiDung}
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="Chưa có phản hồi" />
            )}

            <Divider>Gửi phản hồi mới</Divider>

            <TextArea
              rows={4}
              placeholder="Nhập nội dung phản hồi..."
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              style={{ marginBottom: 16 }}
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={handleReply}
              loading={replying}
              disabled={!replyContent.trim()}
            >
              Gửi phản hồi (Đánh dấu Đã xử lý)
            </Button>
          </>
        )}
      </Modal>
    </ContentCard>
  );
}
