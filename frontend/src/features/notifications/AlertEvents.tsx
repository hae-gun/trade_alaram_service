// 알림 발송 이력 패널 컴포넌트입니다.
// 조건 충족 후 발송된 이벤트의 가격, 등락률, 발송 시각을 표시합니다.
import { Send } from "lucide-react";
import { formatDateTime, formatPrice, formatRate } from "@/lib/format";
import type { AlertEvent } from "@/lib/types";

type Props = {
  events: AlertEvent[];
};

export function AlertEvents({ events }: Props) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">History</p>
          <h2>발송 이력</h2>
        </div>
        <Send size={18} />
      </div>

      <div className="list">
        {events.map((event) => (
          <div className="list-row" key={event.id}>
            <div>
              <strong>{event.stockName}</strong>
              <span>
                {formatPrice(event.triggerPrice)}원 · {formatRate(event.triggerChangeRate)}
              </span>
            </div>
            <span>{formatDateTime(event.sentAt)}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
