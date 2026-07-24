import { Label } from "@/components/ui/label"
import { cn } from "@/lib/utils"

/**
 * Wraps a control with its label, optional description and inline validation error.
 */
export function FormField({
  label,
  htmlFor,
  error,
  required,
  description,
  children,
  className,
}: {
  label?: string
  htmlFor?: string
  error?: string
  required?: boolean
  description?: string
  children: React.ReactNode
  className?: string
}) {
  return (
    <div className={cn("space-y-1.5", className)}>
      {label ? (
        <Label htmlFor={htmlFor}>
          {label}
          {required ? <span className="text-destructive"> *</span> : null}
        </Label>
      ) : null}
      {children}
      {description && !error ? <p className="text-xs text-muted-foreground">{description}</p> : null}
      {error ? <p className="text-xs text-destructive">{error}</p> : null}
    </div>
  )
}
