import React, { useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button, message, Space, Tooltip, Tag } from "antd";
import { PlusOutlined, EyeOutlined, BankOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { householdService, buildingService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import HouseholdFormModal from "./HouseholdFormModal";

export default function HouseholdsPage() {
  const navigate = useNavigate();
  const { data: households, loading, refetch } = useFetch(householdService.getAll);
  const { data: buildingOptions, refetch: fetchBuildings } = useFetch(buildingService.getOptions);
  
  const modal = useModal({
    maHoGiaDinh: "",
    tenChuHo: "",
    soCanHo: "",
    soTang: null,
    dienTich: null,
    soDienThoaiLienHe: "",
    emailLienHe: "",
    trangThai: "Hoat dong",
    idToaNha: undefined,
  });

  useEffect(() => {
    fetchBuildings();
  }, [fetchBuildings]);

  // Navigate to apartment detail page
  const handleViewDetails = useCallback((record) => {
    navigate(`/apartments/${record.id}`);
  }, [navigate]);

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      idToaNha: record?.toaNha?.id,
    });
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
      // Transform idToaNha to toaNha object for backend
      const payload = {
        ...values,
        toaNha: values.idToaNha ? { id: values.idToaNha } : null,
      };
      delete payload.idToaNha;

      if (editingId) {
        await householdService.update(editingId, payload);
        message.success("Cập nhật hộ gia đình thành công");
      } else {
        await householdService.create(payload);
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
    { 
      title: "Tòa nhà", 
      key: "building",
      render: (_, record) => {
        const tenToaNha = record?.toaNha?.tenToaNha;
        if (tenToaNha) {
          return (
            <Tag icon={<BankOutlined />} color="green">
              {tenToaNha}
            </Tag>
          );
        }
        return "-";
      }
    },
    { title: "Diện tích", dataIndex: "dienTich" },
    { title: "Trạng thái", dataIndex: "trangThai" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="Xem chi tiết">
            <Button 
              type="primary" 
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetails(record)}
            >
              Chi tiết
            </Button>
          </Tooltip>
          <ActionButtons
            onEdit={() => handleEdit(record)}
            onDelete={() => handleDelete(record.id)}
            deleteTitle="Xóa hộ gia đình này?"
          />
        </Space>
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
      
      <HouseholdFormModal 
        modal={modal} 
        onSubmit={handleSubmit} 
        buildingOptions={buildingOptions}
      />
    </ContentCard>
  );
}
