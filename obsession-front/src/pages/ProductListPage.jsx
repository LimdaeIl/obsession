import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getProducts } from "../api/productApi";

export default function ProductListPage() {
  const [products, setProducts] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await getProducts({ page: 0, size: 20 });

      setProducts(response.data.content || []);
    } catch (error) {
      setMessage(error.response?.data?.detail || "상품 목록 조회 실패");
    }
  };

  return (
      <div style={{ padding: 24 }}>
        <h2>상품 목록</h2>

        {message && <p>{message}</p>}

        <div style={{ display: "grid", gap: 12 }}>
          {products.map((product) => (
              <div
                  key={product.productId}
                  style={{
                    border: "1px solid #ddd",
                    padding: 16,
                    borderRadius: 8,
                  }}
              >
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <p>{product.price}원</p>
                <p>{product.status}</p>

                <Link to={`/products/${product.productId}`}>상세 보기</Link>
              </div>
          ))}
        </div>
      </div>
  );
}