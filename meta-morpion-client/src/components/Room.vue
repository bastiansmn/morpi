<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import sockjs from "sockjs-client/dist/sockjs"
import Stomp from "stompjs";
import {computed, onMounted, ref} from "vue";
import Game, {GameType} from "../model/game.model.ts";
import axios from "axios";
import {AfterMoveState} from "../model/action.model.ts";
import {CellStatus} from "../model/grid.model.ts";

const router = useRouter();
const route = useRoute();
const roomCode = ref<string>("");
const game = ref<Game | null>(null);
const stompClient = ref<any>(null);

const GRID_SIZE = ref(3);
const SUBGRID_SIZE = ref(3);

const computeCell = (i: number, j: number, ii: number, jj: number) => {
   return {i: ((ii) + ((i) * 3)), j: ((jj) + ((j) * 3))};
}

const confetti = ref<HTMLCanvasElement | null>(null);

const fireConfetti = function fireConfetti() {
   const ctx = confetti.value?.getContext('2d');

   let confettiParticles = Array.from({length: 500}).map(() => ({
      x: Math.random() * window.innerWidth,
      y: 0,
      radius: Math.random() * 10 + 1,
      speedX: Math.random() * 3 - 1.5,
      speedY: Math.random() * 3 + 1,
      color: ["#ff0000", "#00ff00", "#0000ff", "#ffff00", "#ff00ff", "#00ffff"][Math.floor(Math.random() * 6)],
      shape: Math.random() < 0.5 ? "circle" : "square",
   }));

   const animateConfetti = () => {
      if (!confetti || !ctx) return;
      ctx.clearRect(0, 0, confetti.value?.width ?? 0, confetti.value?.height ?? 0);

      confettiParticles.forEach(particle => {
         ctx.fillStyle = particle.color;
         if (particle.shape === "circle") {
            ctx.beginPath();
            ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
            ctx.fill();
         } else {
            ctx.fillRect(particle.x, particle.y, particle.radius * 2, particle.radius * 2);
         }

         particle.x += particle.speedX;
         particle.y += particle.speedY;

         if (particle.y > window.innerHeight) {
            particle.y = 0;
            particle.x = Math.random() * window.innerWidth;
         }
      });

      requestAnimationFrame(animateConfetti);
   };

   console.log("fire confetti");
   animateConfetti();
};

const setWinnerOnCompletedSubgrid = (subgridUUID: string) => {
   const isTheSameCells = (y: number, yIsJ: boolean, cells: CellStatus[][]) => {
      if (yIsJ) {
         if (cells[0][y] == CellStatus.EMPTY) return false;
         return ((cells[0][y] == cells[1][y]) && (cells[1][y] == cells[2][y]));
      } else {
         if (cells[y][0] == CellStatus.EMPTY) return false;
         return ((cells[y][0] == cells[y][1]) && (cells[y][1] == cells[y][2]));
      }
   }

   const isTheSameCellsForDiagonale = (cells: CellStatus[][]) => {
      const diag1 = (cells[0][0] == cells[1][1])
            && (cells[1][1] == cells[2][2])
            && cells[0][0] != CellStatus.EMPTY;
      const diag2 = (cells[2][0] == cells[1][1])
            && (cells[1][1] == cells[0][2])
            && cells[2][0] != CellStatus.EMPTY;
      return diag1 || diag2;
   }

   const subgrid = game.value?.grid.subgrids.flat()
         .find(s => s.uuid === subgridUUID)
         ?.cells;

   if (!subgrid)
      return CellStatus.EMPTY;

   console.log(subgrid);

   for (let i = 0; i < SUBGRID_SIZE.value; i++) {
      if (isTheSameCells(i, true, subgrid)) {
         return subgrid[0][i];
      } else if (isTheSameCells(i, false, subgrid)) {
         return subgrid[i][0];
      }
   }

   if (isTheSameCellsForDiagonale(subgrid)) {
      return subgrid[1][1];
   }

   return CellStatus.EMPTY;
}

onMounted(() => {
   roomCode.value = route.params.roomCode as string;
   axios.post(`/api/game/join-room?roomCode=${roomCode.value}&playerUUID=${sessionStorage.getItem('playerUUID')}`)
         .then(({data}) => {
            game.value = data

            let socket = sockjs('/api/data-info');
            stompClient.value = Stomp.over(socket);
            stompClient.value.debug = null;
            stompClient.value.connect({}, () => {
               console.log(`Connected to room: ${roomCode.value}`);

               stompClient.value.subscribe(`/room/${roomCode.value}`, (val: any) => {
                  const body: AfterMoveState = JSON.parse(val.body);

                  const i = Math.floor(body.i / GRID_SIZE.value);
                  const j = Math.floor(body.j / GRID_SIZE.value);
                  const ii = body.i % SUBGRID_SIZE.value;
                  const jj = body.j % SUBGRID_SIZE.value;

                  if (!game.value) return;
                  game.value.currentPlayerId = body.nextPlayerUUID;
                  game.value.finished = body.gameFinished;
                  game.value.winner = body.winner;
                  game.value.grid.subgrids[i][j].cells[ii][jj] = game.value.currentSymbol;
                  game.value.grid.subgrids[i][j].playable = (body.completedSubgridId !== game.value.grid.subgrids[i][j].uuid);
                  game.value.grid.subgrids[i][j].winner = setWinnerOnCompletedSubgrid(body.completedSubgridId);
                  game.value.currentSymbol = body.nextSymbol;
                  game.value.subgridToPlayId = body.subgridToPlayId;


                  if (game.value.finished) {
                     if (game.value.winner === null) {
                        router.push(`/room/${roomCode.value}/draw`);
                        return;
                     }

                     if (game.value?.gameType === GameType.PVP_LOCAL || game.value.winner?.uuid === sessionStorage.getItem('playerUUID')) {
                        fireConfetti();
                        setTimeout(() => {
                           router.push(`/room/${roomCode.value}/win`);
                        }, 3000);
                        return;
                     } else {
                        setTimeout(() => {
                           router.push(`/room/${roomCode.value}/lose`);
                        }, 3000);
                     }

                  }

                  switch (body.type) {
                     case "AFTER_MOVE":
                        const cell = document.querySelector(`#cell-${body.i}-${body.j}`);
                        if (!cell) return;

                        cell.setAttribute("disabled", "true");
                        cell.classList.add("cursor-not-allowed");
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

const handleMove = (opt: { i: number, j: number }) => {
   if (!game.value?.finished) {
      // Check if cell is disabled
      const cell = document.querySelector(`#cell-${opt.i}-${opt.j}`);
      if (!cell) return;
      if (cell.getAttribute("disabled")) return;

      switch (game.value?.gameType) {
         case GameType.PVP_LOCAL:
            stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({
               i: opt.i,
               j: opt.j,
               playerUUID: game.value.currentPlayerId
            }));
            break;
         case GameType.PVP_ONLINE:
            stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({
               i: opt.i,
               j: opt.j,
               playerUUID: sessionStorage.getItem('playerUUID')
            }));
            break;
         case GameType.PVE:
            stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({
               i: opt.i,
               j: opt.j,
               playerUUID: sessionStorage.getItem('playerUUID')
            }));

            break;
      }
   }
}

const getPlayerTurn = computed(() => {
   return (game.value?.currentPlayerId === game.value?.player1?.uuid
         ? game.value?.player1?.username
         : game.value?.player2?.username);
})
</script>

<template>
   <div>
      <h3 id="turn">Tour de {{ getPlayerTurn }} ({{ game?.currentSymbol }})</h3>
   </div>

   <div id="Players">
      <p>Player 1: {{ game?.player1?.username }}
      </p>
      <p>Player 2: {{ game?.player2?.username }}</p>
   </div>

   <div id="Jeu">
      <div class="grid">
         <div v-for="(sgI, i) in game?.grid.subgrids" class="row-span-1 col-span-3 grid grid-cols-3 grid-rows-1">
            <div
                  :id="sgJ.uuid"
                  v-for="(sgJ, j) in sgI"
                  class="subgrid m-2 rounded row-span-1 col-span-1 grid"
                  :class="{ 'outline': game?.subgridToPlayId === sgJ.uuid && sgJ.playable, 'outline-2': game?.subgridToPlayId === sgJ.uuid && sgJ.playable, 'completed': !sgJ.playable }"
            >
               <div class="subgrid-status">
                  <div v-if="sgJ.playable" class="text-2xl font-bold text-white"></div>
                  <div v-if="!sgJ.playable && sgJ.winner === CellStatus.X" class="text-2xl font-bold text-white">
                     &#10005;
                  </div>
                  <div v-if="!sgJ.playable && sgJ.winner === CellStatus.O" class="text-2xl font-bold text-white">O</div>
               </div>
               <div v-for="(cellI, ii) in sgJ.cells" class="row-span-1 col-span-3 grid grid-rows-1 grid-cols-3">
                  <div
                        :id="`cell-${ii+i*3}-${jj+j*3}`"
                        class="cell p-5 bg-cyan-700 hover:bg-cyan-400 cursor-pointer rounded m-1 relative"
                        v-for="(cellStatus, jj) in cellI"
                        @click="handleMove(computeCell(i, j, ii, jj))"
                  >
                <span v-if="cellStatus === CellStatus.EMPTY" class="absolute">
                  &nbsp;
                </span>
                     <span v-if="cellStatus === CellStatus.X" class="font-bold text-2xl">
                  &#10005;
                </span>
                     <span v-if="cellStatus === CellStatus.O" class="font-semibold text-2xl">
                  O
                </span>
                  </div>
               </div>
            </div>
         </div>
      </div>
      <div id="StatutJeu"></div>
   </div>
   <canvas ref="confetti"></canvas>
</template>

<style scoped lang="scss">
.cell > span {
   position: absolute;
   top: 50%;
   left: 50%;
   transform: translate(-50%, -50%);
}

#Jeu > .grid {
   display: grid;
   grid-template: repeat(3, 1fr) / repeat(3, 1fr);
}

.subgrid {
   user-select: none;
   position: relative;

   & > .subgrid-status {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;

      & > div {
         position: relative;
         width: 100%;
         height: 100%;
         display: grid;
         place-items: center;
         font-size: 8rem;
      }
   }

   &.completed {
      cursor: not-allowed;

      & > .subgrid-status > div {
         z-index: 9999;
      }

      & .cell {
         background: #213547;
         pointer-events: none;
      }
   }
}

canvas {
   position: fixed;
   top: 0;
   left: 0;
   width: 100vw;
   height: 100vh;
   user-select: none;
   pointer-events: none;
}
</style>
