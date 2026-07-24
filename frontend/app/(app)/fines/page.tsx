"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { BanIcon, BanknoteIcon, CircleDollarSignIcon, Loader2Icon, MoreHorizontalIcon, PlusIcon } from "lucide-react"
import { toast } from "sonner"

import { AsyncCombobox } from "@/components/shared/async-combobox"
import { ConfirmDialog } from "@/components/shared/confirm-dialog"
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
import { formatCurrency, formatDate } from "@/lib/format"
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { Fine, FineStatus, FineType, Member, Page } from "@/lib/types"

const PAGE_SIZE = 20

const FINE_TYPE_LABEL: Record<FineType, string> = {
  OVERDUE: "Quá hạn",
  LOST: "Mất",
  DAMAGED: "Hỏng",
}

export default function FinesPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [type, setType] = React.useState<FineType | "ALL">("ALL")
  const [status, setStatus] = React.useState<FineStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "createdAt", dir: "desc" })
  const [createOpen, setCreateOpen] = React.useState(false)
  const [settling, setSettling] = React.useState<Fine | null>(null)
  const [waiving, setWaiving] = React.useState<Fine | null>(null)

  const query = useQuery({
    queryKey: ["fines", { type, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (type !== "ALL") params.set("type", type)
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Fine>>(`/fines?${params.toString()}`)
    },
  })

  const settleMutation = useMutation({
    mutationFn: (id: number) => api.post(`/fines/${id}/settle`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fines"] })
      toast.success("Đã thu phạt")
      setSettling(null)
    },
    onError: (error) => {
      handleMutationError(error)
      setSettling(null)
    },
  })

  const toggleSort = (field: string) =>
    setSort((prev) => ({ field, dir: prev.field === field && prev.dir === "asc" ? "desc" : "asc" }))

  const columns: Column<Fine>[] = [
    {
      key: "memberName",
      header: "Độc giả",
      cell: (f) => (
        <div className="min-w-0">
          <p className="truncate font-medium">{f.memberName}</p>
          <p className="text-xs text-muted-foreground">{f.memberCode}</p>
        </div>
      ),
    },
    { key: "type", header: "Loại", cell: (f) => FINE_TYPE_LABEL[f.type] },
    {
      key: "amount",
      header: "Số tiền",
      className: "text-right",
      cell: (f) => <span className="tabular-nums">{formatCurrency(f.amount)}</span>,
    },
    { key: "status", header: "Trạng thái", cell: (f) => <StatusBadge status={f.status} /> },
    {
      key: "reason",
      header: "Lý do",
      className: "hidden max-w-xs md:table-cell",
      cell: (f) => <span className="line-clamp-1 text-muted-foreground">{f.reason ?? "—"}</span>,
    },
    { key: "createdAt", header: "Ngày tạo", sortable: true, cell: (f) => formatDate(f.createdAt) },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (f) =>
        f.status === "UNPAID" ? (
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
              <MoreHorizontalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => setSettling(f)}>
                <BanknoteIcon /> Thu phạt
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setWaiving(f)}>
                <BanIcon /> Miễn phạt
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null,
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Phạt"
        description="Quản lý các khoản phạt của độc giả"
        actions={
          <Button onClick={() => setCreateOpen(true)}>
            <PlusIcon /> Tạo phạt
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <Select
          value={type}
          onValueChange={(value) => {
            setType(value as FineType | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả loại phạt</SelectItem>
            <SelectItem value="OVERDUE">Quá hạn</SelectItem>
            <SelectItem value="LOST">Mất</SelectItem>
            <SelectItem value="DAMAGED">Hỏng</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as FineStatus | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả trạng thái</SelectItem>
            <SelectItem value="UNPAID">Chưa thu</SelectItem>
            <SelectItem value="PAID">Đã thu</SelectItem>
            <SelectItem value="WAIVED">Đã miễn</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <QueryState
        query={query}
        skeleton={<TableSkeleton />}
        isEmpty={(data) => data.content.length === 0}
        empty={
          <EmptyState
            icon={CircleDollarSignIcon}
            title="Chưa có khoản phạt"
            description="Không có khoản phạt nào khớp bộ lọc hiện tại."
            action={
              <Button onClick={() => setCreateOpen(true)}>
                <PlusIcon /> Tạo phạt
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable columns={columns} rows={data.content} rowKey={(f) => f.id} sort={sort} onSortChange={toggleSort} />
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

      <CreateFineDialog open={createOpen} onOpenChange={setCreateOpen} />
      <WaiveFineDialog fine={waiving} onOpenChange={(open) => !open && setWaiving(null)} />
      <ConfirmDialog
        open={Boolean(settling)}
        onOpenChange={(open) => !open && setSettling(null)}
        title="Thu phạt"
        description={
          settling ? `Xác nhận thu khoản phạt ${formatCurrency(settling.amount)} của ${settling.memberName}?` : ""
        }
        confirmLabel="Thu phạt"
        loading={settleMutation.isPending}
        onConfirm={() => settling && settleMutation.mutate(settling.id)}
      />
    </div>
  )
}

const createSchema = z.object({
  amount: z.number({ error: "Nhập số tiền hợp lệ" }).gt(0, "Số tiền phải lớn hơn 0"),
  reason: z.string().max(2000, "Tối đa 2000 ký tự").optional(),
})

type CreateFormValues = z.infer<typeof createSchema>

function CreateFineDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (open: boolean) => void }) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [type, setType] = React.useState<FineType>("OVERDUE")
  const [memberId, setMemberId] = React.useState<number | null>(null)
  const [memberLabel, setMemberLabel] = React.useState<string | null>(null)
  const [memberError, setMemberError] = React.useState<string | null>(null)
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<CreateFormValues>({ resolver: formResolver<CreateFormValues>(createSchema) })

  React.useEffect(() => {
    if (open) {
      reset({ amount: undefined, reason: "" })
      setType("OVERDUE")
      setMemberId(null)
      setMemberLabel(null)
      setMemberError(null)
    }
  }, [open, reset])

  const mutation = useMutation({
    mutationFn: (values: CreateFormValues) =>
      api.post<Fine>("/fines", { memberId, type, amount: values.amount, reason: values.reason || null }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fines"] })
      toast.success("Đã tạo phạt")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  const loadMembers = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Member>>(`/members?${params.toString()}`)
      return result.content.map((member) => ({ value: member.id, label: member.fullName, hint: member.memberCode }))
    },
    [api]
  )

  const onSubmit = (values: CreateFormValues) => {
    if (!memberId) {
      setMemberError("Vui lòng chọn độc giả")
      return
    }
    mutation.mutate(values)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Tạo phạt</DialogTitle>
          <DialogDescription>Tạo khoản phạt thủ công cho độc giả.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormField label="Độc giả" required error={memberError ?? undefined}>
            <AsyncCombobox
              value={memberId}
              selectedLabel={memberLabel}
              onChange={(value, label) => {
                setMemberId(value)
                setMemberLabel(label)
                setMemberError(null)
              }}
              loadOptions={loadMembers}
              placeholder="Chọn độc giả"
            />
          </FormField>
          <FormField label="Loại phạt" required>
            <Select value={type} onValueChange={(value) => setType(value as FineType)}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="OVERDUE">Quá hạn</SelectItem>
                <SelectItem value="LOST">Mất</SelectItem>
                <SelectItem value="DAMAGED">Hỏng</SelectItem>
              </SelectContent>
            </Select>
          </FormField>
          <FormField label="Số tiền (VND)" htmlFor="amount" required error={errors.amount?.message}>
            <Input
              id="amount"
              type="number"
              min="0"
              step="1"
              placeholder="0"
              {...register("amount", { valueAsNumber: true })}
            />
          </FormField>
          <FormField label="Lý do" htmlFor="reason" error={errors.reason?.message}>
            <Textarea id="reason" rows={3} {...register("reason")} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Tạo
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

const waiveSchema = z.object({
  reason: z.string().trim().min(1, "Vui lòng nhập lý do miễn").max(2000, "Tối đa 2000 ký tự"),
})

type WaiveFormValues = z.infer<typeof waiveSchema>

function WaiveFineDialog({ fine, onOpenChange }: { fine: Fine | null; onOpenChange: (open: boolean) => void }) {
  const api = useApi()
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<WaiveFormValues>({ resolver: formResolver<WaiveFormValues>(waiveSchema) })

  React.useEffect(() => {
    if (fine) {
      reset({ reason: "" })
    }
  }, [fine, reset])

  const mutation = useMutation({
    mutationFn: (values: WaiveFormValues) => api.post(`/fines/${fine!.id}/waive`, { reason: values.reason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fines"] })
      toast.success("Đã miễn phạt")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={Boolean(fine)} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Miễn phạt</DialogTitle>
          <DialogDescription>Nhập lý do miễn khoản phạt này.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Lý do miễn" htmlFor="waive-reason" required error={errors.reason?.message}>
            <Textarea id="waive-reason" rows={3} autoFocus {...register("reason")} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Miễn phạt
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
