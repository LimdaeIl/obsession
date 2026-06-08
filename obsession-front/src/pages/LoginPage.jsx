import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authApi";

export default function LoginPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "customer@test.com",
    password: "1234abcd!",
  });

  const [message, setMessage] = useState("");

  const change = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const submit = async (e) => {
    e.preventDefault();

    try {
      const response = await login(form);
      localStorage.setItem("accessToken", response.data.accessToken);

      setMessage("로그인 성공");
      navigate("/");
    } catch (error) {
      setMessage(error.response?.data?.detail || "로그인 실패");
    }
  };

  return (
      <div style={{ padding: 24 }}>
        <h2>로그인</h2>

        <form onSubmit={submit}>
          <div>
            <input
                name="email"
                value={form.email}
                onChange={change}
                placeholder="이메일"
            />
          </div>

          <div>
            <input
                name="password"
                type="password"
                value={form.password}
                onChange={change}
                placeholder="비밀번호"
            />
          </div>

          <button type="submit">로그인</button>
        </form>

        {message && <p>{message}</p>}
      </div>
  );
}
