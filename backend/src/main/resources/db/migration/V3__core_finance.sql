-- Phase 3: Core Finance (Accounts, Categories, Transactions)
-- Evolves the Phase 1 foundation for real expense tracking. V1/V2 are never
-- edited; every change ships here so production uses ddl-auto=validate against
-- this Flyway-managed schema.

-- ---------------------------------------------------------------------------
-- Accounts: richer metadata for a usable expense tracker.
-- ---------------------------------------------------------------------------
ALTER TABLE accounts
    ADD COLUMN institution VARCHAR(120),
    ADD COLUMN color       VARCHAR(32),
    ADD COLUMN icon        VARCHAR(64);

-- Remap legacy Phase 1 account type values to the Phase 3 enum so any existing
-- rows stay readable. New enum: CASH, CHECKING, SAVINGS, CREDIT_CARD, WALLET.
UPDATE accounts SET type = 'CREDIT_CARD' WHERE type = 'CREDIT';
UPDATE accounts SET type = 'SAVINGS'     WHERE type = 'INVESTMENT';

CREATE INDEX idx_accounts_type ON accounts (type);

-- ---------------------------------------------------------------------------
-- Categories: explicit INCOME / EXPENSE typing.
-- Name uniqueness is now scoped to (user, name, type) so a user can have both an
-- "Other" income category and an "Other" expense category.
-- ---------------------------------------------------------------------------
ALTER TABLE categories
    ADD COLUMN category_type VARCHAR(16) NOT NULL DEFAULT 'EXPENSE';

ALTER TABLE categories DROP CONSTRAINT uq_categories_user_name;
ALTER TABLE categories
    ADD CONSTRAINT uq_categories_user_name_type UNIQUE (user_id, name, category_type);

CREATE INDEX idx_categories_type ON categories (category_type);

-- ---------------------------------------------------------------------------
-- Transactions: payee, currency, tags, and transfer support.
-- ---------------------------------------------------------------------------
ALTER TABLE transactions
    ADD COLUMN merchant             VARCHAR(255),
    ADD COLUMN currency             VARCHAR(8) NOT NULL DEFAULT 'USD',
    ADD COLUMN tags                 VARCHAR(255),
    ADD COLUMN destination_account_id UUID REFERENCES accounts (id) ON DELETE SET NULL;

CREATE INDEX idx_transactions_type ON transactions (type);
CREATE INDEX idx_transactions_destination_account_id ON transactions (destination_account_id);
