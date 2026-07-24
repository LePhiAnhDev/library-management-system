"use client"

import * as React from "react"
import { useRouter } from "next/navigation"

import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { useApi } from "@/lib/api"
import { NAV_ITEMS } from "@/lib/nav"
import type { Book, Member, Page } from "@/lib/types"

export function CommandMenu({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const router = useRouter()
  const api = useApi()
  const [query, setQuery] = React.useState("")
  const [books, setBooks] = React.useState<Book[]>([])
  const [members, setMembers] = React.useState<Member[]>([])

  React.useEffect(() => {
    if (query.trim().length < 2) {
      setBooks([])
      setMembers([])
      return
    }
    let active = true
    const timer = setTimeout(async () => {
      try {
        const [bookPage, memberPage] = await Promise.all([
          api.get<Page<Book>>(`/books?search=${encodeURIComponent(query)}&size=5`),
          api.get<Page<Member>>(`/members?search=${encodeURIComponent(query)}&size=5`),
        ])
        if (active) {
          setBooks(bookPage.content)
          setMembers(memberPage.content)
        }
      } catch {
        if (active) {
          setBooks([])
          setMembers([])
        }
      }
    }, 250)
    return () => {
      active = false
      clearTimeout(timer)
    }
  }, [query, api])

  const go = (href: string) => {
    onOpenChange(false)
    setQuery("")
    router.push(href)
  }

  const navMatches = NAV_ITEMS.filter((item) =>
    item.title.toLowerCase().includes(query.trim().toLowerCase())
  )

  return (
    <CommandDialog open={open} onOpenChange={onOpenChange}>
      <CommandInput placeholder="Tìm kiếm sách, độc giả hoặc điều hướng..." value={query} onValueChange={setQuery} />
      <CommandList>
        <CommandEmpty>Không tìm thấy kết quả</CommandEmpty>
        {navMatches.length > 0 ? (
          <CommandGroup heading="Điều hướng">
            {navMatches.map((item) => (
              <CommandItem key={item.href} value={`nav-${item.href}`} onSelect={() => go(item.href)}>
                <item.icon />
                <span>{item.title}</span>
              </CommandItem>
            ))}
          </CommandGroup>
        ) : null}
        {books.length > 0 ? (
          <CommandGroup heading="Sách">
            {books.map((book) => (
              <CommandItem key={`book-${book.id}`} value={`book-${book.id}`} onSelect={() => go(`/books/${book.id}`)}>
                <span className="truncate">{book.title}</span>
                <span className="ml-auto text-xs text-muted-foreground">{book.isbn}</span>
              </CommandItem>
            ))}
          </CommandGroup>
        ) : null}
        {members.length > 0 ? (
          <CommandGroup heading="Độc giả">
            {members.map((member) => (
              <CommandItem
                key={`member-${member.id}`}
                value={`member-${member.id}`}
                onSelect={() => go(`/members/${member.id}`)}
              >
                <span className="truncate">{member.fullName}</span>
                <span className="ml-auto text-xs text-muted-foreground">{member.memberCode}</span>
              </CommandItem>
            ))}
          </CommandGroup>
        ) : null}
      </CommandList>
    </CommandDialog>
  )
}
