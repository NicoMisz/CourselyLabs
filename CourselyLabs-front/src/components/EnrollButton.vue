<template>
    <div class="column q-gutter-sm">
        <q-btn
        :label="buttonLabel"
        :color="buttonColor"
        :outline="isGuest"
        :loading="loading"
        :icon="buttonIcon"
        unelevated
        no-caps
        class="full-width"
        @click="handleClick"
        />

        <q-chip
        v-if="enrolled"
        color="positive"
        text-color="white"
        icon="check_circle"
        dense
        >
        Inscrito
        </q-chip>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useQuasar } from 'quasar';
import { useAuthStore } from '../stores/auth';

const props = defineProps<{
    courseId: string;
    courseSlug: string;
    free?: boolean;
    price?: number;
    enrolled: boolean;
    loading?: boolean;
}>();

const emit = defineEmits<{
    enroll: [];
    checkout: [];
    continue: [];
}>();

const router = useRouter();
const route = useRoute();
const $q = useQuasar();
const authStore = useAuthStore();

const isGuest = computed(() => !authStore.isLoggedIn);

const buttonLabel = computed(() => {
    if (isGuest.value) return 'Inicia sesion para inscribirte';
    if (props.enrolled) return 'Continuar curso';
    if (props.free) return 'Inscribirme gratis';
    return `Comprar curso${props.price ? ` - ${props.price} EUR` : ''}`;
});

const buttonColor = computed(() => {
    if (isGuest.value) return 'primary';
    if (props.enrolled) return 'primary';
    return props.free ? 'positive' : 'accent';
});

const buttonIcon = computed(() => {
    if (isGuest.value) return 'login';
    if (props.enrolled) return 'play_circle';
    return props.free ? 'check_circle' : 'shopping_cart';
});

function handleClick() {
    if (isGuest.value) {
        router.push({ path: '/login', query: { redirect: route.fullPath } });
        return;
    }

    if (props.enrolled) {
        emit('continue');
        return;
    }

    if (props.free) {
        emit('enroll');
        return;
    }

    $q.notify({
        type: 'info',
        message: 'El checkout se implementara en una rama posterior.',
        position: 'bottom-right',
    });
    emit('checkout');
}
</script>