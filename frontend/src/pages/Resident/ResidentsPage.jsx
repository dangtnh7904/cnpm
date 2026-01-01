import React, { useEffect, useCallback } from "react";
import { Button, message, Tag } from "antd";
import { PlusOutlined, HomeOutlined, BankOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { residentService, householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { parseDate, toDatePayload } from "../../utils";
import ResidentFormModal from "./ResidentFormModal";

export default function ResidentsPage() {
  const { data: residents, loading, refetch } = useFetch(residentService.getAll);
  const { data: householdOptions, refetch: fetchHouseholds } = useFetch(householdService.getOptions);
  const modal = useModal({
    hoTen: "",
    soCCCD: "",
    ngaySinh: null,
    gioiTinh: "Nam",
    quanHeVoiChuHo: "Chu ho",
    trangThai: "Hoat dong",
    soDienThoai: "",
    email: "",
    idHoGiaDinh: undefined,
  });

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      idHoGiaDinh: record?.hoGiaDinh?.id,
      ngaySinh: parseDate(record.ngaySinh),
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await residentService.delete(id);
      message.success("Đã xóa nhân khẩu");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại");
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      const payload = {
        ...values,
        ngaySinh: toDatePayload(values.ngaySinh),
        hoGiaDinh: values.idHoGiaDinh ? { id: values.idHoGiaDinh } : undefined,
      };
      
      if (editingId) {
        await residentService.update(editingId, payload);
        message.success("Cập nhật nhân khẩu thành công");
      } else {
        // Use the new API with hoGiaDinhId as query param
        await residentService.create(payload, values.idHoGiaDinh);
        message.success("Thêm nhân khẩu thành công");
      }
      refetch();
      return true;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Có lỗi xảy ra";
      message.error(errorMessage);
      throw error;
    }
  }, [refetch]);

  const columns = [
    { title: "Họ tên", dataIndex: "hoTen" },
    { title: "CCCD", dataIndex: "soCCCD" },
    { title: "Giới tính", dataIndex: "gioiTinh" },
    { title: "Trạng thái", dataIndex: "trangThai" },
    { title: "Quan hệ", dataIndex: "quanHeVoiChuHo" },
    { 
      title: "Phòng", 
      key: "room",
      render: (_, record) => {
        const soCanHo = record?.hoGiaDinh?.soCanHo;
        const soTang = record?.hoGiaDinh?.soTang;
        if (soCanHo) {
          return (
            <Tag icon={<HomeOutlined />} color="blue">
              {soCanHo}{soTang ? ` - Tầng ${soTang}` : ''}
            </Tag>
          );
        }
        return "-";
      }
    },
    { 
      title: "Tòa", 
      key: "building",
      render: (_, record) => {
        const tenToaNha = record?.hoGiaDinh?.toaNha?.tenToaNha;
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
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa nhân khẩu này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý nhân khẩu"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
          Thêm nhân khẩu
        </Button>
      }
    >
      <DataTable columns={columns} dataSource={Array.isArray(residents) ? residents : []} loading={loading} />
      
      <ResidentFormModal
        modal={modal}
        householdOptions={householdOptions}
        onSubmit={handleSubmit}
      />
    </ContentCard>
  );
}
