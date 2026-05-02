// 백엔드 API 호출을 담당하는 클라이언트 모듈입니다.
// MVP 화면의 조회/생성/삭제/토글 액션을 백엔드 API와 연결합니다.
import type { AlertEvent, AlertRule, AlertType, NotificationChannel, Stock, User, WatchlistItem } from "@/lib/types";

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
    const message = await response.text();
    throw new Error(message || `API request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function fetchStocks(query = ""): Promise<Stock[]> {
  const search = query ? `?query=${encodeURIComponent(query)}` : "";
  return request<Stock[]>(`/api/stocks${search}`);
}

export async function fetchWatchlist(): Promise<WatchlistItem[]> {
  return request<WatchlistItem[]>("/api/watchlist");
}

export async function fetchAlertRules(): Promise<AlertRule[]> {
  return request<AlertRule[]>("/api/alerts");
}

export async function fetchAlertEvents(): Promise<AlertEvent[]> {
  return request<AlertEvent[]>("/api/notifications/events");
}

export async function fetchCurrentUser(): Promise<User> {
  return request<User>("/api/users/me");
}

export async function addWatchlistItem(stockId: string): Promise<WatchlistItem> {
  return request<WatchlistItem>("/api/watchlist", {
    method: "POST",
    body: JSON.stringify({ stockId }),
  });
}

export async function removeWatchlistItem(stockId: string): Promise<void> {
  return request<void>(`/api/watchlist/${stockId}`, {
    method: "DELETE",
  });
}

export async function createAlertRule(input: {
  stockId: string;
  type: AlertType;
  targetPrice?: number;
  changeRate?: number;
}): Promise<AlertRule> {
  return request<AlertRule>("/api/alerts", {
    method: "POST",
    body: JSON.stringify({
      stockId: input.stockId,
      type: input.type,
      targetPrice: input.targetPrice ?? null,
      changeRate: input.changeRate ?? null,
      repeatPolicy: "ONCE",
    }),
  });
}

export async function toggleAlertRule(ruleId: string, enabled: boolean): Promise<AlertRule> {
  return request<AlertRule>(`/api/alerts/${ruleId}`, {
    method: "PATCH",
    body: JSON.stringify({ enabled }),
  });
}

export async function deleteAlertRule(ruleId: string): Promise<void> {
  return request<void>(`/api/alerts/${ruleId}`, {
    method: "DELETE",
  });
}

export async function fetchNotificationChannels(): Promise<NotificationChannel[]> {
  return request<NotificationChannel[]>("/api/notifications/channels");
}

export async function createEmailChannel(email: string): Promise<NotificationChannel> {
  return request<NotificationChannel>("/api/notifications/channels/email", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}
