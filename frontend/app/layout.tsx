import type { Metadata } from "next"
import { ClerkProvider } from "@clerk/nextjs"
import { Be_Vietnam_Pro, Geist_Mono, Inter } from "next/font/google"

import "./globals.css"
import { Providers } from "@/components/providers"
import { ThemeProvider } from "@/components/theme-provider"
import { cn } from "@/lib/utils"

const inter = Inter({ subsets: ["latin", "vietnamese"], variable: "--font-sans" })
const fontDisplay = Be_Vietnam_Pro({
  subsets: ["latin", "vietnamese"],
  weight: ["500", "600", "700"],
  variable: "--font-display",
})
const fontMono = Geist_Mono({ subsets: ["latin"], variable: "--font-mono" })

export const metadata: Metadata = {
  title: "Thư viện",
  description: "Hệ thống quản lý thư viện",
}

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <ClerkProvider
      appearance={{
        variables: {
          colorPrimary: "#15803d",
          borderRadius: "0.65rem",
          fontFamily: "var(--font-sans)",
        },
      }}
    >
      <html
        lang="vi"
        suppressHydrationWarning
        className={cn("antialiased", fontMono.variable, fontDisplay.variable, "font-sans", inter.variable)}
      >
        <body>
          <ThemeProvider>
            <Providers>{children}</Providers>
          </ThemeProvider>
        </body>
      </html>
    </ClerkProvider>
  )
}
