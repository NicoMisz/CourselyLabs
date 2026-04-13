import api from './axios'

export interface SubscriptionInfo {
  id: string
  plan: 'monthly' | 'annual'
  status: 'active' | 'cancelled' | 'past_due' | 'expired'
  currentPeriodStart: string
  currentPeriodEnd: string
  cancelledAt: string | null
  createdAt: string
}

export interface PaymentRecord {
  id: string
  type: string
  description: string
  amount: number
  currency: string
  status: string
  createdAt: string
}

export async function createCheckout(plan: 'monthly' | 'annual'): Promise<string> {
  const { data } = await api.post<{ url: string }>('/api/payments/checkout', { plan })
  return data.url
}

export async function getSubscription(): Promise<SubscriptionInfo | null> {
  const { data } = await api.get<SubscriptionInfo>('/api/payments/subscription')
  return data
}

export async function cancelSubscription(): Promise<void> {
  await api.post('/api/payments/cancel-subscription')
}

export async function getPaymentHistory(): Promise<PaymentRecord[]> {
  const { data } = await api.get<PaymentRecord[]>('/api/payments/history')
  return data
}

export async function checkIsPremium(): Promise<boolean> {
  const { data } = await api.get<{ premium: boolean }>('/api/payments/is-premium')
  return data.premium
}
