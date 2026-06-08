import { useState } from "react";
import { signup } from "../api/authApi";

export default function SignupPage() {
  const [form, setForm] = useState({
    email: "customer@test.com",
    password: "1234abcd!",
    name: "고객",
    phone: "01012345678",
    role: "CUSTOMER",
    companyName: "",
    businessRegistrationNumber: "",
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
      const payload = {
        email: form.email,
        password: form.password,
        name: form.name,
        phone: form.phone,
        role: form.role,
      };

      if (form.role === "BUSINESS") {
        payload.companyName = form.companyName;
        payload.businessRegistrationNumber = form.businessRegistrationNumber;
      }

      await signup(payload);
      setMessage("회원가입 성공");
    } catch (error) {
      setMessage(error.response?.data?.detail || "회원가입 실패");
    }
  };

  return (
      <div style={{ padding: 24 }}>
        <h2>회원가입</h2>

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

          <div>
            <input
                name="name"
                value={form.name}
                onChange={change}
                placeholder="이름"
            />
          </div>

          <div>
            <input
                name="phone"
                value={form.phone}
                onChange={change}
                placeholder="전화번호"
            />
          </div>

          <div>
            <select name="role" value={form.role} onChange={change}>
              <option value="CUSTOMER">고객</option>
              <option value="BUSINESS">사업자</option>
            </select>
          </div>

          {form.role === "BUSINESS" && (
              <>
                <div>
                  <input
                      name="companyName"
                      value={form.companyName}
                      onChange={change}
                      placeholder="회사명"
                  />
                </div>

                <div>
                  <input
                      name="businessRegistrationNumber"
                      value={form.businessRegistrationNumber}
                      onChange={change}
                      placeholder="사업자등록번호"
                  />
                </div>
              </>
          )}

          <button type="submit">회원가입</button>
        </form>

        {message && <p>{message}</p>}
      </div>
  );
}
