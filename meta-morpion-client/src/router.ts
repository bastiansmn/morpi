import { createRouter, createWebHistory } from 'vue-router'
import ListGames from './components/ListGames.vue'

export default createRouter({
   history: createWebHistory(),
   routes: [
      {
         path: '/',
         component: ListGames,
      },
      {
         path: '/room/:roomCode',
         component: () => import('./components/Room.vue'),
      },
      {
         path: '/groupE',
         component: () => import('./components/api_e/ListGames.vue'),
      },
      {
         path: '/roomE',
         component: () => import('./components/api_e/roomE.vue'),
      }
   ]
})