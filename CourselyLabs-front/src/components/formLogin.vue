<template>
    <div class="formLogin">

        <h1 class="h1Registrar"> Iniciar Sessión </h1>
        <q-form @submit="onSubmit" class="q-gutter-md">
          <div class="row justify-between">
            <q-input
              type="text"
              v-model="name"
              label="Your user name *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Please type your password',
                val => val.length >= 6 || 'Password must be at least 6 characters'
              ]"
              class="inputUsePassword"
            />

            <q-input
              type="password"
              v-model="password"
              label="Your password *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Please type your password',
                val => val.length >= 6 || 'Password must be at least 6 characters'
              ]"
              class="inputUsePassword"
            />
          </div>
          <q-toggle v-model="accept" label="I accept the license and terms" />

            <div class="row">
                <q-btn type="submit" color="primary">Iniciar sessión</q-btn>
                <span class="row items-center q-ml-md">
                    <p class="q-ma-sm">Aun no tienes cuenta?</p>
                    <q-btn label="Registrate" type="reset" color="primary" flat class="q-ml-sm" @click="register"/>
                </span>
            </div>
        </q-form>
    </div>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

    const $q = useQuasar()

    const name = ref(null)
    const password = ref(null)
    const accept = ref(false)

    function onSubmit () {
        if (accept.value !== true) {
          $q.notify({
            color: 'red-5',
            textColor: 'white',
            message: 'You must accept the license and terms to submit the form'
          })
          return
        }

        $q.notify({
          color: 'green-5',
          textColor: 'white',
          message: `Form submitted successfully! Name: ${name.value} Password: ${password.value}`
        })
    }

    const router = useRouter()

    function register() {
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

</style>