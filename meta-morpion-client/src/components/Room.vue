<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import sockjs from "sockjs-client/dist/sockjs"
import Stomp from "stompjs";
import {onMounted, onUnmounted, ref} from "vue";
import Game from "../model/game.model.ts";
import axios from "axios";

const router = useRouter();
const route = useRoute();
const roomCode = ref<string>("");
const game = ref<Game | null>(null);
const stompClient = ref<any>(null);

onMounted(() => {
  roomCode.value = route.params.roomCode as string;
  axios.post(`/api/game/join-room?roomCode=${roomCode.value}&playerUUID=${sessionStorage.getItem('playerUUID')}`)
      .then(({data}) => {
        game.value = data

        let socket = new SockJS('/api/data-info');
        stompClient.value = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.value.connect({}, (frame) => {
          console.log('Connected: ' + frame);

          stompClient.value.subscribe(`/room/${roomCode.value}`, (val) => {
            console.log(val);
            console.log(JSON.parse(val.body));
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

const handleMove = (i: number, j: number) => {
  stompClient.value.send(`/app/send-move/${roomCode.value}`, {}, JSON.stringify({i, j, playerUUID: sessionStorage.getItem('playerUUID')}));
}

const GRID_SIZE = ref(3);
const SUBGRID_SIZE = ref(3);
</script>

<template>
  <button v-on:click="handleMove(1, 1)">
    Click me
  </button>
  <h1>Morpi</h1>

  <div><h3>Tour de </h3>
  </div>

  <div id="Jeu">
    <div class="grid grid-rows-1 grid-cols-3">
      <div v-for="j in GRID_SIZE">
        <div v-for="i in GRID_SIZE">
          <div class="grid grid-rows-1 grid-cols-3 m-2">
            <div v-for="jj in SUBGRID_SIZE">
              <div class="p-5 bg-cyan-700 hover:bg-cyan-400 cursor-pointer rounded m-1" v-for="ii in SUBGRID_SIZE" @click="handleMove(((ii-1)+((i-1)*3)), ((jj-1)+((j-1)*3)))">

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

</style>