// frontend/src/pages/RedirectHandler.tsx
import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

export default function RedirectHandler() {
  const { alias } = useParams();

  useEffect(() => {
    if (alias) {
      // Get the base API URL (e.g., https://your-backend.onrender.com/api/v1)
      const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
      
      // Ensure we don't have double slashes or double /api/v1
      // If apiBase is "https://api.com/api/v1", we just need to add /alias
      const finalUrl = apiBase.endsWith('/') ? `${apiBase}${alias}` : `${apiBase}/${alias}`;
      
      window.location.href = finalUrl;
    }
  }, [alias]);

  return (
    <div className="flex h-screen items-center justify-center bg-slate-900 text-indigo-400">
      <p>Redirecting to original URL...</p>
    </div>
  );
}