CREATE TABLE prices
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id        BIGINT      NOT NULL,
    unit_amount_cents INTEGER     NOT NULL,
    currency          VARCHAR(3)  NOT NULL,
    billing_interval  VARCHAR(20) NOT NULL,
    active            BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prices_product
        FOREIGN KEY (product_id)
            REFERENCES products (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_price_amount
        CHECK (unit_amount_cents >= 0),

    CONSTRAINT chk_billing_interval
        CHECK (billing_interval IN ('MONTHLY', 'YEARLY', 'ONE_TIME'))
);