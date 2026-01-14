import React from "react";
import { Form, Input, Button, Card, App, Typography, Select } from "antd";
import { UserOutlined, LockOutlined, MailOutlined, IdcardOutlined } from "@ant-design/icons";
import { useNavigate, Link } from "react-router-dom";
import axiosClient from "../../services/axiosClient";
import "./SignupPage.css";

const { Title } = Typography;
const { Option } = Select;

export default function SignupPage() {
    const { message } = App.useApp();
    const [form] = Form.useForm();
    const navigate = useNavigate();
    const [submitting, setSubmitting] = React.useState(false);

    const onFinish = async (values) => {
        setSubmitting(true);
        try {
            await axiosClient.post("/auth/signup", {
                username: values.username,
                password: values.password,
                fullName: values.fullName,
                email: values.email,
                role: values.role,
            });
            message.success("Đăng ký thành công! Vui lòng đăng nhập.");
            navigate("/login");
        } catch (error) {
            message.error(error.response?.data?.message || "Đăng ký thất bại");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="signup-container">
            <Card className="signup-card">
                <div className="signup-header">
                    <Title level={3}>Đăng ký tài khoản</Title>
                    <p>Tạo tài khoản mới để sử dụng hệ thống</p>
                </div>

                <Form form={form} onFinish={onFinish} layout="vertical" autoComplete="off">
                    <Form.Item
                        name="fullName"
                        rules={[{ required: true, message: "Vui lòng nhập họ tên!" }]}
                    >
                        <Input prefix={<IdcardOutlined />} placeholder="Họ và tên" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="username"
                        rules={[
                            { required: true, message: "Vui lòng nhập tên đăng nhập!" },
                            { min: 4, message: "Tên đăng nhập phải có ít nhất 4 ký tự!" },
                        ]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="Tên đăng nhập" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="email"
                        rules={[
                            { required: true, message: "Vui lòng nhập email!" },
                            { type: "email", message: "Email không hợp lệ!" },
                        ]}
                    >
                        <Input prefix={<MailOutlined />} placeholder="Email" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[
                            { required: true, message: "Vui lòng nhập mật khẩu!" },
                            { min: 6, message: "Mật khẩu phải có ít nhất 6 ký tự!" },
                        ]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="confirmPassword"
                        dependencies={["password"]}
                        rules={[
                            { required: true, message: "Vui lòng xác nhận mật khẩu!" },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue("password") === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error("Mật khẩu xác nhận không khớp!"));
                                },
                            }),
                        ]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Xác nhận mật khẩu" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="role"
                        rules={[{ required: true, message: "Vui lòng chọn vai trò!" }]}
                    >
                        <Select placeholder="Chọn vai trò" size="large">
                            <Option value="MANAGER">Quản lý (Manager)</Option>
                            <Option value="ACCOUNTANT">Kế toán (Accountant)</Option>
                            <Option value="RESIDENT">Cư dân (Resident)</Option>
                        </Select>
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={submitting}
                            block
                            size="large"
                        >
                            Đăng ký
                        </Button>
                    </Form.Item>

                    <div className="signup-footer">
                        <p>
                            Đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link>
                        </p>
                    </div>
                </Form>
            </Card>
        </div>
    );
}
