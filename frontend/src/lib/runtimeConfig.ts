// 브라우저에서 사용하는 런타임 공개 설정입니다.
// Next.js API route를 통해 컨테이너 실행 시점 환경변수를 읽습니다.
export type RuntimeConfig = {
  kakaoRestApiKey: string;
  kakaoRedirectUri: string;
};

export async function fetchRuntimeConfig(): Promise<RuntimeConfig> {
  const response = await fetch("/api/runtime-config", {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("런타임 설정을 불러오지 못했습니다.");
  }

  return response.json() as Promise<RuntimeConfig>;
}
