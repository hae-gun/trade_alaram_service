// 관심종목 패널 컴포넌트입니다.
// 관심종목별 현재가/등락률을 표시하고 삭제 또는 알림 대상 선택 액션을 제공합니다.
import { Bell, Star } from "lucide-react";
import { formatPrice, formatRate } from "@/lib/format";
import type { WatchlistItem } from "@/lib/types";

type Props = {
  items: WatchlistItem[];
  selectedStockId: string;
  onSelectStock: (stockId: string) => void;
  onRemoveWatchlist: (stockId: string) => void;
};

export function Watchlist({ items, selectedStockId, onSelectStock, onRemoveWatchlist }: Props) {
  return (
    <section id="watchlist" className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Watchlist</p>
          <h2>관심종목</h2>
        </div>
        <Star size={18} />
      </div>

      <div className="list">
        {items.map((item) => {
          const isUp = item.changeRate >= 0;
          return (
            <div className={`list-row price-row ${selectedStockId === item.stock.id ? "selected-row" : ""}`} key={item.id}>
              <div>
                <strong>{item.stock.name}</strong>
                <span>{item.stock.symbol}</span>
              </div>
              <div className="row-actions">
                <div className="price-cell">
                  <strong>{formatPrice(item.currentPrice)}원</strong>
                  <span className={isUp ? "rate-up" : "rate-down"}>{formatRate(item.changeRate)}</span>
                </div>
                <button type="button" className="icon-button" onClick={() => onSelectStock(item.stock.id)}>
                  <Bell size={16} />
                  알림 설정
                </button>
                <button type="button" className="danger-button" onClick={() => onRemoveWatchlist(item.stock.id)}>
                  삭제
                </button>
              </div>
            </div>
          );
        })}
        {items.length === 0 && <div className="empty-state">등록된 관심종목이 없습니다.</div>}
      </div>
    </section>
  );
}
