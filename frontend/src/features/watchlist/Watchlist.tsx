// 관심종목 패널 컴포넌트입니다.
// 관심종목별 현재가와 등락률을 표시해 사용자가 감시 대상을 빠르게 확인하게 합니다.
import { Star } from "lucide-react";
import { formatPrice, formatRate } from "@/lib/format";
import type { WatchlistItem } from "@/lib/types";

type Props = {
  items: WatchlistItem[];
};

export function Watchlist({ items }: Props) {
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
            <div className="list-row price-row" key={item.id}>
              <div>
                <strong>{item.stock.name}</strong>
                <span>{item.stock.symbol}</span>
              </div>
              <div className="price-cell">
                <strong>{formatPrice(item.currentPrice)}원</strong>
                <span className={isUp ? "rate-up" : "rate-down"}>{formatRate(item.changeRate)}</span>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
