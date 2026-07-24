"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import {
  FolderTreeIcon,
  Loader2Icon,
  MoreHorizontalIcon,
  PencilIcon,
  PlusIcon,
  Trash2Icon,
} from "lucide-react"
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
import { selectLabel } from "@/lib/labels"
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { Category, Page, RecordStatus } from "@/lib/types"

const PAGE_SIZE = 20

export default function CategoriesPage() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [status, setStatus] = React.useState<RecordStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "name", dir: "asc" })
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Category | null>(null)
  const [deleting, setDeleting] = React.useState<Category | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["categories", { debouncedSearch, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Category>>(`/categories?${params.toString()}`)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.del(`/categories/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] })
      toast.success("Đã xóa thể loại")
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

  const columns: Column<Category>[] = [
    { key: "name", header: "Tên", sortable: true, cell: (c) => <span className="font-medium">{c.name}</span> },
    {
      key: "parentName",
      header: "Thể loại cha",
      cell: (c) => c.parentName ?? <span className="text-muted-foreground">—</span>,
    },
    {
      key: "description",
      header: "Mô tả",
      className: "hidden max-w-xs md:table-cell",
      cell: (c) => <span className="line-clamp-1 text-muted-foreground">{c.description ?? "—"}</span>,
    },
    { key: "status", header: "Trạng thái", cell: (c) => <StatusBadge status={c.status} /> },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (c) => (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem
              onClick={() => {
                setEditing(c)
                setDialogOpen(true)
              }}
            >
              <PencilIcon /> Sửa
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => setDeleting(c)}>
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
        title="Thể loại"
        description="Quản lý thể loại sách, hỗ trợ phân cấp cha con"
        actions={
          <Button onClick={openCreate}>
            <PlusIcon /> Thêm thể loại
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
            icon={FolderTreeIcon}
            title="Chưa có thể loại"
            description="Tạo thể loại đầu tiên để phân loại sách."
            action={
              <Button onClick={openCreate}>
                <PlusIcon /> Thêm thể loại
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-3">
            <DataTable
              columns={columns}
              rows={data.content}
              rowKey={(c) => c.id}
              sort={sort}
              onSortChange={toggleSort}
            />
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

      <CategoryFormDialog open={dialogOpen} onOpenChange={setDialogOpen} editing={editing} />
      <ConfirmDialog
        open={Boolean(deleting)}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Xóa thể loại"
        description={deleting ? `Xóa thể loại "${deleting.name}"?` : ""}
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
  description: z.string().max(2000, "Tối đa 2000 ký tự").optional(),
})

type FormValues = z.infer<typeof schema>

function CategoryFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  editing: Category | null
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [parentId, setParentId] = React.useState<number | null>(null)
  const [parentLabel, setParentLabel] = React.useState<string | null>(null)
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({ resolver: formResolver<FormValues>(schema) })

  React.useEffect(() => {
    if (open) {
      reset({ name: editing?.name ?? "", description: editing?.description ?? "" })
      setParentId(editing?.parentId ?? null)
      setParentLabel(editing?.parentName ?? null)
    }
  }, [open, editing, reset])

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = { name: values.name, description: values.description || null, parentId }
      return editing
        ? api.put<Category>(`/categories/${editing.id}`, payload)
        : api.post<Category>("/categories", payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["categories"] })
      toast.success(editing ? "Đã cập nhật thể loại" : "Đã tạo thể loại")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  const loadParents = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Category>>(`/categories?${params.toString()}`)
      return result.content
        .filter((category) => category.id !== editing?.id)
        .map((category) => ({ value: category.id, label: category.name }))
    },
    [api, editing]
  )

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa thể loại" : "Thêm thể loại"}</DialogTitle>
          <DialogDescription>Điền thông tin thể loại sách.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Tên thể loại" htmlFor="name" required error={errors.name?.message}>
            <Input id="name" autoFocus {...register("name")} />
          </FormField>
          <FormField label="Mô tả" htmlFor="description" error={errors.description?.message}>
            <Textarea id="description" rows={3} {...register("description")} />
          </FormField>
          <FormField label="Thể loại cha" description="Để trống nếu là thể loại gốc">
            <AsyncCombobox
              value={parentId}
              selectedLabel={parentLabel}
              onChange={(value, label) => {
                setParentId(value)
                setParentLabel(label)
              }}
              loadOptions={loadParents}
              placeholder="Không có (thể loại gốc)"
            />
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
