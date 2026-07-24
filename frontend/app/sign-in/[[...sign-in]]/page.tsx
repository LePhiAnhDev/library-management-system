import { SignIn } from "@clerk/nextjs"
import { LibraryBigIcon } from "lucide-react"

export default function SignInPage() {
  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-muted/40 p-6">
      <div className="flex items-center gap-2.5">
        <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-success text-primary-foreground shadow-sm ring-1 ring-inset ring-white/15">
          <LibraryBigIcon className="size-6" />
        </div>
        <div className="grid leading-tight">
          <span className="font-heading text-lg font-semibold tracking-tight">Thư viện</span>
          <span className="text-xs text-muted-foreground">Hệ thống quản lý thư viện</span>
        </div>
      </div>
      <SignIn />
    </div>
  )
}
