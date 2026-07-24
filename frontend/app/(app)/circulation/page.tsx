"use client"

import * as React from "react"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { BookCheckIcon, BookUpIcon, Loader2Icon, TriangleAlertIcon } from "lucide-react"
import { toast } from "sonner"

import { AsyncCombobox } from "@/components/shared/async-combobox"
import { FormField } from "@/components/shared/form-field"
import { PageHeader } from "@/components/shared/page-header"
import { StatusBadge } from "@/components/shared/status-badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { useApi } from "@/lib/api"
import { selectLabel } from "@/lib/labels"
import { formatDate } from "@/lib/format"
import { handleMutationError } from "@/lib/form-errors"
import type { Loan, Member, Page } from "@/lib/types"

type ReturnCondition = "NORMAL" | "LOST" | "DAMAGED"

export default function CirculationPage() {
  return (
    <div className="space-y-6">
      <PageHeader title="Mượn / Trả" description="Cho mượn và nhận trả sách theo mã vạch bản sao" />

      <Tabs defaultValue="checkout" className="w-full">
        <TabsList>
          <TabsTrigger value="checkout">
            <BookUpIcon /> Cho mượn
          </TabsTrigger>
          <TabsTrigger value="return">
            <BookCheckIcon /> Trả sách
          </TabsTrigger>
        </TabsList>
        <TabsContent value="checkout" className="pt-4">
          <CheckoutTab />
        </TabsContent>
        <TabsContent value="return" className="pt-4">
          <ReturnTab />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function CheckoutTab() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [memberId, setMemberId] = React.useState<number | null>(null)
  const [memberLabel, setMemberLabel] = React.useState<string | null>(null)
  const [barcode, setBarcode] = React.useState("")
  const [result, setResult] = React.useState<Loan | null>(null)

  const loadMembers = React.useCallback(
    async (queryText: string) => {
      const params = new URLSearchParams({ status: "ACTIVE", size: "10" })
      if (queryText) params.set("search", queryText)
      const res = await api.get<Page<Member>>(`/members?${params.toString()}`)
      return res.content.map((member) => ({ value: member.id, label: member.fullName, hint: member.memberCode }))
    },
    [api]
  )

  const mutation = useMutation({
    mutationFn: () => api.post<Loan>("/loans", { memberId, barcode: barcode.trim() }),
    onSuccess: (loan) => {
      queryClient.invalidateQueries({ queryKey: ["loans"] })
      toast.success("Đã cho mượn")
      setResult(loan)
      setBarcode("")
    },
    onError: (error) => handleMutationError(error),
  })

  const canSubmit = memberId !== null && barcode.trim().length > 0

  return (
    <Card className="max-w-2xl">
      <CardHeader>
        <CardTitle>Cho mượn</CardTitle>
        <CardDescription>Chọn độc giả, quét mã vạch bản sao rồi xác nhận.</CardDescription>
      </CardHeader>
      <CardContent>
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
          <FormField label="Mã vạch bản sao" htmlFor="checkout-barcode" required>
            <Input
              id="checkout-barcode"
              autoFocus
              value={barcode}
              onChange={(event) => setBarcode(event.target.value)}
              placeholder="Quét hoặc nhập mã vạch"
            />
          </FormField>
          <Button type="submit" disabled={!canSubmit || mutation.isPending}>
            {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
            Cho mượn
          </Button>
        </form>

        {result ? (
          <div className="mt-5 space-y-1.5 rounded-lg border bg-muted/40 p-4 text-sm">
            <div className="flex items-center gap-2">
              <StatusBadge status={result.status} />
              <span className="text-muted-foreground">Mã phiếu {result.code}</span>
            </div>
            <p className="font-medium">{result.bookTitle}</p>
            <p className="text-muted-foreground">
              {result.memberName} · {result.memberCode}
            </p>
            <p>
              Hạn trả: <span className="font-medium">{formatDate(result.dueDate)}</span>
            </p>
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}

function ReturnTab() {
  const api = useApi()
  const queryClient = useQueryClient()
  const [barcode, setBarcode] = React.useState("")
  const [condition, setCondition] = React.useState<ReturnCondition>("NORMAL")
  const [note, setNote] = React.useState("")
  const [result, setResult] = React.useState<Loan | null>(null)

  const mutation = useMutation({
    mutationFn: () => {
      const payload: { barcode: string; condition: ReturnCondition; note?: string } = {
        barcode: barcode.trim(),
        condition,
      }
      if (note.trim()) payload.note = note.trim()
      return api.post<Loan>("/loans/return", payload)
    },
    onSuccess: (loan) => {
      queryClient.invalidateQueries({ queryKey: ["loans"] })
      toast.success("Đã trả sách")
      setResult(loan)
      setBarcode("")
      setNote("")
      setCondition("NORMAL")
    },
    onError: (error) => handleMutationError(error),
  })

  const canSubmit = barcode.trim().length > 0

  return (
    <Card className="max-w-2xl">
      <CardHeader>
        <CardTitle>Trả sách</CardTitle>
        <CardDescription>Quét mã vạch bản sao để ghi nhận trả.</CardDescription>
      </CardHeader>
      <CardContent>
        <form
          onSubmit={(event) => {
            event.preventDefault()
            if (canSubmit) mutation.mutate()
          }}
          className="space-y-4"
        >
          <FormField label="Mã vạch bản sao" htmlFor="return-barcode" required>
            <Input
              id="return-barcode"
              autoFocus
              value={barcode}
              onChange={(event) => setBarcode(event.target.value)}
              placeholder="Quét hoặc nhập mã vạch"
            />
          </FormField>
          <FormField label="Tình trạng sách">
            <Select value={condition} onValueChange={(value) => setCondition(value as ReturnCondition)}>
              <SelectTrigger className="w-full">
                <SelectValue>{(v) => selectLabel(v)}</SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="NORMAL">Bình thường</SelectItem>
                <SelectItem value="LOST">Mất</SelectItem>
                <SelectItem value="DAMAGED">Hỏng</SelectItem>
              </SelectContent>
            </Select>
          </FormField>
          <FormField label="Ghi chú" htmlFor="return-note">
            <Textarea id="return-note" rows={3} value={note} onChange={(event) => setNote(event.target.value)} />
          </FormField>
          <Button type="submit" disabled={!canSubmit || mutation.isPending}>
            {mutation.isPending ? <Loader2Icon className="size-4 animate-spin" /> : null}
            Trả sách
          </Button>
        </form>

        {result ? (
          <div className="mt-5 space-y-1.5 rounded-lg border bg-muted/40 p-4 text-sm">
            <div className="flex items-center gap-2">
              <StatusBadge status={result.status} />
              <span className="text-muted-foreground">Mã phiếu {result.code}</span>
            </div>
            <p className="font-medium">{result.bookTitle}</p>
            <p className="text-muted-foreground">
              {result.memberName} · {result.memberCode}
            </p>
            <p>
              Hạn trả: {formatDate(result.dueDate)} · Ngày trả:{" "}
              <span className="font-medium">{formatDate(result.returnDate)}</span>
            </p>
            {result.overdue ? (
              <div className="mt-1 flex items-center gap-2 rounded-md bg-warning/20 px-2.5 py-1.5 text-warning-foreground">
                <TriangleAlertIcon className="size-4 shrink-0" />
                Trả trễ hạn. Kiểm tra phạt nếu quá hạn.
              </div>
            ) : (
              <p className="text-muted-foreground">Kiểm tra phạt nếu quá hạn.</p>
            )}
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
