import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { App as AntdApp } from "antd";
import { MainLayout } from "./components";
import { AuthProvider, useAuthContext } from "./contexts";
import {
  LoginPage,
  SignupPage,
  ProfilePage,
  HomePage,
  HouseholdsPage,
  ApartmentDetailPage,
  BuildingsPage,
  ResidentsPage,
  TamTruPage,
  TamVangPage,
  LoaiPhiPage,
  DinhMucThuPage,
  DotThuPage,
  DotThuDetailPage,
  GhiChiSoPage,
  PaymentUpdatePage,
  OnlinePaymentPage,
  PaymentResultPage,
  AccountantFeedbackPage,
  ReportDashboard,
  InvoiceManagementPage,
  NotificationPage,
  PaymentHistoryPage,
  FeedbackPage,
  ResidentNotificationPage,
  MyBuildingsPage,
  UserManagementPage,
  BackupPage,
  BuildingUsersPage,
  FeedbackManagementPage,
} from "./pages";
import "./styles.css";


// Protected Route Component
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isAdmin, isAccountant, isResident, isManager } = useAuthContext();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // ADMIN hoặc MANAGER đều có quyền quản lý (MANAGER bị lọc data ở backend)
  const canManage = isAdmin || isManager;

  // Check quyền theo requiredRole
  if (requiredRole === "ADMIN" && !canManage) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "ADMIN_ONLY" && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "RESIDENT" && !isResident) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "ACCOUNTANT" && !isAccountant) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "ADMIN_OR_ACCOUNTANT" && !canManage && !isAccountant) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "RESIDENT_OR_ACCOUNTANT" && !isResident && !isAccountant) {
    return <Navigate to="/" replace />;
  }

  return children;
}

// Main App Shell with Layout
function AppShell() {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route
          path="/households"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <HouseholdsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/apartments/:id"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <ApartmentDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/buildings"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <BuildingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/building-users"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <BuildingUsersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/feedback-management"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <FeedbackManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/residents"
          element={
            <ProtectedRoute requiredRole="ADMIN_ONLY">
              <ResidentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tam-tru"
          element={
            <ProtectedRoute requiredRole="ADMIN_ONLY">
              <TamTruPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tam-vang"
          element={
            <ProtectedRoute requiredRole="ADMIN_ONLY">
              <TamVangPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/loai-phi"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <LoaiPhiPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dinh-muc-thu"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <DinhMucThuPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dot-thu"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <DotThuPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/fee/dot-thu/:id"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <DotThuDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ghi-chi-so"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <GhiChiSoPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/payment/update"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <PaymentUpdatePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/payment/online"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <OnlinePaymentPage />
            </ProtectedRoute>
          }
        />
        {/* Accountant gửi phản ánh cho Manager */}
        <Route
          path="/accountant/feedback"
          element={
            <ProtectedRoute requiredRole="ACCOUNTANT">
              <AccountantFeedbackPage />
            </ProtectedRoute>
          }
        />
        {/* Payment Result - Không cần đăng nhập vì VNPAY redirect về đây */}
        <Route path="/payment-result" element={<PaymentResultPage />} />
        <Route
          path="/report"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <ReportDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/invoice"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <InvoiceManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/fees"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
              <LoaiPhiPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/notification"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <NotificationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resident/payment-history"
          element={
            <ProtectedRoute requiredRole="RESIDENT">
              <PaymentHistoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resident/online-payment"
          element={
            <ProtectedRoute requiredRole="RESIDENT">
              <OnlinePaymentPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resident/notifications"
          element={
            <ProtectedRoute requiredRole="RESIDENT">
              <ResidentNotificationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resident/my-buildings"
          element={
            <ProtectedRoute requiredRole="RESIDENT_OR_ACCOUNTANT">
              <MyBuildingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resident/feedback"
          element={
            <ProtectedRoute requiredRole="RESIDENT">
              <FeedbackPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="ADMIN_ONLY">
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/backup"
          element={
            <ProtectedRoute requiredRole="ADMIN_ONLY">
              <BackupPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </MainLayout>
  );
}

function AppRoutes() {
  const { isAuthenticated, loading } = useAuthContext();

  if (loading) {
    return (
      <div
        style={{
          minHeight: "100vh",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          background: "#0f172a",
          color: "#e2e8f0",
        }}
      >
        Đang tải...
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />}
      />
      <Route
        path="/signup"
        element={isAuthenticated ? <Navigate to="/" replace /> : <SignupPage />}
      />
      <Route
        path="/*"
        element={isAuthenticated ? <AppShell /> : <Navigate to="/login" replace />}
      />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AntdApp>
          <AppRoutes />
        </AntdApp>
      </AuthProvider>
    </BrowserRouter>
  );
}