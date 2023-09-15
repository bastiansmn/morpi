<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {onMounted, onUnmounted, ref} from "vue";
import Game from "../model/game.model.ts";
import axios from "axios";

const router = useRouter();
const route = useRoute();
const roomCode = ref<string>("");
const game = ref<Game | null>(null);

const handleClick = () => {
   axios.post(`/api/game/quit-room?roomCode=${roomCode.value}&playerUUID=${sessionStorage.getItem('playerUUID')}`)
         .then(() => {
            router.push("/");
         })
         .catch((error) => {
            console.error(error);
         });
}

onUnmounted(() => {
   handleClick();
})

onMounted(() => {
   roomCode.value = route.params.roomCode as string;
   axios.get(`/api/game/${roomCode.value}`)
         .then((response) => {
            game.value = response.data as Game;

            if (game.value.finished !== true || game.value.winner !== null)
               router.push(`/room/${roomCode.value}`);
         })
         .catch((error) => {
            console.error(error);
            router.push("/");
         });
})
</script>

<template>
   <pre class="text-2xl font-bold mb-4">
    ______            ___ __   __
   / ____/___ _____ _/ (_) /__/_/
  / __/ / __ `/ __ `/ / / __/ _ \
 / /___/ /_/ / /_/ / / / /_/  __/
/_____/\__, /\__,_/_/_/\__/\___/
      /____/                     </pre>

   <p>
      <span class="font-bold">Joueur 1</span> : {{ game?.player1?.username }}
   </p>

   <p class="mb-2">
      <span class="font-bold">Joueur 2</span> : {{ game?.player2?.username }}
   </p>

   <button @click="handleClick()" class="cursor-pointer rounded bg-cyan-400 hover:bg-cyan-700 transition-all p-2">
      Retour à l'accueil
   </button>
</template>
