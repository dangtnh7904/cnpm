import React, { useEffect, useState } from "react";
import { Row, Col, Card, Statistic, Button, Empty, Spin, Alert } from "antd";
import { TeamOutlined, HomeOutlined, UserSwitchOutlined, BankOutlined, PlusOutlined, DollarOutlined, FileTextOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { ContentCard, EmptyBuildingState } from "../../components";
import { householdService, residentService, tamTruService, tamVangService, buildingService, invoiceService } from "../../services";
import { useAuthContext } from "../../contexts";

export default function HomePage() {
  const navigate = useNavigate();
  const { user, isAdmin, isManager, isResident, isAccountant } = useAuthContext();
  const [stats, setStats] = useState({
    households: 0,
    residents: 0,
    tamTru: 0,
    tamVang: 0,
  });
  const [loading, setLoading] = useState(true);
  
  // State cho Manager Dashboard
  const [buildings, setBuildings] = useState([]);
  
  // State cho Resident Dashboard
  const [residentInfo, setResidentInfo] = useState(null);
  const [pendingInvoices, setPendingInvoices] = useState([]);

  // Redirect Resident và Accountant đến trang phù hợp
  useEffect(() => {
    if (isResident) {
      navigate("/resident/notifications", { replace: true });
    } else if (isAccountant) {
      navigate("/payment/update", { replace: true });
    }
  }, [isResident, isAccountant, navigate]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        if (isAdmin || isManager || isAccountant) {
          // Fetch thống kê cho Admin/Manager/Accountant
          // MANAGER/ACCOUNTANT chỉ thấy HGĐ và Nhân khẩu (đã được backend lọc theo tòa nhà)
          // ADMIN thấy thêm Tạm trú/Tạm vắng
          const basePromises = [
            householdService.getTotalCount().catch(() => 0),
            residentService.getTotalCount().catch(() => 0),
          ];
          
          // Chỉ ADMIN mới fetch Tạm trú/Tạm vắng
          const adminPromises = isAdmin ? [
            tamTruService.getTotalCount().catch(() => 0),
            tamVangService.getTotalCount().catch(() => 0),
          ] : [Promise.resolve(0), Promise.resolve(0)];

          const [households, residents, tamTru, tamVang] = await Promise.all([...basePromises, ...adminPromises]);
          setStats({ households, residents, tamTru, tamVang });
          
          // Fetch danh sách tòa nhà cho Manager
          if (isManager || isAdmin) {
            const buildingsData = await buildingService.getAllForDropdown();
            setBuildings(Array.isArray(buildingsData) ? buildingsData : []);
          }
        } else if (isResident) {
          // Fetch thông tin cho Cư dân
          // TODO: Implement resident-specific data fetching
          // const userHoGiaDinh = user?.hoGiaDinh;
          // if (userHoGiaDinh) {
          //   const invoices = await invoiceService.getByHoGiaDinh(userHoGiaDinh.id);
          //   setPendingInvoices(invoices.filter(inv => inv.trangThai !== 'DaThanhToan'));
          // }
        }
      } catch (error) {
        console.error("Failed to fetch data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAdmin, isManager, isAccountant, isResident, user]);

  // Dashboard cho Cư dân
  if (isResident) {
    const hoGiaDinh = user?.hoGiaDinh;
    
    if (!hoGiaDinh) {
      return (
        <ContentCard title="Trang chủ Cư dân">
          <Alert
            message="Tài khoản chưa được gán vào hộ gia đình"
            description="Vui lòng liên hệ Ban quản lý để được gán vào căn hộ của bạn."
            type="warning"
            showIcon
            style={{ marginBottom: 24 }}
          />
          <Card style={{ textAlign: 'center', padding: 40 }}>
            <Empty description="Bạn chưa được gán vào hộ gia đình nào" />
          </Card>
        </ContentCard>
      );
    }

    return (
      <ContentCard title="Trang chủ Cư dân">
        <Row gutter={[16, 16]}>
          <Col xs={24} md={12}>
            <Card title="Thông tin căn hộ" style={{ height: '100%' }}>
              <p><strong>Mã căn hộ:</strong> {hoGiaDinh.maHoGiaDinh}</p>
              <p><strong>Chủ hộ:</strong> {hoGiaDinh.tenChuHo}</p>
              <p><strong>Số phòng:</strong> {hoGiaDinh.soCanHo}</p>
              <p><strong>Tầng:</strong> {hoGiaDinh.soTang}</p>
              <p><strong>Diện tích:</strong> {hoGiaDinh.dienTich} m²</p>
            </Card>
          </Col>
          <Col xs={24} md={12}>
            <Card title="Hóa đơn chờ thanh toán" style={{ height: '100%' }}>
              {pendingInvoices.length > 0 ? (
                pendingInvoices.map(inv => (
                  <div key={inv.id} style={{ marginBottom: 8 }}>
                    <span>{inv.dotThu?.tenDotThu}</span>
                    <span style={{ float: 'right', color: '#ff4d4f' }}>
                      {new Intl.NumberFormat('vi-VN').format(inv.tongTienPhaiThu - inv.soTienDaDong)} đ
                    </span>
                  </div>
                ))
              ) : (
                <Empty description="Không có hóa đơn chờ thanh toán" />
              )}
            </Card>
          </Col>
        </Row>
        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={12} md={6}>
            <Button type="primary" block icon={<DollarOutlined />} onClick={() => navigate('/resident/payment-history')}>
              Lịch sử thanh toán
            </Button>
          </Col>
          <Col xs={12} md={6}>
            <Button block icon={<FileTextOutlined />} onClick={() => navigate('/resident/feedback')}>
              Gửi phản ánh
            </Button>
          </Col>
        </Row>
      </ContentCard>
    );
  }

  // Dashboard cho Admin/Manager/Accountant
  const statCards = [
    {
      title: "Tòa nhà",
      value: buildings.length,
      icon: <BankOutlined style={{ fontSize: 32, color: "#8b5cf6" }} />,
      color: "#8b5cf6",
      show: isAdmin || isManager,
    },
    {
      title: "Hộ gia đình",
      value: stats.households,
      icon: <HomeOutlined style={{ fontSize: 32, color: "#3b82f6" }} />,
      color: "#3b82f6",
      show: true,
    },
    {
      title: "Nhân khẩu",
      value: stats.residents,
      icon: <TeamOutlined style={{ fontSize: 32, color: "#10b981" }} />,
      color: "#10b981",
      show: true,
    },
    {
      title: "Tạm trú",
      value: stats.tamTru,
      icon: <UserSwitchOutlined style={{ fontSize: 32, color: "#f59e0b" }} />,
      color: "#f59e0b",
      show: isAdmin, // Chỉ ADMIN thấy
    },
    {
      title: "Tạm vắng",
      value: stats.tamVang,
      icon: <UserSwitchOutlined style={{ fontSize: 32, color: "#ef4444", transform: "rotate(180deg)" }} />,
      color: "#ef4444",
      show: isAdmin, // Chỉ ADMIN thấy
    },
  ].filter(s => s.show);

  // Tiêu đề Dashboard theo role
  const dashboardTitle = isAdmin 
    ? "Tổng quan hệ thống" 
    : isManager 
      ? "Tổng quan tòa nhà của bạn"
      : "Tổng quan";

  return (
    <ContentCard title={dashboardTitle}>
      {loading ? (
        <div style={{ textAlign: 'center', padding: 50 }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Row gutter={[16, 16]}>
            {statCards.map((stat) => (
              <Col xs={24} sm={12} lg={6} key={stat.title}>
                <Card
                  style={{
                    background: "rgba(255, 255, 255, 0.02)",
                    border: "1px solid rgba(255, 255, 255, 0.1)",
                    borderRadius: 8,
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                    {stat.icon}
                    <Statistic
                      title={<span style={{ color: "#94a3b8" }}>{stat.title}</span>}
                      value={stat.value}
                      valueStyle={{ color: stat.color }}
                    />
                  </div>
                </Card>
              </Col>
            ))}
          </Row>

          {/* Danh sách tòa nhà cho Manager */}
          {(isAdmin || isManager) && buildings.length > 0 && (
            <Card
              title="Tòa nhà của bạn"
              style={{
                marginTop: 24,
                background: "rgba(255, 255, 255, 0.02)",
                border: "1px solid rgba(255, 255, 255, 0.1)",
              }}
              extra={
                <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/buildings')}>
                  Quản lý tòa nhà
                </Button>
              }
            >
              <Row gutter={[16, 16]}>
                {buildings.slice(0, 6).map((building) => (
                  <Col xs={24} sm={12} md={8} key={building.id}>
                    <Card
                      hoverable
                      onClick={() => navigate(`/households?toaNha=${building.id}`)}
                      style={{ textAlign: 'center' }}
                    >
                      <BankOutlined style={{ fontSize: 48, color: '#3b82f6', marginBottom: 16 }} />
                      <h3 style={{ margin: 0 }}>{building.tenToaNha}</h3>
                      <p style={{ color: '#94a3b8', margin: 0 }}>{building.moTa || 'Nhấn để quản lý'}</p>
                    </Card>
                  </Col>
                ))}
              </Row>
            </Card>
          )}

          {/* Hướng dẫn cho Admin/Manager chưa có tòa nhà */}
          {(isAdmin || isManager) && buildings.length === 0 && (
            <div style={{ marginTop: 24 }}>
              <EmptyBuildingState 
                title="Chưa có tòa nhà nào"
                description="Bạn chưa có tòa nhà nào. Hãy tạo tòa nhà đầu tiên để bắt đầu quản lý chung cư!"
              />
            </div>
          )}

          <Card
            style={{
              marginTop: 24,
              background: "rgba(255, 255, 255, 0.02)",
              border: "1px solid rgba(255, 255, 255, 0.1)",
            }}
          >
            <h3 style={{ color: "#e2e8f0", marginTop: 0 }}>Chào mừng đến với Hệ thống Quản lý Chung cư</h3>
            <p style={{ color: "#94a3b8" }}>
              Sử dụng menu bên trái để điều hướng đến các chức năng quản lý tòa nhà, hộ gia đình, nhân khẩu và phí.
            </p>
          </Card>
        </>
      )}
    </ContentCard>
  );
}
