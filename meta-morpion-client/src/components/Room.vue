<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import sockjs from "sockjs-client/dist/sockjs"
import Stomp from "stompjs";
import {onMounted, onUnmounted, ref} from "vue";
import Game, {GameType} from "../model/game.model.ts";
import axios from "axios";
import {AfterMoveState} from "../model/action.model.ts";
import {CellStatus} from "../model/grid.model.ts";

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

            if (!game.value) return;
            game.value.currentPlayerId = body.nextPlayerUUID;
            game.value.finished = body.gameFinished;
            game.value.winner = body.winnerUUID;

            if(game.value.finished) {
              let winner = (game.value?.player1.uuid === game.value?.winner) ? game.value.player1 : game.value.player2;
              alert(winner.username + " gagne la partie!");
            }

            switch (body.type) {
              case "AFTER_MOVE":
                const cell = document.querySelector(`#cell-${body.i}-${body.j}`);
                if (!cell) return;

                if (game.value?.player1.uuid == body.playerUUID) {
                  cell.setAttribute("disabled", "true");
                  cell.classList.add("bg-red-700");
                  cell.classList.add("cursor-not-allowed");
                  cell.classList.remove("hover:bg-cyan-400");
                  cell.classList.remove("cursor-pointer");
                } else {
                  cell.setAttribute("disabled", "true");
                  cell.classList.add("bg-green-700");
                  cell.classList.add("cursor-not-allowed");
                  cell.classList.remove("hover:bg-cyan-400");
                  cell.classList.remove("cursor-pointer");
                }


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
  if(!game.value?.finished) {
    // Check if cell is disabled
    const cell = document.querySelector(`#cell-${opt.i}-${opt.j}`);
    if (!cell) return;
    if (cell.getAttribute("disabled")) return;

    switch (game.value?.gameType) {
      case GameType.PVP_LOCAL:
        stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({i: opt.i, j: opt.j, playerUUID: game.value.currentPlayerId}));
        break;
      case GameType.PVP_ONLINE:
        stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({i: opt.i, j: opt.j, playerUUID: sessionStorage.getItem('playerUUID')}));
        break;
    }
  }
}

const GRID_SIZE = ref(3);
const SUBGRID_SIZE = ref(3);

const getCell = (i: number, j: number, ii: number, jj: number) => {
  // @ts-ignore
  return game.value?.grid.subgrids[i-1][j-1].cells[ii-1][jj-1];
}
</script>

<template>
  <div>
    <h3 id="turn">Tour de {{game?.currentPlayerId === game?.player1?.uuid ? game?.player1?.username : game?.player2?.username}} ({{game?.currentSymbol}})</h3>
  </div>

  <div id="Players">
    <p>Player 1: {{ game?.player1?.username }}
    </p>
    <p>Player 2: {{ game?.player2?.username }}</p>
  </div>

  <div id="Jeu">
    <div class="grid grid-rows-1 grid-cols-3">
      <div v-for="j in GRID_SIZE">
        <div v-for="i in GRID_SIZE">
          <div class="grid grid-rows-1 grid-cols-3 m-2">
            <div v-for="jj in SUBGRID_SIZE">
              <div
                  :id="`cell-${((ii-1)+((i-1)*3))}-${((jj-1)+((j-1)*3))}`"
                  class="cell p-5 bg-cyan-700 hover:bg-cyan-400 cursor-pointer rounded m-1 relative"
                  v-for="ii in SUBGRID_SIZE"
                  @click="handleMove(computeCell(i, j, ii, jj))"
              >
                <span v-if="getCell(i, j, ii, jj) === CellStatus.EMPTY" class="absolute">
                  &nbsp;
                </span>
                <span v-if="getCell(i, j, ii, jj) === CellStatus.X" class="font-bold text-2xl">
                  &#10005;
                </span>
                <span v-if="getCell(i, j, ii, jj) === CellStatus.O" class="font-semibold text-2xl">
                  O
                </span>
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
.cell > span {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>