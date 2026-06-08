import http from "./http";

export const getProducts = async ({ page = 0, size = 20 } = {}) => {
  const response = await http.get("/api/v1/products", {
    params: { page, size },
  });
  return response.data;
};

export const getProduct = async (productId) => {
  const response = await http.get(`/api/v1/products/${productId}`);
  return response.data;
};
