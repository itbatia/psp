CREATE TABLE IF NOT EXISTS data.accounts
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    bigint      NOT NULL REFERENCES data.users (id),
    number     varchar(34) NOT NULL,
    balance    numeric     NOT NULL,
    currency   varchar(3)  NOT NULL,
    note       text                 DEFAULT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz          DEFAULT NULL
);
comment on column data.accounts.id is 'Идентификатор счёта';
comment on column data.accounts.user_id is 'Идентификатор пользователя';
comment on column data.accounts.number is 'Номер счёта';
comment on column data.accounts.balance is 'Баланс счёта';
comment on column data.accounts.currency is 'Трёхбуквенный код валюты. Определён в ISO 4217';
comment on column data.accounts.note is 'Примечание';
comment on column data.accounts.created_at is 'Дата добавления записи';
comment on column data.accounts.updated_at is 'Дата последнего изменения записи';
