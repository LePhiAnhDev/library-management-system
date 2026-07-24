"use client"

import { Loader2Icon } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

/**
 * Controlled confirmation dialog. The parent owns open state and closes it after the action resolves.
 */
export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel = "Xác nhận",
  cancelLabel = "Hủy",
  destructive,
  loading,
  onConfirm,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  confirmLabel?: string
  cancelLabel?: string
  destructive?: boolean
  loading?: boolean
  onConfirm: () => void
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description ? <DialogDescription>{description}</DialogDescription> : null}
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={loading}>
            {cancelLabel}
          </Button>
          <Button variant={destructive ? "destructive" : "default"} onClick={onConfirm} disabled={loading}>
            {loading ? <Loader2Icon className="size-4 animate-spin" /> : null}
            {confirmLabel}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
