CREATE TABLE IF NOT EXISTS data.cards
(
    id          bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id  bigint      NOT NULL REFERENCES data.accounts (id),
    card_number varchar(19) NOT NULL,
    exp_date    varchar(5)  NOT NULL,
    cvv         int         NOT NULL,
    card_status varchar(7)  NOT NULL DEFAULT 'NEW',
    note        text                 DEFAULT NULL,
    created_at  timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamptz          DEFAULT NULL
);
comment on column data.cards.id is 'Идентификатор карты';
comment on column data.cards.account_id is 'Идентификатор счёта пользователя';
comment on column data.cards.card_number is 'Номер карты';
comment on column data.cards.exp_date is 'Срок действия карты';
comment on column data.cards.cvv is 'Трехзначный защитный код для проверки подлинности карты';
comment on column data.cards.card_status is 'Статус карты: NEW, ACTIVE, BLOCKED, EXPIRED';
comment on column data.cards.note is 'Примечание';
comment on column data.cards.created_at is 'Дата добавления записи';
comment on column data.cards.updated_at is 'Дата последнего изменения записи';
