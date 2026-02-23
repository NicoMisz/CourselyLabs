<template>
    <div>

        <h1> Inici de sessió </h1>
        <q-form
          @submit="onSubmit"

          class="q-gutter-md"
        >
          <q-input
            v-model="name"
            label="Your name *"
            lazy-rules
            :rules="[
              val => val !== null && val !== '' || 'Please type your name'
            ]"
          />

          <q-input
            v-model="surnames"
            label="Your surname *"
            lazy-rules
            :rules="[
              val => val !== null && val !== '' || 'Please type your surnames'
            ]"
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
            />
        
            <q-input
               v-model="city"
                label="Your city"
                lazy-rules
                :rules="[
                  val => val === null || val === '' || val.length > 0 || 'Please type something'
                ]"
            />

            <q-input
            type="tel"
            v-model="phone"
            label="Your phone number"
            lazy-rules
            :rules="[
              val => val === null || val === '' || /^\d{9}$/.test(val) || 'Please type a valid phone number'
            ]"
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
          />

          <q-toggle v-model="accept" label="I accept the license and terms" />

            <div>
                <q-btn label="Submit" type="submit" color="primary"/>
                <q-btn label="Reset" type="reset" color="primary" flat class="q-ml-sm" />
            </div>
        </q-form>
    </div>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar'
import { ref } from 'vue'

    const $q = useQuasar()

    const name = ref(null)
    const surnames = ref(null)
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

</script>

<style scoped>

</style>