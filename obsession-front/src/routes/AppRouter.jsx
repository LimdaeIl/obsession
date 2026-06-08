import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import SignupPage from "../pages/SignupPage";
import ProductListPage from "../pages/ProductListPage";
import ProductDetailPage from "../pages/ProductDetailPage";
import OrderDetailPage from "../pages/OrderDetailPage";
import TossSuccessPage from "../pages/TossSuccessPage";
import TossFailPage from "../pages/TossFailPage";

export default function AppRouter() {
  return (
      <BrowserRouter>
        <nav style={{ display: "flex", gap: 12, padding: 16 }}>
          <Link to="/">상품 목록</Link>
          <Link to="/login">로그인</Link>
          <Link to="/signup">회원가입</Link>
        </nav>

        <Routes>
          <Route path="/" element={<ProductListPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />
          <Route path="/orders/:orderId" element={<OrderDetailPage />} />
          <Route path="/payments/success" element={<TossSuccessPage />} />
          <Route path="/payments/fail" element={<TossFailPage />} />
        </Routes>
      </BrowserRouter>
  );
}
