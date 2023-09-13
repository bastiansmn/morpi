<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import sockjs from "sockjs-client/dist/sockjs"
import Stomp from "stompjs";
import {onMounted, onUnmounted, ref} from "vue";
import Game from "../model/game.model.ts";
import axios from "axios";
import {AfterMoveState} from "../model/action.model.ts";

const router = useRouter();
const route = useRoute();
const roomCode = ref<string>("");
const game = ref<Game | null>(null);
const stompClient = ref<any>(null);

const computeCell = (i: number, j: number, ii: number, jj: number) => {
  return {i: ((ii-1)+((i-1)*3)), j: ((jj-1)+((j-1)*3))};
}

onMounted(() => {
  roomCode.value = route.params.roomCode as string;
  axios.post(`/api/game/join-room?roomCode=${roomCode.value}&playerUUID=${sessionStorage.getItem('playerUUID')}`)
      .then(({data}) => {
        game.value = data

        let socket = sockjs('/api/data-info');
        stompClient.value = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.value.connect({}, (frame: any) => {
          console.log('Connected: ' + frame);

          stompClient.value.subscribe(`/room/${roomCode.value}`, (val: any) => {
            console.log(JSON.parse(val.body));
            const body: AfterMoveState = JSON.parse(val.body);

            switch (body.type) {
              case "AFTER_MOVE":
                const cell = document.querySelector(`#cell-${body.i}-${body.j}`);
                if (!cell) return;
                cell.setAttribute("disabled", "true");
                cell.classList.add("bg-red-700");
                cell.classList.add("cursor-not-allowed");
                cell.classList.remove("hover:bg-cyan-400");
                cell.classList.remove("cursor-pointer");
                break;
            }
          });
        });
      })
      .catch(err => {
        console.error(err);
        router.push("/");
      });
});

onUnmounted(() => {
  stompClient.value.send(`/app/leave-room/${roomCode.value}`, {}, JSON.stringify({playerUUID: sessionStorage.getItem('playerUUID')}));
  stompClient.value.disconnect();
});

const handleMove = (opt: {i: number, j: number}) => {
  // Check if cell is disabled
  const cell = document.querySelector(`#cell-${opt.i}-${opt.j}`);
  if (!cell) return;
  if (cell.getAttribute("disabled")) return;

  stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({i: opt.i, j: opt.j, playerUUID: sessionStorage.getItem('playerUUID')}));
}

const GRID_SIZE = ref(3);
const SUBGRID_SIZE = ref(3);
</script>

<template>
  <div>
    <h3>Tour de </h3>
  </div>

  <div id="Jeu">
    <div class="grid grid-rows-1 grid-cols-3">
      <div v-for="j in GRID_SIZE">
        <div v-for="i in GRID_SIZE">
          <div class="grid grid-rows-1 grid-cols-3 m-2">
            <div v-for="jj in SUBGRID_SIZE">
              <div :id="`cell-${((ii-1)+((i-1)*3))}-${((jj-1)+((j-1)*3))}`" class="p-5 bg-cyan-700 hover:bg-cyan-400 cursor-pointer rounded m-1" v-for="ii in SUBGRID_SIZE" @click="handleMove(computeCell(i, j, ii, jj))">

              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div id="StatutJeu"></div>
  </div>
</template>

<style scoped>
.cross {
  background-image: url("https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Iconic_font_-_fontawesome_-_times.svg/1200px-Iconic_font_-_fontawesome_-_times.svg.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.circle {
  background-image: url("https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Iconic_font_-_fontawesome_-_circle.svg/1200px-Iconic_font_-_fontawesome_-_circle.svg.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
</style>