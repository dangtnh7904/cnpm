import React, { useState, useEffect, useCallback } from "react";
import { Button, Input, Modal, Form, Table, Tag, Card, Descriptions, Select, App, Empty, Spin } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { phanAnhService, buildingService } from "../../services";
import { useAuthContext } from "../../contexts";
import { useModal } from "../../hooks";
import dayjs from "dayjs";

const { TextArea } = Input;
const { Option } = Select;

/**
 * Trang Phản ánh cho Accountant.
 * - Gửi phản ánh/báo lỗi cho Manager về các vấn đề thanh toán
 * - Xem danh sách phản ánh đã gửi
 * - Xem phản hồi từ Manager
 */
export default function AccountantFeedbackPage() {
  const { message } = App.useApp();
  const { user } = useAuthContext();
  const [loading, setLoading] = useState(false);
  const [phanAnhs, setPhanAnhs] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [selectedPhanAnh, setSelectedPhanAnh] = useState(null);
  const [phanHois, setPhanHois] = useState([]);
  const [buildings, setBuildings] = useState([]);
  
  const modal = useModal({
    toaNhaId: undefined,
    tieuDe: "",
    noiDung: "",
  });

  // Lấy danh sách tòa nhà (Accountant có thể thấy tất cả)
  useEffect(() => {
    const fetchBuildings = async () => {
      try {
        const response = await buildingService.getAllForDropdown();
        setBuildings(Array.isArray(response) ? response : []);
      } catch (error) {
        console.error("Error fetching buildings:", error);
        setBuildings([]);
      }
    };
    fetchBuildings();
  }, []);

  // Lấy phản ánh của user
  const fetchPhanAnhs = useCallback(async (page = 0, size = 10) => {
    setLoading(true);
    try {
      const response = await phanAnhService.getMyPhanAnh(page, size);
      const data = response?.content || response;
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
  }, [message]);

  useEffect(() => {
    fetchPhanAnhs();
  }, [fetchPhanAnhs]);

  // Lấy phản hồi khi chọn phản ánh
  useEffect(() => {
    const fetchPhanHois = async () => {
      if (selectedPhanAnh) {
        try {
          const data = await phanAnhService.getPhanHoi(selectedPhanAnh);
          setPhanHois(data);
        } catch (error) {
          console.error("Error fetching phan hoi:", error);
          setPhanHois([]);
        }
      }
    };
    fetchPhanHois();
  }, [selectedPhanAnh]);

  const handleTableChange = (paginationConfig) => {
    fetchPhanAnhs(paginationConfig.current - 1, paginationConfig.pageSize);
  };

  const handleSubmit = useCallback(async (values, editingId) => {
    if (!values.toaNhaId) {
      message.error("Vui lòng chọn tòa nhà");
      return;
    }
    
    await phanAnhService.create({
      toaNhaId: values.toaNhaId,
      tieuDe: values.tieuDe,
      noiDung: values.noiDung,
    });
    
    fetchPhanAnhs();
  }, [fetchPhanAnhs, message]);

  const getStatusTag = (status) => {
    const colorMap = {
      "Đã xử lý": "green",
      "Đang xử lý": "orange",
      "Chờ xử lý": "blue",
    };
    return <Tag color={colorMap[status] || "default"}>{status}</Tag>;
  };

  const columns = [
    { 
      title: "Tiêu đề", 
      dataIndex: "tieuDe",
      width: "35%",
    },
    { 
      title: "Tòa nhà", 
      dataIndex: ["toaNha", "tenToaNha"],
      width: "20%",
    },
    { 
      title: "Ngày gửi", 
      dataIndex: "ngayGui",
      width: "20%",
      render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm"),
    },
    { 
      title: "Trạng thái", 
      dataIndex: "trangThai",
      width: "15%",
      render: getStatusTag,
    },
    {
      title: "",
      width: "10%",
      render: (_, record) => (
        <a onClick={() => setSelectedPhanAnh(record.id)}>
          Xem chi tiết
        </a>
      ),
    },
  ];

  const currentPhanAnh = phanAnhs.find(pa => pa.id === selectedPhanAnh);

  return (
    <ContentCard
      title="Gửi phản ánh cho Quản lý"
      extra={
        <Button 
          type="primary" 
          icon={<PlusOutlined />} 
          onClick={() => modal.openModal({ 
            toaNhaId: buildings.length === 1 ? buildings[0].id : undefined 
          })}
        >
          Gửi phản ánh
        </Button>
      }
    >
      <Spin spinning={loading}>
        {phanAnhs.length === 0 && !loading ? (
          <Empty description="Bạn chưa gửi phản ánh nào" />
        ) : (
          <Table
            columns={columns}
            dataSource={phanAnhs}
            rowKey="id"
            pagination={{
              ...pagination,
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} phản ánh`,
            }}
            onChange={handleTableChange}
          />
        )}
      </Spin>

      {selectedPhanAnh && currentPhanAnh && (
        <Card 
          title="Chi tiết phản ánh" 
          style={{ marginTop: 24 }}
          extra={<a onClick={() => setSelectedPhanAnh(null)}>Đóng</a>}
        >
          <Descriptions bordered column={1}>
            <Descriptions.Item label="Tiêu đề">
              {currentPhanAnh.tieuDe}
            </Descriptions.Item>
            <Descriptions.Item label="Nội dung">
              <div style={{ whiteSpace: "pre-wrap" }}>
                {currentPhanAnh.noiDung}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="Tòa nhà">
              {currentPhanAnh.toaNha?.tenToaNha}
            </Descriptions.Item>
            <Descriptions.Item label="Ngày gửi">
              {dayjs(currentPhanAnh.ngayGui).format("DD/MM/YYYY HH:mm")}
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              {getStatusTag(currentPhanAnh.trangThai)}
            </Descriptions.Item>
          </Descriptions>

          <div style={{ marginTop: 24 }}>
            <h3>Phản hồi từ Quản lý:</h3>
            {phanHois.length > 0 ? (
              <Table
                columns={[
                  { 
                    title: "Nội dung", 
                    dataIndex: "noiDung",
                    width: "60%",
                  },
                  { 
                    title: "Người trả lời", 
                    dataIndex: "nguoiTraLoi",
                    width: "20%",
                  },
                  { 
                    title: "Ngày trả lời", 
                    dataIndex: "ngayTraLoi",
                    width: "20%",
                    render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm"),
                  },
                ]}
                dataSource={phanHois}
                rowKey="id"
                pagination={false}
                size="small"
              />
            ) : (
              <p style={{ color: "#8c8c8c", fontStyle: "italic" }}>
                Chưa có phản hồi
              </p>
            )}
          </div>
        </Card>
      )}

      <FeedbackFormModal 
        modal={modal} 
        onSubmit={handleSubmit} 
        buildings={buildings}
      />
    </ContentCard>
  );
}

function FeedbackFormModal({ modal, onSubmit, buildings }) {
  const { form, open, closeModal, handleSubmit, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, "Gửi phản ánh thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title="Gửi phản ánh cho Quản lý"
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="toaNhaId"
          label="Tòa nhà liên quan"
          rules={[{ required: true, message: "Vui lòng chọn tòa nhà" }]}
        >
          <Select placeholder="Chọn tòa nhà">
            {buildings.map((b) => (
              <Option key={b.id} value={b.id}>
                {b.tenToaNha}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="tieuDe"
          label="Tiêu đề"
          rules={[{ required: true, message: "Vui lòng nhập tiêu đề" }]}
        >
          <Input placeholder="Ví dụ: Lỗi thanh toán hộ A101, Cần hỗ trợ..." />
        </Form.Item>

        <Form.Item
          name="noiDung"
          label="Nội dung"
          rules={[{ required: true, message: "Vui lòng nhập nội dung" }]}
        >
          <TextArea 
            rows={6} 
            placeholder="Mô tả chi tiết vấn đề cần báo cáo cho Quản lý..." 
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
