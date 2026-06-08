import http from "./http";

export const confirmTossPayment = async (payload) => {
  const response = await http.post("/api/v1/payments/toss/confirm", payload);
  return response.data;
};
