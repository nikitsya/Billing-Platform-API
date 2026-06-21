ALTER TABLE subscriptions
    ADD CONSTRAINT uq_subscriptions_customer_id UNIQUE (customer_id);