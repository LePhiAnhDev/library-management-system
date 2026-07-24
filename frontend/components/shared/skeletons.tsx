import { Skeleton } from "@/components/ui/skeleton"

export function TableSkeleton({ rows = 8 }: { rows?: number }) {
  return (
    <div className="space-y-2.5">
      <Skeleton className="h-9 w-full" />
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} className="h-12 w-full" />
      ))}
    </div>
  )
}

export function StatCardsSkeleton({ count = 4 }: { count?: number }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className="h-28 w-full rounded-xl" />
      ))}
    </div>
  )
}

export function DetailSkeleton() {
  return (
    <div className="space-y-4">
      <Skeleton className="h-8 w-1/3" />
      <Skeleton className="h-48 w-full rounded-xl" />
      <Skeleton className="h-48 w-full rounded-xl" />
    </div>
  )
}
