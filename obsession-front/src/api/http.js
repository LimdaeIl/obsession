import axios from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

http.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem("accessToken");

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

let isRefreshing = false;
let refreshSubscribers = [];

const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback);
};

const onRefreshed = (newAccessToken) => {
  refreshSubscribers.forEach((callback) => callback(newAccessToken));
  refreshSubscribers = [];
};

http.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (
          error.response?.status === 401 &&
          !originalRequest._retry &&
          !originalRequest.url.includes("/api/v1/auth/login") &&
          !originalRequest.url.includes("/api/v1/auth/signup") &&
          !originalRequest.url.includes("/api/v1/auth/reissue")
      ) {
        originalRequest._retry = true;

        if (isRefreshing) {
          return new Promise((resolve) => {
            subscribeTokenRefresh((newAccessToken) => {
              originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
              resolve(http(originalRequest));
            });
          });
        }

        isRefreshing = true;

        try {
          const response = await axios.post(
              `${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/reissue`,
              {},
              { withCredentials: true }
          );

          const newAccessToken = response.data.data?.accessToken || response.data.accessToken;

          localStorage.setItem("accessToken", newAccessToken);
          http.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;

          onRefreshed(newAccessToken);

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return http(originalRequest);
        } catch (refreshError) {
          localStorage.removeItem("accessToken");
          window.location.href = "/login";
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }

      return Promise.reject(error);
    }
);

export default http;