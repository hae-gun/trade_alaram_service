"use client";

// SNS 로그인 진입 화면입니다.
// 카카오 OAuth 인가 URL을 구성해 카카오 로그인 화면으로 이동합니다.
import { Bell, LineChart, MessageCircle, ShieldCheck, Sparkles } from "lucide-react";

const KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
const KAKAO_REST_API_KEY = process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY ?? "";
const KAKAO_REDIRECT_URI = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI ?? "http://localhost:3000/auth/kakao/callback";

type Props = {
  message?: string;
};

export function LoginScreen({ message }: Props) {
  function handleKakaoLogin() {
    if (!KAKAO_REST_API_KEY) {
      window.alert("NEXT_PUBLIC_KAKAO_REST_API_KEY 설정이 필요합니다.");
      return;
    }

    const params = new URLSearchParams({
      response_type: "code",
      client_id: KAKAO_REST_API_KEY,
      redirect_uri: KAKAO_REDIRECT_URI,
    });

    window.location.href = `${KAKAO_AUTH_URL}?${params.toString()}`;
  }

  return (
    <main className="login-shell">
      <section className="login-panel" aria-label="소셜 로그인">
        <div className="login-brand">
          <LineChart size={28} />
          <span>Trade Alarm</span>
        </div>
        <h1>관심종목 가격 알림을 한 화면에서 관리하세요</h1>
        <p className="login-copy">국내 주식 관심종목, 알림 조건, 발송 이력을 로그인 후 바로 확인할 수 있습니다.</p>

        {message && <div className="login-message">{message}</div>}

        <button type="button" className="kakao-login-button" onClick={handleKakaoLogin}>
          <MessageCircle size={20} />
          카카오로 계속하기
        </button>

        <div className="login-feature-grid">
          <div>
            <Sparkles size={18} />
            <span>실시간 관심종목</span>
          </div>
          <div>
            <Bell size={18} />
            <span>조건 기반 알림</span>
          </div>
          <div>
            <ShieldCheck size={18} />
            <span>소셜 계정 연동</span>
          </div>
        </div>
      </section>
    </main>
  );
}
