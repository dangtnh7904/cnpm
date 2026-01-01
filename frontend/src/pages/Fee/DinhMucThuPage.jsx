import React, { useEffect, useCallback } from "react";
import { Button, message, Input, Select, Modal, Form, InputNumber } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { feeService, householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";

const { Option } = Select;

export default function DinhMucThuPage() {
  const { data: households, refetch: fetchHouseholds } = useFetch(householdService.getAll, false);
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = React.useState(null);
  const { data: dinhMucs, loading, refetch } = useFetch(
    () => selectedHoGiaDinh ? feeService.getDinhMucByHoGiaDinh(selectedHoGiaDinh) : Promise.resolve([]),
    false
  );
  const { data: loaiPhis } = useFetch(feeService.getActiveLoaiPhi, false);

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  useEffect(() => {
    if (selectedHoGiaDinh) {
      refetch();
    }
  }, [selectedHoGiaDinh, refetch]);

  const modal = useModal({
    idHoGiaDinh: undefined,
    idLoaiPhi: undefined,
    soLuong: 1,
    ghiChu: "",
  });

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      idHoGiaDinh: record.hoGiaDinh?.id,
      idLoaiPhi: record.loaiPhi?.id,
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await feeService.deleteDinhMuc(id);
      message.success("Đã xóa định mức thu");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại");
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    const payload = {
      ...values,
      hoGiaDinh: { id: values.idHoGiaDinh },
      loaiPhi: { id: values.idLoaiPhi },
    };
    
    if (editingId) {
      await feeService.updateDinhMuc(editingId, payload);
    } else {
      await feeService.createDinhMuc(payload);
    }
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Loại phí", render: (record) => record?.loaiPhi?.tenLoaiPhi || "" },
    { 
      title: "Số lượng", 
      dataIndex: "soLuong",
      render: (value, record) => `${value} ${record?.loaiPhi?.donViTinh || ""}`
    },
    { title: "Ghi chú", dataIndex: "ghiChu" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa định mức thu này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý định mức thu"
      extra={
        <>
          <Select
            style={{ width: 200, marginRight: 8 }}
            placeholder="Chọn hộ gia đình"
            onChange={setSelectedHoGiaDinh}
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
            Thêm định mức
          </Button>
        </>
      }
    >
      <DataTable columns={columns} dataSource={dinhMucs} loading={loading} />
      
      <DinhMucFormModal 
        modal={modal} 
        onSubmit={handleSubmit}
        households={households}
        loaiPhis={loaiPhis}
      />
    </ContentCard>
  );
}

function DinhMucFormModal({ modal, onSubmit, households, loaiPhis }) {
  const { form, open, closeModal, handleSubmit, isEditing, loading } = modal;

  const onFinish = async () => {
    const success = await handleSubmit(onSubmit, isEditing ? "Cập nhật thành công" : "Thêm thành công");
    if (success) {
      closeModal();
    }
  };

  return (
    <Modal
      title={isEditing ? "Sửa định mức thu" : "Thêm định mức thu"}
      open={open}
      onCancel={closeModal}
      onOk={onFinish}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="idHoGiaDinh"
          label="Hộ gia đình"
          rules={[{ required: true, message: "Vui lòng chọn hộ gia đình" }]}
        >
          <Select placeholder="Chọn hộ gia đình">
            {(Array.isArray(households) ? households : []).map((h) => (
              <Option key={h.id} value={h.id}>
                {h.maHoGiaDinh} - {h.tenChuHo}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="idLoaiPhi"
          label="Loại phí"
          rules={[{ required: true, message: "Vui lòng chọn loại phí" }]}
        >
          <Select placeholder="Chọn loại phí">
            {loaiPhis?.map((lp) => (
              <Option key={lp.id} value={lp.id}>
                {lp.tenLoaiPhi} ({lp.donGia?.toLocaleString('vi-VN')} đ/{lp.donViTinh || "đơn vị"})
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="soLuong"
          label="Số lượng"
          rules={[{ required: true, message: "Vui lòng nhập số lượng" }]}
        >
          <InputNumber min={0} step={0.1} style={{ width: "100%" }} />
        </Form.Item>

        <Form.Item name="ghiChu" label="Ghi chú">
          <Input.TextArea rows={3} placeholder="Ghi chú về định mức thu" />
        </Form.Item>
      </Form>
    </Modal>
  );
}

