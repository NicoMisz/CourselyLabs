<template>
    <q-item clickable tag="a" :to="isInternalLink ? props.link : undefined"
    :href="isExternalLink ? props.link : undefined"
    :target="isExternalLink ? '_blank' : undefined"
    v-ripple>

    <q-item-section v-if="props.icon" avatar>
        <q-icon :name="props.icon" />
    </q-item-section>

    <q-item-section>
        <q-item-label>{{ props.title }}</q-item-label>
        <q-item-label caption>{{ props.caption }}</q-item-label>
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { computed } from 'vue';
    
    interface Props {
        title: string;
        caption: string;
        link: string;
        icon: string;
    }

    const props = withDefaults(defineProps<Props>(), {
        caption: '',
        link: '#',
        icon: '',
    });

    const isExternalLink = computed(() => {
        return props.link.startsWith('http://') || props.link.startsWith('https://');
    });

    const isInternalLink = computed(() => {
        return !isExternalLink.value && props.link !== '#';
    });
    
    /* const props = defineProps({
        title: {
            type: String,
            required: true,
        },

        caption: {
            type: String,
            default: '',
        },

        link: {
            type: String,
            default: '#',
        },

        icon: {
            type: String,
            default: '',
        },
    }) */
</script>

<style scoped></style>