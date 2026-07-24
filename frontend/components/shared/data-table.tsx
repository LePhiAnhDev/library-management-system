"use client"

import { ArrowDownIcon, ArrowUpIcon, ChevronsUpDownIcon } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { cn } from "@/lib/utils"

export interface Column<T> {
  key: string
  header: string
  sortable?: boolean
  className?: string
  cell: (row: T) => React.ReactNode
}

export interface SortState {
  field: string
  dir: "asc" | "desc"
}

export function DataTable<T>({
  columns,
  rows,
  rowKey,
  sort,
  onSortChange,
  onRowClick,
}: {
  columns: Column<T>[]
  rows: T[]
  rowKey: (row: T) => string | number
  sort?: SortState
  onSortChange?: (field: string) => void
  onRowClick?: (row: T) => void
}) {
  return (
    <div className="overflow-x-auto rounded-xl border bg-card shadow-card">
      <Table>
        <TableHeader>
          <TableRow>
            {columns.map((col) => (
              <TableHead key={col.key} className={col.className}>
                {col.sortable && onSortChange ? (
                  <button
                    type="button"
                    onClick={() => onSortChange(col.key)}
                    className="-mx-1 inline-flex items-center gap-1 rounded px-1 hover:text-foreground"
                  >
                    {col.header}
                    {sort?.field === col.key ? (
                      sort.dir === "asc" ? (
                        <ArrowUpIcon className="size-3.5" />
                      ) : (
                        <ArrowDownIcon className="size-3.5" />
                      )
                    ) : (
                      <ChevronsUpDownIcon className="size-3.5 opacity-40" />
                    )}
                  </button>
                ) : (
                  col.header
                )}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((row) => (
            <TableRow
              key={rowKey(row)}
              className={cn(onRowClick && "cursor-pointer")}
              onClick={onRowClick ? () => onRowClick(row) : undefined}
            >
              {columns.map((col) => (
                <TableCell key={col.key} className={col.className}>
                  {col.cell(row)}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

export function DataTablePagination({
  page,
  size,
  totalPages,
  totalElements,
  onPageChange,
}: {
  page: number
  size: number
  totalPages: number
  totalElements: number
  onPageChange: (page: number) => void
}) {
  const from = totalElements === 0 ? 0 : page * size + 1
  const to = Math.min((page + 1) * size, totalElements)
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 text-sm text-muted-foreground">
      <span>
        {from}-{to} trên {totalElements} mục
      </span>
      <div className="flex items-center gap-2">
        <span>
          Trang {page + 1}/{Math.max(totalPages, 1)}
        </span>
        <Button variant="outline" size="sm" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
          Trước
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Sau
        </Button>
      </div>
    </div>
  )
}
