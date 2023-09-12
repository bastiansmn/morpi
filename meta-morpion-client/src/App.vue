<script setup lang="ts">
import axios from "axios";

const knownUUID = sessionStorage.getItem('playerUUID');

axios.post(`/api/game/register?username=guest&playerUUID=${knownUUID}`)
    .then(({ data }) => {
      sessionStorage.setItem('playerUUID', data.uuid);
      sessionStorage.setItem('playerUsername', data.username);
    })
    .catch(err => console.warn(err));

</script>

<template>
   <router-view></router-view>
</template>
