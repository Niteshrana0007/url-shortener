import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '@/api/authApi'
import { useAuthStore } from '@/auth/authStore'
import { getErrorMessage } from '@/api/client'
import toast from 'react-hot-toast'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(8)
})
type FormData = z.infer<typeof schema>

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema)
  })

  const { mutate, isPending } = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      login(data.accessToken, data.email, data.tenantId, data.role)
      navigate('/dashboard')
    },
    onError: (err) => toast.error(getErrorMessage(err))
  })

  return (
    <div className="min-h-screen bg-slate-900 text-white flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <h1 className="text-4xl font-bold text-indigo-400 mb-2">⚡ SwiftLinkAI</h1>
        <p className="text-slate-400 mb-8">Sign in to your account</p>

        <form onSubmit={handleSubmit((d) => mutate(d))} className="space-y-4">
          <div>
            <label className="block text-sm text-slate-300 mb-1">Email</label>
            <input {...register('email')} type="email"
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            {errors.email && <p className="text-red-400 text-xs mt-1">{errors.email.message}</p>}
          </div>
          <div>
            <label className="block text-sm text-slate-300 mb-1">Password</label>
            <input {...register('password')} type="password"
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            {errors.password && <p className="text-red-400 text-xs mt-1">{errors.password.message}</p>}
          </div>
          <button type="submit" disabled={isPending}
            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 font-semibold py-3 rounded-lg transition">
            {isPending ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        <p className="text-center text-sm text-slate-400 mt-6">
          No account? <Link to="/register" className="text-indigo-400 hover:underline">Register</Link>
        </p>
      </div>
    </div>
  )
}
