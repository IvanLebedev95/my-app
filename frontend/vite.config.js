import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // порт для dev-сервера (не используется в Docker)
    host: true
  }
});