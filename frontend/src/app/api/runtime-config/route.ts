// 컨테이너 실행 시점의 공개 설정을 브라우저에 전달합니다.
// NEXT_PUBLIC_* 값을 빌드 시점에 고정하지 않고 배포 환경별로 바꿀 수 있게 합니다.
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export function GET() {
  return NextResponse.json({
    kakaoRestApiKey: process.env.KAKAO_REST_API_KEY ?? "",
    kakaoRedirectUri: process.env.KAKAO_REDIRECT_URI ?? "http://localhost:3000/auth/kakao/callback",
  });
}
