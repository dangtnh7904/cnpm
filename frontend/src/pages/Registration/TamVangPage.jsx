import React, { useEffect, useCallback } from "react";
import { Button, message, Input } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { tamVangService, residentService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { parseDate, toDatePayload } from "../../utils";
import TamVangFormModal from "./TamVangFormModal";

export default function TamVangPage() {
  const { data: records, loading, refetch } = useFetch(tamVangService.getAll);
  const { data: residentOptions, refetch: fetchResidents } = useFetch(residentService.getOptions);
  const modal = useModal({
    idNhanKhau: undefined,
    noiDen: "",
    lyDo: "",
    ngayBatDau: null,
    ngayKetThuc: null,
  });

  useEffect(() => {
    fetchResidents();
  }, [fetchResidents]);

  const handleSearch = useCallback((value) => {
    refetch({ noiDen: value });
  }, [refetch]);

  const handleEdit = useCallback((record) => {
    modal.openModal({
      ...record,
      idNhanKhau: record?.nhanKhau?.id,
      ngayBatDau: parseDate(record.ngayBatDau),
      ngayKetThuc: parseDate(record.ngayKetThuc),
    });
  }, [modal]);

  const handleDelete = useCallback(async (id) => {
    try {
      await tamVangService.delete(id);
      message.success("Đã xóa hồ sơ tạm vắng");
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
      nhanKhau: values.idNhanKhau ? { id: values.idNhanKhau } : undefined,
    };

    if (editingId) {
      await tamVangService.update(editingId, payload);
    } else {
      await tamVangService.create(payload);
    }
    refetch();
  }, [refetch]);

  const columns = [
    { title: "Nhân khẩu", render: (record) => record?.nhanKhau?.hoTen || "" },
    { title: "Nơi đến", dataIndex: "noiDen" },
    { title: "Bắt đầu", dataIndex: "ngayBatDau" },
    { title: "Kết thúc", dataIndex: "ngayKetThuc" },
    {
      title: "Thao tác",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDelete(record.id)}
          deleteTitle="Xóa hồ sơ tạm vắng này?"
        />
      ),
    },
  ];

  return (
    <ContentCard
      title="Quản lý tạm vắng"
      extra={
        <div style={{ display: 'flex', gap: 10 }}>
          <Input.Search
            placeholder="Tìm theo nơi đến..."
            onSearch={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
            Thêm tạm vắng
          </Button>
        </div>
      }
    >
      <DataTable columns={columns} dataSource={records} loading={loading} />

      <TamVangFormModal
        modal={modal}
        residentOptions={residentOptions}
        onSubmit={handleSubmit}
      />
    </ContentCard>
  );
}
