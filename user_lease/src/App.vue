<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useUserStore } from "@/store/modules/user";

onMounted(() => {
  useUserStore().validateToken();
});
</script>

<style></style>
