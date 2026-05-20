import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi } from 'vitest'

// Mock the auth store
vi.mock('@/auth/authStore', () => ({
  useAuthStore: () => ({
    email: 'test@example.com',
    tenantId: 'tenant-1',
    logout: vi.fn(),
    isAuthenticated: true
  })
}))

// Mock react-query
vi.mock('@tanstack/react-query', () => ({
  useQuery: () => ({
    data: {
      content: [
        {
          alias: 'test-alias',
          shortUrl: 'https://swiftlink.ai/test-alias',
          originalUrl: 'https://example.com',
          tags: ['Tech'],
          category: 'Technology',
          aiGenerated: true,
          createdAt: '2024-01-01T00:00:00'
        }
      ]
    },
    isLoading: false,
    isError: false
  })
}))

import DashboardPage from '@/pages/DashboardPage'

describe('DashboardPage', () => {
  it('renders URL list', () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    )
    expect(screen.getByText('https://swiftlink.ai/test-alias')).toBeInTheDocument()
    expect(screen.getByText('AI')).toBeInTheDocument()
    expect(screen.getByText('Technology')).toBeInTheDocument()
  })

  it('shows shorten button', () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    )
    expect(screen.getByText('+ Shorten URL')).toBeInTheDocument()
  })
})
