CREATE TABLE IF NOT EXISTS data.webhooks
(
    id               bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    transaction_id   varchar(36) NOT NULL REFERENCES data.transactions (transaction_id),
    notification_url text        NOT NULL,
    attempt          int         NOT NULL,
    request          jsonb       NOT NULL,
    response         jsonb       NOT NULL,
    response_status  int         NOT NULL,
    to_resend        boolean     NOT NULL,
    created_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamptz          DEFAULT NULL
);
comment on column data.webhooks.id is 'Идентификатор вебхука';
comment on column data.webhooks.transaction_id is 'Идентификатор транзакции';
comment on column data.webhooks.notification_url is 'URL мерчанта для отправки уведомлений о статусе транзакции';
comment on column data.webhooks.attempt is 'Номер попытки доставки вебхука';
comment on column data.webhooks.request is 'Данные отправленного запроса';
comment on column data.webhooks.response is 'Данные полученного ответа';
comment on column data.webhooks.response_status is 'Статус полученного ответа';
comment on column data.webhooks.to_resend is 'True - вебхук необходимо отправить повторно, False - повторная отправка не требуется';
comment on column data.webhooks.created_at is 'Дата добавления записи';
comment on column data.webhooks.updated_at is 'Дата последнего изменения записи';
