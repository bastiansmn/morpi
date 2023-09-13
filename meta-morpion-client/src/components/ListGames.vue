<script setup lang="ts">
import {onMounted, ref} from "vue";
import Game, {GameType} from "../model/game.model.ts";
import axios from "axios";
import DynamicTitle from "./DynamicTitle.vue";
import Player from "../model/player.model.ts";
import router from "../router.ts";

const gameList = ref<Game[]>([])
const player = ref<Player | null>(null);
const currentGame = ref<Game | null>(null);

onMounted(() => {
   axios.get("/api/game/all")
         .then(({data}) => {
            gameList.value = data
         })
})

function registerPlayer(){
  let pseudoInput = document.getElementById("pseudo") as HTMLInputElement | null;

  return axios.post("/api/game/register", null, {
    params: {
      username: pseudoInput?.value
    }
  })
}

function gameForm($event: any) {
  $event.preventDefault();

  registerPlayer()
      .then(({data}) => {
        player.value = data
        sessionStorage.setItem("playerUUID", data.uuid as string);
        sessionStorage.setItem("playerUsername", data.username as string);

        let isFirstValue = (document.querySelector("input[type='radio'][name=player]:checked") as HTMLInputElement)?.value;
        let isFirst = (isFirstValue === "x");

        let gameTypeValue = (document.querySelector("input[type='radio'][name=typeGame]:checked") as HTMLInputElement)?.value;
        let gameType: GameType;
        switch(gameTypeValue) {
          case "IA":
            gameType = GameType.PVE;
            break;
          case "Multi":
            gameType = GameType.PVP_ONLINE;
            break;
          default:
            gameType = GameType.PVP_LOCAL;
        }

        axios.post("/api/game/create", null, {
          params: {
            gameType: gameType,
            playerUUID: player.value?.uuid,
            firstToPlay: isFirst
          }
        }).then((({data}) => {
          currentGame.value = data
          let path: string = `/room/${currentGame.value?.roomCode}`;
          router.push(path);
        })).catch(err => {
          console.error(err);
        })
      });
}
</script>

<template>
   <dynamic-title class="mb-10"></dynamic-title>

  <form>
    <ul>
      <li>
        <label for="pseudo">Pseudo&nbsp;:&nbsp;</label>
        <input type="text" name="user_name" id="pseudo"/>
      </li>
      <li>
        <input type="radio" name="player" value="x" id="x" checked> <label for="x">X (first)</label> &nbsp;
        <input type="radio" name="player" value="o" id="o"> <label for="o">O (second)</label><br>
      </li>
      <li>
        <input type="radio" name="typeGame" value="Local" id="Local" checked> <label for="Local">Local</label> &nbsp;
        <input type="radio" name="typeGame" value="Multi" id="Multi"> <label for="Multi">Multi</label> &nbsp;
        <input type="radio" name="typeGame" value="IA" id="IA"> <label for="IA">IA</label><br>
      </li>
    </ul>
    <button type="submit" @click="gameForm($event)">Créer une partie</button>
  </form>

   <h1 class="text-3xl mb-12">
      Liste des parties disponibles
   </h1>

   <div>
      <router-link :to="`/room/${game.roomCode}`"
                   class="flex justify-start p-4 bg-cyan-700 hover:bg-cyan-400 transition-all rounded text-white hover:text-white mb-2"
                   :key="game.roomCode" v-for="game in gameList">
        <span class="font-bold">
         {{ game.roomCode }}
        </span>
         <span>&nbsp;- Partie de {{ game.player1?.username }}
        </span>
      </router-link>
  </div>
</template>