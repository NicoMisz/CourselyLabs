<template>
  <q-page class="flex justify-center items-start q-pt-xl">
    <q-card class="profile-card q-pa-lg">
      <q-card-section class="text-center">
        <q-avatar size="80px" color="primary" text-color="white" class="q-mb-md">
          <q-icon name="account_circle" size="60px" />
        </q-avatar>

        <template v-if="!editing">
          <div class="text-h5">{{ user?.firstName }} {{ user?.lastName }}</div>
          <div class="text-subtitle2 text-grey q-mt-xs">{{ user?.email }}</div>
          <q-badge :color="roleColor" class="q-mt-sm">{{ roleLabel }}</q-badge>
        </template>
      </q-card-section>

      <!-- Modo lectura -->
      <template v-if="!editing">
        <q-separator />

        <q-card-section v-if="user?.bio">
          <div class="text-caption text-grey q-mb-xs">Sobre mi</div>
          <p class="q-mb-none">{{ user.bio }}</p>
        </q-card-section>

        <q-card-section>
          <div class="text-caption text-grey">
            Cuenta verificada: {{ user?.isVerified ? 'Si' : 'No' }}
          </div>
        </q-card-section>

        <q-card-actions align="center">
          <q-btn flat color="primary" label="Editar perfil" icon="edit" @click="startEditing" />
        </q-card-actions>
      </template>

      <!-- Modo edicion -->
      <template v-else>
        <q-separator />

        <q-card-section>
          <q-form @submit.prevent="saveProfile" class="q-gutter-sm">
            <q-input
              v-model="form.firstName"
              label="Nombre"
              outlined
              dense
              :rules="[v => v.length >= 2 || 'Minimo 2 caracteres']"
            />
            <q-input
              v-model="form.lastName"
              label="Apellido"
              outlined
              dense
              :rules="[v => v.length >= 2 || 'Minimo 2 caracteres']"
            />
            <q-input
              v-model="form.bio"
              label="Sobre mi"
              outlined
              dense
              type="textarea"
              autogrow
            />
          </q-form>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" @click="editing = false" />
          <q-btn
            unelevated
            color="primary"
            label="Guardar"
            :loading="saving"
            @click="saveProfile"
          />
        </q-card-actions>
      </template>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useQuasar } from 'quasar'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'

const $q = useQuasar()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const editing = ref(false)
const saving = ref(false)
const form = reactive({
  firstName: '',
  lastName: '',
  bio: '',
})

const roleLabel = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'Administrador'
    case 'premium': return 'Premium'
    default: return 'Usuario'
  }
})

const roleColor = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'negative'
    case 'premium': return 'warning'
    default: return 'primary'
  }
})

function startEditing() {
  form.firstName = user.value?.firstName || ''
  form.lastName = user.value?.lastName || ''
  form.bio = user.value?.bio || ''
  editing.value = true
}

async function saveProfile() {
  if (form.firstName.length < 2 || form.lastName.length < 2) return

  saving.value = true
  try {
    const { data } = await api.put(`/api/users/${user.value?.id}`, {
      email: user.value?.email,
      firstName: form.firstName,
      lastName: form.lastName,
      bio: form.bio,
    })
    authStore.user = data
    editing.value = false
    $q.notify({ type: 'positive', message: 'Perfil actualizado' })
  } catch (err: unknown) {
    const resp = (err as { response?: { status?: number; data?: unknown } })?.response
    console.error('Profile update error:', resp?.status, resp?.data)
    $q.notify({ type: 'negative', message: 'Error al actualizar el perfil' })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-card {
  width: 100%;
  max-width: 500px;
}
</style>
