export interface ShortenUrlRequest {
  longUrl: string
  customAlias?: string
  expiresAt?: string
}

export interface ShortenUrlResponse {
  shortUrl: string
  alias: string
  originalUrl: string
  tags: string[]
  category: string
  seoTitle?: string
  qrCodeUrl?: string
  expiresAt?: string
  createdAt: string
  aiGenerated: boolean
}

export interface AuthRequest {
  email: string
  password: string
}

export interface RegisterRequest extends AuthRequest {
  tenantId: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  email: string
  tenantId: string
  role: string
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  traceId: string
  fieldErrors?: Record<string, string>
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface AnalyticsSummary {
  alias: string
  totalClicks: number
  topCountries: { country: string; hits: number }[]
  dailyHits: { date: string; hits: number }[]
}
