-- V7: Subscriptions and payment history

CREATE TABLE IF NOT EXISTS subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stripe_subscription_id  VARCHAR(255) UNIQUE,
    stripe_customer_id      VARCHAR(255),
    plan                    VARCHAR(20) NOT NULL CHECK (plan IN ('monthly', 'annual')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'cancelled', 'past_due', 'expired')),
    current_period_start    TIMESTAMP,
    current_period_end      TIMESTAMP,
    cancelled_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_sub_id ON subscriptions(stripe_subscription_id);

CREATE TABLE IF NOT EXISTS payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stripe_session_id       VARCHAR(255) UNIQUE,
    stripe_payment_intent_id VARCHAR(255),
    type                    VARCHAR(20) NOT NULL CHECK (type IN ('subscription', 'one_time')),
    description             VARCHAR(500),
    amount                  DECIMAL(10,2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'eur',
    status                  VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed')),
    created_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_stripe_session ON payments(stripe_session_id);
