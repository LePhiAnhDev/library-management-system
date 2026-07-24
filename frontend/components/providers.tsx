"use client"

import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { useState } from "react"

import { Toaster } from "@/components/ui/sonner"
import { TooltipProvider } from "@/components/ui/tooltip"

/**
 * Client-side providers: TanStack Query for data fetching / the four UI states, tooltips and toasts.
 */
export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
        },
      })
  )

  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>{children}</TooltipProvider>
      <Toaster richColors closeButton position="top-right" />
    </QueryClientProvider>
  )
}
