import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { kakaoLogin } from "../api/authApi";
import { useAuth } from "../context/AuthContext";

export default function KakaoCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { fetchMe } = useAuth();

  const [message, setMessage] = useState("카카오 로그인 처리 중...");
  const calledRef = useRef(false);

  useEffect(() => {
    if (calledRef.current) return;
    calledRef.current = true;

    const code = searchParams.get("code");

    if (!code) {
      setMessage("카카오 인가 코드가 없습니다.");
      return;
    }

    const login = async () => {
      try {
        const response = await kakaoLogin(code);
        const accessToken = response.data?.accessToken || response.accessToken;

        localStorage.setItem("accessToken", accessToken);

        await fetchMe();

        navigate("/", { replace: true });
      } catch (error) {
        setMessage(
            error.response?.data?.detail ||
            error.response?.data?.message ||
            "카카오 로그인 실패"
        );
      }
    };

    login();
  }, [searchParams, navigate, fetchMe]);

  return <div style={{ padding: 24 }}>{message}</div>;
}
