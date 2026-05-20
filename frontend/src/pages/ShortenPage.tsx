import { useNavigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { urlApi } from '@/api/urlApi'
import { getErrorMessage } from '@/api/client'
import toast from 'react-hot-toast'

const schema = z.object({
  longUrl: z.string().url('Must be a valid URL'),
  customAlias: z.string()
    .regex(/^[a-zA-Z0-9-_]{3,50}$/, 'Only alphanumeric, hyphens, underscores (3-50 chars)')
    .optional()
    .or(z.literal(''))
})

type FormData = z.infer<typeof schema>

export default function ShortenPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema)
  })

  const { mutate, isPending } = useMutation({
    mutationFn: urlApi.shorten,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['urls', 'list'] })
      toast.success(`Shortened! ${data.shortUrl}`)
      navigate('/dashboard')
    },
    onError: (err) => toast.error(getErrorMessage(err))
  })

  const onSubmit = (data: FormData) => {
    mutate({
      longUrl: data.longUrl,
      customAlias: data.customAlias || undefined
    })
  }

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-md">
        <Link to="/dashboard" className="text-indigo-400 text-sm mb-6 block">← Back to dashboard</Link>
        <h1 className="text-3xl font-bold mb-2">Shorten a URL</h1>
        <p className="text-slate-400 mb-8">Our AI will generate a smart alias, tags, and SEO metadata.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Long URL <span className="text-red-400">*</span>
            </label>
            <input
              {...register('longUrl')}
              type="url"
              placeholder="https://example.com/very/long/url"
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-3 text-sm
                         focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-slate-500"
            />
            {errors.longUrl && (
              <p className="text-red-400 text-xs mt-1">{errors.longUrl.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Custom Alias <span className="text-slate-500">(optional)</span>
            </label>
            <input
              {...register('customAlias')}
              type="text"
              placeholder="my-custom-alias"
              className="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-3 text-sm
                         focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder:text-slate-500"
            />
            {errors.customAlias && (
              <p className="text-red-400 text-xs mt-1">{errors.customAlias.message}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={isPending}
            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50
                       disabled:cursor-not-allowed text-white font-semibold py-3 rounded-lg transition">
            {isPending ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                AI is generating…
              </span>
            ) : '⚡ Shorten with AI'}
          </button>
        </form>
      </div>
    </div>
  )
}
