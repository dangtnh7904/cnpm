import React, { useCallback } from "react";
import { Button, message } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import HouseholdFormModal from "./HouseholdFormModal";

export default function HouseholdsPage() {
  const { data: households, loading, refetch } = useFetch(householdService.getAll);
  const modal = useModal({
    maHoGiaDinh: "",
    tenChuHo: "",
    soCanHo: "",
    soTang: null,
    dienTich: null,
    soDienThoaiLienHe: "",
    emailLienHe: "",
    trangThai: "Hoat dong",
  });

  const handleEdit = useCallback((record) => {
    modal.openModal(record);
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await householdService.delete(id);
      message.success("Đã xóa hộ gia đình");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại");
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    if (editingId) {
      await householdService.update(editingId, values);
    } else {
      await householdService.create(values);
    }
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Mã hộ", dataIndex: "maHoGiaDinh" },
    { title: "Chủ hộ", dataIndex: "tenChuHo" },
    { title: "Số căn hộ", dataIndex: "soCanHo" },
    { title: "Tầng", dataIndex: "soTang" },
    { title: "Diện tích", dataIndex: "dienTich" },
    { title: "Trạng thái", dataIndex: "trangThai" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa hộ gia đình này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý hộ gia đình"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
          Thêm hộ
        </Button>
      }
    >
      <DataTable columns={columns} dataSource={households} loading={loading} />
      
      <HouseholdFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}
