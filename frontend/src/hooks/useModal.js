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
      const result = await onSubmit(values, editingId);
      // Only show success message if onSubmit didn't throw and didn't return false
      if (result !== false) {
        if (successMessage) {
          message.success(successMessage);
        }
        closeModal();
      }
      return result !== false;
    } catch (err) {
      if (err.errorFields) {
        // Validation error - don't show error message, form will show validation errors
        return false;
      }
      // Error message should be handled by onSubmit function
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
