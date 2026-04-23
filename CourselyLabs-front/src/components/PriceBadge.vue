<template>
  <div class="price-wrap">
    <q-chip v-if="isFree" dense color="positive" text-color="white">Gratis</q-chip>
    <q-chip v-else dense color="amber-8" text-color="white" icon="workspace_premium">
      Premium
    </q-chip>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isFree: boolean
  price?: number | null
}>()
</script>

<style scoped>
.price-wrap {
  display: flex;
  align-items: center;
}
</style>
