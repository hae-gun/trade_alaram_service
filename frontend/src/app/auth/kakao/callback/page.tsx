"use client";

// 카카오 OAuth 콜백 페이지입니다.
// URL의 인가 코드를 백엔드 로그인 API로 전달하고 로컬 로그인 세션을 저장합니다.
import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { LoginScreen } from "@/features/auth/LoginScreen";
import { loginWithKakao } from "@/lib/api";

const KAKAO_REDIRECT_URI = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI ?? "http://localhost:3000/auth/kakao/callback";

function KakaoCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [message, setMessage] = useState("카카오 로그인 처리 중입니다.");

  useEffect(() => {
    const code = searchParams.get("code");
    const error = searchParams.get("error");

    if (error) {
      setMessage("카카오 로그인이 취소되었거나 실패했습니다.");
      return;
    }

    if (!code) {
      setMessage("카카오 인가 코드가 없습니다.");
      return;
    }

    const authorizationCode = code;

    async function completeLogin() {
      try {
        const response = await loginWithKakao(authorizationCode, KAKAO_REDIRECT_URI);
        window.localStorage.setItem("trade_alarm_user", JSON.stringify(response.user));
        router.replace("/");
      } catch (loginError) {
        setMessage(loginError instanceof Error ? loginError.message : "카카오 로그인에 실패했습니다.");
      }
    }

    void completeLogin();
  }, [router, searchParams]);

  return <LoginScreen message={message} />;
}

export default function KakaoCallbackPage() {
  return (
    <Suspense fallback={<LoginScreen message="카카오 로그인 처리 중입니다." />}>
      <KakaoCallbackContent />
    </Suspense>
  );
}
