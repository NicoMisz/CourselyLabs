<template>
  <q-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    show-if-above
    bordered
    class="bg-grey-2"
    :mini="mini"
    @mouseenter="$emit('update:mini', false)"
    @mouseleave="$emit('update:mini', true)"
    :width="250"
    :mini-width="60"
  >
    <q-list>
      <q-item
        v-for="link in linksList"
        :key="link.title"
        clickable
        :to="link.link"
        active-class="text-primary bg-primary-light"
      >
        <q-item-section avatar>
          <q-icon :name="link.icon" />
          <q-tooltip v-if="mini" anchor="center right" self="center left" :offset="[10, 0]">
            {{ link.title }}
          </q-tooltip>
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ link.title }}</q-item-label>
          <q-item-label caption>{{ link.caption }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
  </q-drawer>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean
  mini: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
  'update:mini': [value: boolean]
}>()

const linksList = [
  {
    title: 'Inicio',
    caption: 'Pagina principal',
    icon: 'home',
    link: '/',
  },
  {
    title: 'Cursos',
    caption: 'Explorar cursos',
    icon: 'school',
    link: '/cursos',
  },
  {
    title: 'Caixa',
    caption: 'Carrito de compra',
    icon: 'shopping_cart',
    link: '/carrito',
  },
]
</script>