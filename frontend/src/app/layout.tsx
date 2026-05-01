// Next.js App Router의 루트 레이아웃입니다.
// 모든 페이지에 공통 HTML 언어 설정과 전역 스타일을 적용합니다.
import type { Metadata } from "next";
import "@/styles/globals.css";

export const metadata: Metadata = {
  title: "Trade Alarm",
  description: "국내 주식 관심종목 가격 알림 서비스",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
