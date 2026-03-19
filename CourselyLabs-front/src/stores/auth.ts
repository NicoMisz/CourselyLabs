import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api, { setSessionCallbacks } from '@/api/axios'
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const user = ref<User | null>(
    JSON.parse(localStorage.getItem('user') ?? 'null') as User | null,
  )

  const isLoggedIn = computed(() => !!accessToken.value && !!user.value)

  // Sync Pinia state when axios refreshes or expires the token
  setSessionCallbacks(
    (newAccess, newRefresh) => {
      accessToken.value = newAccess
      if (newRefresh) refreshToken.value = newRefresh
    },
    () => {
      accessToken.value = null
      refreshToken.value = null
      user.value = null
    },
  )
  const userRole = computed(() => user.value?.role ?? null)

  async function login(credentials: LoginRequest): Promise<void> {
    const { data } = await api.post<AuthResponse>('/api/auth/login', credentials)
    setSession(data)
  }

  async function register(payload: RegisterRequest): Promise<void> {
    const { data } = await api.post<AuthResponse>('/api/auth/register', payload)
    setSession(data)
  }

  async function logout(): Promise<void> {
    const token = refreshToken.value
    clearSession()
    if (token) {
      await api.post('/api/auth/logout', { refreshToken: token }).catch(() => {})
    }
  }

  function setSession(data: AuthResponse): void {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify(data.user))
  }

  function clearSession(): void {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { accessToken, refreshToken, user, isLoggedIn, userRole, login, register, logout }
})
