"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { Building2Icon, Loader2Icon, MoreHorizontalIcon, PencilIcon, PlusIcon, Trash2Icon } from "lucide-react"
import { toast } from "sonner"

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
import { selectLabel } from "@/lib/labels"
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { Page, Publisher, RecordStatus } from "@/lib/types"

const PAGE_SIZE = 20

export default function PublishersPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [status, setStatus] = React.useState<RecordStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "name", dir: "asc" })
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Publisher | null>(null)
  const [deleting, setDeleting] = React.useState<Publisher | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["publishers", { debouncedSearch, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Publisher>>(`/publishers?${params.toString()}`)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.del(`/publishers/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["publishers"] })
      toast.success("Đã xóa nhà xuất bản")
      setDeleting(null)
    },
    onError: (error) => {
      handleMutationError(error)
      setDeleting(null)
    },
  })

  const toggleSort = (field: string) =>
    setSort((prev) => ({ field, dir: prev.field === field && prev.dir === "asc" ? "desc" : "asc" }))

  const openCreate = () => {
    setEditing(null)
    setDialogOpen(true)
  }

  const columns: Column<Publisher>[] = [
    { key: "name", header: "Tên nhà xuất bản", sortable: true, cell: (p) => <span className="font-medium">{p.name}</span> },
    {
      key: "email",
      header: "Email",
      cell: (p) => p.email ?? <span className="text-muted-foreground">—</span>,
    },
    {
      key: "phone",
      header: "Điện thoại",
      cell: (p) => p.phone ?? <span className="text-muted-foreground">—</span>,
    },
    { key: "status", header: "Trạng thái", cell: (p) => <StatusBadge status={p.status} /> },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (p) => (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem
              onClick={() => {
                setEditing(p)
                setDialogOpen(true)
              }}
            >
              <PencilIcon /> Sửa
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => setDeleting(p)}>
              <Trash2Icon /> Xóa
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Nhà xuất bản"
        description="Quản lý nhà xuất bản sách"
        actions={
          <Button onClick={openCreate}>
            <PlusIcon /> Thêm nhà xuất bản
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <Input
          placeholder="Tìm theo tên..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="max-w-xs"
        />
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as RecordStatus | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue>{(v) => selectLabel(v, "Tất cả trạng thái")}</SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả trạng thái</SelectItem>
            <SelectItem value="ACTIVE">Hoạt động</SelectItem>
            <SelectItem value="INACTIVE">Ngừng</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <QueryState
        query={query}
        skeleton={<TableSkeleton />}
        isEmpty={(data) => data.content.length === 0}
        empty={
          <EmptyState
            icon={Building2Icon}
            title="Chưa có nhà xuất bản"
            description="Thêm nhà xuất bản để gán cho sách."
            action={
              <Button onClick={openCreate}>
                <PlusIcon /> Thêm nhà xuất bản
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable columns={columns} rows={data.content} rowKey={(p) => p.id} sort={sort} onSortChange={toggleSort} />
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

      <PublisherFormDialog open={dialogOpen} onOpenChange={setDialogOpen} editing={editing} />
      <ConfirmDialog
        open={Boolean(deleting)}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Xóa nhà xuất bản"
        description={deleting ? `Xóa nhà xuất bản "${deleting.name}"?` : ""}
        destructive
        confirmLabel="Xóa"
        loading={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  )
}

const schema = z.object({
  name: z.string().trim().min(1, "Tên không được để trống").max(255, "Tối đa 255 ký tự"),
  address: z.string().max(500, "Tối đa 500 ký tự").optional(),
  phone: z.string().max(30, "Tối đa 30 ký tự").optional(),
  email: z.preprocess(
    (value) => (value === "" ? undefined : value),
    z.string().email("Email không hợp lệ").max(320, "Tối đa 320 ký tự").optional()
  ),
})

type FormValues = z.infer<typeof schema>

function PublisherFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  editing: Publisher | null
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({ resolver: formResolver<FormValues>(schema) })

  React.useEffect(() => {
    if (open) {
      reset({
        name: editing?.name ?? "",
        address: editing?.address ?? "",
        phone: editing?.phone ?? "",
        email: editing?.email ?? "",
      })
    }
  }, [open, editing, reset])

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = {
        name: values.name,
        address: values.address || null,
        phone: values.phone || null,
        email: values.email || null,
      }
      return editing
        ? api.put<Publisher>(`/publishers/${editing.id}`, payload)
        : api.post<Publisher>("/publishers", payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["publishers"] })
      toast.success(editing ? "Đã cập nhật nhà xuất bản" : "Đã tạo nhà xuất bản")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa nhà xuất bản" : "Thêm nhà xuất bản"}</DialogTitle>
          <DialogDescription>Điền thông tin nhà xuất bản.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Tên nhà xuất bản" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" autoFocus {...register("name")} />
          </FormField>
          <FormField label="Địa chỉ" htmlFor="address" error={errors.address?.message}>
            <Textarea id="address" rows={2} {...register("address")} />
          </FormField>
          <FormField label="Điện thoại" htmlFor="phone" error={errors.phone?.message}>
            <Input id="phone" {...register("phone")} />
          </FormField>
          <FormField label="Email" htmlFor="email" error={errors.email?.message}>
            <Input id="email" type="email" {...register("email")} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              {editing ? "Lưu thay đổi" : "Tạo"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
