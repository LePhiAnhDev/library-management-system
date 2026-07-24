"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { Loader2Icon, MoreHorizontalIcon, PencilIcon, PenLineIcon, PlusIcon, Trash2Icon } from "lucide-react"
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
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { Author, Page, RecordStatus } from "@/lib/types"

const PAGE_SIZE = 20

export default function AuthorsPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [status, setStatus] = React.useState<RecordStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "fullName", dir: "asc" })
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Author | null>(null)
  const [deleting, setDeleting] = React.useState<Author | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["authors", { debouncedSearch, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Author>>(`/authors?${params.toString()}`)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.del(`/authors/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["authors"] })
      toast.success("Đã xóa tác giả")
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

  const columns: Column<Author>[] = [
    { key: "fullName", header: "Tên tác giả", sortable: true, cell: (a) => <span className="font-medium">{a.fullName}</span> },
    {
      key: "biography",
      header: "Tiểu sử",
      className: "hidden max-w-md md:table-cell",
      cell: (a) => <span className="line-clamp-1 text-muted-foreground">{a.biography ?? "—"}</span>,
    },
    { key: "status", header: "Trạng thái", cell: (a) => <StatusBadge status={a.status} /> },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (a) => (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem
              onClick={() => {
                setEditing(a)
                setDialogOpen(true)
              }}
            >
              <PencilIcon /> Sửa
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => setDeleting(a)}>
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
        title="Tác giả"
        description="Quản lý tác giả sách"
        actions={
          <Button onClick={openCreate}>
            <PlusIcon /> Thêm tác giả
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
            <SelectValue />
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
            icon={PenLineIcon}
            title="Chưa có tác giả"
            description="Thêm tác giả để gán cho sách."
            action={
              <Button onClick={openCreate}>
                <PlusIcon /> Thêm tác giả
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable columns={columns} rows={data.content} rowKey={(a) => a.id} sort={sort} onSortChange={toggleSort} />
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

      <AuthorFormDialog open={dialogOpen} onOpenChange={setDialogOpen} editing={editing} />
      <ConfirmDialog
        open={Boolean(deleting)}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Xóa tác giả"
        description={deleting ? `Xóa tác giả "${deleting.fullName}"?` : ""}
        destructive
        confirmLabel="Xóa"
        loading={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  )
}

const schema = z.object({
  fullName: z.string().trim().min(1, "Tên không được để trống").max(255, "Tối đa 255 ký tự"),
  biography: z.string().max(2000, "Tối đa 2000 ký tự").optional(),
})

type FormValues = z.infer<typeof schema>

function AuthorFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  editing: Author | null
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
      reset({ fullName: editing?.fullName ?? "", biography: editing?.biography ?? "" })
    }
  }, [open, editing, reset])

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = { fullName: values.fullName, biography: values.biography || null }
      return editing ? api.put<Author>(`/authors/${editing.id}`, payload) : api.post<Author>("/authors", payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["authors"] })
      toast.success(editing ? "Đã cập nhật tác giả" : "Đã tạo tác giả")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa tác giả" : "Thêm tác giả"}</DialogTitle>
          <DialogDescription>Điền thông tin tác giả.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Tên tác giả" htmlFor="fullName" required error={errors.fullName?.message}>
            <Input id="fullName" autoFocus {...register("fullName")} />
          </FormField>
          <FormField label="Tiểu sử" htmlFor="biography" error={errors.biography?.message}>
            <Textarea id="biography" rows={4} {...register("biography")} />
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
