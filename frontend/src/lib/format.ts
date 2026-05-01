// 화면 표시용 포맷터 모음입니다.
// 가격, 등락률, 날짜를 한국어 주식 서비스 UX에 맞게 변환합니다.
export function formatPrice(value: number): string {
  return new Intl.NumberFormat("ko-KR", {
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatRate(value: number): string {
  const prefix = value > 0 ? "+" : "";
  return `${prefix}${value.toFixed(2)}%`;
}

export function formatDateTime(value: string | null): string {
  if (!value) return "-";
  return new Intl.DateTimeFormat("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}
