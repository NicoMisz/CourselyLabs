<template>
    <q-layout
        view="hHr LpR lff"
    >
        <q-header elevated>
            <q-toolbar>
                <q-btn flat @click="drawerLeft = !drawerLeft" round dense icon="menu" />

                <q-toolbar-title> CourselyLab - Header</q-toolbar-title>

                <div class="q-pr-xl">Quasar v{{ $q.version }}</div>
                <q-btn flat round dense icon="account_circle" @click="$router.push('/login')"/>
            </q-toolbar>
        </q-header>

        <q-drawer v-model="drawerLeft" show-if-above bordered 
            :mini="miniState"
            @mouseenter="miniState = false"
            @mouseleave="miniState = true"
            :width="250"
            :mini-width="60"
            class="bg-white"
            overlay
            :breakpoint="1024"
        >

            <q-list :class="$q.dark.isActive ? 'text-white' : 'text-black'">
                <q-item-label header class="text-weight-bold"> Essential Links </q-item-label>

                <EssentialLink v-for="link in linksList" :key="link.title" v-bind="link" />
            </q-list>
        </q-drawer>

        <q-page-container class="container">
            <router-view />
        </q-page-container>

    </q-layout>

</template>

<script setup lang="ts">
    import { ref } from 'vue';
    import { useQuasar } from 'quasar';
    import EssentialLink from '../components/EssentialLinks.vue';

    interface Link {
        title: string;
        caption: string;
        icon: string;
        link: string;
    }

    const $q = useQuasar();
    const drawerLeft =  ref(false); 
    const miniState = ref<boolean>(true);
    
    
    const linksList = ref<Link[]>([
        {
            title: 'Docs',
            caption: 'quasar.dev',
            icon: 'school',
            link: 'https://quasar.dev',
        },
        {
            title: 'Inicio',
            caption: 'quasar.dev',
            icon: 'las la-home',
            link: '/#/home',
        },
        {
            title: 'Caixa',
            caption: 'quasar.dev',
            icon: 'las la-cash-register',
            link: '/#/carrito',
        },
        {
            title: 'Cursos',
            caption: 'quasar.dev',
            icon: 'las la-cash-register',
            link: 'cursos',
        },
        {
            title: 'Sortir',
            caption: 'quasar.dev',
            icon: 'las la-external-link-alt',
            link: 'https://www.google.com/',
        },
    ]);
</script>

<style scoped>

</style>