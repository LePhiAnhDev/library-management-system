"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { LibraryBigIcon } from "lucide-react"

import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { NAV_GROUPS, isActivePath } from "@/lib/nav"

export function AppSidebar() {
  const pathname = usePathname()

  return (
    <Sidebar>
      <SidebarHeader>
        <div className="flex items-center gap-2.5 px-2 py-2">
          <div className="flex size-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-success text-primary-foreground shadow-sm ring-1 ring-inset ring-white/15">
            <LibraryBigIcon className="size-5" />
          </div>
          <div className="grid text-sm leading-tight">
            <span className="font-heading font-semibold tracking-tight">Thư viện</span>
            <span className="text-xs text-muted-foreground">Quản lý thư viện</span>
          </div>
        </div>
      </SidebarHeader>
      <SidebarContent>
        {NAV_GROUPS.map((group) => (
          <SidebarGroup key={group.label}>
            <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {group.items.map((item) => (
                  <SidebarMenuItem key={item.href}>
                    <SidebarMenuButton
                      isActive={isActivePath(pathname, item.href)}
                      tooltip={item.title}
                      render={<Link href={item.href} />}
                    >
                      <item.icon />
                      <span>{item.title}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>
    </Sidebar>
  )
}
