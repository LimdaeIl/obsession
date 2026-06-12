import {useState} from "react";
import {Navigate, useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoggedIn, loading } = useAuth();

  const [form, setForm] = useState({
    email: "customer@test.com",
    password: "1234abcd!",
  });

  const [message, setMessage] = useState("");const kakaoLogin = () => {
    window.location.href = "https://kauth.kakao.com/oauth/authorize" +
        `?client_id=${import.meta.env.VITE_KAKAO_REST_API_KEY}` +
        `&redirect_uri=${encodeURIComponent(
            import.meta.env.VITE_KAKAO_REDIRECT_URI)}` +
        "&response_type=code";
  };

  if (loading) return <div style={{ padding: 24 }}>확인 중...</div>;
  if (isLoggedIn) return <Navigate to="/" replace />;

  const change = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const submit = async (e) => {
    e.preventDefault();

    try {
      await login(form);
      navigate("/", { replace: true });
    } catch (error) {
      setMessage(error.response?.data?.detail || error.response?.data?.message || "로그인 실패");
    }
  };

  return (
      <div style={{ padding: 24 }}>
        <h2>로그인</h2>

        <form onSubmit={submit}>
          <input name="email" value={form.email} onChange={change} placeholder="이메일" />
          <br />
          <input name="password" type="password" value={form.password} onChange={change} placeholder="비밀번호" />
          <br />
          <button type="submit">로그인</button>
          <button type="button" onClick={kakaoLogin}>카카오로 로그인</button>
        </form>

        {message && <p>{message}</p>}
      </div>
  );
}
