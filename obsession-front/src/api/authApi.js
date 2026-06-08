import http from "./http";

export const signup = async (payload) => {
  const response = await http.post("/api/v1/auth/signup", payload);
  return response.data;
};

export const login = async (payload) => {
  const response = await http.post("/api/v1/auth/login", payload);
  return response.data;
};
