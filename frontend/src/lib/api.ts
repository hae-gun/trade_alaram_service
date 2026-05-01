// 백엔드 API 호출을 담당하는 클라이언트 모듈입니다.
// 백엔드가 꺼져 있어도 화면 개발이 가능하도록 MVP용 fallback 데이터를 제공합니다.
import type { AlertEvent, AlertRule, Stock, WatchlistItem } from "@/lib/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

const fallbackStocks: Stock[] = [
  { id: "demo-samsung", market: "KOSPI", symbol: "005930", name: "삼성전자" },
  { id: "demo-sk-hynix", market: "KOSPI", symbol: "000660", name: "SK하이닉스" },
  { id: "demo-naver", market: "KOSPI", symbol: "035420", name: "NAVER" },
];

export async function fetchStocks(query = ""): Promise<Stock[]> {
  try {
    const search = query ? `?query=${encodeURIComponent(query)}` : "";
    return await request<Stock[]>(`/api/stocks${search}`);
  } catch {
    return fallbackStocks;
  }
}

export async function fetchWatchlist(): Promise<WatchlistItem[]> {
  try {
    return await request<WatchlistItem[]>("/api/watchlist");
  } catch {
    return [
      {
        id: "watch-demo-1",
        stock: fallbackStocks[0],
        currentPrice: 74200,
        changeRate: 1.24,
        createdAt: new Date().toISOString(),
      },
      {
        id: "watch-demo-2",
        stock: fallbackStocks[1],
        currentPrice: 183500,
        changeRate: -0.72,
        createdAt: new Date().toISOString(),
      },
    ];
  }
}

export async function fetchAlertRules(): Promise<AlertRule[]> {
  try {
    return await request<AlertRule[]>("/api/alerts");
  } catch {
    return [
      {
        id: "rule-demo-1",
        stock: fallbackStocks[0],
        type: "ABOVE_PRICE",
        targetPrice: 76000,
        changeRate: null,
        enabled: true,
        repeatPolicy: "ONCE",
        lastTriggeredAt: null,
        createdAt: new Date().toISOString(),
      },
    ];
  }
}

export async function fetchAlertEvents(): Promise<AlertEvent[]> {
  try {
    return await request<AlertEvent[]>("/api/notifications/events");
  } catch {
    return [
      {
        id: "event-demo-1",
        stockSymbol: "005930",
        stockName: "삼성전자",
        triggerPrice: 74200,
        triggerChangeRate: 1.24,
        message: "삼성전자 가격 조건이 충족되었습니다.",
        status: "SENT",
        sentAt: new Date().toISOString(),
      },
    ];
  }
}
