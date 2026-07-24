"use client"

import * as React from "react"
import { ChevronsUpDownIcon, Loader2Icon } from "lucide-react"

import {
  Command,
  CommandEmpty,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"

export interface ComboOption {
  value: number
  label: string
  hint?: string
}

/**
 * Searchable async select for choosing a related entity (author, category, member, book...).
 * Filtering is server-side, so cmdk's built-in filter is disabled and results are debounced.
 */
export function AsyncCombobox({
  value,
  selectedLabel,
  onChange,
  loadOptions,
  placeholder = "Chọn...",
  emptyText = "Không có kết quả",
  disabled,
}: {
  value: number | null
  selectedLabel?: string | null
  onChange: (value: number | null, label: string | null) => void
  loadOptions: (query: string) => Promise<ComboOption[]>
  placeholder?: string
  emptyText?: string
  disabled?: boolean
}) {
  const [open, setOpen] = React.useState(false)
  const [query, setQuery] = React.useState("")
  const [options, setOptions] = React.useState<ComboOption[]>([])
  const [loading, setLoading] = React.useState(false)
  const loadRef = React.useRef(loadOptions)
  loadRef.current = loadOptions

  React.useEffect(() => {
    if (!open) return
    let active = true
    setLoading(true)
    const timer = setTimeout(() => {
      loadRef
        .current(query)
        .then((opts) => {
          if (active) setOptions(opts)
        })
        .catch(() => {
          if (active) setOptions([])
        })
        .finally(() => {
          if (active) setLoading(false)
        })
    }, 250)
    return () => {
      active = false
      clearTimeout(timer)
    }
  }, [open, query])

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        disabled={disabled}
        className={cn(
          "flex h-8 w-full items-center justify-between gap-2 rounded-lg border border-input bg-background px-2.5 text-sm outline-none",
          "focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50"
        )}
      >
        <span className={cn("truncate", !value && "text-muted-foreground")}>
          {value ? selectedLabel ?? `#${value}` : placeholder}
        </span>
        <ChevronsUpDownIcon className="size-4 shrink-0 opacity-50" />
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0" align="start">
        <Command shouldFilter={false}>
          <CommandInput placeholder="Tìm kiếm..." value={query} onValueChange={setQuery} />
          <CommandList>
            {loading ? (
              <div className="flex items-center justify-center gap-2 py-6 text-sm text-muted-foreground">
                <Loader2Icon className="size-4 animate-spin" /> Đang tải
              </div>
            ) : (
              <>
                <CommandEmpty>{emptyText}</CommandEmpty>
                {value ? (
                  <CommandItem
                    value="__clear__"
                    onSelect={() => {
                      onChange(null, null)
                      setOpen(false)
                    }}
                    className="text-muted-foreground"
                  >
                    Bỏ chọn
                  </CommandItem>
                ) : null}
                {options.map((opt) => (
                  <CommandItem
                    key={opt.value}
                    value={String(opt.value)}
                    onSelect={() => {
                      onChange(opt.value, opt.label)
                      setOpen(false)
                    }}
                  >
                    <div className="flex flex-col">
                      <span>{opt.label}</span>
                      {opt.hint ? <span className="text-xs text-muted-foreground">{opt.hint}</span> : null}
                    </div>
                  </CommandItem>
                ))}
              </>
            )}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
