"use client";

// MVP 대시보드의 클라이언트 상태와 백엔드 API 연결을 담당합니다.
// admin 사용자 기준으로 종목 검색, 관심종목, 알림 조건, 알림 채널/이력을 한 화면에서 조작합니다.
import { useCallback, useEffect, useMemo, useState } from "react";
import { Bell, LineChart, RefreshCw, Search, Star } from "lucide-react";
import { AlertRules } from "@/features/alerts/AlertRules";
import { AlertEvents } from "@/features/notifications/AlertEvents";
import { StockSearch } from "@/features/stocks/StockSearch";
import { Watchlist } from "@/features/watchlist/Watchlist";
import {
  addWatchlistItem,
  createAlertRule,
  createEmailChannel,
  deleteAlertRule,
  fetchAlertEvents,
  fetchAlertRules,
  fetchCurrentUser,
  fetchNotificationChannels,
  fetchStocks,
  fetchWatchlist,
  removeWatchlistItem,
  toggleAlertRule,
} from "@/lib/api";
import type { AlertEvent, AlertRule, AlertType, NotificationChannel, Stock, User, WatchlistItem } from "@/lib/types";

export function Dashboard() {
  const [user, setUser] = useState<User | null>(null);
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [watchlist, setWatchlist] = useState<WatchlistItem[]>([]);
  const [alertRules, setAlertRules] = useState<AlertRule[]>([]);
  const [alertEvents, setAlertEvents] = useState<AlertEvent[]>([]);
  const [channels, setChannels] = useState<NotificationChannel[]>([]);
  const [query, setQuery] = useState("");
  const [selectedStockId, setSelectedStockId] = useState("");
  const [alertType, setAlertType] = useState<AlertType>("ABOVE_PRICE");
  const [alertValue, setAlertValue] = useState("");
  const [email, setEmail] = useState("admin@tradealarm.local");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");

  const selectedStock = useMemo(() => {
    return stocks.find((stock) => stock.id === selectedStockId)
      ?? watchlist.find((item) => item.stock.id === selectedStockId)?.stock
      ?? null;
  }, [selectedStockId, stocks, watchlist]);

  const loadDashboard = useCallback(async (nextQuery = "") => {
    setLoading(true);
    setMessage("");
    try {
      const [nextUser, nextStocks, nextWatchlist, nextRules, nextEvents, nextChannels] = await Promise.all([
        fetchCurrentUser(),
        fetchStocks(nextQuery),
        fetchWatchlist(),
        fetchAlertRules(),
        fetchAlertEvents(),
        fetchNotificationChannels(),
      ]);

      setUser(nextUser);
      setStocks(nextStocks);
      setWatchlist(nextWatchlist);
      setAlertRules(nextRules);
      setAlertEvents(nextEvents);
      setChannels(nextChannels);
      setSelectedStockId((current) => current || nextWatchlist[0]?.stock.id || nextStocks[0]?.id || "");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadDashboard("");
  }, [loadDashboard]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadDashboard(query);
    }, 250);

    return () => window.clearTimeout(timer);
  }, [query, loadDashboard]);

  async function runAction(action: () => Promise<void>, successMessage: string) {
    setBusy(true);
    setMessage("");
    try {
      await action();
      setMessage(successMessage);
      await loadDashboard(query);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "요청 처리 중 오류가 발생했습니다.");
    } finally {
      setBusy(false);
    }
  }

  function handleCreateAlert() {
    if (!selectedStockId) {
      setMessage("알림을 만들 종목을 먼저 선택하세요.");
      return;
    }

    const numericValue = Number(alertValue);
    if (!Number.isFinite(numericValue) || numericValue <= 0) {
      setMessage("알림 조건 값은 0보다 큰 숫자로 입력하세요.");
      return;
    }

    const isPriceAlert = alertType === "ABOVE_PRICE" || alertType === "BELOW_PRICE";
    void runAction(
      async () => {
        await createAlertRule({
          stockId: selectedStockId,
          type: alertType,
          targetPrice: isPriceAlert ? numericValue : undefined,
          changeRate: isPriceAlert ? undefined : numericValue,
        });
        setAlertValue("");
      },
      "알림 조건을 생성했습니다.",
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <LineChart size={24} />
          <span>Trade Alarm</span>
        </div>
        <nav className="nav-list" aria-label="주요 메뉴">
          <a href="#stocks"><Search size={18} />종목</a>
          <a href="#watchlist"><Star size={18} />관심종목</a>
          <a href="#alerts"><Bell size={18} />알림</a>
        </nav>
      </aside>

      <section className="content">
        <header className="topbar">
          <div>
            <p className="eyebrow">MVP Dashboard</p>
            <h1>국내 주식 가격 알림</h1>
          </div>
          <div className="topbar-actions">
            <button type="button" className="icon-button" disabled={loading || busy} onClick={() => void loadDashboard(query)}>
              <RefreshCw size={16} />
              새로고침
            </button>
            <div className="user-chip">{user?.nickname ?? "admin"}</div>
          </div>
        </header>

        {message && <div className="toast">{message}</div>}

        <section className="metric-grid" aria-label="서비스 현황">
          <div className="metric">
            <span>관심종목</span>
            <strong>{watchlist.length}</strong>
          </div>
          <div className="metric">
            <span>활성 알림</span>
            <strong>{alertRules.filter((rule) => rule.enabled).length}</strong>
          </div>
          <div className="metric">
            <span>발송 이력</span>
            <strong>{alertEvents.length}</strong>
          </div>
        </section>

        <div className="dashboard-grid">
          <StockSearch
            stocks={stocks}
            query={query}
            selectedStockId={selectedStockId}
            onQueryChange={setQuery}
            onSelectStock={setSelectedStockId}
            onAddWatchlist={(stockId) => void runAction(
              async () => {
                await addWatchlistItem(stockId);
                setSelectedStockId(stockId);
              },
              "관심종목에 추가했습니다.",
            )}
          />
          <Watchlist
            items={watchlist}
            selectedStockId={selectedStockId}
            onSelectStock={setSelectedStockId}
            onRemoveWatchlist={(stockId) => void runAction(
              async () => {
                await removeWatchlistItem(stockId);
              },
              "관심종목에서 삭제했습니다.",
            )}
          />
          <AlertRules
            rules={alertRules}
            selectedStockName={selectedStock?.name ?? ""}
            alertType={alertType}
            alertValue={alertValue}
            onAlertTypeChange={setAlertType}
            onAlertValueChange={setAlertValue}
            onCreateAlert={handleCreateAlert}
            onToggleAlert={(ruleId, enabled) => void runAction(
              async () => {
                await toggleAlertRule(ruleId, enabled);
              },
              "알림 상태를 변경했습니다.",
            )}
            onDeleteAlert={(ruleId) => void runAction(
              async () => {
                await deleteAlertRule(ruleId);
              },
              "알림 조건을 삭제했습니다.",
            )}
          />
          <AlertEvents
            events={alertEvents}
            channels={channels}
            email={email}
            onEmailChange={setEmail}
            onCreateEmailChannel={() => void runAction(
              async () => {
                await createEmailChannel(email);
              },
              "이메일 채널을 등록했습니다.",
            )}
          />
        </div>
      </section>
    </main>
  );
}
