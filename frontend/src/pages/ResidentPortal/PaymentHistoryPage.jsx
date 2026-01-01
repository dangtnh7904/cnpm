import React, { useState, useEffect } from "react";
import { Card, Table, Tag, Descriptions, Select, Button } from "antd";
import { ContentCard } from "../../components";
import { residentService, householdService } from "../../services";
import { useFetch } from "../../hooks";
import dayjs from "dayjs";

const { Option } = Select;

export default function PaymentHistoryPage() {
  const [selectedHoGiaDinh, setSelectedHoGiaDinh] = useState(null);
  const [selectedHoaDon, setSelectedHoaDon] = useState(null);
  const { data: households, refetch: fetchHouseholds } = useFetch(householdService.getAll, false);
  const { data: hoaDons, loading, refetch } = useFetch(
    () => selectedHoGiaDinh ? residentService.getPaymentHistory(selectedHoGiaDinh) : Promise.resolve([]),
    false
  );
  const { data: paymentDetails, refetch: fetchDetails } = useFetch(
    () => selectedHoaDon ? residentService.getPaymentDetails(selectedHoaDon) : Promise.resolve([]),
    false
  );
  const { data: currentDebt } = useFetch(
    () => selectedHoGiaDinh ? residentService.getCurrentDebt(selectedHoGiaDinh) : Promise.resolve({ tongCongNo: 0 }),
    false
  );

  useEffect(() => {
    fetchHouseholds();
  }, [fetchHouseholds]);

  useEffect(() => {
    if (selectedHoGiaDinh) {
      refetch();
    }
  }, [selectedHoGiaDinh, refetch]);

  useEffect(() => {
    if (selectedHoaDon) {
      fetchDetails();
    }
  }, [selectedHoaDon, fetchDetails]);

  const columns = [
    { title: "Mã hóa đơn", render: (record) => `HD${String(record.id).padStart(6, '0')}` },
    { title: "Đợt thu", render: (record) => record?.dotThu?.tenDotThu || "" },
    { 
      title: "Tổng phải thu", 
      render: (record) => new Intl.NumberFormat('vi-VN').format(record.tongTienPhaiThu || 0) + " đ"
    },
    { 
      title: "Đã đóng", 
      render: (record) => new Intl.NumberFormat('vi-VN').format(record.soTienDaDong || 0) + " đ"
    },
    { 
      title: "Còn nợ", 
      render: (record) => {
        const conNo = (record.tongTienPhaiThu || 0) - (record.soTienDaDong || 0);
        return <span style={{ color: conNo > 0 ? '#ff4d4f' : '#52c41a' }}>
          {new Intl.NumberFormat('vi-VN').format(conNo)} đ
        </span>;
      }
    },
    { 
      title: "Trạng thái", 
      dataIndex: "trangThai",
      render: (status) => {
        const color = status === "Đã đóng" ? "green" : status === "Đang nợ" ? "orange" : "red";
        return <Tag color={color}>{status}</Tag>;
      }
    },
    {
      title: "Thao tác",
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedHoaDon(record.id)}>
          Xem chi tiết
        </Button>
      ),
    },
  ];

  return (
    <ContentCard title="Lịch sử thanh toán">
      <div style={{ marginBottom: 24 }}>
        <Select
          style={{ width: 300 }}
          placeholder="Chọn hộ gia đình"
          onChange={setSelectedHoGiaDinh}
          value={selectedHoGiaDinh}
        >
          {(Array.isArray(households) ? households : []).map((h) => (
            <Option key={h.id} value={h.id}>
              {h.maHoGiaDinh} - {h.tenChuHo}
            </Option>
          ))}
        </Select>
      </div>

      {selectedHoGiaDinh && currentDebt && (
        <Card style={{ marginBottom: 24 }}>
          <Descriptions title="Công nợ hiện tại" bordered>
            <Descriptions.Item label="Tổng công nợ">
              <Tag color="red" style={{ fontSize: 16 }}>
                {new Intl.NumberFormat('vi-VN').format(currentDebt.tongCongNo || 0)} đ
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      <Table
        columns={columns}
        dataSource={Array.isArray(hoaDons) ? hoaDons : []}
        loading={loading}
        rowKey="id"
        pagination={{ pageSize: 10 }}
      />

      {selectedHoaDon && paymentDetails && (
        <Card title="Chi tiết thanh toán" style={{ marginTop: 24 }}>
          <Table
            columns={[
              { title: "Ngày nộp", dataIndex: "ngayNop", render: (date) => dayjs(date).format("DD/MM/YYYY HH:mm") },
              { 
                title: "Số tiền", 
                dataIndex: "soTien",
                render: (value) => new Intl.NumberFormat('vi-VN').format(value) + " đ"
              },
              { title: "Hình thức", dataIndex: "hinhThuc" },
              { title: "Người nộp", dataIndex: "nguoiNop" },
              { title: "Ghi chú", dataIndex: "ghiChu" },
            ]}
            dataSource={Array.isArray(paymentDetails) ? paymentDetails : []}
            rowKey="id"
            pagination={false}
          />
        </Card>
      )}
    </ContentCard>
  );
}

