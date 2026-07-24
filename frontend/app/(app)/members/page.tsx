"use client"

import * as React from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import {
  EyeIcon,
  Loader2Icon,
  MoreHorizontalIcon,
  PencilIcon,
  PlusIcon,
  RefreshCwIcon,
  Trash2Icon,
  UsersIcon,
} from "lucide-react"
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
import type { Member, MemberStatus, MembershipType, Page } from "@/lib/types"

const PAGE_SIZE = 20

const MEMBERSHIP_LABELS: Record<MembershipType, string> = {
  REGULAR: "Thường",
  STUDENT: "Sinh viên",
  PREMIUM: "Premium",
}

export default function MembersPage() {
  const api = useApi()
  const router = useRouter()
  const queryClient = useQueryClient()
  const [search, setSearch] = React.useState("")
  const [debouncedSearch, setDebouncedSearch] = React.useState("")
  const [membershipType, setMembershipType] = React.useState<MembershipType | "ALL">("ALL")
  const [status, setStatus] = React.useState<MemberStatus | "ALL">("ALL")
  const [page, setPage] = React.useState(0)
  const [sort, setSort] = React.useState<SortState>({ field: "fullName", dir: "asc" })
  const [dialogOpen, setDialogOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Member | null>(null)
  const [statusEditing, setStatusEditing] = React.useState<Member | null>(null)
  const [deleting, setDeleting] = React.useState<Member | null>(null)

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search)
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [search])

  const query = useQuery({
    queryKey: ["members", { debouncedSearch, membershipType, status, page, sort }],
    queryFn: () => {
      const params = new URLSearchParams()
      if (debouncedSearch) params.set("search", debouncedSearch)
      if (membershipType !== "ALL") params.set("membershipType", membershipType)
      if (status !== "ALL") params.set("status", status)
      params.set("page", String(page))
      params.set("size", String(PAGE_SIZE))
      params.set("sort", `${sort.field},${sort.dir}`)
      return api.get<Page<Member>>(`/members?${params.toString()}`)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.del(`/members/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["members"] })
      toast.success("Đã xóa độc giả")
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

  const columns: Column<Member>[] = [
    {
      key: "memberCode",
      header: "Mã thẻ",
      cell: (m) => <span className="font-mono text-xs">{m.memberCode}</span>,
    },
    { key: "fullName", header: "Họ tên", sortable: true, cell: (m) => <span className="font-medium">{m.fullName}</span> },
    {
      key: "email",
      header: "Email",
      className: "hidden md:table-cell",
      cell: (m) => <span className="text-muted-foreground">{m.email}</span>,
    },
    {
      key: "membershipType",
      header: "Loại thẻ",
      cell: (m) => MEMBERSHIP_LABELS[m.membershipType],
    },
    { key: "status", header: "Trạng thái", cell: (m) => <StatusBadge status={m.status} /> },
    {
      key: "actions",
      header: "",
      className: "w-10",
      cell: (m) => (
        <div onClick={(event) => event.stopPropagation()}>
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" aria-label="Hành động" />}>
              <MoreHorizontalIcon />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem render={<Link href={`/members/${m.id}`} />}>
                <EyeIcon /> Xem hồ sơ
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => {
                  setEditing(m)
                  setDialogOpen(true)
                }}
              >
                <PencilIcon /> Sửa
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setStatusEditing(m)}>
                <RefreshCwIcon /> Đổi trạng thái
              </DropdownMenuItem>
              <DropdownMenuItem variant="destructive" onClick={() => setDeleting(m)}>
                <Trash2Icon /> Xóa
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Độc giả"
        description="Quản lý thẻ độc giả và trạng thái thành viên"
        actions={
          <Button onClick={openCreate}>
            <PlusIcon /> Thêm độc giả
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <Input
          placeholder="Tìm theo tên, mã thẻ, email..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="max-w-xs"
        />
        <Select
          value={membershipType}
          onValueChange={(value) => {
            setMembershipType(value as MembershipType | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue>{(v) => selectLabel(v, "Tất cả loại thẻ")}</SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả loại thẻ</SelectItem>
            <SelectItem value="REGULAR">Thường</SelectItem>
            <SelectItem value="STUDENT">Sinh viên</SelectItem>
            <SelectItem value="PREMIUM">Premium</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={status}
          onValueChange={(value) => {
            setStatus(value as MemberStatus | "ALL")
            setPage(0)
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue>{(v) => selectLabel(v, "Tất cả trạng thái")}</SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả trạng thái</SelectItem>
            <SelectItem value="ACTIVE">Hoạt động</SelectItem>
            <SelectItem value="SUSPENDED">Tạm khóa</SelectItem>
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
            icon={UsersIcon}
            title="Chưa có độc giả"
            description="Thêm độc giả để bắt đầu quản lý mượn trả."
            action={
              <Button onClick={openCreate}>
                <PlusIcon /> Thêm độc giả
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
              rowKey={(m) => m.id}
              sort={sort}
              onSortChange={toggleSort}
              onRowClick={(m) => router.push(`/members/${m.id}`)}
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

      <MemberFormDialog open={dialogOpen} onOpenChange={setDialogOpen} editing={editing} />
      <MemberStatusDialog
        open={Boolean(statusEditing)}
        onOpenChange={(open) => !open && setStatusEditing(null)}
        member={statusEditing}
      />
      <ConfirmDialog
        open={Boolean(deleting)}
        onOpenChange={(open) => !open && setDeleting(null)}
        title="Xóa độc giả"
        description={deleting ? `Xóa độc giả "${deleting.fullName}"?` : ""}
        destructive
        confirmLabel="Xóa"
        loading={deleteMutation.isPending}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
      />
    </div>
  )
}

const schema = z.object({
  fullName: z.string().trim().min(1, "Họ tên không được để trống").max(255, "Tối đa 255 ký tự"),
  email: z
    .string()
    .trim()
    .min(1, "Email không được để trống")
    .email("Email không hợp lệ")
    .max(320, "Tối đa 320 ký tự"),
  phone: z.string().max(30, "Tối đa 30 ký tự").optional(),
  address: z.string().max(500, "Tối đa 500 ký tự").optional(),
  expiryDate: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

function MemberFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  editing: Member | null
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [membershipType, setMembershipType] = React.useState<MembershipType>("REGULAR")
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
        fullName: editing?.fullName ?? "",
        email: editing?.email ?? "",
        phone: editing?.phone ?? "",
        address: editing?.address ?? "",
        expiryDate: editing?.expiryDate ? editing.expiryDate.slice(0, 10) : "",
      })
      setMembershipType(editing?.membershipType ?? "REGULAR")
    }
  }, [open, editing, reset])

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = {
        fullName: values.fullName,
        email: values.email,
        phone: values.phone || null,
        address: values.address || null,
        membershipType,
        expiryDate: values.expiryDate || null,
      }
      return editing
        ? api.put<Member>(`/members/${editing.id}`, payload)
        : api.post<Member>("/members", payload)
    },
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ["members"] })
      toast.success(editing ? "Đã cập nhật độc giả" : `Đã tạo độc giả, mã thẻ ${created.memberCode}`)
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa độc giả" : "Thêm độc giả"}</DialogTitle>
          <DialogDescription>Điền thông tin độc giả. Mã thẻ được tạo tự động.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Họ tên" htmlFor="fullName" required error={errors.fullName?.message}>
            <Input id="fullName" autoFocus {...register("fullName")} />
          </FormField>
          <FormField label="Email" htmlFor="email" required error={errors.email?.message}>
            <Input id="email" type="email" {...register("email")} />
          </FormField>
          <FormField label="Điện thoại" htmlFor="phone" error={errors.phone?.message}>
            <Input id="phone" {...register("phone")} />
          </FormField>
          <FormField label="Địa chỉ" htmlFor="address" error={errors.address?.message}>
            <Textarea id="address" rows={2} {...register("address")} />
          </FormField>
          <FormField label="Loại thẻ" required>
            <Select value={membershipType} onValueChange={(value) => setMembershipType(value as MembershipType)}>
              <SelectTrigger className="w-full">
                <SelectValue>{(v) => selectLabel(v)}</SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="REGULAR">Thường</SelectItem>
                <SelectItem value="STUDENT">Sinh viên</SelectItem>
                <SelectItem value="PREMIUM">Premium</SelectItem>
              </SelectContent>
            </Select>
          </FormField>
          <FormField label="Ngày hết hạn" htmlFor="expiryDate" error={errors.expiryDate?.message}>
            <Input id="expiryDate" type="date" {...register("expiryDate")} />
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

function MemberStatusDialog({
  open,
  onOpenChange,
  member,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  member: Member | null
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const [status, setStatus] = React.useState<MemberStatus>("ACTIVE")

  React.useEffect(() => {
    if (open && member) {
      setStatus(member.status)
    }
  }, [open, member])

  const mutation = useMutation({
    mutationFn: () => api.post<Member>(`/members/${member?.id}/status`, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["members"] })
      toast.success("Đã đổi trạng thái độc giả")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Đổi trạng thái</DialogTitle>
          <DialogDescription>{member ? `Cập nhật trạng thái cho ${member.fullName}.` : ""}</DialogDescription>
        </DialogHeader>
        <FormField label="Trạng thái">
          <Select value={status} onValueChange={(value) => setStatus(value as MemberStatus)}>
            <SelectTrigger className="w-full">
              <SelectValue>{(v) => selectLabel(v)}</SelectValue>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ACTIVE">Hoạt động</SelectItem>
              <SelectItem value="SUSPENDED">Tạm khóa</SelectItem>
              <SelectItem value="EXPIRED">Hết hạn</SelectItem>
            </SelectContent>
          </Select>
        </FormField>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
            Hủy
          </Button>
          <Button onClick={() => mutation.mutate()} disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
            Lưu
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
