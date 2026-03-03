<template>
    <div class="formRegister">

        <h1 class="h1Registrar">Crear tu cuenta</h1>

        <q-banner v-if="errorMessage" class="bg-negative text-white q-mb-md rounded-borders">
          {{ errorMessage }}
        </q-banner>

        <q-form @submit="onSubmit" class="q-gutter-md">

          <div class="row justify-between">
            <q-input
              v-model="firstName"
              label="Nombre *"
              lazy-rules
              :rules="[val => val !== null && val !== '' || 'Introduce tu nombre']"
              class="inputNameSurname"
            />

            <q-input
              v-model="lastName"
              label="Apellidos *"
              lazy-rules
              :rules="[val => val !== null && val !== '' || 'Introduce tus apellidos']"
              class="inputNameSurname"
            />
          </div>

          <div class="row justify-between">
            <q-input
              type="email"
              v-model="email"
              label="Tu email *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Introduce tu email',
                val => /.+@.+\..+/.test(val) || 'Introduce un email válido'
              ]"
              class="inputNameSurname"
            />

            <q-input
              type="password"
              v-model="password"
              label="Tu contraseña *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Introduce tu contraseña',
                val => val.length >= 8 || 'Mínimo 8 caracteres'
              ]"
              class="inputNameSurname"
            />
          </div>

          <q-input
            v-model="bio"
            label="Sobre ti (opcional)"
            type="textarea"
            autogrow
            :rules="[val => !val || val.length <= 500 || 'Máximo 500 caracteres']"
          />

          <q-toggle v-model="accept" label="Acepto los términos y condiciones" />

            <div class="row">
                <q-btn type="submit" color="primary" :loading="loading">Registrarme</q-btn>
                <span class="row items-center q-ml-md">
                    <p class="q-ma-sm">¿Ya tienes cuenta?</p>
                    <q-btn label="Iniciar sesión" color="primary" flat class="q-ml-sm" @click="goLogin"/>
                </span>
            </div>
        </q-form>
    </div>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const $q = useQuasar()

const firstName = ref('')
const lastName = ref('')
const email = ref('')
const password = ref('')
const bio = ref('')
const accept = ref(false)
const loading = ref(false)
const errorMessage = ref('')

const router = useRouter()
const authStore = useAuthStore()

async function onSubmit() {
  if (!accept.value) {
    $q.notify({ color: 'red-5', textColor: 'white', message: 'Debes aceptar los términos y condiciones.' })
    return
  }

  errorMessage.value = ''
  loading.value = true
  try {
    await authStore.register({
      email: email.value,
      password: password.value,
      firstName: firstName.value,
      lastName: lastName.value,
      bio: bio.value || undefined,
    })
    router.push('/')
  } catch (err: unknown) {
    const response = (err as { response?: { data?: { message?: string }; status?: number } })?.response
    if (response?.status === 400 && response?.data?.message) {
      errorMessage.value = response.data.message
    } else if (response?.status === 409) {
      errorMessage.value = 'Ya existe una cuenta con ese email.'
    } else {
      errorMessage.value = 'Error al crear la cuenta. Inténtalo de nuevo.'
    }
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>

  .h1Registrar {
    text-align: center;
    margin-bottom: 2rem;
    font-size: 40px;
  }

  .formRegister{
    max-width: 75%;
    height: 92vh;
    margin: 0 auto;
  }

  .inputNameSurname {
    width: 48%;
  }

</style>
