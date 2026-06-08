import http from "./http";

export const prepareTossPayment = async (orderId) => {
  const response = await http.post(`/api/v1/payments/toss/prepare/${orderId}`);
  return response.data;
};

export const confirmTossPayment = async (payload) => {
  const response = await http.post("/api/v1/payments/toss/confirm", payload);
  return response.data;
};
