import React from "react";
import { Form, Input, Button, Card, message, Typography } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "../../contexts";
import "./LoginPage.css";

const { Title } = Typography;

export default function LoginPage() {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { login } = useAuthContext();
  const [submitting, setSubmitting] = React.useState(false);

  const onFinish = async (values) => {
    setSubmitting(true);
    try {
      await login(values.username, values.password);
      message.success("Đăng nhập thành công!");
      navigate("/");
    } catch (error) {
      message.error(error.response?.data?.message || "Đăng nhập thất bại");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-container">
      <Card className="login-card">
        <div className="login-header">
          <Title level={3}>Hệ thống Quản lý Chung cư</Title>
          <p>Đăng nhập để tiếp tục</p>
        </div>

        <Form form={form} onFinish={onFinish} layout="vertical" autoComplete="off">
          <Form.Item
            name="username"
            rules={[{ required: true, message: "Vui lòng nhập tên đăng nhập!" }]}
          >
            <Input prefix={<UserOutlined />} placeholder="Tên đăng nhập" size="large" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu!" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu" size="large" />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={submitting}
              block
              size="large"
            >
              Đăng nhập
            </Button>
          </Form.Item>
        </Form>

        <div className="login-demo">
          <p><strong>Tài khoản mẫu:</strong></p>
          <p>👤 Quản lý: <code>admin</code> / <code>Admin@123</code></p>
          <p>💰 Kế toán: <code>accountant</code> / <code>Accountant@123</code></p>
        </div>
      </Card>
    </div>
  );
}
