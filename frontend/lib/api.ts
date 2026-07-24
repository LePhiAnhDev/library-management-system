"use client"

import { useAuth } from "@clerk/nextjs"
import { useMemo } from "react"

const BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

export interface FieldError {
  field: string
  message: string
}

export class ApiError extends Error {
  constructor(
    public status: number,
    public errorCode: string | undefined,
    message: string,
    public fieldErrors?: FieldError[]
  ) {
    super(message)
    this.name = "ApiError"
  }
}

export interface ApiClient {
  get: <T>(path: string) => Promise<T>
  post: <T>(path: string, body?: unknown) => Promise<T>
  put: <T>(path: string, body?: unknown) => Promise<T>
  patch: <T>(path: string, body?: unknown) => Promise<T>
  del: (path: string) => Promise<void>
  upload: <T>(path: string, form: FormData) => Promise<T>
  download: (path: string) => Promise<Blob>
}

/**
 * Typed client for the backend. A fresh Clerk token is fetched per request (getToken auto-refreshes),
 * so the short session-token TTL is never an issue. Responses are unwrapped from the ApiResponse
 * envelope; failures throw an ApiError carrying the errorCode and any field errors.
 */
export function useApi(): ApiClient {
  const { getToken } = useAuth()

  return useMemo<ApiClient>(() => {
    async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
      const token = await getToken()
      const headers: Record<string, string> = {}
      if (token) {
        headers.Authorization = `Bearer ${token}`
      }
      let payload: BodyInit | undefined
      if (body instanceof FormData) {
        payload = body
      } else if (body !== undefined) {
        headers["Content-Type"] = "application/json"
        payload = JSON.stringify(body)
      }
      const res = await fetch(`${BASE}/api/v1${path}`, { method, headers, body: payload })
      if (res.status === 204) {
        return undefined as T
      }
      const json = await res.json().catch(() => null)
      if (!res.ok || (json && json.success === false)) {
        const fieldErrors = Array.isArray(json?.data) ? (json.data as FieldError[]) : undefined
        throw new ApiError(res.status, json?.errorCode, json?.message ?? `Lỗi ${res.status}`, fieldErrors)
      }
      return json?.data as T
    }

    return {
      get: <T>(path: string) => request<T>("GET", path),
      post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
      put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
      patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, body),
      del: (path: string) => request<void>("DELETE", path),
      upload: <T>(path: string, form: FormData) => request<T>("POST", path, form),
      download: async (path: string) => {
        const token = await getToken()
        const res = await fetch(`${BASE}/api/v1${path}`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
        if (!res.ok) {
          throw new ApiError(res.status, undefined, `Lỗi ${res.status}`)
        }
        return res.blob()
      },
    }
  }, [getToken])
}
