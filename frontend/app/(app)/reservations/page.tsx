"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { BookmarkCheckIcon, Loader2Icon, MoreHorizontalIcon, PlusIcon, XIcon } from "lucide-react"
import { toast } from "sonner"

import { AsyncCombobox } from "@/components/shared/async-combobox"
import { ConfirmDialog } from "@/components/shared/confirm-dialog"
import { DataTable, DataTablePagination, type Column } from "@/components/shared/data-table"
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useApi } from "@/lib/api"
import { formatDate, formatDateTime } from "@/lib/format"
import { handleMutationError } from "@/lib/form-errors"
import type { Book, Member, Page, Reservation, ReservationStatus } from "@/lib/types"

const PAGE_SIZE = 20

export default function ReservationsPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [status, setStatus] = React.useState<ReservationStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [cancelling, setCancelling] = React.useState<Reservation | null>(null)

  const query = useQuery({
    queryKey: ["reservations", { status, page }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", "createdAt,desc")
      return api.get<Page<Reservation>>(`/reservations?${params.toString()}`)
    },
  })

  const cancelMutation = useMutation({
    mutationFn: (id: number) => api.post<Reservation>(`/reservations/${id}/cancel`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reservations"] })
      toast.success("Đã hủy đặt trước")
      setCancelling(null)
    },
    onError: (error) => {
      handleMutationError(error)
      setCancelling(null)
    },
  })

  const columns: Column<Reservation>[] = [
    {
      key: "bookTitle",
      header: "Sách",
      className: "max-w-xs",
      cell: (r) => <span className="line-clamp-1 font-medium">{r.bookTitle}</span>,
    },
    {
      key: "memberName",
      header: "Độc giả",
      cell: (r) => (
        <div className="flex flex-col">
          <span>{r.memberName}</span>
          <span className="text-xs text-muted-foreground">{r.memberCode}</span>
        </div>
      ),
    },
    { key: "status", header: "Trạng thái", cell: (r) => <StatusBadge status={r.status} /> },
    { key: "reservationDate", header: "Ngày đặt", cell: (r) => formatDateTime(r.reservationDate) },
    {
      key: "pickupExpiry",
      header: "Hạn nhận",
      cell: (r) =>
        r.status === "READY" ? formatDate(r.pickupExpiry) : <span className="text-muted-foreground">—</span>,
    },
    {
      key: "heldCopyBarcode",
      header: "Bản sao giữ",
      cell: (r) => r.heldCopyBarcode ?? <span className="text-muted-foreground">—</span>,
    },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (r) =>
        r.status === "PENDING" || r.status === "READY" ? (
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
              <MoreHorizontalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem variant="destructive" onClick={() => setCancelling(r)}>
                <XIcon /> Hủy
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null,
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Đặt trước"
        description="Quản lý yêu cầu đặt trước sách của độc giả"
        actions={
          <Button onClick={() => setDialogOpen(true)}>
            <PlusIcon /> Thêm đặt trước
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as ReservationStatus | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả trạng thái</SelectItem>
            <SelectItem value="PENDING">Chờ</SelectItem>
            <SelectItem value="READY">Sẵn sàng nhận</SelectItem>
            <SelectItem value="FULFILLED">Đã nhận</SelectItem>
            <SelectItem value="CANCELLED">Đã hủy</SelectItem>
            <SelectItem value="EXPIRED">Hết hạn</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <QueryState
        query={query}
        skeleton={<TableSkeleton />}
        isEmpty={(data) => data.content.length === 0}
        empty={
          <EmptyState
            icon={BookmarkCheckIcon}
            title="Chưa có đặt trước"
            description="Tạo yêu cầu đặt trước cho độc giả khi sách hết bản sao sẵn sàng."
            action={
              <Button onClick={() => setDialogOpen(true)}>
                <PlusIcon /> Thêm đặt trước
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable columns={columns} rows={data.content} rowKey={(r) => r.id} />
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

      <ReservationFormDialog open={dialogOpen} onOpenChange={setDialogOpen} />
      <ConfirmDialog
        open={Boolean(cancelling)}
        onOpenChange={(open) => !open && setCancelling(null)}
        title="Hủy đặt trước"
        description={cancelling ? `Hủy đặt trước sách "${cancelling.bookTitle}"?` : ""}
        destructive
        confirmLabel="Hủy đặt trước"
        cancelLabel="Đóng"
        loading={cancelMutation.isPending}
        onConfirm={() => cancelling && cancelMutation.mutate(cancelling.id)}
      />
    </div>
  )
}

function ReservationFormDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [memberId, setMemberId] = React.useState<number | null>(null)
  const [memberLabel, setMemberLabel] = React.useState<string | null>(null)
  const [bookId, setBookId] = React.useState<number | null>(null)
  const [bookLabel, setBookLabel] = React.useState<string | null>(null)

  React.useEffect(() => {
    if (open) {
      setMemberId(null)
      setMemberLabel(null)
      setBookId(null)
      setBookLabel(null)
    }
  }, [open])

  const loadMembers = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Member>>(`/members?${params.toString()}`)
      return result.content.map((member) => ({ value: member.id, label: member.fullName, hint: member.memberCode }))
    },
    [api]
  )

  const loadBooks = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Book>>(`/books?${params.toString()}`)
      return result.content.map((book) => ({ value: book.id, label: book.title, hint: book.isbn }))
    },
    [api]
  )

  const mutation = useMutation({
    mutationFn: () => api.post<Reservation>("/reservations", { memberId, bookId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reservations"] })
      toast.success("Đã tạo đặt trước")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error),
  })

  const canSubmit = memberId !== null && bookId !== null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Thêm đặt trước</DialogTitle>
          <DialogDescription>Chọn độc giả và sách cần đặt trước.</DialogDescription>
        </DialogHeader>
        <form
          onSubmit={(event) => {
            event.preventDefault()
            if (canSubmit) mutation.mutate()
          }}
          className="space-y-4"
        >
          <FormField label="Độc giả" required>
            <AsyncCombobox
              value={memberId}
              selectedLabel={memberLabel}
              onChange={(value, label) => {
                setMemberId(value)
                setMemberLabel(label)
              }}
              loadOptions={loadMembers}
              placeholder="Chọn độc giả"
            />
          </FormField>
          <FormField label="Sách" required>
            <AsyncCombobox
              value={bookId}
              selectedLabel={bookLabel}
              onChange={(value, label) => {
                setBookId(value)
                setBookLabel(label)
              }}
              loadOptions={loadBooks}
              placeholder="Chọn sách"
            />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={!canSubmit || mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Tạo
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
