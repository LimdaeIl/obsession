import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getProduct } from "../api/productApi";
import { createOrder } from "../api/orderApi";

export default function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetchProduct();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      const response = await getProduct(productId);
      setProduct(response.data);
    } catch (error) {
      setMessage(error.response?.data?.detail || "상품 상세 조회 실패");
    }
  };

  const order = async () => {
    try {
      const response = await createOrder({
        orderLines: [
          {
            productId: Number(productId),
            quantity: Number(quantity),
          },
        ],
      });

      const orderId = response.data.orderId;
      navigate(`/orders/${orderId}`);
    } catch (error) {
      setMessage(error.response?.data?.detail || "주문 생성 실패");
    }
  };

  if (!product) {
    return (
        <div style={{ padding: 24 }}>
          <p>{message || "상품 정보를 불러오는 중..."}</p>
        </div>
    );
  }

  return (
      <div style={{ padding: 24 }}>
        <h2>{product.name}</h2>

        <p>{product.description}</p>
        <p>{product.price}원</p>
        <p>{product.status}</p>

        {product.images?.length > 0 && (
            <div>
              <h3>이미지</h3>
              {product.images.map((image) => (
                  <img
                      key={image.imageId}
                      src={image.imageUrl}
                      alt={product.name}
                      style={{ width: 160, display: "block", marginBottom: 8 }}
                  />
              ))}
            </div>
        )}

        <div>
          <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
          />

          <button onClick={order}>주문하기</button>
        </div>

        {message && <p>{message}</p>}
      </div>
  );
}