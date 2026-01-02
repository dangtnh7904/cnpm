import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { App as AntdApp } from "antd";
import { MainLayout } from "./components";
import { AuthProvider, useAuthContext } from "./contexts";
import {
  LoginPage,
  HomePage,
  HouseholdsPage,
  ApartmentDetailPage,
  BuildingsPage,
  TamTruPage,
  TamVangPage,
  LoaiPhiPage,
  DinhMucThuPage,
  DotThuPage,
  DotThuDetailPage,
  GhiChiSoPage,
  PaymentUpdatePage,
  OnlinePaymentPage,
  ReportDashboard,
  InvoiceManagementPage,
  NotificationPage,
  PaymentHistoryPage,
  FeedbackPage,
  UserManagementPage,
  BackupPage,
} from "./pages";
import "./styles.css";

// Protected Route Component
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isAdmin, isAccountant, isResident } = useAuthContext();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === "ADMIN" && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "RESIDENT" && !isResident) {
    return <Navigate to="/" replace />;
  }

  if (requiredRole === "ADMIN_OR_ACCOUNTANT" && !isAdmin && !isAccountant) {
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
          path="/tam-tru"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <TamTruPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tam-vang"
          element={
            <ProtectedRoute requiredRole="ADMIN">
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
        <Route
          path="/report"
          element={
            <ProtectedRoute requiredRole="ADMIN_OR_ACCOUNTANT">
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
            <ProtectedRoute requiredRole="ADMIN">
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/backup"
          element={
            <ProtectedRoute requiredRole="ADMIN">
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