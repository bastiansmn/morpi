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
      }
   ]
})