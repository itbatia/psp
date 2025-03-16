CREATE TABLE IF NOT EXISTS data.transactions
(
    transaction_id   varchar(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id_from  bigint      NOT NULL REFERENCES data.accounts (id),
    account_id_to    bigint      NOT NULL REFERENCES data.accounts (id),
    payment_method   varchar(10) NOT NULL,
    amount           numeric     NOT NULL,
    type             varchar(6)  NOT NULL,
    notification_url text        NOT NULL,
    language         varchar(2)  NOT NULL,
    status           varchar(11) NOT NULL    DEFAULT 'IN_PROGRESS',
    message          text                    DEFAULT NULL,
    request          jsonb       NOT NULL,
    created_at       timestamptz NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamptz             DEFAULT NULL
);
comment on column data.transactions.transaction_id is 'Идентификатор транзакции';
comment on column data.transactions.account_id_from is 'Идентификатор счёта списания';
comment on column data.transactions.account_id_to is 'Идентификатор счёта пополнения';
comment on column data.transactions.payment_method is 'Способ оплаты: CARD';
comment on column data.transactions.amount is 'Сумма операции';
comment on column data.transactions.type is 'Тип операции: PAYOUT (списание со счёта), TOPUP (пополнения счёта)';
comment on column data.transactions.notification_url is 'URL мерчанта для отправки уведомлений о статусе транзакции';
comment on column data.transactions.language is 'Двухбуквенный код языка. Определён в ISO 639-1';
comment on column data.transactions.status is 'Статус транзакции: IN_PROGRESS, FAILED, SUCCESS';
comment on column data.transactions.message is 'Текстовое сообщение с описанием статуса транзакции';
comment on column data.transactions.request is 'Исходный запрос мерчанта о создании транзакции';
comment on column data.transactions.created_at is 'Дата добавления записи';
comment on column data.transactions.updated_at is 'Дата последнего изменения записи';
