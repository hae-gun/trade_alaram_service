// 종목 검색 패널 컴포넌트입니다.
// 현재는 서버에서 받은 종목 목록을 표시하며, 이후 검색 입력을 API 요청과 연결합니다.
import { Search } from "lucide-react";
import type { Stock } from "@/lib/types";

type Props = {
  stocks: Stock[];
};

export function StockSearch({ stocks }: Props) {
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
        <input placeholder="종목명 또는 코드 검색" />
      </label>

      <div className="list">
        {stocks.map((stock) => (
          <div className="list-row" key={stock.id}>
            <div>
              <strong>{stock.name}</strong>
              <span>{stock.symbol}</span>
            </div>
            <span className="market-badge">{stock.market}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
