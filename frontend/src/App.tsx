import { Suspense, lazy } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { ProtectedRoute } from '@/auth/ProtectedRoute'
import RedirectHandler from './pages/RedirectHandler'

const LoginPage     = lazy(() => import('@/pages/LoginPage'))
const RegisterPage  = lazy(() => import('@/pages/RegisterPage'))
const DashboardPage = lazy(() => import('@/pages/DashboardPage'))
const ShortenPage   = lazy(() => import('@/pages/ShortenPage'))
const AnalyticsPage = lazy(() => import('@/pages/AnalyticsPage'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 2 },
    mutations: { retry: 1 }
  }
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Suspense fallback={<div className="flex h-screen items-center justify-center text-indigo-400">Loading…</div>}>
          <Routes>
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/:alias"   element={<RedirectHandler />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/"           element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard"  element={<DashboardPage />} />
              <Route path="/shorten"    element={<ShortenPage />} />
              <Route path="/analytics/:alias" element={<AnalyticsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
      <Toaster position="top-right" />
    </QueryClientProvider>
  )
}
