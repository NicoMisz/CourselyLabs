<template>
  <q-page class="q-pa-lg">
    <h1 class="text-h5 q-mb-lg">Gestion de usuarios</h1>

    <!-- Filters -->
    <div class="row q-gutter-md q-mb-md items-end">
      <q-input
        v-model="filters.search"
        outlined
        dense
        placeholder="Buscar por nombre o email..."
        class="col-12 col-sm"
        clearable
        debounce="400"
        @update:model-value="resetAndFetch"
      >
        <template #prepend><q-icon name="search" /></template>
      </q-input>

      <q-select
        v-model="filters.role"
        :options="roleFilterOptions"
        outlined
        dense
        emit-value
        map-options
        label="Rol"
        class="col-12 col-sm-auto"
        style="min-width: 150px"
        clearable
        @update:model-value="resetAndFetch"
      />

      <q-select
        v-model="filters.isActive"
        :options="statusFilterOptions"
        outlined
        dense
        emit-value
        map-options
        label="Estado"
        class="col-12 col-sm-auto"
        style="min-width: 150px"
        clearable
        @update:model-value="resetAndFetch"
      />
    </div>

    <!-- Table -->
    <q-table
      :rows="users"
      :columns="columns"
      row-key="id"
      :loading="loading"
      flat
      bordered
      v-model:pagination="pagination"
      @request="onRequest"
    >
      <!-- Name + avatar -->
      <template #body-cell-name="props">
        <q-td :props="props">
          <div class="row items-center q-gutter-sm no-wrap">
            <q-avatar size="32px" color="primary" text-color="white" font-size="14px">
              {{ (props.row.firstName?.[0] || '') + (props.row.lastName?.[0] || '') }}
            </q-avatar>
            <div>
              <div>{{ props.row.firstName }} {{ props.row.lastName }}</div>
              <div class="text-caption text-grey-6">{{ props.row.email }}</div>
            </div>
          </div>
        </q-td>
      </template>

      <!-- Role chip -->
      <template #body-cell-role="props">
        <q-td :props="props">
          <q-chip
            :color="roleColor(props.row.role)"
            text-color="white"
            size="sm"
            dense
          >
            {{ roleLabel(props.row.role) }}
          </q-chip>
        </q-td>
      </template>

      <!-- Status -->
      <template #body-cell-status="props">
        <q-td :props="props">
          <q-chip
            :color="props.row.isActive ? 'positive' : 'negative'"
            text-color="white"
            size="sm"
            dense
          >
            {{ props.row.isActive ? 'Activo' : 'Baneado' }}
          </q-chip>
          <q-chip v-if="!props.row.isVerified" color="grey" text-color="white" size="sm" dense class="q-ml-xs">
            Sin verificar
          </q-chip>
        </q-td>
      </template>

      <!-- Actions -->
      <template #body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat dense round icon="more_vert">
            <q-menu>
              <q-list dense>
                <q-item clickable v-close-popup @click="openRoleDialog(props.row)">
                  <q-item-section avatar><q-icon name="swap_horiz" size="20px" /></q-item-section>
                  <q-item-section>Cambiar rol</q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="openGrantPremiumDialog(props.row)">
                  <q-item-section avatar><q-icon name="workspace_premium" size="20px" color="amber-8" /></q-item-section>
                  <q-item-section>Conceder Premium</q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="handleRevokePremium(props.row)">
                  <q-item-section avatar><q-icon name="cancel_presentation" size="20px" color="grey-7" /></q-item-section>
                  <q-item-section>Revocar Premium</q-item-section>
                </q-item>
                <q-item
                  v-if="props.row.isActive && props.row.role !== 'admin'"
                  clickable v-close-popup
                  @click="handleBan(props.row)"
                >
                  <q-item-section avatar><q-icon name="block" size="20px" color="negative" /></q-item-section>
                  <q-item-section class="text-negative">Banear</q-item-section>
                </q-item>
                <q-item
                  v-if="!props.row.isActive"
                  clickable v-close-popup
                  @click="handleUnban(props.row)"
                >
                  <q-item-section avatar><q-icon name="check_circle" size="20px" color="positive" /></q-item-section>
                  <q-item-section class="text-positive">Desbanear</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>
        </q-td>
      </template>

      <!-- No results -->
      <template #no-data>
        <div class="text-center q-pa-lg text-grey-6">
          <q-icon name="search_off" size="48px" class="q-mb-sm" />
          <div>No se encontraron usuarios con estos filtros</div>
        </div>
      </template>
    </q-table>

    <!-- Change role dialog -->
    <q-dialog v-model="roleDialog">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">Cambiar rol</div>
          <p class="text-body2 q-mt-sm">{{ roleUser?.firstName }} {{ roleUser?.lastName }} ({{ roleUser?.email }})</p>
        </q-card-section>
        <q-card-section>
          <q-select
            v-model="newRole"
            :options="roleOptions"
            label="Nuevo rol"
            outlined
            emit-value
            map-options
          />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn color="primary" label="Guardar" :loading="savingRole" @click="handleChangeRole" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Grant premium dialog -->
    <q-dialog v-model="grantDialog">
      <q-card style="min-width: 420px">
        <q-card-section>
          <div class="text-h6 row items-center">
            <q-icon name="workspace_premium" color="amber-8" size="24px" class="q-mr-sm" />
            Conceder Premium
          </div>
          <p class="text-body2 q-mt-sm">
            {{ grantUser?.firstName }} {{ grantUser?.lastName }} ({{ grantUser?.email }})
          </p>
        </q-card-section>

        <q-card-section class="q-gutter-sm">
          <div class="text-caption text-grey-7">Duración:</div>
          <div class="row q-gutter-xs">
            <q-btn
              v-for="preset in presets"
              :key="preset.label"
              :outline="grantDuration !== preset.value"
              :color="grantDuration === preset.value ? 'primary' : 'grey-7'"
              :label="preset.label"
              no-caps
              size="sm"
              @click="selectPreset(preset.value)"
            />
          </div>

          <q-input
            v-model="grantExpiresDate"
            label="Expira el"
            outlined
            mask="####-##-##"
            hint="YYYY-MM-DD"
            class="q-mt-sm"
          >
            <template #append>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy>
                  <q-date v-model="grantExpiresDate" mask="YYYY-MM-DD" />
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancelar" v-close-popup />
          <q-btn
            color="amber-8"
            text-color="white"
            label="Conceder Premium"
            icon="check"
            :loading="grantingPremium"
            :disable="!grantExpiresDate"
            @click="handleGrantPremium"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getAdminUsers, changeUserRole, banUser, unbanUser, grantPremium, revokePremium } from '../../api/admin'

const $q = useQuasar()
const loading = ref(true)
const users = ref<any[]>([])

const filters = reactive({
  search: '',
  role: null as string | null,
  isActive: null as boolean | null,
})

const pagination = ref({
  page: 1,
  rowsPerPage: 20,
  rowsNumber: 0,
})

const columns = [
  { name: 'name', label: 'Usuario', field: 'firstName', align: 'left' as const, sortable: true },
  { name: 'role', label: 'Rol', field: 'role', align: 'center' as const },
  { name: 'status', label: 'Estado', field: 'isActive', align: 'center' as const },
  { name: 'createdAt', label: 'Registro', field: 'createdAt', align: 'center' as const, format: (v: string) => v ? new Date(v).toLocaleDateString('es') : '' },
  { name: 'actions', label: '', field: 'id', align: 'right' as const },
]

const roleFilterOptions = [
  { label: 'Usuario', value: 'user' },
  { label: 'Premium', value: 'premium' },
  { label: 'Admin', value: 'admin' },
]

const statusFilterOptions = [
  { label: 'Activo', value: true },
  { label: 'Baneado', value: false },
]

const roleOptions = [
  { label: 'Usuario', value: 'user' },
  { label: 'Premium', value: 'premium' },
  { label: 'Administrador', value: 'admin' },
]

// Role dialog
const roleDialog = ref(false)
const roleUser = ref<any>(null)
const newRole = ref('')
const savingRole = ref(false)

// Grant premium
const grantDialog = ref(false)
const grantUser = ref<any>(null)
const grantDuration = ref<string>('year')
const grantExpiresDate = ref<string>('')
const grantingPremium = ref(false)

const presets = [
  { label: '1 mes', value: 'month' },
  { label: '3 meses', value: '3months' },
  { label: '6 meses', value: '6months' },
  { label: '1 año', value: 'year' },
  { label: '10 años', value: 'decade' },
]

function toISODate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function selectPreset(value: string) {
  grantDuration.value = value
  const now = new Date()
  const target = new Date(now)
  switch (value) {
    case 'month': target.setMonth(target.getMonth() + 1); break
    case '3months': target.setMonth(target.getMonth() + 3); break
    case '6months': target.setMonth(target.getMonth() + 6); break
    case 'year': target.setFullYear(target.getFullYear() + 1); break
    case 'decade': target.setFullYear(target.getFullYear() + 10); break
  }
  grantExpiresDate.value = toISODate(target)
}

function openGrantPremiumDialog(user: any) {
  grantUser.value = user
  selectPreset('year')
  grantDialog.value = true
}

async function handleGrantPremium() {
  if (!grantUser.value || !grantExpiresDate.value) return
  grantingPremium.value = true
  try {
    const iso = `${grantExpiresDate.value.replace(/\//g, '-')}T23:59:59`
    await grantPremium(grantUser.value.id, iso)
    grantDialog.value = false
    $q.notify({
      type: 'positive',
      message: `Premium concedido hasta ${grantExpiresDate.value}`,
      position: 'bottom-right',
    })
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Error al conceder Premium'
    $q.notify({ type: 'negative', message: msg, position: 'bottom-right' })
  } finally {
    grantingPremium.value = false
  }
}

async function handleRevokePremium(user: any) {
  try {
    await revokePremium(user.id)
    $q.notify({
      type: 'info',
      message: `Premium revocado para ${user.firstName} ${user.lastName}`,
      position: 'bottom-right',
    })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al revocar Premium', position: 'bottom-right' })
  }
}

function roleColor(role: string) {
  switch (role) {
    case 'admin': return 'deep-purple'
    case 'premium': return 'amber-8'
    default: return 'grey'
  }
}

function roleLabel(role: string) {
  switch (role) {
    case 'admin': return 'Admin'
    case 'premium': return 'Premium'
    default: return 'Usuario'
  }
}

function resetAndFetch() {
  pagination.value.page = 1
  fetchUsers()
}

async function fetchUsers() {
  loading.value = true
  try {
    const data = await getAdminUsers({
      search: filters.search || undefined,
      role: filters.role || undefined,
      isActive: filters.isActive,
      page: pagination.value.page - 1,
      size: pagination.value.rowsPerPage,
    })
    users.value = data.content
    pagination.value.rowsNumber = data.totalElements
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar usuarios', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

async function onRequest(props: any) {
  pagination.value.page = props.pagination.page
  pagination.value.rowsPerPage = props.pagination.rowsPerPage
  await fetchUsers()
}

function openRoleDialog(user: any) {
  roleUser.value = user
  newRole.value = user.role
  roleDialog.value = true
}

async function handleChangeRole() {
  if (!roleUser.value) return
  savingRole.value = true
  try {
    const updated = await changeUserRole(roleUser.value.id, newRole.value)
    const idx = users.value.findIndex(u => u.id === roleUser.value.id)
    if (idx >= 0) users.value[idx] = updated
    roleDialog.value = false
    $q.notify({ type: 'positive', message: `Rol cambiado a "${roleLabel(newRole.value)}"`, position: 'bottom-right' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err?.response?.data?.message || 'Error al cambiar rol', position: 'bottom-right' })
  } finally {
    savingRole.value = false
  }
}

async function handleBan(user: any) {
  try {
    const updated = await banUser(user.id)
    const idx = users.value.findIndex(u => u.id === user.id)
    if (idx >= 0) users.value[idx] = updated
    $q.notify({ type: 'info', message: `${user.firstName} ${user.lastName} baneado`, position: 'bottom-right' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err?.response?.data?.message || 'Error al banear', position: 'bottom-right' })
  }
}

async function handleUnban(user: any) {
  try {
    const updated = await unbanUser(user.id)
    const idx = users.value.findIndex(u => u.id === user.id)
    if (idx >= 0) users.value[idx] = updated
    $q.notify({ type: 'positive', message: `${user.firstName} ${user.lastName} desbaneado`, position: 'bottom-right' })
  } catch {
    $q.notify({ type: 'negative', message: 'Error al desbanear', position: 'bottom-right' })
  }
}

onMounted(fetchUsers)
</script>
