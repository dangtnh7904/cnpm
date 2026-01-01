import React, { useEffect, useCallback } from "react";
import { Button, message, Input } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { tamTruService, householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { parseDate, toDatePayload } from "../../utils";
import TamTruFormModal from "./TamTruFormModal";

export default function TamTruPage() {
  const { data: records, loading, refetch } = useFetch(tamTruService.getAll);
  const { data: householdOptions, refetch: fetchHouseholds } = useFetch(householdService.getOptions);
  const modal = useModal({
    hoTen: "",
    soCCCD: "",
    soDienThoai: "",
    ngayBatDau: null,
    ngayKetThuc: null,
    lyDo: "",
    idHoGiaDinh: undefined,
  });

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  const handleSearch = useCallback((value) => {
    refetch({ hoTen: value });
  }, [refetch]);

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      idHoGiaDinh: record?.hoGiaDinh?.id,
      ngayBatDau: parseDate(record.ngayBatDau),
      ngayKetThuc: parseDate(record.ngayKetThuc),
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await tamTruService.delete(id);
      message.success("Đã xóa hồ sơ tạm trú");
      refetch();
    } catch (error) {
      message.error("Xóa thất bại");
    }
  }, [refetch]);

  const handleSubmit = useCallback(async (values, editingId) => {
    const payload = {
      ...values,
      ngayBatDau: toDatePayload(values.ngayBatDau),
      ngayKetThuc: toDatePayload(values.ngayKetThuc),
      hoGiaDinh: values.idHoGiaDinh ? { id: values.idHoGiaDinh } : undefined,
    };

    if (editingId) {
      await tamTruService.update(editingId, payload);
    } else {
      await tamTruService.create(payload);
    }
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Họ tên", dataIndex: "hoTen" },
    { title: "CCCD", dataIndex: "soCCCD" },
    { title: "Bắt đầu", dataIndex: "ngayBatDau" },
    { title: "Kết thúc", dataIndex: "ngayKetThuc" },
    { title: "Hộ", render: (record) => record?.hoGiaDinh?.maHoGiaDinh || "" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa hồ sơ tạm trú này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý tạm trú"
      extra={
        <div style={{ display: 'flex', gap: 10 }}>
          <Input.Search
            placeholder="Tìm theo họ tên..."
            onSearch={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
            Thêm tạm trú
          </Button>
        </div>
      }
    >
      <DataTable columns={columns} dataSource={records} loading={loading} />

      <TamTruFormModal
        modal={modal}
        householdOptions={householdOptions}
        onSubmit={handleSubmit}
      />
    </ContentCard>
  );
}
