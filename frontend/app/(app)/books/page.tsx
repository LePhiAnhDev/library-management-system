"use client"

import * as React from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import {
  BookMarkedIcon,
  EyeIcon,
  LayoutGridIcon,
  ListIcon,
  Loader2Icon,
  MoreHorizontalIcon,
  PencilIcon,
  PlusIcon,
  Trash2Icon,
  XIcon,
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
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
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
import type { Author, AuthorSummary, Book, Category, Page, Publisher, RecordStatus } from "@/lib/types"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 20

function AvailabilityChip({ available, total }: { available: number; total: number }) {
  const inStock = available > 0
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset",
        inStock ? "bg-success/10 text-success ring-success/25" : "bg-destructive/10 text-destructive ring-destructive/25"
      )}
    >
      {inStock ? `Còn ${available}/${total}` : "Hết"}
    </span>
  )
}

function BookCover({ url, title, className }: { url: string | null; title: string; className?: string }) {
  if (url) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img src={url} alt={title} className={cn("h-full w-full object-cover", className)} loading="lazy" />
    )
  }
  return (
    <div className={cn("flex h-full w-full items-center justify-center bg-muted text-muted-foreground", className)}>
      <BookMarkedIcon className="size-10" />
    </div>
  )
}

export default function BooksPage() {
  const api = useApi()
  const router = useRouter()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [categoryId, setCategoryId] = React.useState<number | null>(null)
  const [categoryLabel, setCategoryLabel] = React.useState<string | null>(null)
  const [available, setAvailable] = React.useState(false)
  const [status, setStatus] = React.useState<RecordStatus | "ALL">("ALL")
  const [view, setView] = React.useState<"grid" | "list">("grid")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "title", dir: "asc" })
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Book | null>(null)
  const [deleting, setDeleting] = React.useState<Book | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["books", { debouncedSearch, categoryId, available, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (categoryId != null) params.set("categoryId", String(categoryId))
      if (available) params.set("available", "true")
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Book>>(`/books?${params.toString()}`)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.del(`/books/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success("Đã xóa sách")
      setDeleting(null)
    },
    onError: (error) => {
      handleMutationError(error)
      setDeleting(null)
    },
  })

  const loadCategories = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Category>>(`/categories?${params.toString()}`)
      return result.content.map((category) => ({ value: category.id, label: category.name }))
    },
    [api]
  )

  const toggleSort = (field: string) =>
    setSort((prev) => ({ field, dir: prev.field === field && prev.dir === "asc" ? "desc" : "asc" }))

  const openCreate = () => {
    setEditing(null)
    setDialogOpen(true)
  }

  const columns: Column<Book>[] = [
    {
      key: "title",
      header: "Tiêu đề",
      sortable: true,
      cell: (b) => (
        <div className="min-w-0">
          <Link href={`/books/${b.id}`} className="font-medium hover:underline">
            {b.title}
          </Link>
          {b.subtitle ? <p className="line-clamp-1 text-xs text-muted-foreground">{b.subtitle}</p> : null}
        </div>
      ),
    },
    {
      key: "authors",
      header: "Tác giả",
      className: "hidden md:table-cell",
      cell: (b) =>
        b.authors.length > 0 ? (
          <span className="line-clamp-1">{b.authors.map((a) => a.fullName).join(", ")}</span>
        ) : (
          <span className="text-muted-foreground">—</span>
        ),
    },
    {
      key: "categoryName",
      header: "Thể loại",
      className: "hidden lg:table-cell",
      cell: (b) => b.categoryName ?? <span className="text-muted-foreground">—</span>,
    },
    {
      key: "copies",
      header: "Còn / Tổng",
      className: "tabular-nums",
      cell: (b) => `${b.availableCopies}/${b.totalCopies}`,
    },
    { key: "status", header: "Trạng thái", cell: (b) => <StatusBadge status={b.status} /> },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (b) => (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
            <MoreHorizontalIcon />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => router.push(`/books/${b.id}`)}>
              <EyeIcon /> Xem
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => {
                setEditing(b)
                setDialogOpen(true)
              }}
            >
              <PencilIcon /> Sửa
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={() => setDeleting(b)}>
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
        title="Danh mục sách"
        description="Quản lý đầu sách và các bản sao trong thư viện"
        actions={
          <Button onClick={openCreate}>
            <PlusIcon /> Thêm sách
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <Input
          placeholder="Tìm theo tiêu đề, ISBN..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="max-w-xs"
        />
        <div className="w-52">
          <AsyncCombobox
            value={categoryId}
            selectedLabel={categoryLabel}
            onChange={(value, label) => {
              setCategoryId(value)
              setCategoryLabel(label)
              setPage(0)
            }}
            loadOptions={loadCategories}
            placeholder="Mọi thể loại"
          />
        </div>
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
        <div className="flex items-center gap-2">
          <Checkbox
            id="available-filter"
            checked={available}
            onCheckedChange={(checked) => {
              setAvailable(checked === true)
              setPage(0)
            }}
          />
          <label htmlFor="available-filter" className="cursor-pointer text-sm select-none">
            Chỉ còn sách
          </label>
        </div>
        <div className="ml-auto flex items-center gap-1 rounded-lg border p-0.5">
          <Button
            variant={view === "grid" ? "secondary" : "ghost"}
            size="icon-sm"
            aria-label="Xem dạng lưới"
            onClick={() => setView("grid")}
          >
            <LayoutGridIcon />
          </Button>
          <Button
            variant={view === "list" ? "secondary" : "ghost"}
            size="icon-sm"
            aria-label="Xem dạng danh sách"
            onClick={() => setView("list")}
          >
            <ListIcon />
          </Button>
        </div>
      </div>

      <QueryState
        query={query}
        skeleton={<TableSkeleton />}
        isEmpty={(data) => data.content.length === 0}
        empty={
          <EmptyState
            icon={BookMarkedIcon}
            title="Chưa có sách"
            description="Thêm đầu sách đầu tiên vào thư viện."
            action={
              <Button onClick={openCreate}>
                <PlusIcon /> Thêm sách
              </Button>
            }
          />
        }
      >
        {(data) => (
          <div className="space-y-4">
            {view === "grid" ? (
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5">
                {data.content.map((book) => (
                  <Link
                    key={book.id}
                    href={`/books/${book.id}`}
                    className="group flex flex-col overflow-hidden rounded-xl bg-card text-card-foreground ring-1 ring-foreground/10 transition-all hover:ring-foreground/25"
                  >
                    <div className="aspect-[3/4] w-full overflow-hidden">
                      <BookCover url={book.coverImageUrl} title={book.title} />
                    </div>
                    <div className="flex flex-1 flex-col gap-1 p-3">
                      <p className="line-clamp-2 font-medium leading-snug group-hover:text-primary">{book.title}</p>
                      {book.authors.length > 0 ? (
                        <p className="line-clamp-1 text-xs text-muted-foreground">
                          {book.authors.map((a) => a.fullName).join(", ")}
                        </p>
                      ) : null}
                      {book.categoryName ? (
                        <p className="line-clamp-1 text-xs text-muted-foreground">{book.categoryName}</p>
                      ) : null}
                      <div className="mt-auto pt-1.5">
                        <AvailabilityChip available={book.availableCopies} total={book.totalCopies} />
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <DataTable
                columns={columns}
                rows={data.content}
                rowKey={(b) => b.id}
                sort={sort}
                onSortChange={toggleSort}
              />
            )}
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

      <BookFormDialog open={dialogOpen} onOpenChange={setDialogOpen} editing={editing} />
      <ConfirmDialog
        open={Boolean(deleting)}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Xóa sách"
        description={deleting ? `Xóa sách "${deleting.title}"?` : ""}
        destructive
        confirmLabel="Xóa"
        loading={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  )
}

const schema = z.object({
  isbn: z.string().trim().min(1, "ISBN không được để trống").max(20, "Tối đa 20 ký tự"),
  title: z.string().trim().min(1, "Tiêu đề không được để trống").max(500, "Tối đa 500 ký tự"),
  subtitle: z.string().optional(),
  description: z.string().optional(),
  language: z.string().optional(),
  publicationYear: z
    .string()
    .optional()
    .refine((value) => !value || /^\d{1,4}$/.test(value), "Năm xuất bản không hợp lệ"),
  pageCount: z
    .string()
    .optional()
    .refine((value) => !value || /^\d+$/.test(value), "Số trang không hợp lệ"),
})

type FormValues = z.infer<typeof schema>

function BookFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  editing: Book | null
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [categoryId, setCategoryId] = React.useState<number | null>(null)
  const [categoryLabel, setCategoryLabel] = React.useState<string | null>(null)
  const [categoryError, setCategoryError] = React.useState<string | undefined>(undefined)
  const [publisherId, setPublisherId] = React.useState<number | null>(null)
  const [publisherLabel, setPublisherLabel] = React.useState<string | null>(null)
  const [authors, setAuthors] = React.useState<AuthorSummary[]>([])
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
        isbn: editing?.isbn ?? "",
        title: editing?.title ?? "",
        subtitle: editing?.subtitle ?? "",
        description: editing?.description ?? "",
        language: editing?.language ?? "",
        publicationYear: editing?.publicationYear != null ? String(editing.publicationYear) : "",
        pageCount: editing?.pageCount != null ? String(editing.pageCount) : "",
      })
      setCategoryId(editing?.categoryId ?? null)
      setCategoryLabel(editing?.categoryName ?? null)
      setCategoryError(undefined)
      setPublisherId(editing?.publisherId ?? null)
      setPublisherLabel(editing?.publisherName ?? null)
      setAuthors(editing?.authors ?? [])
    }
  }, [open, editing, reset])

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = {
        isbn: values.isbn,
        title: values.title,
        subtitle: values.subtitle || null,
        description: values.description || null,
        categoryId,
        publisherId,
        authorIds: authors.map((a) => a.id),
        publicationYear: values.publicationYear ? Number(values.publicationYear) : null,
        language: values.language || null,
        pageCount: values.pageCount ? Number(values.pageCount) : null,
      }
      return editing ? api.put<Book>(`/books/${editing.id}`, payload) : api.post<Book>("/books", payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success(editing ? "Đã cập nhật sách" : "Đã tạo sách")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  const loadCategories = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Category>>(`/categories?${params.toString()}`)
      return result.content.map((category) => ({ value: category.id, label: category.name }))
    },
    [api]
  )

  const loadPublishers = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Publisher>>(`/publishers?${params.toString()}`)
      return result.content.map((publisher) => ({ value: publisher.id, label: publisher.name }))
    },
    [api]
  )

  const loadAuthors = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const result = await api.get<Page<Author>>(`/authors?${params.toString()}`)
      return result.content.map((author) => ({ value: author.id, label: author.fullName }))
    },
    [api]
  )

  const onSubmit = (values: FormValues) => {
    if (categoryId == null) {
      setCategoryError("Vui lòng chọn thể loại")
      return
    }
    mutation.mutate(values)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa sách" : "Thêm sách"}</DialogTitle>
          <DialogDescription>Điền thông tin đầu sách.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="max-h-[60vh] space-y-4 overflow-y-auto pr-1">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-[1fr_1.5fr]">
              <FormField label="ISBN" htmlFor="isbn" required error={errors.isbn?.message}>
                <Input id="isbn" autoFocus {...register("isbn")} />
              </FormField>
              <FormField label="Tiêu đề" htmlFor="title" required error={errors.title?.message}>
                <Input id="title" {...register("title")} />
              </FormField>
            </div>
            <FormField label="Tiêu đề phụ" htmlFor="subtitle" error={errors.subtitle?.message}>
              <Input id="subtitle" {...register("subtitle")} />
            </FormField>
            <FormField label="Mô tả" htmlFor="description" error={errors.description?.message}>
              <Textarea id="description" rows={3} {...register("description")} />
            </FormField>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <FormField label="Thể loại" required error={categoryError}>
                <AsyncCombobox
                  value={categoryId}
                  selectedLabel={categoryLabel}
                  onChange={(value, label) => {
                    setCategoryId(value)
                    setCategoryLabel(label)
                    if (value != null) setCategoryError(undefined)
                  }}
                  loadOptions={loadCategories}
                  placeholder="Chọn thể loại"
                />
              </FormField>
              <FormField label="Nhà xuất bản">
                <AsyncCombobox
                  value={publisherId}
                  selectedLabel={publisherLabel}
                  onChange={(value, label) => {
                    setPublisherId(value)
                    setPublisherLabel(label)
                  }}
                  loadOptions={loadPublishers}
                  placeholder="Chọn nhà xuất bản"
                />
              </FormField>
            </div>
            <FormField label="Tác giả" description="Chọn một hoặc nhiều tác giả">
              <div className="space-y-2">
                {authors.length > 0 ? (
                  <div className="flex flex-wrap gap-1.5">
                    {authors.map((author) => (
                      <Badge key={author.id} variant="secondary" className="gap-1 pr-1">
                        {author.fullName}
                        <button
                          type="button"
                          aria-label={`Bỏ ${author.fullName}`}
                          className="rounded-full p-0.5 hover:bg-foreground/10"
                          onClick={() => setAuthors((prev) => prev.filter((a) => a.id !== author.id))}
                        >
                          <XIcon className="size-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                ) : null}
                <AsyncCombobox
                  value={null}
                  selectedLabel={null}
                  onChange={(value, label) => {
                    if (value != null && !authors.some((a) => a.id === value)) {
                      setAuthors((prev) => [...prev, { id: value, fullName: label ?? `#${value}` }])
                    }
                  }}
                  loadOptions={loadAuthors}
                  placeholder="Thêm tác giả..."
                />
              </div>
            </FormField>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              <FormField label="Năm xuất bản" htmlFor="publicationYear" error={errors.publicationYear?.message}>
                <Input id="publicationYear" type="number" {...register("publicationYear")} />
              </FormField>
              <FormField label="Ngôn ngữ" htmlFor="language" error={errors.language?.message}>
                <Input id="language" {...register("language")} />
              </FormField>
              <FormField label="Số trang" htmlFor="pageCount" error={errors.pageCount?.message}>
                <Input id="pageCount" type="number" {...register("pageCount")} />
              </FormField>
            </div>
          </div>
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
