import React, { useEffect, useCallback } from "react";
import { Button, message, Input, Tag, Tooltip } from "antd";
import { PlusOutlined, CheckCircleOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { tamTruService, householdService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import { parseDate, toDatePayload } from "../../utils";
import TamTruFormModal from "./TamTruFormModal";

export default function TamTruPage() {
  const { data: records, loading, refetch } = useFetch(tamTruService.getAll);
  const { data: householdOptions, refetch: fetchHouseholds } = useFetch(householdService.getOptions);
  
  // Initial values khớp với DangKyTamTruDTO
  const modal = useModal({
    hoTen: "",
    soCCCD: "",
    ngaySinh: null,
    gioiTinh: undefined,
    soDienThoai: "",
    email: "",
    quanHeVoiChuHo: undefined,
    hoGiaDinhId: undefined,
    diaChiThuongTru: "",
    maGiayTamTru: "",
    ngayBatDau: null,
    ngayKetThuc: null,
    lyDo: "",
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
      hoGiaDinhId: record?.nhanKhau?.hoGiaDinh?.id,
      hoTen: record?.nhanKhau?.hoTen,
      soCCCD: record?.nhanKhau?.soCCCD,
      ngaySinh: parseDate(record?.nhanKhau?.ngaySinh),
      gioiTinh: record?.nhanKhau?.gioiTinh,
      soDienThoai: record?.nhanKhau?.soDienThoai,
      email: record?.nhanKhau?.email,
      quanHeVoiChuHo: record?.nhanKhau?.quanHeVoiChuHo,
      ngayBatDau: parseDate(record.ngayBatDau),
      ngayKetThuc: parseDate(record.ngayKetThuc),
    });
  }, [modal]);

  const handleHuyTamTru = useCallback(async (id) => {
    try {
      await tamTruService.huyTamTru(id);
      message.success("Đã hủy tạm trú thành công");
      refetch();
    } catch (error) {
      const errorMsg = error.response?.data?.message || "Hủy tạm trú thất bại";
      message.error(errorMsg);
    }
  }, [refetch]);

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
    try {
      // Payload khớp với DangKyTamTruDTO
      const payload = {
        hoGiaDinhId: values.hoGiaDinhId,
        hoTen: values.hoTen,
        soCCCD: values.soCCCD,
        ngaySinh: toDatePayload(values.ngaySinh),
        gioiTinh: values.gioiTinh,
        soDienThoai: values.soDienThoai || null,
        email: values.email || null,
        quanHeVoiChuHo: values.quanHeVoiChuHo,
        diaChiThuongTru: values.diaChiThuongTru,
        maGiayTamTru: values.maGiayTamTru || null,
        ngayBatDau: toDatePayload(values.ngayBatDau),
        ngayKetThuc: toDatePayload(values.ngayKetThuc),
        lyDo: values.lyDo,
      };

      if (editingId) {
        await tamTruService.update(editingId, payload);
        message.success("Cập nhật thành công");
      } else {
        await tamTruService.create(payload);
        message.success("Đăng ký tạm trú thành công");
      }
      refetch();
      return true;
    } catch (error) {
      // Hiển thị lỗi chi tiết
      const errorData = error.response?.data;
      if (errorData?.errors && Array.isArray(errorData.errors)) {
        // Hiển thị từng lỗi
        errorData.errors.forEach(err => message.error(err));
      } else if (errorData?.message) {
        message.error(errorData.message);
      } else {
        message.error("Có lỗi xảy ra");
      }
      throw error;
    }
  }, [refetch]);

  const columns = [
    { 
      title: "Họ tên", 
      key: "hoTen",
      render: (_, record) => record?.nhanKhau?.hoTen || record?.hoTen || "-"
    },
    { 
      title: "CCCD", 
      key: "soCCCD",
      render: (_, record) => record?.nhanKhau?.soCCCD || record?.soCCCD || "-"
    },
    { 
      title: "Quan hệ", 
      key: "quanHe",
      render: (_, record) => record?.nhanKhau?.quanHeVoiChuHo || "-"
    },
    { title: "Từ ngày", dataIndex: "ngayBatDau" },
    { title: "Đến ngày", dataIndex: "ngayKetThuc", render: (v) => v || "Chưa xác định" },
    { 
      title: "Hộ gia đình", 
      key: "hoGiaDinh",
      render: (_, record) => record?.nhanKhau?.hoGiaDinh?.maHoGiaDinh || "-" 
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 4 }}>
          <Tooltip title="Hủy tạm trú (người đã rời đi)">
            <Button 
              size="small" 
              type="primary" 
              ghost
              icon={<CheckCircleOutlined />}
              onClick={() => handleHuyTamTru(record.id)}
            >
              Hủy
            </Button>
          </Tooltip>
          <ActionButtons
            onEdit={() => handleEdit(record)}
            onDelete={() => handleDelete(record.id)}
            deleteTitle="Xóa hồ sơ tạm trú này?"
          />
        </div>
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
            Đăng ký tạm trú
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
