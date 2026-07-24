"use client"

import Link from "next/link"
import { useQuery } from "@tanstack/react-query"
import {
  AlertTriangleIcon,
  BookMarkedIcon,
  CircleDollarSignIcon,
  ClipboardListIcon,
  LibraryBigIcon,
  UsersIcon,
} from "lucide-react"

import { EmptyState } from "@/components/shared/empty-state"
import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { StatCard } from "@/components/shared/stat-card"
import { StatCardsSkeleton, TableSkeleton } from "@/components/shared/skeletons"
import { StatusBadge } from "@/components/shared/status-badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useApi } from "@/lib/api"
import { formatCurrency, formatDate } from "@/lib/format"
import type { DashboardStats, Loan, Page, TopBook } from "@/lib/types"

export default function DashboardPage() {
  const api = useApi()
  const stats = useQuery({ queryKey: ["dashboard"], queryFn: () => api.get<DashboardStats>("/reports/dashboard") })
  const topBooks = useQuery({
    queryKey: ["report", "top-books"],
    queryFn: () => api.get<TopBook[]>("/reports/top-books?limit=5"),
  })
  const overdue = useQuery({
    queryKey: ["loans", "overdue", "dashboard"],
    queryFn: () => api.get<Page<Loan>>("/loans?status=OVERDUE&size=6&sort=dueDate,asc"),
  })

  return (
    <div className="space-y-6">
      <PageHeader title="Bảng điều khiển" description="Tổng quan hoạt động thư viện" />

      <QueryState query={stats} skeleton={<StatCardsSkeleton count={6} />}>
        {(s) => (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
            <StatCard label="Đầu sách" value={s.totalBooks} icon={BookMarkedIcon} />
            <StatCard label="Bản sao" value={s.totalCopies} icon={LibraryBigIcon} />
            <StatCard label="Độc giả" value={s.totalMembers} icon={UsersIcon} />
            <StatCard label="Đang mượn" value={s.borrowedCount} icon={ClipboardListIcon} accent="warning" />
            <StatCard label="Quá hạn" value={s.overdueCount} icon={AlertTriangleIcon} accent="danger" />
            <StatCard
              label="Phạt đã thu (tháng)"
              value={formatCurrency(s.finesCollectedThisMonth)}
              icon={CircleDollarSignIcon}
              accent="success"
              hint={`${s.pendingReservations} đặt trước đang chờ`}
            />
          </div>
        )}
      </QueryState>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Sách quá hạn</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={overdue}
              skeleton={<TableSkeleton rows={4} />}
              isEmpty={(p) => p.content.length === 0}
              empty={
                <EmptyState
                  icon={AlertTriangleIcon}
                  title="Không có sách quá hạn"
                  description="Mọi phiếu mượn đều đang trong hạn."
                />
              }
            >
              {(p) => (
                <ul className="divide-y">
                  {p.content.map((loan) => (
                    <li key={loan.id} className="flex items-center justify-between gap-3 py-2.5 text-sm">
                      <div className="min-w-0">
                        <p className="truncate font-medium">{loan.bookTitle}</p>
                        <p className="text-xs text-muted-foreground">
                          {loan.memberName} · Hạn {formatDate(loan.dueDate)}
                        </p>
                      </div>
                      <StatusBadge status="OVERDUE" />
                    </li>
                  ))}
                </ul>
              )}
            </QueryState>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Sách được mượn nhiều</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={topBooks}
              skeleton={<TableSkeleton rows={4} />}
              isEmpty={(rows) => rows.length === 0}
              empty={<EmptyState icon={BookMarkedIcon} title="Chưa có dữ liệu mượn" />}
            >
              {(rows) => (
                <ol className="space-y-2.5">
                  {rows.map((book, index) => (
                    <li key={book.bookId} className="flex items-center gap-3 text-sm">
                      <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-medium">
                        {index + 1}
                      </span>
                      <Link href={`/books/${book.bookId}`} className="truncate font-medium hover:underline">
                        {book.title}
                      </Link>
                      <span className="ml-auto shrink-0 text-muted-foreground">{book.borrowCount} lượt</span>
                    </li>
                  ))}
                </ol>
              )}
            </QueryState>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
