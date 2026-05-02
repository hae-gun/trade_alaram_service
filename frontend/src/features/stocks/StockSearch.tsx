// 종목 검색 패널 컴포넌트입니다.
// 백엔드 종목 검색 API 결과를 표시하고 관심종목 추가 액션을 호출합니다.
import { Search } from "lucide-react";
import type { Stock } from "@/lib/types";

type Props = {
  stocks: Stock[];
  query: string;
  selectedStockId: string;
  onQueryChange: (query: string) => void;
  onSelectStock: (stockId: string) => void;
  onAddWatchlist: (stockId: string) => void;
};

export function StockSearch({
  stocks,
  query,
  selectedStockId,
  onQueryChange,
  onSelectStock,
  onAddWatchlist,
}: Props) {
  return (
    <section id="stocks" className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Stocks</p>
          <h2>종목 검색</h2>
        </div>
        <Search size={18} />
      </div>

      <label className="search-box">
        <Search size={18} />
        <input
          placeholder="종목명 또는 코드 검색"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
        />
      </label>

      <div className="list">
        {stocks.map((stock) => (
          <div className={`list-row ${selectedStockId === stock.id ? "selected-row" : ""}`} key={stock.id}>
            <div>
              <strong>{stock.name}</strong>
              <span>{stock.symbol}</span>
            </div>
            <div className="row-actions">
              <span className="market-badge">{stock.market}</span>
              <button type="button" className="icon-button" onClick={() => onSelectStock(stock.id)}>
                선택
              </button>
              <button type="button" className="primary-button" onClick={() => onAddWatchlist(stock.id)}>
                관심등록
              </button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
