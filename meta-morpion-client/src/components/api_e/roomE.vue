<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import Game from "../../model/api_e/model.game.ts";

const router = useRouter();
const route = useRoute();
const roomCode = ref<string>("");
const game = ref<Game | null>(null);
const stompClient = ref<any>(null);

const GRID_SIZE = ref(3);
const SUBGRID_SIZE = ref(3);
</script>

<template>

</template>

<style scoped>

</style>