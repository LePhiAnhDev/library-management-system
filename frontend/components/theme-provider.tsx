"use client"

import { ThemeProvider as NextThemesProvider } from "next-themes"

/**
 * The app uses the light theme only (see spec 9.1). forcedTheme pins light and no toggle is exposed.
 */
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  return (
    <NextThemesProvider attribute="class" forcedTheme="light" enableSystem={false} disableTransitionOnChange>
      {children}
    </NextThemesProvider>
  )
}
