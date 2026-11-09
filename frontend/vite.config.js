import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Les appels /api sont envoyés au back (Spring Boot) pendant le développement
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
