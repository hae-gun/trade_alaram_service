// 종목 검색 패널 컴포넌트입니다.
// 백엔드 종목 검색 API 결과를 표시하고 관심종목 추가 액션을 호출합니다.
import { useMemo, useState } from "react";
import { Bell, ChevronLeft, ChevronRight, Search } from "lucide-react";
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
  const pageSize = 6;
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(stocks.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const pagedStocks = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize;
    return stocks.slice(startIndex, startIndex + pageSize);
  }, [stocks, currentPage]);

  function handleQueryChange(nextQuery: string) {
    setPage(1);
    onQueryChange(nextQuery);
  }

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
          onChange={(event) => handleQueryChange(event.target.value)}
        />
      </label>

      <div className="list">
        {pagedStocks.map((stock) => (
          <div className={`list-row ${selectedStockId === stock.id ? "selected-row" : ""}`} key={stock.id}>
            <div>
              <strong>{stock.name}</strong>
              <span>{stock.symbol}</span>
            </div>
            <div className="row-actions">
              <span className="market-badge">{stock.market}</span>
              <button type="button" className="icon-button" onClick={() => onSelectStock(stock.id)}>
                <Bell size={16} />
                알림 설정
              </button>
              <button type="button" className="primary-button" onClick={() => onAddWatchlist(stock.id)}>
                관심등록
              </button>
            </div>
          </div>
        ))}
        {stocks.length === 0 && <div className="empty-state">검색 결과가 없습니다.</div>}
      </div>

      {stocks.length > pageSize && (
        <div className="pagination" aria-label="종목 검색 페이지">
          <button
            type="button"
            className="icon-button"
            onClick={() => setPage((value) => Math.max(1, value - 1))}
            disabled={currentPage === 1}
            aria-label="이전 페이지"
          >
            <ChevronLeft size={16} />
          </button>
          <span>
            {currentPage} / {totalPages}
          </span>
          <button
            type="button"
            className="icon-button"
            onClick={() => setPage((value) => Math.min(totalPages, value + 1))}
            disabled={currentPage === totalPages}
            aria-label="다음 페이지"
          >
            <ChevronRight size={16} />
          </button>
        </div>
      )}
    </section>
  );
}
