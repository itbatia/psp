CREATE TABLE IF NOT EXISTS data.users
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type       varchar(8)  NOT NULL,
    status     varchar(7)  NOT NULL DEFAULT 'ACTIVE',
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz          DEFAULT NULL
);
comment on column data.users.id is 'Идентификатор пользователя';
comment on column data.users.type is 'Тип пользователя: MERCHANT, CUSTOMER';
comment on column data.users.status is 'Статус пользователя: ACTIVE, BANNED, DELETED';
comment on column data.users.created_at is 'Дата добавления записи';
comment on column data.users.updated_at is 'Дата последнего изменения записи';
