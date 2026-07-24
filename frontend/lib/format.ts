const currencyFormat = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
})

const dateFormat = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
})

const dateTimeFormat = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
})

export function formatCurrency(amount: number | null | undefined): string {
  return currencyFormat.format(amount ?? 0)
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return "—"
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? "—" : dateFormat.format(date)
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return "—"
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? "—" : dateTimeFormat.format(date)
}
