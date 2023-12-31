import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
   plugins: [vue()],
   server: {
      port: 4000,
      proxy: {
         '^/api': {
            target: 'http://localhost:8080',
            ws: true,
            rewrite: (path) => path.replace(/^\/api/, '')
         }
      }
   }
})
