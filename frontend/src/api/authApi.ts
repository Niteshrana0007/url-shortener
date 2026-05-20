import { apiClient } from './client'
import type { AuthRequest, AuthResponse, RegisterRequest } from './types'

export const authApi = {
  login: async (payload: AuthRequest): Promise<AuthResponse> => {
    const { data } = await apiClient.post<AuthResponse>('/auth/login', payload)
    return data
  },

  register: async (payload: RegisterRequest): Promise<AuthResponse> => {
    const { data } = await apiClient.post<AuthResponse>('/auth/register', payload)
    return data
  }
}
