import { createContext, useContext, useEffect, useState } from "react";
import { getMe, login as loginApi, logout as logoutApi } from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchMe = async () => {
    try {
      const response = await getMe();
      const userData = response.data || response;
      setUser(userData);
    } catch {
      localStorage.removeItem("accessToken");
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
      setLoading(false);
      return;
    }

    fetchMe();
  }, []);

  const login = async (payload) => {
    const response = await loginApi(payload);

    const accessToken = response.data?.accessToken || response.accessToken;
    const userData = response.data?.user || response.user;

    localStorage.setItem("accessToken", accessToken);

    if (userData) {
      setUser(userData);
    } else {
      await fetchMe();
    }

    return response;
  };

  const logout = async () => {
    try {
      await logoutApi();
    } catch {
      // 백엔드 로그아웃 실패해도 프론트 토큰은 제거
    }

    localStorage.removeItem("accessToken");
    setUser(null);
    window.location.href = "/login";
  };

  return (
      <AuthContext.Provider value={{ user, loading, login, logout, isLoggedIn: !!user }}>
        {children}
      </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
