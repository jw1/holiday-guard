import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080'
    }
  },
  build: {
    outDir: 'target/classes/static'
  },
  test: {
    globals: true,
    environment: 'jsdom',
  }
})
