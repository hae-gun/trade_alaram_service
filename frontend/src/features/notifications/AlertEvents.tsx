// 알림 발송 이력 패널 컴포넌트입니다.
// 이메일 채널 등록 폼과 조건 충족 후 기록된 알림 발송 이벤트를 표시합니다.
import { Send } from "lucide-react";
import { formatDateTime, formatPrice, formatRate } from "@/lib/format";
import type { AlertEvent, NotificationChannel } from "@/lib/types";

type Props = {
  events: AlertEvent[];
  channels: NotificationChannel[];
  email: string;
  onEmailChange: (email: string) => void;
  onCreateEmailChannel: () => void;
};

export function AlertEvents({ events, channels, email, onEmailChange, onCreateEmailChannel }: Props) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">History</p>
          <h2>발송 이력</h2>
        </div>
        <Send size={18} />
      </div>

      <div className="form-grid compact-form">
        <label>
          <span>이메일 채널</span>
          <input
            placeholder="admin@example.com"
            value={email}
            onChange={(event) => onEmailChange(event.target.value)}
          />
        </label>
        <button type="button" className="primary-button form-submit" onClick={onCreateEmailChannel}>
          채널 등록
        </button>
      </div>

      <div className="channel-list">
        {channels.map((channel) => (
          <span className="market-badge" key={channel.id}>
            {channel.type}: {channel.destination}
          </span>
        ))}
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
        {events.length === 0 && <div className="empty-state">발송 이력이 없습니다.</div>}
      </div>
    </section>
  );
}
