"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { UserButton } from "@clerk/nextjs"
import { SearchIcon } from "lucide-react"

import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { SidebarTrigger } from "@/components/ui/sidebar"
import { SEGMENT_LABELS } from "@/lib/nav"

export function AppHeader({ onOpenCommand }: { onOpenCommand: () => void }) {
  const pathname = usePathname()
  const segments = pathname.split("/").filter(Boolean)
  const section = segments[0]
  const hasDetail = segments.length > 1

  return (
    <header className="sticky top-0 z-20 flex h-14 shrink-0 items-center gap-2 border-b bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/80">
      <SidebarTrigger />
      <Separator orientation="vertical" className="mr-1 h-5" />
      <Breadcrumb>
        <BreadcrumbList>
          {!section ? (
            <BreadcrumbItem>
              <BreadcrumbPage>Bảng điều khiển</BreadcrumbPage>
            </BreadcrumbItem>
          ) : (
            <>
              <BreadcrumbItem className="hidden sm:inline-flex">
                <BreadcrumbLink render={<Link href="/" />}>Bảng điều khiển</BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator className="hidden sm:inline-flex" />
              <BreadcrumbItem>
                {hasDetail ? (
                  <BreadcrumbLink render={<Link href={`/${section}`} />}>
                    {SEGMENT_LABELS[section] ?? section}
                  </BreadcrumbLink>
                ) : (
                  <BreadcrumbPage>{SEGMENT_LABELS[section] ?? section}</BreadcrumbPage>
                )}
              </BreadcrumbItem>
              {hasDetail ? (
                <>
                  <BreadcrumbSeparator />
                  <BreadcrumbItem>
                    <BreadcrumbPage>Chi tiết</BreadcrumbPage>
                  </BreadcrumbItem>
                </>
              ) : null}
            </>
          )}
        </BreadcrumbList>
      </Breadcrumb>
      <div className="ml-auto flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          className="gap-2 text-muted-foreground"
          onClick={onOpenCommand}
        >
          <SearchIcon className="size-4" />
          <span className="hidden md:inline">Tìm kiếm...</span>
          <kbd className="hidden rounded border bg-muted px-1.5 font-mono text-[10px] md:inline">Ctrl K</kbd>
        </Button>
        <UserButton />
      </div>
    </header>
  )
}
