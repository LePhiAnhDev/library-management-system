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
  const toneClass = {
    default: "bg-primary/10 text-primary ring-primary/15",
    danger: "bg-destructive/10 text-destructive ring-destructive/15",
    warning: "bg-warning/20 text-warning-foreground ring-warning/25",
    success: "bg-success/10 text-success ring-success/15",
  }[accent ?? "default"]

  return (
    <Card className="transition-shadow duration-200 hover:shadow-card-hover">
      <CardContent className="flex items-start justify-between gap-3">
        <div className="min-w-0 space-y-1.5">
          <p className="text-sm font-medium text-muted-foreground">{label}</p>
          <p className="font-heading text-3xl font-semibold tracking-tight tabular-nums">{value}</p>
          {hint ? <p className="truncate text-xs text-muted-foreground">{hint}</p> : null}
        </div>
        {Icon ? (
          <div className={cn("flex size-11 shrink-0 items-center justify-center rounded-xl ring-1 ring-inset", toneClass)}>
            <Icon className="size-5" />
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
