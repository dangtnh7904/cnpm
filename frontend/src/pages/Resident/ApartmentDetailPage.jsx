import React, { useState, useCallback, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { 
  Card, 
  Descriptions, 
  Table, 
  Button, 
  Space, 
  Tag, 
  message, 
  Spin,
  Typography,
  Divider,
  Empty
} from "antd";
import { 
  PlusOutlined, 
  ArrowLeftOutlined, 
  HomeOutlined, 
  UserOutlined,
  PhoneOutlined,
  MailOutlined
} from "@ant-design/icons";
import { householdService, residentService } from "../../services";
import { ActionButtons } from "../../components";
import { useModal } from "../../hooks";
import ResidentFormModal from "./ResidentFormModal";
import dayjs from "dayjs";
import { DATE_FORMAT } from "../../constants";

const { Title, Text } = Typography;

export default function ApartmentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [apartment, setApartment] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Modal for adding/editing resident
  const modal = useModal({
    hoTen: "",
    soCCCD: "",
    ngaySinh: null,
    gioiTinh: "Nam",
    quanHeVoiChuHo: "",
    trangThai: "Hoat dong",
    soDienThoai: "",
    email: "",
  });

  // Fetch apartment details (includes residents list from backend)
  const fetchApartment = useCallback(async () => {
    try {
      setLoading(true);
      const data = await householdService.getById(id);
      setApartment(data);
    } catch (error) {
      console.error("Error fetching apartment:", error);
      message.error("Không thể tải thông tin căn hộ");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchApartment();
  }, [fetchApartment]);

  // Handle adding a new resident
  const handleAddResident = useCallback(() => {
    modal.openModal();
  }, [modal]);

  // Handle editing a resident
  const handleEditResident = useCallback((record) => {
    modal.openModal({
      ...record,
      ngaySinh: record.ngaySinh ? dayjs(record.ngaySinh) : null,
    });
  }, [modal]);

  // Handle deleting a resident
  const handleDeleteResident = useCallback(async (residentId) => {
    try {
      await residentService.delete(residentId);
      message.success("Đã xóa nhân khẩu");
      fetchApartment(); // Refresh data
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Xóa thất bại";
      message.error(errorMessage);
    }
  }, [fetchApartment]);

  // Handle form submission (create/update resident)
  const handleSubmit = useCallback(async (values, editingId) => {
    try {
      // Format date properly
      const payload = {
        ...values,
        ngaySinh: values.ngaySinh ? values.ngaySinh.format(DATE_FORMAT) : null,
      };

      if (editingId) {
        // Update existing resident
        await residentService.update(editingId, payload);
        message.success("Cập nhật nhân khẩu thành công");
      } else {
        // Create new resident with apartmentId as query param
        await residentService.create(payload, id);
        message.success("Thêm nhân khẩu thành công");
      }
      
      // Refresh apartment data to get updated residents list
      await fetchApartment();
      return true;
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || "Có lỗi xảy ra";
      message.error(errorMessage);
      throw error;
    }
  }, [id, fetchApartment]);

  // Get status tag color
  const getStatusColor = (status) => {
    switch (status) {
      case "Đang ở":
      case "Hoat dong":
        return "green";
      case "Tam dung":
        return "orange";
      case "Tam vang":
        return "red";
      default:
        return "default";
    }
  };

  // Resident table columns
  const residentColumns = [
    {
      title: "Họ tên",
      dataIndex: "hoTen",
      key: "hoTen",
      render: (text, record) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
          {record.laChuHo && <Tag color="gold">Chủ hộ</Tag>}
        </Space>
      ),
    },
    {
      title: "CCCD",
      dataIndex: "soCCCD",
      key: "soCCCD",
    },
    {
      title: "Ngày sinh",
      dataIndex: "ngaySinh",
      key: "ngaySinh",
      render: (date) => date ? dayjs(date).format("DD/MM/YYYY") : "-",
    },
    {
      title: "Giới tính",
      dataIndex: "gioiTinh",
      key: "gioiTinh",
    },
    {
      title: "Quan hệ với chủ hộ",
      dataIndex: "quanHeVoiChuHo",
      key: "quanHeVoiChuHo",
      render: (text) => text || "-",
    },
    {
      title: "Số điện thoại",
      dataIndex: "soDienThoai",
      key: "soDienThoai",
      render: (phone) => phone || "-",
    },
    {
      title: "Trạng thái",
      dataIndex: "trangThai",
      key: "trangThai",
      render: (status) => (
        <Tag color={getStatusColor(status)}>{status || "Hoạt động"}</Tag>
      ),
    },
    {
      title: "Thao tác",
      key: "actions",
      render: (_, record) => (
        <ActionButtons
          onEdit={() => handleEditResident(record)}
          onDelete={() => handleDeleteResident(record.id)}
          deleteTitle="Xóa nhân khẩu này?"
        />
      ),
    },
  ];

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: 400 }}>
        <Spin size="large" tip="Đang tải..." />
      </div>
    );
  }

  if (!apartment) {
    return (
      <Card>
        <Empty description="Không tìm thấy căn hộ" />
        <div style={{ textAlign: "center", marginTop: 16 }}>
          <Button onClick={() => navigate("/households")}>
            <ArrowLeftOutlined /> Quay lại
          </Button>
        </div>
      </Card>
    );
  }

  const residents = apartment.danhSachNhanKhau || [];

  return (
    <div style={{ padding: "0" }}>
      {/* Back Button */}
      <Button 
        type="link" 
        icon={<ArrowLeftOutlined />} 
        onClick={() => navigate("/households")}
        style={{ marginBottom: 16, padding: 0 }}
      >
        Quay lại danh sách căn hộ
      </Button>

      {/* Apartment Info Card */}
      <Card 
        style={{ marginBottom: 24 }}
        title={
          <Space>
            <HomeOutlined style={{ fontSize: 20 }} />
            <Title level={4} style={{ margin: 0 }}>
              {apartment.toaNha?.tenToaNha && `${apartment.toaNha.tenToaNha} - `}Căn hộ {apartment.soCanHo} - Tầng {apartment.soTang}
            </Title>
          </Space>
        }
        extra={
          <Tag color={getStatusColor(apartment.trangThai)} style={{ fontSize: 14 }}>
            {apartment.trangThai || "Chưa xác định"}
          </Tag>
        }
      >
        <Descriptions column={{ xs: 1, sm: 2, md: 3 }} bordered size="small">
          <Descriptions.Item label="Tòa nhà">
            <Text strong>{apartment.toaNha?.tenToaNha || "Chưa gán"}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Mã hộ gia đình">
            <Text strong>{apartment.maHoGiaDinh}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Chủ hộ">
            <Text strong>{apartment.tenChuHo || "Chưa có"}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Diện tích">
            {apartment.dienTich ? `${apartment.dienTich} m²` : "-"}
          </Descriptions.Item>
          <Descriptions.Item label={<><PhoneOutlined /> Điện thoại</>}>
            {apartment.soDienThoaiLienHe || "-"}
          </Descriptions.Item>
          <Descriptions.Item label={<><MailOutlined /> Email</>}>
            {apartment.emailLienHe || "-"}
          </Descriptions.Item>
          <Descriptions.Item label="Số nhân khẩu">
            <Tag color="blue">{residents.length} người</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Residents List Card */}
      <Card
        title={
          <Space>
            <UserOutlined style={{ fontSize: 18 }} />
            <span>Danh sách nhân khẩu</span>
          </Space>
        }
        extra={
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={handleAddResident}
          >
            Thêm nhân khẩu
          </Button>
        }
      >
        <Table
          columns={residentColumns}
          dataSource={residents}
          rowKey="id"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} nhân khẩu`,
          }}
          locale={{
            emptyText: (
              <Empty 
                description="Chưa có nhân khẩu nào" 
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            ),
          }}
        />
      </Card>

      {/* Resident Form Modal - apartmentId is auto-locked */}
      <ResidentFormModal 
        modal={modal} 
        onSubmit={handleSubmit}
        apartmentId={id}
        apartmentInfo={apartment}
      />
    </div>
  );
}
