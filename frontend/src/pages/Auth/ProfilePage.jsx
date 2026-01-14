import React, { useState, useEffect } from "react";
import { Form, Input, Button, Card, App, Typography, Spin, Divider } from "antd";
import { UserOutlined, LockOutlined, IdcardOutlined, MailOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { authService } from "../../services";
import { useAuthContext } from "../../contexts";
import "./ProfilePage.css";

const { Title, Text } = Typography;

export default function ProfilePage() {
    const { message } = App.useApp();
    const [form] = Form.useForm();
    const navigate = useNavigate();
    const { user } = useAuthContext();
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [profile, setProfile] = useState(null);

    useEffect(() => {
        loadProfile();
    }, []);

    const loadProfile = async () => {
        try {
            const data = await authService.getProfile();
            setProfile(data);
            form.setFieldsValue({
                fullName: data.fullName,
            });
        } catch (error) {
            message.error("Không thể tải thông tin profile");
        } finally {
            setLoading(false);
        }
    };

    const onFinish = async (values) => {
        setSubmitting(true);
        try {
            const updateData = {};

            if (values.fullName && values.fullName !== profile?.fullName) {
                updateData.fullName = values.fullName;
            }

            if (values.newPassword) {
                if (!values.currentPassword) {
                    message.error("Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu!");
                    setSubmitting(false);
                    return;
                }
                updateData.currentPassword = values.currentPassword;
                updateData.newPassword = values.newPassword;
            }

            if (Object.keys(updateData).length === 0) {
                message.info("Không có thay đổi nào để cập nhật");
                setSubmitting(false);
                return;
            }

            const updatedProfile = await authService.updateProfile(updateData);

            // Cập nhật localStorage với fullName mới
            if (updateData.fullName) {
                localStorage.setItem("fullName", updatedProfile.fullName);
            }

            message.success("Cập nhật thông tin thành công!");
            form.setFieldsValue({ currentPassword: "", newPassword: "", confirmPassword: "" });
            setProfile(updatedProfile);

            // Reload trang để cập nhật header
            window.location.reload();
        } catch (error) {
            message.error(error.response?.data?.message || "Cập nhật thất bại");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="profile-loading">
                <Spin size="large" />
            </div>
        );
    }

    return (
        <div className="profile-container">
            <Card className="profile-card">
                <div className="profile-header">
                    <Title level={3}>Thông tin tài khoản</Title>
                    <Text type="secondary">Cập nhật thông tin cá nhân và mật khẩu</Text>
                </div>

                <Divider />

                <div className="profile-info">
                    <div className="profile-info-item">
                        <Text type="secondary">Tên đăng nhập:</Text>
                        <Text strong>{profile?.username}</Text>
                    </div>
                    <div className="profile-info-item">
                        <Text type="secondary">Email:</Text>
                        <Text strong>{profile?.email}</Text>
                    </div>
                    <div className="profile-info-item">
                        <Text type="secondary">Vai trò:</Text>
                        <Text strong>{profile?.role}</Text>
                    </div>
                </div>

                <Divider />

                <Form form={form} onFinish={onFinish} layout="vertical" autoComplete="off">
                    <Title level={5}>Chỉnh sửa thông tin</Title>

                    <Form.Item
                        name="fullName"
                        label="Họ và tên"
                        rules={[{ required: true, message: "Vui lòng nhập họ tên!" }]}
                    >
                        <Input prefix={<IdcardOutlined />} placeholder="Họ và tên" size="large" />
                    </Form.Item>

                    <Divider />

                    <Title level={5}>Đổi mật khẩu (để trống nếu không đổi)</Title>

                    <Form.Item
                        name="currentPassword"
                        label="Mật khẩu hiện tại"
                        rules={[
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (getFieldValue("newPassword") && !value) {
                                        return Promise.reject(new Error("Vui lòng nhập mật khẩu hiện tại!"));
                                    }
                                    return Promise.resolve();
                                },
                            }),
                        ]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu hiện tại" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="newPassword"
                        label="Mật khẩu mới"
                        rules={[
                            { min: 6, message: "Mật khẩu phải có ít nhất 6 ký tự!" },
                        ]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu mới" size="large" />
                    </Form.Item>

                    <Form.Item
                        name="confirmPassword"
                        label="Xác nhận mật khẩu mới"
                        dependencies={["newPassword"]}
                        rules={[
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue("newPassword") === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error("Mật khẩu xác nhận không khớp!"));
                                },
                            }),
                        ]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="Xác nhận mật khẩu" size="large" />
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={submitting}
                            block
                            size="large"
                        >
                            Lưu thay đổi
                        </Button>
                    </Form.Item>

                    <Button block size="large" onClick={() => navigate(-1)}>
                        Quay lại
                    </Button>
                </Form>
            </Card>
        </div>
    );
}

