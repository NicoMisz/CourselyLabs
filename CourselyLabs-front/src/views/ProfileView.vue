<template>
  <q-page class="q-pa-md q-pa-lg-lg">
    <div class="profile-container q-mx-auto">
      <!-- Email no verificado -->
      <q-banner v-if="user && !user.isVerified" class="bg-warning-1 q-mb-md" rounded inline-actions>
        <template #avatar>
          <q-icon name="warning" color="warning" />
        </template>
        Tu email no esta verificado. Revisa tu bandeja de entrada o spam.
        <template #action>
          <q-btn
            flat
            color="warning"
            label="Reenviar email"
            no-caps
            :loading="resendingFromProfile"
            @click="resendFromProfile"
          />
        </template>
      </q-banner>

      <!-- Header -->
      <div class="profile-header q-pa-lg q-mb-md rounded-borders">
        <div class="row items-center q-gutter-md">
          <q-avatar size="96px" color="primary" text-color="white" class="profile-avatar">
            <img v-if="user?.profilePictureUrl" :src="user.profilePictureUrl" />
            <span v-else class="text-h4">{{ initials }}</span>
          </q-avatar>
          <div class="col">
            <div class="text-h5 text-weight-bold">{{ user?.firstName }} {{ user?.lastName }}</div>
            <div class="text-body2 text-grey-6">{{ user?.email }}</div>
            <div class="q-mt-xs q-gutter-x-sm">
              <q-badge :color="roleColor" :label="roleLabel" />
              <q-badge v-if="user?.isVerified" color="positive" icon="verified" label="Verificado" />
            </div>
          </div>
          <q-btn
            v-if="!editingProfile"
            flat
            color="primary"
            icon="edit"
            label="Editar"
            no-caps
            @click="startEditingProfile"
          />
        </div>
      </div>

      <div class="row q-col-gutter-md">
        <!-- Columna izquierda -->
        <div class="col-12 col-md-8">
          <!-- Info personal -->
          <q-card flat bordered class="q-mb-md">
            <q-card-section>
              <div class="text-subtitle1 text-weight-medium q-mb-md">Informacion personal</div>

              <template v-if="!editingProfile">
                <div class="row q-col-gutter-md">
                  <div class="col-6">
                    <div class="text-caption text-grey-6">Nombre</div>
                    <div class="text-body1">{{ user?.firstName }}</div>
                  </div>
                  <div class="col-6">
                    <div class="text-caption text-grey-6">Apellido</div>
                    <div class="text-body1">{{ user?.lastName }}</div>
                  </div>
                  <div class="col-12">
                    <div class="text-caption text-grey-6">Email</div>
                    <div class="text-body1">{{ user?.email }}</div>
                  </div>
                  <div class="col-12" v-if="user?.bio">
                    <div class="text-caption text-grey-6">Sobre mi</div>
                    <div class="text-body2" style="white-space: pre-line">{{ user.bio }}</div>
                  </div>
                </div>
              </template>

              <template v-else>
                <q-form @submit.prevent="saveProfile" class="q-gutter-sm">
                  <div class="row q-col-gutter-sm">
                    <div class="col-6">
                      <q-input
                        v-model="profileForm.firstName"
                        label="Nombre"
                        outlined
                        dense
                        :rules="[v => v.length >= 2 || 'Minimo 2 caracteres']"
                      />
                    </div>
                    <div class="col-6">
                      <q-input
                        v-model="profileForm.lastName"
                        label="Apellido"
                        outlined
                        dense
                        :rules="[v => v.length >= 2 || 'Minimo 2 caracteres']"
                      />
                    </div>
                  </div>
                  <q-input
                    v-model="profileForm.bio"
                    label="Sobre mi"
                    outlined
                    dense
                    type="textarea"
                    autogrow
                    counter
                    maxlength="500"
                  />
                  <div class="row justify-end q-gutter-sm q-mt-sm">
                    <q-btn flat label="Cancelar" no-caps @click="editingProfile = false" />
                    <q-btn
                      unelevated
                      color="primary"
                      label="Guardar cambios"
                      no-caps
                      :loading="savingProfile"
                      @click="saveProfile"
                    />
                  </div>
                </q-form>
              </template>
            </q-card-section>
          </q-card>

          <!-- Seguridad -->
          <q-card flat bordered>
            <q-card-section>
              <div class="text-subtitle1 text-weight-medium q-mb-md">Seguridad</div>

              <template v-if="!editingPassword">
                <div class="row items-center justify-between">
                  <div>
                    <div class="text-body2">Contraseña</div>
                    <div class="text-caption text-grey-6">Ultima actualizacion: {{ formattedDate(user?.updatedAt) }}</div>
                  </div>
                  <q-btn flat color="primary" label="Cambiar contraseña" no-caps icon="lock" @click="editingPassword = true" />
                </div>
              </template>

              <template v-else>
                <q-form @submit.prevent="changePassword" class="q-gutter-sm">
                  <q-input
                    v-model="passwordForm.currentPassword"
                    label="Contraseña actual"
                    outlined
                    dense
                    :type="showCurrentPw ? 'text' : 'password'"
                    :rules="[v => !!v || 'Obligatorio']"
                  >
                    <template #append>
                      <q-icon
                        :name="showCurrentPw ? 'visibility_off' : 'visibility'"
                        class="cursor-pointer"
                        @click="showCurrentPw = !showCurrentPw"
                      />
                    </template>
                  </q-input>
                  <q-input
                    v-model="passwordForm.newPassword"
                    label="Nueva contraseña"
                    outlined
                    dense
                    :type="showNewPw ? 'text' : 'password'"
                    :rules="[v => v.length >= 8 || 'Minimo 8 caracteres']"
                  >
                    <template #append>
                      <q-icon
                        :name="showNewPw ? 'visibility_off' : 'visibility'"
                        class="cursor-pointer"
                        @click="showNewPw = !showNewPw"
                      />
                    </template>
                  </q-input>
                  <q-input
                    v-model="passwordForm.confirmPassword"
                    label="Confirmar nueva contraseña"
                    outlined
                    dense
                    :type="showConfirmPw ? 'text' : 'password'"
                    :rules="[v => v === passwordForm.newPassword || 'Las contraseñas no coinciden']"
                  >
                    <template #append>
                      <q-icon
                        :name="showConfirmPw ? 'visibility_off' : 'visibility'"
                        class="cursor-pointer"
                        @click="showConfirmPw = !showConfirmPw"
                      />
                    </template>
                  </q-input>
                  <div class="row justify-end q-gutter-sm q-mt-sm">
                    <q-btn flat label="Cancelar" no-caps @click="resetPasswordForm" />
                    <q-btn
                      unelevated
                      color="primary"
                      label="Actualizar contraseña"
                      no-caps
                      :loading="savingPassword"
                      @click="changePassword"
                    />
                  </div>
                </q-form>
              </template>
            </q-card-section>
          </q-card>
        </div>

        <!-- Columna derecha — stats -->
        <div class="col-12 col-md-4">
          <q-card flat bordered class="q-mb-md">
            <q-card-section>
              <div class="text-subtitle1 text-weight-medium q-mb-md">Actividad</div>
              <div class="q-gutter-md">
                <div class="row items-center q-gutter-sm">
                  <q-icon name="school" size="24px" color="primary" />
                  <div>
                    <div class="text-body1 text-weight-medium">{{ stats.enrolled }}</div>
                    <div class="text-caption text-grey-6">Cursos inscritos</div>
                  </div>
                </div>
                <div class="row items-center q-gutter-sm">
                  <q-icon name="check_circle" size="24px" color="positive" />
                  <div>
                    <div class="text-body1 text-weight-medium">{{ stats.completed }}</div>
                    <div class="text-caption text-grey-6">Cursos completados</div>
                  </div>
                </div>
                <div class="row items-center q-gutter-sm">
                  <q-icon name="trending_up" size="24px" color="accent" />
                  <div>
                    <div class="text-body1 text-weight-medium">{{ stats.inProgress }}</div>
                    <div class="text-caption text-grey-6">En progreso</div>
                  </div>
                </div>
              </div>
            </q-card-section>
          </q-card>

          <q-card flat bordered>
            <q-card-section>
              <div class="text-subtitle1 text-weight-medium q-mb-sm">Cuenta</div>
              <div class="text-caption text-grey-6">
                Miembro desde {{ formattedDate(user?.createdAt) }}
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'
import { getMyCourses } from '@/api/enrollment'
import type { EnrolledCourse } from '@/types/enrollment'

const $q = useQuasar()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const initials = computed(() => {
  const f = user.value?.firstName?.[0] || ''
  const l = user.value?.lastName?.[0] || ''
  return (f + l).toUpperCase()
})

const roleLabel = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'Administrador'
    case 'premium': return 'Premium'
    case 'instructor': return 'Instructor'
    default: return 'Estudiante'
  }
})

const roleColor = computed(() => {
  switch (user.value?.role) {
    case 'admin': return 'negative'
    case 'premium': return 'warning'
    case 'instructor': return 'info'
    default: return 'primary'
  }
})

function formattedDate(iso?: string): string {
  if (!iso) return '-'
  return new Date(iso).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' })
}

// --- Profile editing ---
const editingProfile = ref(false)
const savingProfile = ref(false)
const profileForm = reactive({ firstName: '', lastName: '', bio: '' })

function startEditingProfile() {
  profileForm.firstName = user.value?.firstName || ''
  profileForm.lastName = user.value?.lastName || ''
  profileForm.bio = user.value?.bio || ''
  editingProfile.value = true
}

async function saveProfile() {
  if (profileForm.firstName.length < 2 || profileForm.lastName.length < 2) return
  savingProfile.value = true
  try {
    const { data } = await api.put(`/api/users/${user.value?.id}`, {
      email: user.value?.email,
      firstName: profileForm.firstName,
      lastName: profileForm.lastName,
      bio: profileForm.bio,
    })
    authStore.updateUser(data)
    editingProfile.value = false
    $q.notify({ type: 'positive', message: 'Perfil actualizado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al actualizar el perfil', position: 'bottom-right' })
  } finally {
    savingProfile.value = false
  }
}

// --- Password change ---
const editingPassword = ref(false)
const savingPassword = ref(false)
const showCurrentPw = ref(false)
const showNewPw = ref(false)
const showConfirmPw = ref(false)
const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

function resetPasswordForm() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  showCurrentPw.value = false
  showNewPw.value = false
  showConfirmPw.value = false
  editingPassword.value = false
}

async function changePassword() {
  if (!passwordForm.currentPassword || passwordForm.newPassword.length < 8) return
  if (passwordForm.newPassword !== passwordForm.confirmPassword) return

  savingPassword.value = true
  try {
    await api.patch('/api/auth/change-password', {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    resetPasswordForm()
    $q.notify({ type: 'positive', message: 'Contraseña actualizada', position: 'bottom-right' })
  } catch (err: unknown) {
    const status = (err as { response?: { status?: number } })?.response?.status
    const msg = status === 400
      ? 'La contraseña actual es incorrecta'
      : 'Error al cambiar la contraseña'
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  } finally {
    savingPassword.value = false
  }
}

// --- Stats ---
// --- Resend verification from profile banner ---
const resendingFromProfile = ref(false)

async function resendFromProfile() {
  if (!user.value?.email) return
  resendingFromProfile.value = true
  try {
    await api.post(`/api/auth/resend-verification?email=${encodeURIComponent(user.value.email)}`)
    $q.notify({ type: 'positive', message: 'Email de verificacion reenviado', position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'No se pudo reenviar el email', position: 'bottom-right' })
  } finally {
    resendingFromProfile.value = false
  }
}

// --- Stats ---
const stats = reactive({ enrolled: 0, completed: 0, inProgress: 0 })

onMounted(async () => {
  try {
    const courses: EnrolledCourse[] = await getMyCourses()
    stats.enrolled = courses.length
    stats.completed = courses.filter(c => c.progressStatus === 'completado').length
    stats.inProgress = courses.filter(c => c.progressStatus === 'en-curso').length
  } catch {
    // stats stay at 0
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 900px;
}
.profile-header {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: white;
}
.profile-header .text-grey-6 {
  color: rgba(255, 255, 255, 0.7) !important;
}
.profile-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
}
</style>
