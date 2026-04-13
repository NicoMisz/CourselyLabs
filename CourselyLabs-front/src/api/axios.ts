import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

// --- Session sync callbacks ---
let onTokenRefreshed: ((accessToken: string, refreshToken?: string) => void) | null = null
let onSessionExpired: (() => void) | null = null

export function setSessionCallbacks(
  refreshed: (accessToken: string, refreshToken?: string) => void,
  expired: () => void,
) {
  onTokenRefreshed = refreshed
  onSessionExpired = expired
}

// --- Request interceptor ---
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- Response interceptor ---
let isRefreshing = false
let failedQueue: { resolve: (token: string) => void; reject: (err: unknown) => void }[] = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)))
  failedQueue = []
}

function clearSessionAndRedirect() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  onSessionExpired?.()
  window.location.href = '/login'
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as RetryConfig  // ✅ aquí dentro
    if (
      error.response?.status !== 401 ||
      original._retry ||
      original.url?.includes('/api/auth/refresh') ||
      original.url?.includes('/api/auth/login')
    ) {
      return Promise.reject(error)
    }

    const storedRefresh = localStorage.getItem('refreshToken')
    if (!storedRefresh) {
      clearSessionAndRedirect()
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then((token) => {
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      })
    }

    original._retry = true
    isRefreshing = true

    try {
      const { data } = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
        { refreshToken: storedRefresh },
      )
      localStorage.setItem('accessToken', data.accessToken)
      if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken)
      onTokenRefreshed?.(data.accessToken, data.refreshToken)
      processQueue(null, data.accessToken)
      original.headers.Authorization = `Bearer ${data.accessToken}`
      return api(original)
    } catch (err) {
      processQueue(err, null)
      clearSessionAndRedirect()
      return Promise.reject(err)
    } finally {
      isRefreshing = false
    }
  },
)

export default api