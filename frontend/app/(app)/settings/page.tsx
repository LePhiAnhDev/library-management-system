"use client"

import * as React from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { Loader2Icon, PencilIcon } from "lucide-react"
import { toast } from "sonner"

import { FormField } from "@/components/shared/form-field"
import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { DetailSkeleton } from "@/components/shared/skeletons"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { useApi } from "@/lib/api"
import { formResolver, handleMutationError } from "@/lib/form-errors"
import type { LoanPolicy, MembershipType, Settings } from "@/lib/types"

const POLICY_LABEL: Record<MembershipType, string> = {
  REGULAR: "Thường",
  STUDENT: "Sinh viên",
  PREMIUM: "Premium",
}

const POLICY_ORDER: MembershipType[] = ["REGULAR", "STUDENT", "PREMIUM"]

export default function SettingsPage() {
  const api = useApi()
  const query = useQuery({ queryKey: ["settings"], queryFn: () => api.get<Settings>("/settings") })

  return (
    <div className="space-y-6">
      <PageHeader title="Cấu hình" description="Quản lý thông tin thư viện, phí phạt và chính sách mượn" />

      <QueryState query={query} skeleton={<DetailSkeleton />}>
        {(settings) => (
          <div className="space-y-6">
            <LibrarySettingsForm settings={settings} />
            <LoanPoliciesCard policies={settings.loanPolicies} />
          </div>
        )}
      </QueryState>
    </div>
  )
}

const settingsSchema = z.object({
  libraryName: z.string().trim().min(1, "Tên thư viện không được để trống").max(255, "Tối đa 255 ký tự"),
  libraryAddress: z.string().max(500, "Tối đa 500 ký tự").optional(),
  overdueFinePerDay: z.number({ error: "Nhập số hợp lệ" }).min(0, "Không được âm"),
  fineBlockThreshold: z.number({ error: "Nhập số hợp lệ" }).min(0, "Không được âm"),
  reservationHoldDays: z.number({ error: "Nhập số hợp lệ" }).int("Phải là số nguyên").min(1, "Tối thiểu 1 ngày"),
  lostDefaultFee: z.number({ error: "Nhập số hợp lệ" }).min(0, "Không được âm"),
  damagedDefaultFee: z.number({ error: "Nhập số hợp lệ" }).min(0, "Không được âm"),
})

type SettingsFormValues = z.infer<typeof settingsSchema>

function LibrarySettingsForm({ settings }: { settings: Settings }) {
  const api = useApi()
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isDirty },
  } = useForm<SettingsFormValues>({ resolver: formResolver<SettingsFormValues>(settingsSchema) })

  React.useEffect(() => {
    reset({
      libraryName: settings.libraryName,
      libraryAddress: settings.libraryAddress ?? "",
      overdueFinePerDay: settings.overdueFinePerDay,
      fineBlockThreshold: settings.fineBlockThreshold,
      reservationHoldDays: settings.reservationHoldDays,
      lostDefaultFee: settings.lostDefaultFee,
      damagedDefaultFee: settings.damagedDefaultFee,
    })
  }, [settings, reset])

  React.useEffect(() => {
    if (!isDirty) return
    const handler = (event: BeforeUnloadEvent) => {
      event.preventDefault()
      event.returnValue = ""
    }
    window.addEventListener("beforeunload", handler)
    return () => window.removeEventListener("beforeunload", handler)
  }, [isDirty])

  const mutation = useMutation({
    mutationFn: (values: SettingsFormValues) =>
      api.put<Settings>("/settings", { ...values, libraryAddress: values.libraryAddress || null }),
    onSuccess: (_data, values) => {
      queryClient.invalidateQueries({ queryKey: ["settings"] })
      reset(values)
      toast.success("Đã cập nhật cấu hình")
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Thông tin thư viện & phạt</CardTitle>
        <CardDescription>Cấu hình chung áp dụng cho toàn hệ thống.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <FormField
              label="Tên thư viện"
              htmlFor="libraryName"
              required
              error={errors.libraryName?.message}
              className="sm:col-span-2"
            >
              <Input id="libraryName" {...register("libraryName")} />
            </FormField>
            <FormField
              label="Địa chỉ"
              htmlFor="libraryAddress"
              error={errors.libraryAddress?.message}
              className="sm:col-span-2"
            >
              <Input id="libraryAddress" {...register("libraryAddress")} />
            </FormField>
            <FormField
              label="Phí phạt quá hạn mỗi ngày (VND)"
              htmlFor="overdueFinePerDay"
              error={errors.overdueFinePerDay?.message}
            >
              <Input
                id="overdueFinePerDay"
                type="number"
                min="0"
                step="1"
                {...register("overdueFinePerDay", { valueAsNumber: true })}
              />
            </FormField>
            <FormField
              label="Ngưỡng phạt chặn mượn (VND)"
              htmlFor="fineBlockThreshold"
              error={errors.fineBlockThreshold?.message}
            >
              <Input
                id="fineBlockThreshold"
                type="number"
                min="0"
                step="1"
                {...register("fineBlockThreshold", { valueAsNumber: true })}
              />
            </FormField>
            <FormField
              label="Số ngày giữ chỗ đặt trước"
              htmlFor="reservationHoldDays"
              error={errors.reservationHoldDays?.message}
            >
              <Input
                id="reservationHoldDays"
                type="number"
                min="1"
                step="1"
                {...register("reservationHoldDays", { valueAsNumber: true })}
              />
            </FormField>
            <FormField
              label="Phí mặc định khi mất sách (VND)"
              htmlFor="lostDefaultFee"
              error={errors.lostDefaultFee?.message}
            >
              <Input
                id="lostDefaultFee"
                type="number"
                min="0"
                step="1"
                {...register("lostDefaultFee", { valueAsNumber: true })}
              />
            </FormField>
            <FormField
              label="Phí mặc định khi hỏng sách (VND)"
              htmlFor="damagedDefaultFee"
              error={errors.damagedDefaultFee?.message}
            >
              <Input
                id="damagedDefaultFee"
                type="number"
                min="0"
                step="1"
                {...register("damagedDefaultFee", { valueAsNumber: true })}
              />
            </FormField>
          </div>
          <div className="flex justify-end">
            <Button type="submit" disabled={mutation.isPending || !isDirty}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Lưu thay đổi
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function LoanPoliciesCard({ policies }: { policies: LoanPolicy[] }) {
  const [editing, setEditing] = React.useState<LoanPolicy | null>(null)
  const ordered = POLICY_ORDER.map((membershipType) =>
    policies.find((policy) => policy.membershipType === membershipType)
  ).filter((policy): policy is LoanPolicy => Boolean(policy))

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Chính sách mượn theo loại thẻ</CardTitle>
          <CardDescription>Giới hạn số sách, thời gian mượn và số lần gia hạn cho từng loại thẻ.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto rounded-xl border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loại thẻ</TableHead>
                  <TableHead className="text-right">Số sách tối đa</TableHead>
                  <TableHead className="text-right">Số ngày mượn</TableHead>
                  <TableHead className="text-right">Số lần gia hạn</TableHead>
                  <TableHead className="w-10" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {ordered.map((policy) => (
                  <TableRow key={policy.membershipType}>
                    <TableCell className="font-medium">{POLICY_LABEL[policy.membershipType]}</TableCell>
                    <TableCell className="text-right tabular-nums">{policy.maxBooks}</TableCell>
                    <TableCell className="text-right tabular-nums">{policy.loanPeriodDays}</TableCell>
                    <TableCell className="text-right tabular-nums">{policy.maxRenewals}</TableCell>
                    <TableCell>
                      <Button variant="outline" size="sm" onClick={() => setEditing(policy)}>
                        <PencilIcon /> Sửa
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <LoanPolicyDialog policy={editing} onOpenChange={(open) => !open && setEditing(null)} />
    </>
  )
}

const policySchema = z.object({
  maxBooks: z.number({ error: "Nhập số hợp lệ" }).int("Phải là số nguyên").min(1, "Tối thiểu 1"),
  loanPeriodDays: z.number({ error: "Nhập số hợp lệ" }).int("Phải là số nguyên").min(1, "Tối thiểu 1"),
  maxRenewals: z.number({ error: "Nhập số hợp lệ" }).int("Phải là số nguyên").min(0, "Không được âm"),
})

type PolicyFormValues = z.infer<typeof policySchema>

function LoanPolicyDialog({
  policy,
  onOpenChange,
}: {
  policy: LoanPolicy | null
  onOpenChange: (open: boolean) => void
}) {
  const api = useApi()
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<PolicyFormValues>({ resolver: formResolver<PolicyFormValues>(policySchema) })

  React.useEffect(() => {
    if (policy) {
      reset({
        maxBooks: policy.maxBooks,
        loanPeriodDays: policy.loanPeriodDays,
        maxRenewals: policy.maxRenewals,
      })
    }
  }, [policy, reset])

  const mutation = useMutation({
    mutationFn: (values: PolicyFormValues) => api.put(`/settings/loan-policies/${policy!.membershipType}`, values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["settings"] })
      toast.success("Đã cập nhật chính sách mượn")
      onOpenChange(false)
    },
    onError: (error) => handleMutationError(error, setError),
  })

  return (
    <Dialog open={Boolean(policy)} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Chính sách mượn: {policy ? POLICY_LABEL[policy.membershipType] : ""}</DialogTitle>
          <DialogDescription>Cập nhật giới hạn mượn cho loại thẻ này.</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <FormField label="Số sách tối đa" htmlFor="maxBooks" required error={errors.maxBooks?.message}>
            <Input id="maxBooks" type="number" min="1" step="1" autoFocus {...register("maxBooks", { valueAsNumber: true })} />
          </FormField>
          <FormField label="Số ngày mượn" htmlFor="loanPeriodDays" required error={errors.loanPeriodDays?.message}>
            <Input id="loanPeriodDays" type="number" min="1" step="1" {...register("loanPeriodDays", { valueAsNumber: true })} />
          </FormField>
          <FormField label="Số lần gia hạn" htmlFor="maxRenewals" required error={errors.maxRenewals?.message}>
            <Input id="maxRenewals" type="number" min="0" step="1" {...register("maxRenewals", { valueAsNumber: true })} />
          </FormField>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={mutation.isPending}>
              Hủy
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
              Lưu thay đổi
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
