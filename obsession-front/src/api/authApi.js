import http from "./http";

export const signup = async (payload) => {
  const response = await http.post("/api/v1/auth/signup", payload);
  return response.data;
};

export const login = async (payload) => {
  const response = await http.post("/api/v1/auth/login", payload);
  return response.data;
};

export const reissue = async () => {
  const response = await http.post("/api/v1/auth/reissue");
  return response.data;
};

export const getMe = async () => {
  const response = await http.get("/api/v1/members/me");
  return response.data;
};

export const logout = async () => {
  const response = await http.post("/api/v1/auth/logout");
  return response.data;
};

