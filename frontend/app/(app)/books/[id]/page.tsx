"use client"

import * as React from "react"
import { useParams } from "next/navigation"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import {
  BookMarkedIcon,
  Loader2Icon,
  MoreHorizontalIcon,
  PackageIcon,
  PlusIcon,
  RefreshCwIcon,
  Trash2Icon,
  UploadIcon,
} from "lucide-react"
import { toast } from "sonner"

import { ConfirmDialog } from "@/components/shared/confirm-dialog"
import { EmptyState } from "@/components/shared/empty-state"
import { FormField } from "@/components/shared/form-field"
import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { DetailSkeleton, TableSkeleton } from "@/components/shared/skeletons"
import { StatusBadge } from "@/components/shared/status-badge"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
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
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Textarea } from "@/components/ui/textarea"
import { useApi } from "@/lib/api"
import { selectLabel } from "@/lib/labels"
import { formatDate } from "@/lib/format"
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { Book, BookCopy } from "@/lib/types"

function CoverImage({ url, title }: { url: string | null; title: string }) {
  if (url) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img src={url} alt={title} className="h-full w-full object-cover" />
    )
  }
  return (
    <div className="flex h-full w-full items-center justify-center bg-muted text-muted-foreground">
      <BookMarkedIcon className="size-12" />
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: React.ReactNode }) {
  const isEmpty = value === null || value === undefined || value === ""
  return (
    <div className="space-y-0.5">
      <dt className="text-xs text-muted-foreground">{label}</dt>
      <dd className="text-sm">{isEmpty ? <span className="text-muted-foreground">—</span> : value}</dd>
    </div>
  )
}

export default function BookDetailPage() {
  const params = useParams()
  const id = String(params.id)
  const api = useApi()

  const bookQuery = useQuery({
    queryKey: ["book", id],
    queryFn: () => api.get<Book>(`/books/${id}`),
  })

  return (
    <div className="space-y-6">
      <QueryState query={bookQuery} skeleton={<DetailSkeleton />}>
        {(book) => <BookDetail book={book} bookId={id} />}
      </QueryState>
    </div>
  )
}

function BookDetail({ book, bookId }: { book: Book; bookId: string }) {
  const api = useApi()
  const queryClient = useQueryClient()
  const fileInputRef = React.useRef<HTMLInputElement>(null)
  const [addOpen, setAddOpen] = React.useState(false)
  const [statusTarget, setStatusTarget] = React.useState<BookCopy | null>(null)
  const [deleteTarget, setDeleteTarget] = React.useState<BookCopy | null>(null)

  const copiesQuery = useQuery({
    queryKey: ["book-copies", bookId],
    queryFn: () => api.get<BookCopy[]>(`/books/${bookId}/copies`),
  })

  const uploadMutation = useMutation({
    mutationFn: (file: File) => {
      const form = new FormData()
      form.append("file", file)
      return api.upload<Book>(`/books/${bookId}/cover`, form)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["book", bookId] })
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success("Đã tải ảnh bìa")
    },
    onError: (error) => handleMutationError(error),
  })

  const deleteMutation = useMutation({
    mutationFn: (copyId: number) => api.del(`/book-copies/${copyId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["book-copies", bookId] })
      queryClient.invalidateQueries({ queryKey: ["book", bookId] })
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success("Đã xóa bản sao")
      setDeleteTarget(null)
    },
    onError: (error) => {
      handleMutationError(error)
      setDeleteTarget(null)
    },
  })

  const onFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ""
    if (file) uploadMutation.mutate(file)
  }

  const authorsText = book.authors.length > 0 ? book.authors.map((a) => a.fullName).join(", ") : null

  return (
    <div className="space-y-6">
      <PageHeader title={book.title} description={book.subtitle ?? undefined} />

      <div className="grid gap-6 lg:grid-cols-[260px_1fr]">
        <div className="space-y-4">
          <Card className="gap-0 overflow-hidden py-0">
            <div className="aspect-[3/4] w-full overflow-hidden">
              <CoverImage url={book.coverImageUrl} title={book.title} />
            </div>
            <CardContent className="flex flex-col gap-2 py-3">
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={onFileChange}
              />
              <Button
                variant="outline"
                className="w-full"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploadMutation.isPending}
              >
                {uploadMutation.isPending ? (
                  <Loader2Icon className="size-4 animate-spin" />
                ) : (
                  <UploadIcon />
                )}
                {book.coverImageUrl ? "Đổi ảnh bìa" : "Tải ảnh bìa"}
              </Button>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Thông tin sách</CardTitle>
              <CardAction>
                <StatusBadge status={book.status} />
              </CardAction>
            </CardHeader>
            <CardContent className="space-y-4">
              <dl className="grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-3">
                <InfoRow label="ISBN" value={book.isbn} />
                <InfoRow label="Thể loại" value={book.categoryName} />
                <InfoRow label="Nhà xuất bản" value={book.publisherName} />
                <InfoRow label="Tác giả" value={authorsText} />
                <InfoRow label="Năm xuất bản" value={book.publicationYear} />
                <InfoRow label="Ngôn ngữ" value={book.language} />
                <InfoRow label="Số trang" value={book.pageCount} />
                <InfoRow label="Bản sao sẵn sàng" value={`${book.availableCopies}/${book.totalCopies}`} />
              </dl>
              {book.description ? (
                <div className="space-y-1 border-t pt-4">
                  <p className="text-xs text-muted-foreground">Mô tả</p>
                  <p className="text-sm whitespace-pre-line">{book.description}</p>
                </div>
              ) : null}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Bản sao</CardTitle>
              <CardDescription>{book.availableCopies}/{book.totalCopies} sẵn sàng</CardDescription>
              <CardAction>
                <Button size="sm" onClick={() => setAddOpen(true)}>
                  <PlusIcon /> Thêm bản sao
                </Button>
              </CardAction>
            </CardHeader>
            <CardContent>
              <QueryState
                query={copiesQuery}
                skeleton={<TableSkeleton rows={4} />}
                isEmpty={(data) => data.length === 0}
                empty={
                  <EmptyState
                    icon={PackageIcon}
                    title="Chưa có bản sao"
                    description="Thêm bản sao để có thể cho mượn đầu sách này."
                    action={
                      <Button size="sm" onClick={() => setAddOpen(true)}>
                        <PlusIcon /> Thêm bản sao
                      </Button>
                    }
                  />
                }
              >
                {(copies) => (
                  <div className="overflow-x-auto rounded-xl border">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Mã vạch</TableHead>
                          <TableHead>Vị trí kệ</TableHead>
                          <TableHead>Trạng thái</TableHead>
                          <TableHead>Ngày nhập</TableHead>
                          <TableHead className="w-10" />
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {copies.map((copy) => (
                          <TableRow key={copy.id}>
                            <TableCell className="font-medium">{copy.barcode}</TableCell>
                            <TableCell>
                              {copy.shelfLocation ?? <span className="text-muted-foreground">—</span>}
                            </TableCell>
                            <TableCell>
                              <StatusBadge status={copy.status} />
                            </TableCell>
                            <TableCell>{formatDate(copy.acquiredDate)}</TableCell>
                            <TableCell>
                              <DropdownMenu>
                                <DropdownMenuTrigger
                                  render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}
                                >
                                  <MoreHorizontalIcon />
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                  <DropdownMenuItem onClick={() => setStatusTarget(copy)}>
                                    <RefreshCwIcon /> Đổi trạng thái
                                  </DropdownMenuItem>
                                  <DropdownMenuItem
                                    variant="destructive"
                                    onClick={() => setDeleteTarget(copy)}
                                  >
                                    <Trash2Icon /> Xóa
                                  </DropdownMenuItem>
                                </DropdownMenuContent>
                              </DropdownMenu>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </QueryState>
            </CardContent>
          </Card>
        </div>
      </div>

      <AddCopyDialog open={addOpen} onOpenChange={setAddOpen} bookId={bookId} />
      <ChangeCopyStatusDialog
        copy={statusTarget}
        bookId={bookId}
        onOpenChange={(open) => !open && setStatusTarget(null)}
      />
      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        title="Xóa bản sao"
        description={deleteTarget ? `Xóa bản sao có mã vạch "${deleteTarget.barcode}"?` : ""}
        destructive
        confirmLabel="Xóa"
        loading={deleteMutation.isPending}
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
      />
    </div>
  )
}

const addSchema = z.object({
  barcode: z.string().trim().min(1, "Mã vạch không được để trống"),
  shelfLocation: z.string().optional(),
  acquiredDate: z.string().optional(),
  conditionNote: z.string().optional(),
})

type AddCopyValues = z.infer<typeof addSchema>

function AddCopyDialog({
  open,
  onOpenChange,
  bookId,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  bookId: string
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<AddCopyValues>({ resolver: formResolver<AddCopyValues>(addSchema) })

  React.useEffect(() => {
    if (open) {
      reset({ barcode: "", shelfLocation: "", acquiredDate: "", conditionNote: "" })
    }
  }, [open, reset])

  const mutation = useMutation({
    mutationFn: (values: AddCopyValues) => {
      const payload = {
        barcode: values.barcode,
        shelfLocation: values.shelfLocation || null,
        acquiredDate: values.acquiredDate || null,
        conditionNote: values.conditionNote || null,
      }
      return api.post<BookCopy>(`/books/${bookId}/copies`, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["book-copies", bookId] })
      queryClient.invalidateQueries({ queryKey: ["book", bookId] })
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success("Đã thêm bản sao")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Thêm bản sao</DialogTitle>
          <DialogDescription>Tạo một bản sao mới cho đầu sách.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Mã vạch" htmlFor="barcode" required error={errors.barcode?.message}>
            <Input id="barcode" autoFocus {...register("barcode")} />
          </FormField>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormField label="Vị trí kệ" htmlFor="shelfLocation" error={errors.shelfLocation?.message}>
              <Input id="shelfLocation" {...register("shelfLocation")} />
            </FormField>
            <FormField label="Ngày nhập" htmlFor="acquiredDate" error={errors.acquiredDate?.message}>
              <Input id="acquiredDate" type="date" {...register("acquiredDate")} />
            </FormField>
          </div>
          <FormField label="Ghi chú tình trạng" htmlFor="conditionNote" error={errors.conditionNote?.message}>
            <Textarea id="conditionNote" rows={3} {...register("conditionNote")} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Thêm
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

const COPY_STATUS_OPTIONS: { value: string; label: string }[] = [
  { value: "AVAILABLE", label: "Sẵn sàng" },
  { value: "LOST", label: "Mất" },
  { value: "DAMAGED", label: "Hỏng" },
  { value: "MAINTENANCE", label: "Bảo trì" },
]

function ChangeCopyStatusDialog({
  copy,
  bookId,
  onOpenChange,
}: {
  copy: BookCopy | null
  bookId: string
  onOpenChange: (open: boolean) => void
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [status, setStatus] = React.useState("AVAILABLE")
  const [note, setNote] = React.useState("")

  React.useEffect(() => {
    if (copy) {
      const settable = COPY_STATUS_OPTIONS.some((option) => option.value === copy.status)
      setStatus(settable ? copy.status : "AVAILABLE")
      setNote(copy.conditionNote ?? "")
    }
  }, [copy])

  const mutation = useMutation({
    mutationFn: () =>
      api.post(`/book-copies/${copy?.id}/status`, { status, conditionNote: note.trim() || null }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["book-copies", bookId] })
      queryClient.invalidateQueries({ queryKey: ["book", bookId] })
      queryClient.invalidateQueries({ queryKey: ["books"] })
      toast.success("Đã cập nhật trạng thái")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error),
  })

  return (
    <Dialog open={Boolean(copy)} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Đổi trạng thái bản sao</DialogTitle>
          <DialogDescription>{copy ? `Mã vạch ${copy.barcode}` : ""}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <FormField label="Trạng thái">
            <Select value={status} onValueChange={(value) => setStatus(value as string)}>
              <SelectTrigger className="w-full">
                <SelectValue>{(v) => selectLabel(v)}</SelectValue>
              </SelectTrigger>
              <SelectContent>
                {COPY_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </FormField>
          <FormField label="Ghi chú tình trạng" description="Không bắt buộc">
            <Textarea rows={3} value={note} onChange={(event) => setNote(event.target.value)} />
          </FormField>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
            Hủy
          </Button>
          <Button onClick={() => mutation.mutate()} disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
            Cập nhật
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
