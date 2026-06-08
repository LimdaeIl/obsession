import { loadTossPayments, ANONYMOUS } from "@tosspayments/tosspayments-sdk";
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { getOrder } from "../api/orderApi";

export default function OrderDetailPage() {
  const { orderId } = useParams();

  const [order, setOrder] = useState(null);
  const [widgets, setWidgets] = useState(null);
  const [message, setMessage] = useState("");

  const renderedRef = useRef(false);

  useEffect(() => {
    fetchOrder();
  }, [orderId]);

  useEffect(() => {
    if (!order || renderedRef.current) return;

    renderTossWidgets();
    renderedRef.current = true;
  }, [order]);

  const fetchOrder = async () => {
    try {
      const response = await getOrder(orderId);
      setOrder(response.data);
    } catch (error) {
      setMessage(error.response?.data?.detail || "주문 상세 조회 실패");
    }
  };

  const renderTossWidgets = async () => {
    try {
      const tossPayments = await loadTossPayments(
          import.meta.env.VITE_TOSS_CLIENT_KEY
      );

      const widgets = tossPayments.widgets({
        customerKey: ANONYMOUS,
      });

      await widgets.setAmount({
        currency: "KRW",
        value: Number(order.totalAmount),
      });

      await widgets.renderPaymentMethods({
        selector: "#payment-method",
        variantKey: "DEFAULT",
      });

      await widgets.renderAgreement({
        selector: "#agreement",
        variantKey: "AGREEMENT",
      });

      setWidgets(widgets);
    } catch (error) {
      console.error(error);
      setMessage(error.message || "결제위젯 렌더링 실패");
    }
  };

  const pay = async () => {
    try {
      if (!widgets) {
        setMessage("결제위젯이 아직 준비되지 않았습니다.");
        return;
      }

      await widgets.requestPayment({
        orderId: `ORDER-${order.orderId}`,
        orderName: order.orderLines.map((line) => line.productName).join(", "),
        successUrl: `${window.location.origin}/payments/success`,
        failUrl: `${window.location.origin}/payments/fail`,
        customerEmail: "customer@test.com",
        customerName: "테스트 고객",
      });
    } catch (error) {
      console.error(error);
      setMessage(error.message || "결제 요청 실패");
    }
  };

  if (!order) {
    return (
        <div style={{ padding: 24 }}>
          <p>{message || "주문 정보를 불러오는 중..."}</p>
        </div>
    );
  }

  return (
      <div style={{ padding: 24 }}>
        <h2>주문 상세</h2>

        <p>주문번호: {order.orderId}</p>
        <p>상태: {order.status}</p>
        <p>총 금액: {order.totalAmount}원</p>

        <h3>주문 상품</h3>

        {order.orderLines.map((line) => (
            <div
                key={line.productId}
                style={{
                  border: "1px solid #ddd",
                  padding: 12,
                  marginBottom: 8,
                }}
            >
              <p>상품명: {line.productName}</p>
              <p>가격: {line.price}원</p>
              <p>수량: {line.quantity}</p>
              <p>금액: {line.lineAmount}원</p>
            </div>
        ))}

        {order.status === "CREATED" && (
            <>
              <div id="payment-method" />
              <div id="agreement" />

              <button onClick={pay}>결제하기</button>
            </>
        )}

        {message && <p>{message}</p>}
      </div>
  );
}