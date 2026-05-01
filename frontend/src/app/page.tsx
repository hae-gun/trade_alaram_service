// MVP 대시보드 메인 페이지입니다.
// 종목, 관심종목, 알림 조건, 발송 이력을 한 화면에 배치해 첫 프로토타입 흐름을 확인합니다.
import { Bell, LineChart, Search, Star } from "lucide-react";
import { AlertEvents } from "@/features/notifications/AlertEvents";
import { AlertRules } from "@/features/alerts/AlertRules";
import { StockSearch } from "@/features/stocks/StockSearch";
import { Watchlist } from "@/features/watchlist/Watchlist";
import { fetchAlertEvents, fetchAlertRules, fetchStocks, fetchWatchlist } from "@/lib/api";

export default async function Home() {
  const [stocks, watchlist, alertRules, alertEvents] = await Promise.all([
    fetchStocks(),
    fetchWatchlist(),
    fetchAlertRules(),
    fetchAlertEvents(),
  ]);

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
          <div className="user-chip">Demo Investor</div>
        </header>

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
          <StockSearch stocks={stocks} />
          <Watchlist items={watchlist} />
          <AlertRules rules={alertRules} />
          <AlertEvents events={alertEvents} />
        </div>
      </section>
    </main>
  );
}
