CREATE TABLE IF NOT EXISTS data.customers
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id    bigint       NOT NULL REFERENCES data.users (id),
    first_name varchar(100) NOT NULL,
    last_name  varchar(100) NOT NULL,
    country    varchar(2)   NOT NULL,
    note       text        DEFAULT NULL,
    updated_at timestamptz DEFAULT NULL
);
comment on column data.customers.id is 'Идентификатор покупателя';
comment on column data.customers.user_id is 'Идентификатор пользователя';
comment on column data.customers.first_name is 'Имя покупателя';
comment on column data.customers.last_name is 'Фамилия покупателя';
comment on column data.customers.country is 'Двухбуквенный код страны alpha-2. Определён в ISO 3166-1';
comment on column data.customers.note is 'Примечание';
comment on column data.customers.updated_at is 'Дата последнего изменения записи';
