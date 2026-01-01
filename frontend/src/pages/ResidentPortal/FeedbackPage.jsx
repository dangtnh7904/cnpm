import React, { useState, useEffect, useCallback } from "react";
import { Button, Input, Modal, Form, Table, Tag, Card, Descriptions, Select, message } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard } from "../../components";
import { phanAnhService, householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import dayjs from "dayjs";

const { TextArea } = Input;
const { Option } = Select;

export default function FeedbackPage() {
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = useState(null);
  const [selectedPhanAnh, setSelectedPhanAnh] = useState(null);
  const { data: households, refetch: fetchHouseholds } = useFetch(householdService.getAll, false);
  const { data: phanAnhs, loading, refetch } = useFetch(
    () => selectedHoGiaDinh ? phanAnhService.getByHoGiaDinh(selectedHoGiaDinh) : Promise.resolve([]),
    false
  );
  const { data: phanHois, refetch: fetchPhanHois } = useFetch(
    () => selectedPhanAnh ? phanAnhService.getPhanHoi(selectedPhanAnh) : Promise.resolve([]),
    false
  );

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  useEffect(() => {
    if (selectedHoGiaDinh) {
      refetch();
    }
  }, [selectedHoGiaDinh, refetch]);

  useEffect(() => {
    if (selectedPhanAnh) {
      fetchPhanHois();
    }
  }, [selectedPhanAnh, fetchPhanHois]);

  const modal = useModal({
    idHoGiaDinh: undefined,
    tieuDe: "",
    noiDung: "",
  });

  const handleSubmit = useCallback(async (values, editingId) => {
    if (!values.idHoGiaDinh) {
      message.error("Vui lòng chọn hộ gia đình");
      return;
    }
    
    const payload = {
      tieuDe: values.tieuDe,
      noiDung: values.noiDung,
      hoGiaDinh: { id: values.idHoGiaDinh },
    };
    
    await phanAnhService.create(payload);
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Tiêu đề", dataIndex: "tieuDe" },
    { 
      title: "Ngày gửi", 
      dataIndex: "ngayGui",
      render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm")
    },
    { 
      title: "Trạng thái", 
      dataIndex: "trangThai",
      render: (status) => {
        const color = status === "Đã xong" ? "green" : status === "Đang xử lý" ? "orange" : "blue";
        return <Tag color={color}>{status}</Tag>;
      }
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedPhanAnh(record.id)}>
          Xem chi tiết
        </Button>
      ),
    },
  ];

  return (
    <ContentCard
      title="Phản ánh và phản hồi"
      extra={
        <>
          <Select
            style={{ width: 200, marginRight: 8 }}
            placeholder="Chọn hộ gia đình"
            onChange={(value) => {
              setSelectedHoGiaDinh(value);
              setSelectedPhanAnh(null);
            }}
            value={selectedHoGiaDinh}
          >
            {(Array.isArray(households) ? households : []).map((h) => (
              <Option key={h.id} value={h.id}>
                {h.maHoGiaDinh} - {h.tenChuHo}
              </Option>
            ))}
          </Select>
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={() => modal.openModal({ idHoGiaDinh: selectedHoGiaDinh })}
            disabled={!selectedHoGiaDinh}
          >
            Gửi phản ánh
          </Button>
        </>
      }
    >
      <Table
        columns={columns}
        dataSource={Array.isArray(phanAnhs) ? phanAnhs : []}
        loading={loading}
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />

      {selectedPhanAnh && (
        <Card title="Chi tiết phản ánh" style={{ marginTop: 24 }}>
          {phanAnhs?.find(pa => pa.id === selectedPhanAnh) && (
            <>
              <Descriptions bordered>
                <Descriptions.Item label="Tiêu đề" span={2}>
                  {phanAnhs.find(pa => pa.id === selectedPhanAnh).tieuDe}
                </Descriptions.Item>
                <Descriptions.Item label="Nội dung" span={2}>
                  {phanAnhs.find(pa => pa.id === selectedPhanAnh).noiDung}
                </Descriptions.Item>
                <Descriptions.Item label="Trạng thái">
                  <Tag color={phanAnhs.find(pa => pa.id === selectedPhanAnh).trangThai === "Đã xong" ? "green" : "orange"}>
                    {phanAnhs.find(pa => pa.id === selectedPhanAnh).trangThai}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>

              <div style={{ marginTop: 24 }}>
                <h3>Phản hồi từ Ban quản trị:</h3>
                {phanHois && phanHois.length > 0 ? (
                  <Table
                    columns={[
                      { title: "Nội dung", dataIndex: "noiDung" },
                      { title: "Người trả lời", dataIndex: "nguoiTraLoi" },
                      { 
                        title: "Ngày trả lời", 
                        dataIndex: "ngayTraLoi",
                        render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm")
                      },
                    ]}
                    dataSource={Array.isArray(phanHois) ? phanHois : []}
                    rowKey="id"
                    pagination={false}
                  />
                ) : (
                  <p style={{ color: "#8c8c8c" }}>Chưa có phản hồi</p>
                )}
              </div>
            </>
          )}
        </Card>
      )}

      <FeedbackFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}

function FeedbackFormModal({ modal, onSubmit }) {
  const { form, open, closeModal, handleSubmit, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, "Gửi phản ánh thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title="Gửi phản ánh"
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="idHoGiaDinh"
          hidden
        >
          <Input type="hidden" />
        </Form.Item>

        <Form.Item
          name="tieuDe"
          label="Tiêu đề"
          rules={[{ required: true, message: "Vui lòng nhập tiêu đề" }]}
        >
          <Input placeholder="Tiêu đề phản ánh" />
        </Form.Item>

        <Form.Item
          name="noiDung"
          label="Nội dung"
          rules={[{ required: true, message: "Vui lòng nhập nội dung" }]}
        >
          <TextArea rows={6} placeholder="Mô tả chi tiết vấn đề cần phản ánh" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

