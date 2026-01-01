import React, { useCallback } from "react";
import { Button, message, Modal, Input, Typography } from "antd";
import { PlusOutlined, ExclamationCircleOutlined } from "@ant-design/icons";
import { ContentCard, ActionButtons, DataTable } from "../../components";
import { buildingService } from "../../services";
import { useFetch, useModal } from "../../hooks";
import BuildingFormModal from "./BuildingFormModal";

export default function BuildingsPage() {
  const { data: buildings, loading, refetch } = useFetch(buildingService.getAll);

  const [deleteModalVisible, setDeleteModalVisible] = React.useState(false);
  const [buildingToDelete, setBuildingToDelete] = React.useState(null);
  const [confirmName, setConfirmName] = React.useState("");

  const modal = useModal({
    tenToaNha: "",
    moTa: "",
  });

  const handleEdit = useCallback((record) => {
    modal.openModal(record);
  }, [modal]);

  const handleDeleteClick = (record) => {
    setBuildingToDelete(record);
    setConfirmName("");
    setDeleteModalVisible(true);
  };

  const handleConfirmDelete = async () => {
    if (!buildingToDelete) return;

    if (confirmName !== buildingToDelete.tenToaNha) {
      message.error("Tên tòa nhà không khớp!");
      return;
    }

    try {
      await buildingService.delete(buildingToDelete.id);
      message.success("Đã xóa tòa nhà");
      setDeleteModalVisible(false);
      setBuildingToDelete(null);
      refetch();
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Xóa thất bại";
      message.error(errorMessage);
    }
  };

  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      if (editingId) {
        await buildingService.update(editingId, values);
        message.success("Cập nhật tòa nhà thành công");
      } else {
        await buildingService.create(values);
        message.success("Thêm tòa nhà thành công");
      }
      await refetch();
      return true;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Có lỗi xảy ra";
      message.error(errorMessage);
      throw error;
    }
  }, [refetch]);

  const columns = [
    {
      title: "ID",
      dataIndex: "id",
      width: 80,
    },
    {
      title: "Tên tòa nhà",
      dataIndex: "tenToaNha",
    },
    {
      title: "Mô tả",
      dataIndex: "moTa",
      ellipsis: true,
    },
    {
      title: "Thao tác",
      width: 150,
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEdit(record)}
          onDelete={() => handleDeleteClick(record)}
          confirmDelete={false}
        />
      ),
    },
  ];

  return (
    <>
      <ContentCard
        title="Quản lý tòa nhà"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => modal.openModal()}>
            Thêm tòa nhà
          </Button>
        }
      >
        <DataTable
          columns={columns}
          dataSource={Array.isArray(buildings) ? buildings : []}
          loading={loading}
        />

        <BuildingFormModal modal={modal} onSubmit={handleSubmit} />
      </ContentCard>

      <Modal
        title={
          <span>
            <ExclamationCircleOutlined style={{ color: "#ff4d4f", marginRight: 8 }} />
            Xác nhận xóa tòa nhà
          </span>
        }
        open={deleteModalVisible}
        onOk={handleConfirmDelete}
        onCancel={() => {
          setDeleteModalVisible(false);
          setBuildingToDelete(null);
        }}
        okText="Xóa tòa nhà"
        okButtonProps={{ danger: true, disabled: confirmName !== buildingToDelete?.tenToaNha }}
        cancelText="Hủy"
      >
        <div style={{ marginBottom: 16 }}>
          <Typography.Text type="danger" strong>
            Cảnh báo: Hành động này sẽ xóa toàn bộ các hộ gia đình thuộc tòa nhà này và không thể hoàn tác!
          </Typography.Text>
        </div>
        <p>
          Vui lòng nhập chính xác tên tòa nhà <strong>{buildingToDelete?.tenToaNha}</strong> để xác nhận xóa:
        </p>
        <Input
          value={confirmName}
          onChange={(e) => setConfirmName(e.target.value)}
          placeholder="Nhập tên tòa nhà..."
          status={confirmName && confirmName !== buildingToDelete?.tenToaNha ? "error" : ""}
        />
      </Modal>
    </>
  );
}
