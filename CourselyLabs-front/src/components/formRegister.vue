<template>
    <div class="formRegister">

        <h1 class="h1Registrar"> Crear tu cuenta </h1>
        <q-form @submit="onSubmit" class="q-gutter-md">
          
          <div class="row justify-between">
              <q-input
              v-model="name"
              label="Your name *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Please type your name'
              ]"
              class="inputNameSurname"
            />

            <q-input
              v-model="surnames"
              label="Your surnames *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Please type your surnames'
              ]"
              class="inputNameSurname"
            />
          </div>
          
          <div class="row justify-between">
            <q-input
              type="date"
              v-model="Birthday"
              label="Your date birth *"
              lazy-rules
              :rules="[   
                val => val !== null && val !== '' || 'Please type your age',
                val => val > 0 && val < 100 || 'Please type a real age'
              ]"
              class="inputBirthAge"
            />

            <q-input
              type="number"
              v-model="age"
              label="Your age *"
              lazy-rules
              :rules="[   
                val => val !== null && val !== '' || 'Please type your age',
                val => val > 0 && val < 100 || 'Please type a real age'
              ]"
              class="inputBirthAge"
            />
          </div>
            
            <q-input
                v-model="city"
                label="Your city"
                lazy-rules
                :rules="[
                  val => val === null || val === '' || val.length > 0 || 'Please type something'
                ]"
            />
          <div class="row justify-between">
            <q-input
              type="tel"
              v-model="phone"
              label="Your phone number"
              lazy-rules
              :rules="[
                val => val === null || val === '' || /^\d{9}$/.test(val) || 'Please type a valid phone number'
              ]"
              class="inputPhoneMail"
            />
          

            <q-input
              type="email"
              v-model="email"
              label="Your email *"
              lazy-rules
              :rules="[
                val => val !== null && val !== '' || 'Please type your email',
                val => /.+@.+\..+/.test(val) || 'Please type a valid email'
              ]"
              class="inputPhoneMail"
            />
          </div>
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
                <q-btn type="submit" color="primary">Registrate</q-btn>
                <span class="row items-center q-ml-md">
                    <p class="q-ma-sm">Ya tienes cuenta?</p>
                    <q-btn label="Iniciar sessión" type="reset" color="primary" flat class="q-ml-sm " @click="inicioSession"/>
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
    const surnames = ref(null)
    const Birthday = ref(null)
    const age = ref(null)
    const city = ref(null)
    const phone = ref(null)
    const email = ref(null)
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
          message: `Form submitted successfully! Name: ${name.value} ${surnames.value}, Age: ${age.value}, City: ${city.value}, Phone: ${phone.value}, Email: ${email.value}`
        })
    }

    const router = useRouter()
    function inicioSession() {
        router.push('/iniSession')
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

  .inputNameSurname, .inputPhoneMail, .inputUsePassword, .inputBirthAge {
    width: 48%;
  }

</style>