"use client"

import * as React from "react"

import { AppHeader } from "@/components/app-header"
import { AppSidebar } from "@/components/app-sidebar"
import { CommandMenu } from "@/components/command-menu"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const [commandOpen, setCommandOpen] = React.useState(false)

  React.useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault()
        setCommandOpen((prev) => !prev)
      }
    }
    window.addEventListener("keydown", onKeyDown)
    return () => window.removeEventListener("keydown", onKeyDown)
  }, [])

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <AppHeader onOpenCommand={() => setCommandOpen(true)} />
        <main className="flex-1 space-y-6 p-4 md:p-6">{children}</main>
      </SidebarInset>
      <CommandMenu open={commandOpen} onOpenChange={setCommandOpen} />
    </SidebarProvider>
  )
}
