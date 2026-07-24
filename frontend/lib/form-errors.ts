import { zodResolver } from "@hookform/resolvers/zod"
import type { FieldValues, Path, Resolver, UseFormSetError } from "react-hook-form"
import type { ZodType } from "zod"
import { toast } from "sonner"

import { ApiError } from "@/lib/api"

/**
 * Wraps zodResolver and casts to the form's Resolver type. Centralises the one cast needed because
 * the installed zod 4 minor and @hookform/resolvers type overloads do not line up (runtime is fine).
 */
export function formResolver<T extends FieldValues>(schema: ZodType): Resolver<T> {
  return zodResolver(schema as never) as unknown as Resolver<T>
}

/**
 * Maps a mutation error to the UI: 422 field errors attach to the matching form fields,
 * anything else surfaces as a toast.
 */
export function handleMutationError<T extends FieldValues>(
  error: unknown,
  setError?: UseFormSetError<T>
): void {
  if (error instanceof ApiError) {
    if (error.fieldErrors && error.fieldErrors.length > 0 && setError) {
      let attached = false
      for (const fieldError of error.fieldErrors) {
        if (fieldError.field) {
          setError(fieldError.field as Path<T>, { type: "server", message: fieldError.message })
          attached = true
        }
      }
      if (attached) {
        return
      }
    }
    toast.error(error.message)
    return
  }
  toast.error("Đã xảy ra lỗi không mong muốn")
}
