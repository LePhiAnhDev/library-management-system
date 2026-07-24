"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import {
  ClipboardListIcon,
  Loader2Icon,
  MoreHorizontalIcon,
  RefreshCwIcon,
  Undo2Icon,
} from "lucide-react"
import { toast } from "sonner"

import { DataTable, DataTablePagination, type Column, type SortState } from "@/components/shared/data-table"
import { EmptyState } from "@/components/shared/empty-state"
import { FormField } from "@/components/shared/form-field"
import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { TableSkeleton } from "@/components/shared/skeletons"
import { StatusBadge } from "@/components/shared/status-badge"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { useApi } from "@/lib/api"
import { formatDate } from "@/lib/format"
import { handleMutationError } from "@/lib/form-errors"
import type { Loan, LoanStatus, Page } from "@/lib/types"

const PAGE_SIZE = 20

type ReturnCondition = "NORMAL" | "LOST" | "DAMAGED"

export default function LoansPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [status, setStatus] = React.useState<LoanStatus | "ALL">("ALL")
  const [from, setFrom] = React.useState("")
  const [to, setTo] = React.useState("")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "createdAt", dir: "desc" })
  const [returning, setReturning] = React.useState<Loan | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["loans", { debouncedSearch, status, from, to, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (status !== "ALL") params.set("status", status)
      if (from) params.set("from", from)
      if (to) params.set("to", to)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Loan>>(`/loans?${params.toString()}`)
    },
  })

  const renewMutation = useMutation({
    mutationFn: (id: number) => api.post<Loan>(`/loans/${id}/renew`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["loans"] })
      toast.success("Đã gia hạn")
    },
    onError: (error) => handleMutationError(error),
  })

  const toggleSort = (field: string) =>
    setSort((prev) => ({ field, dir: prev.field === field && prev.dir === "asc" ? "desc" : "asc" }))

  const columns: Column<Loan>[] = [
    { key: "code", header: "Mã phiếu", sortable: true, cell: (l) => <span className="font-medium">{l.code}</span> },
    {
      key: "bookTitle",
      header: "Sách",
      className: "max-w-xs",
      cell: (l) => <span className="line-clamp-1">{l.bookTitle}</span>,
    },
    {
      key: "memberName",
      header: "Độc giả",
      cell: (l) => (
        <div className="flex flex-col">
          <span>{l.memberName}</span>
          <span className="text-xs text-muted-foreground">{l.memberCode}</span>
        </div>
      ),
    },
    { key: "borrowDate", header: "Ngày mượn", sortable: true, cell: (l) => formatDate(l.borrowDate) },
    { key: "dueDate", header: "Hạn trả", cell: (l) => formatDate(l.dueDate) },
    { key: "status", header: "Trạng thái", cell: (l) => <StatusBadge status={l.status} /> },
    {
      key: "renewCount",
      header: "Gia hạn",
      className: "text-center",
      cell: (l) => <span className="tabular-nums">{l.renewCount}</span>,
    },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (l) =>
        l.status === "RETURNED" ? null : (
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
              <MoreHorizontalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => renewMutation.mutate(l.id)}>
                <RefreshCwIcon /> Gia hạn
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setReturning(l)}>
                <Undo2Icon /> Trả sách
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader title="Phiếu mượn" description="Theo dõi và xử lý phiếu mượn sách" />

      <div className="flex flex-wrap items-center gap-3">
        <Input
          placeholder="Tìm theo mã phiếu..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="max-w-xs"
        />
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as LoanStatus | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            <SelectItem value="BORROWED">Đang mượn</SelectItem>
            <SelectItem value="RETURNED">Đã trả</SelectItem>
            <SelectItem value="OVERDUE">Quá hạn</SelectItem>
          </SelectContent>
        </Select>
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Từ</span>
          <Input
            type="date"
            value={from}
            onChange={(event) => {
              setFrom(event.target.value)
              setPage(0)
            }}
            className="w-40"
          />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Đến</span>
          <Input
            type="date"
            value={to}
            onChange={(event) => {
              setTo(event.target.value)
              setPage(0)
            }}
            className="w-40"
          />
        </div>
      </div>

      <QueryState
        query={query}
        skeleton={<TableSkeleton />}
        isEmpty={(data) => data.content.length === 0}
        empty={
          <EmptyState
            icon={ClipboardListIcon}
            title="Chưa có phiếu mượn"
            description="Tạo phiếu mượn tại màn hình Mượn / Trả."
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable columns={columns} rows={data.content} rowKey={(l) => l.id} sort={sort} onSortChange={toggleSort} />
            <DataTablePagination
              page={data.page}
              size={data.size}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onPageChange={setPage}
            />
          </div>
        )}
      </QueryState>

      <ReturnLoanDialog
        loan={returning}
        open={Boolean(returning)}
        onOpenChange={(open) => !open && setReturning(null)}
      />
    </div>
  )
}

function ReturnLoanDialog({
  loan,
  open,
  onOpenChange,
}: {
  loan: Loan | null
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [condition, setCondition] = React.useState<ReturnCondition>("NORMAL")
  const [note, setNote] = React.useState("")
  const [overrideFee, setOverrideFee] = React.useState("")

  React.useEffect(() => {
    if (open) {
      setCondition("NORMAL")
      setNote("")
      setOverrideFee("")
    }
  }, [open])

  const mutation = useMutation({
    mutationFn: () => {
      const payload: { condition: ReturnCondition; note?: string; overrideFee?: number } = { condition }
      if (note.trim()) payload.note = note.trim()
      if ((condition === "LOST" || condition === "DAMAGED") && overrideFee.trim()) {
        payload.overrideFee = Number(overrideFee)
      }
      return api.post<Loan>(`/loans/${loan!.id}/return`, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["loans"] })
      toast.success("Đã trả sách")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error),
  })

  const showFee = condition === "LOST" || condition === "DAMAGED"

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Trả sách</DialogTitle>
          <DialogDescription>{loan ? `Phiếu ${loan.code} · ${loan.bookTitle}` : ""}</DialogDescription>
        </DialogHeader>
        <form
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate()
          }}
          className="space-y-4"
        >
          <FormField label="Tình trạng sách">
            <Select value={condition} onValueChange={(value) => setCondition(value as ReturnCondition)}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NORMAL">Bình thường</SelectItem>
                <SelectItem value="LOST">Mất</SelectItem>
                <SelectItem value="DAMAGED">Hỏng</SelectItem>
              </SelectContent>
            </Select>
          </FormField>
          {showFee ? (
            <FormField label="Phí thay thế (VND)" htmlFor="overrideFee" description="Để trống để dùng phí mặc định">
              <Input
                id="overrideFee"
                type="number"
                min={0}
                value={overrideFee}
                onChange={(event) => setOverrideFee(event.target.value)}
                placeholder="Phí mặc định"
              />
            </FormField>
          ) : null}
          <FormField label="Ghi chú" htmlFor="note">
            <Textarea id="note" rows={3} value={note} onChange={(event) => setNote(event.target.value)} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Trả sách
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
