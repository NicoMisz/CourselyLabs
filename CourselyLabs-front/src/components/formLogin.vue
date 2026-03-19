<template>
    <div class="formLogin" :class="{ shake: shaking }">

        <h1 class="h1Registrar">Iniciar sesión</h1>

        <q-banner v-if="errorMessage" class="bg-negative text-white q-mb-md rounded-borders">
          {{ errorMessage }}
        </q-banner>

        <q-form @submit="onSubmit" class="q-gutter-md">
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
              class="inputUsePassword"
            />

            <q-input
              type="password"
              v-model="password"
              label="Tu contraseña *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Introduce tu contraseña',
                val => val.length >= 6 || 'Mínimo 6 caracteres'
              ]"
              class="inputUsePassword"
            />
          </div>

            <div class="row">
                <q-btn type="submit" color="primary" :loading="loading">Iniciar sesión</q-btn>
                <span class="row items-center q-ml-md">
                    <p class="q-ma-sm">¿Aún no tienes cuenta?</p>
                    <q-btn label="Regístrate" color="primary" flat class="q-ml-sm" @click="goRegister"/>
                </span>
            </div>
        </q-form>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')
const shaking = ref(false)

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

/* function triggerShake() {
  shaking.value = true
  setTimeout(() => { shaking.value = false }, 500)
} */

const redirectTarget = computed(() => {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && redirect.length > 0 ? redirect : '/';
});

async function onSubmit() {
  errorMessage.value = '';
  loading.value = true;

  try {
    await authStore.login({ email: email.value, password: password.value });
    router.push(redirectTarget.value);
  } catch (err: unknown) {
    const status = (err as { response?: { status?: number } })?.response?.status;
    if (status === 401) {
      errorMessage.value = 'Email o contraseña incorrectos.';
    } else {
      errorMessage.value = 'Error de conexión. Inténtalo de nuevo.';
    }
  } finally {
    loading.value = false;
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<style scoped>

  .h1Registrar {
    text-align: center;
    margin-bottom: 2rem;
    font-size: 40px;
  }

  .formLogin{
    max-width: 75%;
    height: 92vh;
    margin: 0 auto;
  }

  .inputUsePassword{
    width: 48%;
  }

  @keyframes shake {
    0%, 100% { transform: translateX(0); }
    20%       { transform: translateX(-8px); }
    40%       { transform: translateX(8px); }
    60%       { transform: translateX(-6px); }
    80%       { transform: translateX(6px); }
  }

  .shake {
    animation: shake 0.5s ease;
  }

</style>
