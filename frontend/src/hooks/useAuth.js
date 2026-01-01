import { useState, useEffect, useCallback } from "react";
import authService from "../services/authService";

export default function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    const userData = await authService.login(username, password);
    authService.saveUser(userData);
    setUser(userData);
    return userData;
  }, []);

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
  }, []);

  return {
    user,
    loading,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ADMIN",
    isAccountant: user?.role === "ACCOUNTANT",
    login,
    logout,
  };
}
