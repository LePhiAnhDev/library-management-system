import type { LucideIcon } from "lucide-react"

import { cn } from "@/lib/utils"

/**
 * Designed empty state with an invitation to act (spec 9.6).
 */
export function EmptyState({
  icon: Icon,
  title,
  description,
  action,
  className,
}: {
  icon?: LucideIcon
  title: string
  description?: string
  action?: React.ReactNode
  className?: string
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-border/70 bg-muted/20 px-6 py-14 text-center",
        className
      )}
    >
      {Icon ? (
        <div className="flex size-14 items-center justify-center rounded-2xl bg-muted text-muted-foreground ring-1 ring-inset ring-border/60">
          <Icon className="size-6" />
        </div>
      ) : null}
      <div className="space-y-1">
        <p className="font-heading font-semibold">{title}</p>
        {description ? <p className="mx-auto max-w-sm text-sm text-muted-foreground">{description}</p> : null}
      </div>
      {action}
    </div>
  )
}
