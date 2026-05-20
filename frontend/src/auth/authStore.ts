import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  accessToken: string | null
  email: string | null
  tenantId: string | null
  role: string | null
  isAuthenticated: boolean
  login: (token: string, email: string, tenantId: string, role: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      email: null,
      tenantId: null,
      role: null,
      isAuthenticated: false,

      login: (token, email, tenantId, role) =>
        set({ accessToken: token, email, tenantId, role, isAuthenticated: true }),

      logout: () =>
        set({ accessToken: null, email: null, tenantId: null, role: null, isAuthenticated: false })
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        email: state.email,
        tenantId: state.tenantId,
        role: state.role,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
)
