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
  password: z.string().min(8, 'Password must be at least 8 characters'),
  tenantId: z.string().min(3, 'Tenant ID must be at least 3 characters').max(64)
})
type FormData = z.infer<typeof schema>

export default function RegisterPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema)
  })

  const { mutate, isPending } = useMutation({
    mutationFn: authApi.register,
    onSuccess: (data) => {
      login(data.accessToken, data.email, data.tenantId, data.role)
      navigate('/dashboard')
    },
    onError: (err) => toast.error(getErrorMessage(err))
  })

  const fields: { name: keyof FormData; label: string; type: string; placeholder: string }[] = [
    { name: 'email', label: 'Email', type: 'email', placeholder: 'you@company.com' },
    { name: 'password', label: 'Password', type: 'password', placeholder: 'Min. 8 characters' },
    { name: 'tenantId', label: 'Organisation / Tenant ID', type: 'text', placeholder: 'my-company' }
  ]

  return (
    <div className="min-h-screen bg-slate-900 text-white flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <h1 className="text-4xl font-bold text-indigo-400 mb-2">⚡ SwiftLinkAI</h1>
        <p className="text-slate-400 mb-8">Create your account</p>
        <form onSubmit={handleSubmit((d) => mutate(d))} className="space-y-4">
          {fields.map(({ name, label, type, placeholder }) => (
            <div key={name}>
              <label className="block text-sm text-slate-300 mb-1">{label}</label>
              <input {...register(name)} type={type} placeholder={placeholder}
                className="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-slate-500" />
              {errors[name] && <p className="text-red-400 text-xs mt-1">{errors[name]?.message}</p>}
            </div>
          ))}
          <button type="submit" disabled={isPending}
            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 font-semibold py-3 rounded-lg transition">
            {isPending ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
        <p className="text-center text-sm text-slate-400 mt-6">
          Already have an account? <Link to="/login" className="text-indigo-400 hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
