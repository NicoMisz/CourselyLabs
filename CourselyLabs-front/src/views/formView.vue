<template>
    <div class="q-pa-md form-view-wrap">

        <Form />
    
  </div>
</template>

<script setup lang="ts">
import Form from '@/components/formLogin.vue';

</script>

<style scoped>
.form-view-wrap {
  background: var(--app-bg);
  color: var(--app-text);
  min-height: calc(100vh - 64px);
}
</style>