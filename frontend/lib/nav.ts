import {
  ArrowLeftRightIcon,
  BarChart3Icon,
  BookMarkedIcon,
  BookmarkCheckIcon,
  Building2Icon,
  CircleDollarSignIcon,
  ClipboardListIcon,
  FolderTreeIcon,
  LayoutDashboardIcon,
  type LucideIcon,
  PenLineIcon,
  SettingsIcon,
  UsersIcon,
} from "lucide-react"

export interface NavItem {
  title: string
  href: string
  icon: LucideIcon
}

export interface NavGroup {
  label: string
  items: NavItem[]
}

export const NAV_GROUPS: NavGroup[] = [
  {
    label: "Thư viện",
    items: [
      { title: "Bảng điều khiển", href: "/", icon: LayoutDashboardIcon },
      { title: "Danh mục sách", href: "/books", icon: BookMarkedIcon },
      { title: "Độc giả", href: "/members", icon: UsersIcon },
    ],
  },
  {
    label: "Lưu thông",
    items: [
      { title: "Mượn / Trả", href: "/circulation", icon: ArrowLeftRightIcon },
      { title: "Phiếu mượn", href: "/loans", icon: ClipboardListIcon },
      { title: "Đặt trước", href: "/reservations", icon: BookmarkCheckIcon },
      { title: "Phạt", href: "/fines", icon: CircleDollarSignIcon },
    ],
  },
  {
    label: "Quản trị",
    items: [
      { title: "Thể loại", href: "/categories", icon: FolderTreeIcon },
      { title: "Tác giả", href: "/authors", icon: PenLineIcon },
      { title: "Nhà xuất bản", href: "/publishers", icon: Building2Icon },
      { title: "Báo cáo", href: "/reports", icon: BarChart3Icon },
      { title: "Cấu hình", href: "/settings", icon: SettingsIcon },
    ],
  },
]

export const NAV_ITEMS: NavItem[] = NAV_GROUPS.flatMap((group) => group.items)

export function isActivePath(pathname: string, href: string): boolean {
  if (href === "/") {
    return pathname === "/"
  }
  return pathname === href || pathname.startsWith(`${href}/`)
}

export const SEGMENT_LABELS: Record<string, string> = {
  books: "Danh mục sách",
  members: "Độc giả",
  categories: "Thể loại",
  authors: "Tác giả",
  publishers: "Nhà xuất bản",
  loans: "Phiếu mượn",
  circulation: "Mượn / Trả",
  reservations: "Đặt trước",
  fines: "Phạt",
  reports: "Báo cáo",
  settings: "Cấu hình",
}
