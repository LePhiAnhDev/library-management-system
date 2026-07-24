"use client"

import type { UseQueryResult } from "@tanstack/react-query"
import { TriangleAlertIcon } from "lucide-react"

import { Button } from "@/components/ui/button"

export function ErrorState({ message, onRetry }: { message?: string; onRetry?: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed px-6 py-14 text-center">
      <div className="rounded-full bg-destructive/10 p-3 text-destructive">
        <TriangleAlertIcon className="size-6" />
      </div>
      <div className="space-y-1">
        <p className="font-medium">Không tải được dữ liệu</p>
        <p className="mx-auto max-w-sm text-sm text-muted-foreground">
          {message ?? "Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử lại."}
        </p>
      </div>
      {onRetry ? (
        <Button variant="outline" size="sm" onClick={onRetry}>
          Thử lại
        </Button>
      ) : null}
    </div>
  )
}

/**
 * Renders the four data states consistently: loading (skeleton), error (retry), empty, populated.
 */
export function QueryState<T>({
  query,
  skeleton,
  isEmpty,
  empty,
  children,
}: {
  query: UseQueryResult<T>
  skeleton: React.ReactNode
  isEmpty?: (data: T) => boolean
  empty?: React.ReactNode
  children: (data: T) => React.ReactNode
}) {
  if (query.isPending) {
    return <>{skeleton}</>
  }
  if (query.isError) {
    const message = query.error instanceof Error ? query.error.message : undefined
    return <ErrorState message={message} onRetry={() => query.refetch()} />
  }
  const data = query.data as T
  if (isEmpty && isEmpty(data)) {
    return <>{empty}</>
  }
  return <>{children(data)}</>
}
