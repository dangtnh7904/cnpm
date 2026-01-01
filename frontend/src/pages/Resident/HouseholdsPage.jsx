import React, { useCallback, useEffect } from "react";
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
      // Wait a bit before refetching to ensure backend has processed the delete
      setTimeout(() => {
        refetch();
      }, 100);
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Xóa thất bại";
      message.error(errorMessage);
      console.error("Delete error:", error);
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      if (editingId) {
        await householdService.update(editingId, values);
        message.success("Cập nhật hộ gia đình thành công");
      } else {
        await householdService.create(values);
        message.success("Thêm hộ gia đình thành công");
      }
      // Refetch immediately
      await refetch();
      return true;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Có lỗi xảy ra";
      message.error(errorMessage);
      throw error; // Re-throw để useModal biết có lỗi
    }
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
      <DataTable columns={columns} dataSource={Array.isArray(households) ? households : []} loading={loading} />
      
      <HouseholdFormModal modal={modal} onSubmit={handleSubmit} />
    </ContentCard>
  );
}
