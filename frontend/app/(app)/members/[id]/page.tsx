"use client"

import { useParams } from "next/navigation"
import { useQuery } from "@tanstack/react-query"

import { PageHeader } from "@/components/shared/page-header"
import { QueryState } from "@/components/shared/query-state"
import { DetailSkeleton } from "@/components/shared/skeletons"
import { StatusBadge } from "@/components/shared/status-badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useApi } from "@/lib/api"
import { formatCurrency, formatDate } from "@/lib/format"
import type { FineType, MemberProfile, MembershipType } from "@/lib/types"

const MEMBERSHIP_LABELS: Record<MembershipType, string> = {
  REGULAR: "Thường",
  STUDENT: "Sinh viên",
  PREMIUM: "Premium",
}

const FINE_TYPE_LABELS: Record<FineType, string> = {
  OVERDUE: "Quá hạn",
  LOST: "Mất sách",
  DAMAGED: "Hỏng sách",
}

export default function MemberProfilePage() {
  const api = useApi()
  const { id } = useParams<{ id: string }>()

  const query = useQuery({
    queryKey: ["member-profile", id],
    queryFn: () => api.get<MemberProfile>(`/members/${id}/profile`),
  })

  return (
    <div className="space-y-6">
      <QueryState query={query} skeleton={<DetailSkeleton />}>
        {(profile) => {
          const { member } = profile
          return (
            <div className="space-y-6">
              <PageHeader
                title={member.fullName}
                description={`Mã thẻ ${member.memberCode}`}
                actions={<StatusBadge status={member.status} />}
              />

              <div className="grid gap-4 lg:grid-cols-2">
                <Card>
                  <CardHeader>
                    <CardTitle>Thông tin thẻ</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-3 text-sm sm:grid-cols-2">
                      <Info label="Mã thẻ" value={<span className="font-mono">{member.memberCode}</span>} />
                      <Info label="Loại thẻ" value={MEMBERSHIP_LABELS[member.membershipType]} />
                      <Info label="Email" value={member.email} />
                      <Info label="Điện thoại" value={member.phone ?? "—"} />
                      <Info label="Địa chỉ" value={member.address ?? "—"} className="sm:col-span-2" />
                      <Info label="Ngày tham gia" value={formatDate(member.joinDate)} />
                      <Info label="Ngày hết hạn" value={formatDate(member.expiryDate)} />
                      <Info label="Trạng thái" value={<StatusBadge status={member.status} />} />
                    </dl>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Đang mượn ({profile.currentLoans.length})</CardTitle>
                  </CardHeader>
                  <CardContent>
                    {profile.currentLoans.length === 0 ? (
                      <p className="py-2 text-sm text-muted-foreground">Không có sách đang mượn.</p>
                    ) : (
                      <ul className="divide-y">
                        {profile.currentLoans.map((loan) => (
                          <li key={loan.id} className="flex items-center justify-between gap-3 py-2.5 first:pt-0 last:pb-0">
                            <div className="min-w-0">
                              <p className="truncate text-sm font-medium">{loan.bookTitle}</p>
                              <p className="text-xs text-muted-foreground">Hạn trả {formatDate(loan.dueDate)}</p>
                            </div>
                            <StatusBadge status={loan.status} />
                          </li>
                        ))}
                      </ul>
                    )}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Phạt chưa thu</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <p className="text-2xl font-semibold text-destructive">
                      {formatCurrency(profile.totalUnpaidFines)}
                    </p>
                    {profile.unpaidFines.length === 0 ? (
                      <p className="py-2 text-sm text-muted-foreground">Không có khoản phạt chưa thu.</p>
                    ) : (
                      <ul className="divide-y">
                        {profile.unpaidFines.map((fine) => (
                          <li key={fine.id} className="flex items-start justify-between gap-3 py-2.5 first:pt-0 last:pb-0">
                            <div className="min-w-0">
                              <p className="text-sm font-medium">{FINE_TYPE_LABELS[fine.type]}</p>
                              {fine.reason ? (
                                <p className="truncate text-xs text-muted-foreground">{fine.reason}</p>
                              ) : null}
                            </div>
                            <span className="shrink-0 text-sm font-medium">{formatCurrency(fine.amount)}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Đặt trước đang chờ ({profile.activeReservations.length})</CardTitle>
                  </CardHeader>
                  <CardContent>
                    {profile.activeReservations.length === 0 ? (
                      <p className="py-2 text-sm text-muted-foreground">Không có đặt trước đang chờ.</p>
                    ) : (
                      <ul className="divide-y">
                        {profile.activeReservations.map((reservation) => (
                          <li
                            key={reservation.id}
                            className="flex items-center justify-between gap-3 py-2.5 first:pt-0 last:pb-0"
                          >
                            <p className="min-w-0 truncate text-sm font-medium">{reservation.bookTitle}</p>
                            <StatusBadge status={reservation.status} />
                          </li>
                        ))}
                      </ul>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>
          )
        }}
      </QueryState>
    </div>
  )
}

function Info({
  label,
  value,
  className,
}: {
  label: string
  value: React.ReactNode
  className?: string
}) {
  return (
    <div className={className}>
      <dt className="text-xs text-muted-foreground">{label}</dt>
      <dd className="mt-0.5">{value}</dd>
    </div>
  )
}
