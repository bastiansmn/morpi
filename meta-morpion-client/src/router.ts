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
         component: () => import('./components/Room.vue')
      },
      {
         path: '/room/:roomCode/win',
         component: () => import('./components/Win.vue'),
      },
      {
         path: '/room/:roomCode/draw',
         component: () => import('./components/Draw.vue'),
      },
      {
         path: '/room/:roomCode/lose',
         component: () => import('./components/Lose.vue'),
      }
   ]
})