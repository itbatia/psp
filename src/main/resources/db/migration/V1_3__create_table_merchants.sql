CREATE TABLE IF NOT EXISTS data.merchants
(
    id           bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id      bigint NOT NULL REFERENCES data.users (id),
    api_id       text   NOT NULL UNIQUE,
    api_key      text   NOT NULL,
    ip_addresses text[] NOT NULL,
    note         text        DEFAULT NULL,
    updated_at   timestamptz DEFAULT NULL
);
comment on column data.merchants.id is 'Идентификатор мерчанта';
comment on column data.merchants.user_id is 'Идентификатор пользователя';
comment on column data.merchants.api_id is 'Идентификатор API мерчанта';
comment on column data.merchants.api_key is 'Секретный ключ мерчанта';
comment on column data.merchants.ip_addresses is 'Список доверенных IP-адресов';
comment on column data.merchants.note is 'Примечание';
comment on column data.merchants.updated_at is 'Дата последнего изменения записи';
