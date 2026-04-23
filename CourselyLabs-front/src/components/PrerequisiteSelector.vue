<template>
    <div>
        <!-- Selector para añadir -->
        <q-select
            :model-value="modelValue"
            :options="options"
            option-label="title"
            option-value="id"
            use-input
            clearable
            label="Añadir prerequisito"
            :loading="loading"
            @filter="onFilter"
            @update:model-value="(val) => { if (val) add(val.id) }"
        />

        <!-- Lista de prerequisitos añadidos -->
        <q-list bordered separator class="q-mt-md" v-if="prerequisites.length">
        <q-item v-for="p in prerequisites" :key="p.id">
            <q-item-section>{{ p.prerequisiteCourse.title }}</q-item-section>
            <q-item-section side>
            <q-btn
                flat
                round
                icon="delete"
                color="negative"
                @click="remove(p.id)"
            />
            </q-item-section>
        </q-item>
        </q-list>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { usePrerequisites } from '../composables/usePrerequisites';

const props = defineProps<{
    courseId: number;
    modelValue: number | null; // ← prop que faltaba
}>();

const onFilter = (val: string, update: (fn: () => void) => void) => {
    search(val).then(() => update(() => {}));
};

defineEmits<{
    (e: 'update:modelValue', value: number | null): void;
}>();

const { prerequisites, options, loading, load, search, add, remove } =
    usePrerequisites(props.courseId);

onMounted(load);
</script>