-- Borrowing rules per membership type (editable defaults).
CREATE TABLE loan_policies (
    membership_type  VARCHAR(20) PRIMARY KEY,
    max_books        INT NOT NULL,
    loan_period_days INT NOT NULL,
    max_renewals     INT NOT NULL
);

INSERT INTO loan_policies (membership_type, max_books, loan_period_days, max_renewals) VALUES
    ('REGULAR', 3, 14, 1),
    ('STUDENT', 5, 21, 2),
    ('PREMIUM', 10, 30, 3);

-- Single-row library configuration. Money in VND (whole numbers) as NUMERIC.
CREATE TABLE library_settings (
    id                    BIGINT PRIMARY KEY,
    library_name          VARCHAR(255) NOT NULL,
    library_address       VARCHAR(500),
    overdue_fine_per_day  NUMERIC(15, 2) NOT NULL,
    fine_block_threshold  NUMERIC(15, 2) NOT NULL,
    reservation_hold_days INT NOT NULL,
    lost_default_fee      NUMERIC(15, 2) NOT NULL,
    damaged_default_fee   NUMERIC(15, 2) NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL
);

INSERT INTO library_settings
    (id, library_name, library_address, overdue_fine_per_day, fine_block_threshold,
     reservation_hold_days, lost_default_fee, damaged_default_fee, updated_at)
VALUES
    (1, 'Thư viện', NULL, 5000, 50000, 3, 200000, 50000, now());
