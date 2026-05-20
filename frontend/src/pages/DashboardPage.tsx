import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { urlApi } from '@/api/urlApi'
import { useAuthStore } from '@/auth/authStore'
import toast from 'react-hot-toast'

export default function DashboardPage() {
  const { email, tenantId, logout } = useAuthStore()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['urls', 'list'],
    queryFn: () => urlApi.listUrls(0, 20)
  })

  if (isLoading) return <Loader />
  if (isError) return <p className="text-red-400 p-8">Failed to load URLs.</p>

  return (
    <div className="min-h-screen bg-slate-900 text-white">
      {/* Nav */}
      <nav className="border-b border-slate-700 px-6 py-4 flex items-center justify-between">
        <h1 className="text-xl font-bold text-indigo-400">⚡ SwiftLinkAI</h1>
        <div className="flex items-center gap-4">
          <span className="text-sm text-slate-400">{email} · {tenantId}</span>
          <Link to="/shorten"
            className="bg-indigo-600 hover:bg-indigo-500 px-4 py-2 rounded-lg text-sm font-medium transition">
            + Shorten URL
          </Link>
          <button onClick={logout} className="text-sm text-slate-400 hover:text-white transition">
            Sign out
          </button>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto px-6 py-8">
        <h2 className="text-2xl font-semibold mb-6">Your URLs</h2>

        {data?.content.length === 0 ? (
                  <EmptyState />
                ) : (
                  <div className="space-y-3">
                    {data?.content.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="space-y-3">
            {/* Uncomment and add safe fallback array mapping */}
            {(data?.content || []).map((url) => (
              <UrlCard key={url.alias} url={url} />
            ))}
          </div>
        )}
          </div>
        )}
      </main>
    </div>
  )
}

function UrlCard({ url }: { url: import('@/api/types').ShortenUrlResponse }) {
  const handleCopy = () => {
    navigator.clipboard.writeText(url.shortUrl)
    toast.success('Copied to clipboard!')
  }

  return (
    <div className="bg-slate-800 rounded-xl p-4 flex items-center justify-between gap-4">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-indigo-400 font-mono font-medium">{url.shortUrl}</span>
          {url.aiGenerated && (
            <span className="text-xs bg-indigo-900 text-indigo-300 px-2 py-0.5 rounded-full">AI</span>
          )}
          {url.category && (
            <span className="text-xs bg-slate-700 text-slate-300 px-2 py-0.5 rounded-full">
              {url.category}
            </span>
          )}
        </div>
        <p className="text-slate-400 text-sm truncate">{url.originalUrl}</p>
        {url.tags?.length > 0 && (
          <div className="flex gap-1 mt-1">
            {url.tags.map((t) => (
              <span key={t} className="text-xs bg-slate-700 text-slate-300 px-1.5 py-0.5 rounded">
                {t}
              </span>
            ))}
          </div>
        )}
      </div>
      <div className="flex items-center gap-2 shrink-0">
        <button onClick={handleCopy}
          className="text-xs bg-slate-700 hover:bg-slate-600 px-3 py-1.5 rounded-lg transition">
          Copy
        </button>
        <Link to={`/analytics/${url.alias}`}
          className="text-xs bg-slate-700 hover:bg-slate-600 px-3 py-1.5 rounded-lg transition">
          Analytics
        </Link>
      </div>
    </div>
  )
}

function EmptyState() {
  return (
    <div className="text-center py-20 text-slate-400">
      <p className="text-5xl mb-4">🔗</p>
      <p className="text-lg">No URLs yet.</p>
      <Link to="/shorten"
        className="mt-4 inline-block bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2 rounded-lg transition">
        Shorten your first URL
      </Link>
    </div>
  )
}

function Loader() {
  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-indigo-400 border-t-transparent rounded-full animate-spin" />
    </div>
  )
}
