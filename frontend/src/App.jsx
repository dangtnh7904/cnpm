import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { MainLayout } from "./components";
import { AuthProvider, useAuthContext } from "./contexts";
import {
  LoginPage,
  HomePage,
  HouseholdsPage,
  ResidentsPage,
  TamTruPage,
  TamVangPage,
} from "./pages";
import "./styles.css";

// Protected Route Component
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isAdmin } = useAuthContext();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === "ADMIN" && !isAdmin) {
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
          path="/residents"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <ResidentsPage />
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
          path="/fees"
          element={
            <div className="content-card" style={{ padding: 24 }}>
              <h2 style={{ color: "#e2e8f0" }}>Quản lý phí</h2>
              <p style={{ color: "#94a3b8" }}>Chức năng đang được phát triển...</p>
            </div>
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
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}