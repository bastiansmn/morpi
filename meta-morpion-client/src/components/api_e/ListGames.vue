<script setup lang="ts">
import {onMounted, ref} from "vue";
import Game from "../../model/api_e/model.game.ts";
import Player, {GameValue} from "../../model/api_e/model.player.ts";
import axios from "axios";
import router from "../../router.ts";

const game = ref<Game | null>(null)
const player = ref<Player | null>(null);

onMounted(() => {

})

function initGame($event: any) {
  $event.preventDefault();

  let pseudoInput = document.getElementById("pseudo") as HTMLInputElement | null;

  let isFirstValue = (document.querySelector("input[type='radio'][name=player]:checked") as HTMLInputElement)?.value;
  let isFirst = (isFirstValue === "x");

   player.value = {
      playerName: pseudoInput?.value.length === 0 ? "guest" : pseudoInput?.value,
      gameValue: isFirst ? GameValue.x_value : GameValue.o_value
   }

  return axios.post(`/morpion/init?starter=${isFirst}`, null, {
    params: {
      body: player.value
    }
  }).then((({data}) => {
     game.value = data
     let path: string = `/roomE`;
     router.push(path);
  }))
}

function changeAPI() {
  let api = (document.querySelector("input[type='radio'][name=api]:checked") as HTMLInputElement)?.value;
  switch(api) {
    case "A":
      let pathA: string = `/groupA}`;
      router.push(pathA)
      break;
    case "C":
      let pathC: string = `/`;
      router.push(pathC)
      break;
    case "E":
      let pathE: string = `/groupE`;
      router.push(pathE)
      break;
  }
}

</script>

<template>
  Groupe E


  <form id="apiGroup">
    <ul>
      <li>
        <label for="API">APIs&nbsp;:&nbsp;</label>
      </li>
      <li>
        <input type="radio" name="api" value="A" id="A" @click="changeAPI"> <label for="A">A</label> &nbsp;
        <input type="radio" name="api" value="C" id="C" @click="changeAPI" > <label for="C">C</label> &nbsp;
        <input type="radio" name="api" value="E" id="E" @click="changeAPI" checked> <label for="E">E</label><br>
      </li>
    </ul>
  </form>

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
        <input type="radio" name="start" value="First" id="First" checked> <label for="First">First</label> &nbsp;
        <input type="radio" name="start" value="Second" id="Second"> <label for="Second">Second</label> &nbsp;

      </li>
    </ul>
    <button type="submit" @click="initGame($event)">Créer une partie</button>
  </form>

</template>