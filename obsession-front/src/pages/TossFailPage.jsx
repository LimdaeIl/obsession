import { Link, useSearchParams } from "react-router-dom";

const parseInternalOrderId = (tossOrderId) => {
  if (!tossOrderId) return null;

  const parts = tossOrderId.split("-");
  if (parts.length < 2) return null;

  return parts[1];
};

export default function TossFailPage() {
  const [searchParams] = useSearchParams();

  const code = searchParams.get("code");
  const message = searchParams.get("message");
  const tossOrderId = searchParams.get("orderId");

  const orderId = parseInternalOrderId(tossOrderId);

  return (
      <div style={{ padding: 24 }}>
        <h2>결제 실패</h2>

        <p>실패 코드: {code}</p>
        <p>실패 메시지: {message}</p>

        {orderId && <Link to={`/orders/${orderId}`}>주문 상세로 이동</Link>}
      </div>
  );
}
