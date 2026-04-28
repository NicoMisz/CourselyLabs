<template>
  <section class="hero q-pa-md rounded-borders">
    <div class="row q-col-gutter-lg items-start">
      <div class="col-12 col-md-5">
        <img v-if="thumbnailUrl" :src="thumbnailUrl" :alt="title" class="hero-thumb" />
        <div v-else class="hero-fallback">
          <q-icon name="sym_o_school" size="56px" color="white" />
        </div>
      </div>

      <div class="col-12 col-md-7">
        <h1 class="text-h4 q-my-none">{{ title }}</h1>
        <p class="text-body1 q-mt-sm q-mb-md">{{ shortDescription || 'Sin descripción corta.' }}</p>

        <div class="row q-gutter-sm items-center q-mb-md">
          <LevelBadge :level="level" />
          <PriceBadge :is-free="isFree" :price="price" />
          <q-chip dense icon="groups" color="grey-2" text-color="dark">
            {{ studentsLabel }}
          </q-chip>
        </div>

        <div class="text-caption text-grey-7 row items-center q-gutter-xs">
          <q-icon name="schedule" size="16px" />
          <span>{{ updatedLabel }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import LevelBadge from './LevelBadge.vue';
import PriceBadge from './PriceBadge.vue';

const props = defineProps<{
  title: string;
  shortDescription?: string;
  level?: string;
  isFree?: boolean;
  price?: number;
  thumbnailUrl?: string;
  studentsCount?: number;
  updatedAt?: string;
}>();

const studentsLabel = computed(() => {
  const n = props.studentsCount || 0;
  if (n >= 1000) return `${(n / 1000).toFixed(1)}K estudiantes`;
  return `${n} estudiantes`;
});

const updatedLabel = computed(() => {
  if (!props.updatedAt) return 'Actualización no disponible';
  const now = Date.now();
  const then = new Date(props.updatedAt).getTime();
  const diffMs = now - then;
  const diffMin = Math.floor(diffMs / 60000);
  const diffH = Math.floor(diffMin / 60);
  const diffD = Math.floor(diffH / 24);

  if (diffMin < 1) return 'Actualizado hace un momento';
  if (diffMin < 60) return `Actualizado hace ${diffMin} min`;
  if (diffH < 24) return `Actualizado hace ${diffH}h`;
  if (diffD < 30) return `Actualizado hace ${diffD} ${diffD === 1 ? 'dia' : 'dias'}`;
  return `Actualizado el ${new Date(props.updatedAt).toLocaleDateString('es-ES')}`;
});
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(234, 88, 12, 0.08));
}
.hero-thumb {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: 12px;
}
.hero-fallback {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f766e, #ea580c);
}
</style>