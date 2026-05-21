// frontend/src/pages/RedirectHandler.tsx
import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

export default function RedirectHandler() {
  const { alias } = useParams();

  useEffect(() => {
    if (alias) {
      // Direct call to the BACKEND URL (Render), not the Vercel URL
      // This triggers the 302 redirect from UrlShortenerController.java
      const backendUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
      window.location.href = `${backendUrl}/${alias}`;
    }
  }, [alias]);

  return <div className="flex h-screen items-center justify-center text-indigo-400">Redirecting...</div>;
}