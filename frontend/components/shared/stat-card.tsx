import type { LucideIcon } from "lucide-react"

import { Card, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"

/**
 * KPI tile for the dashboard.
 */
export function StatCard({
  label,
  value,
  icon: Icon,
  hint,
  accent,
}: {
  label: string
  value: string | number
  icon?: LucideIcon
  hint?: string
  accent?: "default" | "danger" | "warning" | "success"
}) {
  const accentClass = {
    default: "text-muted-foreground",
    danger: "text-destructive",
    warning: "text-warning-foreground",
    success: "text-success",
  }[accent ?? "default"]

  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-3">
        <div className="min-w-0 space-y-1">
          <p className="text-sm text-muted-foreground">{label}</p>
          <p className="font-heading text-2xl font-semibold tracking-tight tabular-nums">{value}</p>
          {hint ? <p className="truncate text-xs text-muted-foreground">{hint}</p> : null}
        </div>
        {Icon ? (
          <div className={cn("rounded-lg bg-muted p-2", accentClass)}>
            <Icon className="size-5" />
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
