import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { urlApi } from '@/api/urlApi'

export default function AnalyticsPage() {
  const { alias } = useParams<{ alias: string }>()

  const { data, isLoading } = useQuery({
    queryKey: ['url-details', alias],
    queryFn: () => urlApi.getUrlDetails(alias!),
    enabled: !!alias
  })

  if (isLoading) return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-indigo-400 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="min-h-screen bg-slate-900 text-white px-6 py-8">
      <div className="max-w-4xl mx-auto">
        <Link to="/dashboard" className="text-indigo-400 text-sm mb-6 block">← Dashboard</Link>
        <h1 className="text-2xl font-bold mb-1">Analytics: <span className="text-indigo-400">{alias}</span></h1>
        <p className="text-slate-400 text-sm mb-8">{data?.originalUrl}</p>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <Stat label="Category" value={data?.category ?? '—'} />
          <Stat label="Tags" value={data?.tags?.join(', ') ?? '—'} />
          <Stat label="Created" value={data?.createdAt ? new Date(data.createdAt).toLocaleDateString() : '—'} />
          <Stat label="AI Generated" value={data?.aiGenerated ? 'Yes ✨' : 'No'} />
        </div>

        <div className="bg-slate-800 rounded-xl p-6">
          <h2 className="text-lg font-semibold mb-4">Click Activity (coming soon)</h2>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={[
              { day: 'Mon', hits: 12 }, { day: 'Tue', hits: 28 }, { day: 'Wed', hits: 19 },
              { day: 'Thu', hits: 35 }, { day: 'Fri', hits: 42 }, { day: 'Sat', hits: 15 }, { day: 'Sun', hits: 8 }
            ]}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="day" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: 8 }} />
              <Bar dataKey="hits" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-slate-800 rounded-xl p-4">
      <p className="text-slate-400 text-xs mb-1">{label}</p>
      <p className="text-white font-medium truncate">{value}</p>
    </div>
  )
}
