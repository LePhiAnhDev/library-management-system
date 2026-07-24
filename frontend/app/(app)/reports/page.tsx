"use client"

import * as React from "react"
import { useQuery } from "@tanstack/react-query"
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts"
import {
  AlertTriangleIcon,
  BarChart3Icon,
  BookMarkedIcon,
  CircleDollarSignIcon,
  ClipboardListIcon,
  DownloadIcon,
  LibraryBigIcon,
  Loader2Icon,
  PackageIcon,
  UsersIcon,
} from "lucide-react"

import { EmptyState } from "@/components/shared/empty-state"
import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { StatCard } from "@/components/shared/stat-card"
import { StatCardsSkeleton, TableSkeleton } from "@/components/shared/skeletons"
import { StatusBadge } from "@/components/shared/status-badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import { useApi } from "@/lib/api"
import { formatCurrency, formatDate } from "@/lib/format"
import { handleMutationError } from "@/lib/form-errors"
import { cn } from "@/lib/utils"
import type {
  ActiveMember,
  DashboardStats,
  FinesSummary,
  InventoryRow,
  LoanTrendPoint,
  TopBook,
} from "@/lib/types"

const chartConfig = {
  count: { label: "Lượt mượn", color: "var(--chart-2)" },
} satisfies ChartConfig

function toInputDate(date: Date) {
  const offsetMs = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 10)
}

function defaultFrom() {
  const date = new Date()
  date.setDate(date.getDate() - 30)
  return toInputDate(date)
}

export default function ReportsPage() {
  const api = useApi()
  const [from, setFrom] = React.useState(defaultFrom)
  const [to, setTo] = React.useState(() => toInputDate(new Date()))
  const [exporting, setExporting] = React.useState<"loans" | "fines" | null>(null)

  const range = `from=${from}&to=${to}`

  const dashboard = useQuery({
    queryKey: ["report", "dashboard"],
    queryFn: () => api.get<DashboardStats>("/reports/dashboard"),
  })
  const finesSummary = useQuery({
    queryKey: ["report", "fines-summary", { from, to }],
    queryFn: () => api.get<FinesSummary>(`/reports/fines-summary?${range}`),
  })
  const loansOverTime = useQuery({
    queryKey: ["report", "loans-over-time", { from, to }],
    queryFn: () => api.get<LoanTrendPoint[]>(`/reports/loans-over-time?${range}`),
  })
  const inventory = useQuery({
    queryKey: ["report", "inventory"],
    queryFn: () => api.get<InventoryRow[]>("/reports/inventory"),
  })
  const topBooks = useQuery({
    queryKey: ["report", "top-books", { from, to }],
    queryFn: () => api.get<TopBook[]>(`/reports/top-books?${range}&limit=10`),
  })
  const activeMembers = useQuery({
    queryKey: ["report", "active-members", { from, to }],
    queryFn: () => api.get<ActiveMember[]>(`/reports/active-members?${range}&limit=10`),
  })

  const handleExport = async (kind: "loans" | "fines") => {
    setExporting(kind)
    try {
      const blob = await api.download(`/reports/export/${kind}?${range}`)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement("a")
      anchor.href = url
      anchor.download = `${kind === "loans" ? "phieu-muon" : "phat"}-${from}-${to}.csv`
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      URL.revokeObjectURL(url)
    } catch (error) {
      handleMutationError(error)
    } finally {
      setExporting(null)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Báo cáo"
        description="Thống kê và phân tích hoạt động thư viện"
        actions={
          <>
            <Button variant="outline" onClick={() => handleExport("loans")} disabled={exporting !== null}>
              {exporting === "loans" ? <Loader2Icon className="size-4 animate-spin" /> : <DownloadIcon />}
              Xuất CSV phiếu mượn
            </Button>
            <Button variant="outline" onClick={() => handleExport("fines")} disabled={exporting !== null}>
              {exporting === "fines" ? <Loader2Icon className="size-4 animate-spin" /> : <DownloadIcon />}
              Xuất CSV phạt
            </Button>
          </>
        }
      />

      <div className="flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="from">Từ ngày</Label>
          <Input
            id="from"
            type="date"
            value={from}
            max={to}
            onChange={(event) => setFrom(event.target.value)}
            className="w-auto"
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="to">Đến ngày</Label>
          <Input
            id="to"
            type="date"
            value={to}
            min={from}
            onChange={(event) => setTo(event.target.value)}
            className="w-auto"
          />
        </div>
      </div>

      <QueryState query={dashboard} skeleton={<StatCardsSkeleton count={6} />}>
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
            />
          </div>
        )}
      </QueryState>

      <Card>
        <CardHeader>
          <CardTitle>Tổng hợp phạt</CardTitle>
        </CardHeader>
        <CardContent>
          <QueryState
            query={finesSummary}
            skeleton={
              <div className="grid gap-4 sm:grid-cols-3">
                {Array.from({ length: 3 }).map((_, index) => (
                  <Skeleton key={index} className="h-20 w-full rounded-lg" />
                ))}
              </div>
            }
          >
            {(summary) => (
              <div className="grid gap-4 sm:grid-cols-3">
                <SummaryTile label="Đã thu" value={formatCurrency(summary.collected)} tone="text-success" />
                <SummaryTile label="Đã miễn" value={formatCurrency(summary.waived)} tone="text-warning-foreground" />
                <SummaryTile label="Chưa thu" value={formatCurrency(summary.unpaidTotal)} tone="text-destructive" />
              </div>
            )}
          </QueryState>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Lượt mượn theo thời gian</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={loansOverTime}
              skeleton={<Skeleton className="aspect-video max-h-[280px] w-full rounded-xl" />}
              isEmpty={(rows) => rows.length === 0}
              empty={
                <EmptyState
                  icon={BarChart3Icon}
                  title="Chưa có dữ liệu"
                  description="Không có lượt mượn trong khoảng thời gian đã chọn."
                />
              }
            >
              {(rows) => (
                <ChartContainer config={chartConfig} className="aspect-auto h-[280px] w-full">
                  <BarChart accessibilityLayer data={rows} margin={{ left: 4, right: 4, top: 8 }}>
                    <CartesianGrid vertical={false} />
                    <XAxis
                      dataKey="date"
                      tickLine={false}
                      axisLine={false}
                      tickMargin={8}
                      minTickGap={24}
                      tickFormatter={(value) => formatDate(value as string)}
                    />
                    <YAxis tickLine={false} axisLine={false} width={32} allowDecimals={false} />
                    <ChartTooltip content={<ChartTooltipContent labelFormatter={(value) => formatDate(value as string)} />} />
                    <Bar dataKey="count" fill="var(--color-count)" radius={4} />
                  </BarChart>
                </ChartContainer>
              )}
            </QueryState>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Tồn kho theo tình trạng</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={inventory}
              skeleton={<TableSkeleton rows={5} />}
              isEmpty={(rows) => rows.length === 0}
              empty={<EmptyState icon={PackageIcon} title="Chưa có dữ liệu tồn kho" />}
            >
              {(rows) => (
                <ul className="divide-y">
                  {rows.map((row) => (
                    <li key={row.status} className="flex items-center justify-between py-2.5 text-sm">
                      <StatusBadge status={row.status} />
                      <span className="font-medium tabular-nums">{row.count}</span>
                    </li>
                  ))}
                </ul>
              )}
            </QueryState>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Sách mượn nhiều nhất</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={topBooks}
              skeleton={<TableSkeleton rows={5} />}
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
                      <span className="truncate font-medium">{book.title}</span>
                      <span className="ml-auto shrink-0 text-muted-foreground">{book.borrowCount} lượt</span>
                    </li>
                  ))}
                </ol>
              )}
            </QueryState>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Độc giả tích cực</CardTitle>
          </CardHeader>
          <CardContent>
            <QueryState
              query={activeMembers}
              skeleton={<TableSkeleton rows={5} />}
              isEmpty={(rows) => rows.length === 0}
              empty={<EmptyState icon={UsersIcon} title="Chưa có dữ liệu" />}
            >
              {(rows) => (
                <ol className="space-y-2.5">
                  {rows.map((member, index) => (
                    <li key={member.memberId} className="flex items-center gap-3 text-sm">
                      <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-medium">
                        {index + 1}
                      </span>
                      <div className="min-w-0">
                        <p className="truncate font-medium">{member.fullName}</p>
                        <p className="text-xs text-muted-foreground">{member.memberCode}</p>
                      </div>
                      <span className="ml-auto shrink-0 text-muted-foreground">{member.loanCount} lượt</span>
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

function SummaryTile({ label, value, tone }: { label: string; value: string; tone: string }) {
  return (
    <div className="rounded-lg border p-4">
      <p className="text-sm text-muted-foreground">{label}</p>
      <p className={cn("font-heading text-2xl font-semibold tabular-nums", tone)}>{value}</p>
    </div>
  )
}
