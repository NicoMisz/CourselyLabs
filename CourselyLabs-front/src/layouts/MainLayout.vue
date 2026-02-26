<template>
  <q-layout view="hHh Lpr fFf" class="bg-white">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          @click="toggleLeftDrawer"
          aria-label="Menu"
          icon="menu"
        />

                <q-toolbar-title> CourselyLabs - Header</q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      class="bg-grey-2"
      :mini="miniState"
      @mouseenter="miniState = false"
            @mouseleave="miniState = true"
            :width="250"
            :mini-width="60"
    >






    
      <q-list>
        <q-item-label header>Essential Links</q-item-label>
        <q-item clickable target="_blank" rel="noopener" href="https://quasar.dev">
          <q-item-section avatar>
            <q-icon name="school" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Docs</q-item-label>
            <q-item-label caption>https://quasar.dev</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable target="_blank" rel="noopener" href="https://github.quasar.dev">
          <q-item-section avatar>
            <q-icon name="code" />
          </q-item-section>
          <q-item-section>
            <q-item-label>GitHub</q-item-label>
            <q-item-label caption>github.com/quasarframework</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable target="_blank" rel="noopener" href="http://chat.quasar.dev">
          <q-item-section avatar>
            <q-icon name="chat" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Discord Chat Channel</q-item-label>
            <q-item-label caption>https://chat.quasar.dev</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable target="_blank" rel="noopener" href="https://forum.quasar.dev">
          <q-item-section avatar>
            <q-icon name="record_voice_over" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Forum</q-item-label>
            <q-item-label caption>https://forum.quasar.dev</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable target="_blank" rel="noopener" href="https://twitter.quasar.dev">
          <q-item-section avatar>
            <q-icon name="rss_feed" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Twitter</q-item-label>
            <q-item-label caption>@quasarframework</q-item-label>
          </q-item-section>
        </q-item>
        <q-item clickable target="_blank" rel="noopener" href="https://facebook.quasar.dev">
          <q-item-section avatar>
            <q-icon name="public" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Facebook</q-item-label>
            <q-item-label caption>@QuasarFramework</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
    <q-footer elevated>
      <q-toolbar>
        <q-toolbar-title>
          CourselyLabs - Todos los derechos reservados
        </q-toolbar-title>
      </q-toolbar>
    </q-footer>
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
    const leftDrawerOpen = ref(false)
    function toggleLeftDrawer () {
      leftDrawerOpen.value = !leftDrawerOpen.value
    }
    
</script>

<style scoped>

</style>