// 알림 조건 패널 컴포넌트입니다.
// 종목별 가격/등락률 조건 생성 폼과 기존 조건의 토글/삭제 액션을 제공합니다.
import { Bell } from "lucide-react";
import { formatDateTime, formatPrice } from "@/lib/format";
import type { AlertRule, AlertType } from "@/lib/types";

type Props = {
  rules: AlertRule[];
  selectedStockName: string;
  alertType: AlertType;
  alertValue: string;
  onAlertTypeChange: (type: AlertType) => void;
  onAlertValueChange: (value: string) => void;
  onCreateAlert: () => void;
  onToggleAlert: (ruleId: string, enabled: boolean) => void;
  onDeleteAlert: (ruleId: string) => void;
};

const alertTypeLabels: Record<AlertType, string> = {
  ABOVE_PRICE: "목표가 이상",
  BELOW_PRICE: "목표가 이하",
  UP_RATE: "상승률",
  DOWN_RATE: "하락률",
};

export function AlertRules({
  rules,
  selectedStockName,
  alertType,
  alertValue,
  onAlertTypeChange,
  onAlertValueChange,
  onCreateAlert,
  onToggleAlert,
  onDeleteAlert,
}: Props) {
  return (
    <section id="alerts" className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Rules</p>
          <h2>알림 조건</h2>
        </div>
        <Bell size={18} />
      </div>

      <div className="form-grid">
        <label>
          <span>대상 종목</span>
          <input value={selectedStockName || "종목을 선택하세요"} readOnly />
        </label>
        <label>
          <span>조건</span>
          <select value={alertType} onChange={(event) => onAlertTypeChange(event.target.value as AlertType)}>
            <option value="ABOVE_PRICE">목표가 이상</option>
            <option value="BELOW_PRICE">목표가 이하</option>
            <option value="UP_RATE">상승률 이상</option>
            <option value="DOWN_RATE">하락률 이상</option>
          </select>
        </label>
        <label>
          <span>{alertType === "ABOVE_PRICE" || alertType === "BELOW_PRICE" ? "가격" : "등락률"}</span>
          <input
            inputMode="decimal"
            placeholder={alertType === "ABOVE_PRICE" || alertType === "BELOW_PRICE" ? "예: 75000" : "예: 3"}
            value={alertValue}
            onChange={(event) => onAlertValueChange(event.target.value)}
          />
        </label>
        <button type="button" className="primary-button form-submit" onClick={onCreateAlert}>
          알림 생성
        </button>
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
            <div className="row-actions">
              <span className={rule.enabled ? "status-on" : "status-off"}>
                {rule.enabled ? "활성" : `발송 ${formatDateTime(rule.lastTriggeredAt)}`}
              </span>
              <button type="button" className="icon-button" onClick={() => onToggleAlert(rule.id, !rule.enabled)}>
                {rule.enabled ? "끄기" : "켜기"}
              </button>
              <button type="button" className="danger-button" onClick={() => onDeleteAlert(rule.id)}>
                삭제
              </button>
            </div>
          </div>
        ))}
        {rules.length === 0 && <div className="empty-state">생성된 알림 조건이 없습니다.</div>}
      </div>
    </section>
  );
}
