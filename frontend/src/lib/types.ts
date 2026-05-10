// 프론트엔드에서 공유하는 API 응답 타입 정의입니다.
// 백엔드 DTO와 화면 컴포넌트 사이의 데이터 계약을 명확히 하기 위해 분리했습니다.
export type Market = "KOSPI" | "KOSDAQ" | "KONEX";

export type Stock = {
  id: string;
  market: Market;
  symbol: string;
  name: string;
};

export type WatchlistItem = {
  id: string;
  stock: Stock;
  currentPrice: number;
  changeRate: number;
  createdAt: string;
};

export type AlertType = "ABOVE_PRICE" | "BELOW_PRICE" | "UP_RATE" | "DOWN_RATE";

export type AlertRule = {
  id: string;
  stock: Stock;
  type: AlertType;
  targetPrice: number | null;
  changeRate: number | null;
  enabled: boolean;
  repeatPolicy: "ONCE" | "COOLDOWN";
  lastTriggeredAt: string | null;
  createdAt: string;
};

export type AlertEvent = {
  id: string;
  stockSymbol: string;
  stockName: string;
  triggerPrice: number;
  triggerChangeRate: number;
  message: string;
  status: "PENDING" | "SENT" | "FAILED";
  sentAt: string;
};

export type NotificationChannel = {
  id: string;
  type: "EMAIL" | "KAKAO_ALIMTALK" | "WEB_PUSH";
  destination: string;
  verified: boolean;
  enabled: boolean;
};

export type User = {
  id: string;
  email: string;
  nickname: string;
};

export type KakaoLoginResponse = {
  user: User;
  provider: "KAKAO";
  isNewUser: boolean;
};
