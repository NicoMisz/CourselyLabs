<template>
    <q-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)">
        <q-card class="q-pa-lg" style="min-width: 320px; max-width: 420px; width: 100%;">
        <q-card-section class="text-center">
            <div class="success-mark q-mx-auto q-mb-md">
            <q-icon name="check" size="42px" color="white" />
            </div>
            <div class="text-h6 q-mb-sm">Te has inscrito</div>
            <div class="text-body2 text-grey-7">
            Ya tienes acceso al curso y puedes continuar cuando quieras.
            </div>
        </q-card-section>

        <q-card-actions align="right">
            <q-btn flat color="grey-7" label="Seguir explorando" @click="emit('close')" />
            <q-btn unelevated color="primary" label="Ir al curso" @click="emit('goToCourse')" />
        </q-card-actions>
        </q-card>
    </q-dialog>
</template>

<script setup lang="ts">
defineProps<{ modelValue: boolean }>();

const emit = defineEmits<{
    'update:modelValue': [value: boolean];
    close: [];
    goToCourse: [];
}>();
</script>

<style scoped>
.success-mark {
    width: 72px;
    height: 72px;
    border-radius: 999px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #0f766e, #16a34a);
}
</style>