import { Link, useSearchParams } from "react-router-dom";

export default function TossFailPage() {
  const [searchParams] = useSearchParams();

  const code = searchParams.get("code");
  const message = searchParams.get("message");
  const orderId = searchParams.get("orderId");

  const parsedOrderId = orderId?.replace("ORDER-", "");

  return (
      <div style={{ padding: 24 }}>
        <h2>결제 실패</h2>

        <p>실패 코드: {code}</p>
        <p>실패 메시지: {message}</p>

        {parsedOrderId && (
            <Link to={`/orders/${parsedOrderId}`}>주문 상세로 이동</Link>
        )}
      </div>
  );
}
