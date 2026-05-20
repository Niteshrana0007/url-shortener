import { apiClient } from './client'
import type { PageResponse, ShortenUrlRequest, ShortenUrlResponse } from './types'

export const urlApi = {
  shorten: async (payload: ShortenUrlRequest): Promise<ShortenUrlResponse> => {
    const { data } = await apiClient.post<ShortenUrlResponse>('/shorten', payload)
    return data
  },

  listUrls: async (page = 0, size = 20): Promise<PageResponse<ShortenUrlResponse>> => {
    const { data } = await apiClient.get<PageResponse<ShortenUrlResponse>>('/urls', {
      params: { page, size }
    })
    return data
  },

  getUrlDetails: async (alias: string): Promise<ShortenUrlResponse> => {
    const { data } = await apiClient.get<ShortenUrlResponse>(`/urls/${alias}`)
    return data
  },

  deactivateUrl: async (alias: string): Promise<void> => {
    await apiClient.delete(`/urls/${alias}`)
  }
}
