// Vietnamese labels for enum values shown on Base UI <Select> triggers.
//
// Base UI's <Select.Value> renders the raw stored value (e.g. "BORROWED") unless it is
// given a formatter function, so every Select feeds its value through selectLabel(). The
// labels here mirror the <SelectItem> option text exactly, so the closed trigger and the
// open option list always read the same. Status/type values are shared across screens
// (a loan and its fine both use "OVERDUE" -> "Quá hạn"), which is why they live in one map.

export const ENUM_LABELS: Record<string, string> = {
  // Reference record status
  ACTIVE: "Hoạt động",
  INACTIVE: "Ngừng",
  // Book copy status
  AVAILABLE: "Sẵn sàng",
  RESERVED: "Đang giữ",
  MAINTENANCE: "Bảo trì",
  // Member status
  SUSPENDED: "Tạm khóa",
  EXPIRED: "Hết hạn",
  // Loan status
  BORROWED: "Đang mượn",
  RETURNED: "Đã trả",
  OVERDUE: "Quá hạn",
  // Reservation status
  PENDING: "Chờ",
  READY: "Sẵn sàng nhận",
  FULFILLED: "Đã nhận",
  CANCELLED: "Đã hủy",
  // Fine status
  UNPAID: "Chưa thu",
  PAID: "Đã thu",
  WAIVED: "Đã miễn",
  // Return condition / fine type
  NORMAL: "Bình thường",
  LOST: "Mất",
  DAMAGED: "Hỏng",
  // Member card type
  REGULAR: "Thường",
  STUDENT: "Sinh viên",
  PREMIUM: "Premium",
}

/**
 * Formats a Select value for display on the trigger. `all` is the label for the "ALL"
 * sentinel used by filter selects; its wording varies per screen ("Tất cả trạng thái",
 * "Tất cả loại thẻ", ...) so each caller passes its own. Selects without an ALL option
 * omit it. Unknown values fall back to the raw string rather than rendering blank.
 */
export function selectLabel(value: unknown, all?: string): string {
  if (value == null || value === "ALL") return all ?? ""
  return ENUM_LABELS[String(value)] ?? String(value)
}
