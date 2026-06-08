import http from "./http";

export const createOrder = async (payload) => {
  const response = await http.post("/api/v1/orders", payload);
  return response.data;
};

export const getOrder = async (orderId) => {
  const response = await http.get(`/api/v1/orders/${orderId}`);
  return response.data;
};

export const getOrders = async ({ page = 0, size = 20 } = {}) => {
  const response = await http.get("/api/v1/orders", {
    params: { page, size },
  });
  return response.data;
};
