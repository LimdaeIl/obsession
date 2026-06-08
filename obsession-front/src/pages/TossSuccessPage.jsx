import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { confirmTossPayment } from "../api/paymentApi";

export default function TossSuccessPage() {
  const [searchParams] = useSearchParams();

  const calledRef = useRef(false);

  const [message, setMessage] = useState("결제 승인 처리 중...");
  const [orderId, setOrderId] = useState(null);

  useEffect(() => {
    if (calledRef.current) return;

    calledRef.current = true;
    confirm();
  }, []);

  const confirm = async () => {
    try {
      const paymentKey = searchParams.get("paymentKey");
      const tossOrderId = searchParams.get("orderId");
      const amount = searchParams.get("amount");

      if (!paymentKey || !tossOrderId || !amount) {
        setMessage("결제 승인 파라미터가 올바르지 않습니다.");
        return;
      }

      await confirmTossPayment({
        paymentKey,
        orderId: tossOrderId,
        amount: Number(amount),
      });

      setOrderId(tossOrderId.replace("ORDER-", ""));
      setMessage("결제 승인에 성공했습니다.");
    } catch (error) {
      setMessage(error.response?.data?.detail || "결제 승인 실패");
    }
  };

  return (
      <div style={{ padding: 24 }}>
        <h2>결제 성공</h2>

        <p>{message}</p>

        {orderId && <Link to={`/orders/${orderId}`}>주문 상세로 이동</Link>}
      </div>
  );
}

