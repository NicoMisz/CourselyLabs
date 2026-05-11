import api from './axios'

// ── Conexión del usuario con echo ──────────────────────────────

export interface EchoConnectionStatus {
  connected: boolean
  username?: string | null
  roleName?: string | null
  error?: string | null
}

export async function getEchoStatus(): Promise<EchoConnectionStatus> {
  const { data } = await api.get<EchoConnectionStatus>('/api/me/echo')
  return data
}

export async function connectEcho(token: string): Promise<EchoConnectionStatus> {
  const { data } = await api.put<EchoConnectionStatus>('/api/me/echo', { token })
  return data
}

export async function disconnectEcho(): Promise<void> {
  await api.delete('/api/me/echo')
}

// ── Operar el laboratorio ──────────────────────────────────────

export type LabState = 'NO_TOKEN' | 'NO_VM' | 'STOPPED' | 'RUNNING' | 'ERROR'

export interface LabStatus {
  state: LabState
  vmId?: number | null
  vmName?: string | null
  message?: string | null
}

export interface LabConsole {
  ok: boolean
  /** Payload bruto del ticket VNC tal como lo devuelve echo (ticket, port, host…). */
  ticket?: Record<string, unknown> | null
  error?: string | null
}

export async function getLabStatus(blockId: string): Promise<LabStatus> {
  const { data } = await api.get<LabStatus>(`/api/labs/${blockId}/status`)
  return data
}

export async function startLab(blockId: string): Promise<LabStatus> {
  const { data } = await api.post<LabStatus>(`/api/labs/${blockId}/start`)
  return data
}

export async function stopLab(blockId: string): Promise<LabStatus> {
  const { data } = await api.post<LabStatus>(`/api/labs/${blockId}/stop`)
  return data
}

export async function openLabConsole(blockId: string): Promise<LabConsole> {
  const { data } = await api.post<LabConsole>(`/api/labs/${blockId}/console`)
  return data
}
