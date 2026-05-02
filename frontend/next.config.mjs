// Next.js 런타임 설정입니다.
// 현재는 React Strict Mode를 켜서 개발 중 잠재 문제를 더 빨리 확인합니다.
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: "standalone",
};

export default nextConfig;
