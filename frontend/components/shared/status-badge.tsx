import { cn } from "@/lib/utils"

type Tone = "success" | "warning" | "danger" | "info" | "neutral"

const toneClass: Record<Tone, string> = {
  success: "bg-success/10 text-success ring-success/25",
  warning: "bg-warning/20 text-warning-foreground ring-warning/40",
  danger: "bg-destructive/10 text-destructive ring-destructive/25",
  info: "bg-info/10 text-info ring-info/25",
  neutral: "bg-muted text-muted-foreground ring-border",
}

// Semantic mapping shared across the whole app so a status always reads the same way.
const statusMap: Record<string, { tone: Tone; label: string }> = {
  // Reference records
  ACTIVE: { tone: "success", label: "Hoạt động" },
  INACTIVE: { tone: "neutral", label: "Ngừng" },
  // Book copies
  AVAILABLE: { tone: "success", label: "Sẵn sàng" },
  RESERVED: { tone: "info", label: "Đang giữ" },
  LOST: { tone: "danger", label: "Mất" },
  DAMAGED: { tone: "danger", label: "Hỏng" },
  MAINTENANCE: { tone: "neutral", label: "Bảo trì" },
  // Loans
  BORROWED: { tone: "warning", label: "Đang mượn" },
  RETURNED: { tone: "success", label: "Đã trả" },
  OVERDUE: { tone: "danger", label: "Quá hạn" },
  // Members
  SUSPENDED: { tone: "warning", label: "Tạm khóa" },
  EXPIRED: { tone: "danger", label: "Hết hạn" },
  // Reservations
  PENDING: { tone: "warning", label: "Chờ" },
  READY: { tone: "info", label: "Sẵn sàng nhận" },
  FULFILLED: { tone: "success", label: "Đã nhận" },
  CANCELLED: { tone: "neutral", label: "Đã hủy" },
  // Fines
  UNPAID: { tone: "danger", label: "Chưa thu" },
  PAID: { tone: "success", label: "Đã thu" },
  WAIVED: { tone: "neutral", label: "Đã miễn" },
}

export function StatusBadge({ status, className }: { status: string; className?: string }) {
  const entry = statusMap[status] ?? { tone: "neutral" as Tone, label: status }
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset",
        toneClass[entry.tone],
        className
      )}
    >
      {entry.label}
    </span>
  )
}
