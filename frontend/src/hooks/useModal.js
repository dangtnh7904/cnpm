import { useState, useCallback } from "react";
import { Form, message } from "antd";

export default function useModal(initialValues = {}) {
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);

  const openModal = useCallback((record = null) => {
    if (record) {
      setEditingId(record.id);
      form.setFieldsValue(record);
    } else {
      setEditingId(null);
      form.setFieldsValue(initialValues);
    }
    setOpen(true);
  }, [form, initialValues]);

  const closeModal = useCallback(() => {
    setOpen(false);
    setEditingId(null);
    form.resetFields();
  }, [form]);

  const handleSubmit = useCallback(async (onSubmit, successMessage) => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      await onSubmit(values, editingId);
      message.success(successMessage || (editingId ? "Cập nhật thành công" : "Thêm thành công"));
      closeModal();
      return true;
    } catch (err) {
      if (err.errorFields) return false; // validation error
      message.error(err.response?.data?.message || "Có lỗi xảy ra");
      return false;
    } finally {
      setLoading(false);
    }
  }, [form, editingId, closeModal]);

  return {
    form,
    open,
    editingId,
    loading,
    isEditing: !!editingId,
    openModal,
    closeModal,
    handleSubmit,
  };
}
