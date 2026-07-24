import type { Metadata } from "next"
import { ClerkProvider } from "@clerk/nextjs"
import { Geist_Mono, Inter } from "next/font/google"

import "./globals.css"
import { Providers } from "@/components/providers"
import { ThemeProvider } from "@/components/theme-provider"
import { cn } from "@/lib/utils"

const inter = Inter({ subsets: ["latin"], variable: "--font-sans" })
const fontMono = Geist_Mono({ subsets: ["latin"], variable: "--font-mono" })

export const metadata: Metadata = {
  title: "Thư viện",
  description: "Hệ thống quản lý thư viện",
}

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <ClerkProvider>
      <html
        lang="vi"
        suppressHydrationWarning
        className={cn("antialiased", fontMono.variable, "font-sans", inter.variable)}
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
