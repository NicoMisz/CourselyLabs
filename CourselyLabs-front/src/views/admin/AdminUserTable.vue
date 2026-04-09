<template>
  <q-page class="q-pa-lg">
    <h1 class="text-h5 q-mb-lg">Gestion de usuarios</h1>

    <q-table
      :rows="users"
      :columns="columns"
      row-key="id"
      :loading="loading"
      flat
      bordered
      :pagination="pagination"
      @request="onRequest"
    >
      <!-- Name + avatar -->
      <template #body-cell-name="props">
        <q-td :props="props">
          <div class="row items-center q-gutter-sm">
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
                <!-- Change role -->
                <q-item clickable v-close-popup @click="openRoleDialog(props.row)">
                  <q-item-section avatar><q-icon name="swap_horiz" size="20px" /></q-item-section>
                  <q-item-section>Cambiar rol</q-item-section>
                </q-item>

                <!-- Ban/Unban -->
                <q-item
                  v-if="props.row.isActive && props.row.role !== 'admin'"
                  clickable
                  v-close-popup
                  @click="handleBan(props.row)"
                >
                  <q-item-section avatar><q-icon name="block" size="20px" color="negative" /></q-item-section>
                  <q-item-section class="text-negative">Banear</q-item-section>
                </q-item>
                <q-item
                  v-if="!props.row.isActive"
                  clickable
                  v-close-popup
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
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { getAdminUsers, changeUserRole, banUser, unbanUser } from '../../api/admin'

const $q = useQuasar()
const loading = ref(true)
const users = ref<any[]>([])

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

async function onRequest(props: any) {
  const { page, rowsPerPage } = props.pagination
  loading.value = true
  try {
    const data = await getAdminUsers(page - 1, rowsPerPage)
    users.value = data.content
    pagination.value = {
      page,
      rowsPerPage,
      rowsNumber: data.totalElements,
    }
  } catch {
    $q.notify({ type: 'negative', message: 'Error al cargar usuarios', position: 'bottom-right' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  onRequest({ pagination: pagination.value })
})
</script>
