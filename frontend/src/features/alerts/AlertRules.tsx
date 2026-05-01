// 알림 조건 패널 컴포넌트입니다.
// 종목별 가격/등락률 조건과 활성 상태를 사용자에게 보여줍니다.
import { Bell } from "lucide-react";
import { formatDateTime, formatPrice } from "@/lib/format";
import type { AlertRule, AlertType } from "@/lib/types";

type Props = {
  rules: AlertRule[];
};

const alertTypeLabels: Record<AlertType, string> = {
  ABOVE_PRICE: "목표가 이상",
  BELOW_PRICE: "목표가 이하",
  UP_RATE: "상승률",
  DOWN_RATE: "하락률",
};

export function AlertRules({ rules }: Props) {
  return (
    <section id="alerts" className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Rules</p>
          <h2>알림 조건</h2>
        </div>
        <Bell size={18} />
      </div>

      <div className="list">
        {rules.map((rule) => (
          <div className="list-row" key={rule.id}>
            <div>
              <strong>{rule.stock.name}</strong>
              <span>
                {alertTypeLabels[rule.type]} · {rule.targetPrice ? `${formatPrice(rule.targetPrice)}원` : `${rule.changeRate}%`}
              </span>
            </div>
            <span className={rule.enabled ? "status-on" : "status-off"}>
              {rule.enabled ? "활성" : `발송 ${formatDateTime(rule.lastTriggeredAt)}`}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}
