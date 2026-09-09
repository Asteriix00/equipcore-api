CREATE TABLE categories
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(50)              NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(50)              NOT NULL,
    last_name  VARCHAR(50)              NOT NULL,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(255)             NOT NULL,
    role       VARCHAR(10)              NOT NULL,
    is_enabled BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE equipments
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(50)              NOT NULL,
    serial_number VARCHAR(50)              NOT NULL UNIQUE,
    status        VARCHAR(9)               NOT NULL,
    description   VARCHAR(255),
    purchase_date DATE,
    category_id   UUID                     NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE,

    constraint fk_equipment_category
        foreign key (category_id)
            references categories (id)
            on delete restrict
);

CREATE TABLE assignments
(
    id                      UUID PRIMARY KEY,
    user_id                 UUID                     NOT NULL,
    user_full_name          VARCHAR(101)             NOT NULL,
    user_email              VARCHAR(100)             NOT NULL,
    equipment_id            UUID                     NOT NULL,
    equipment_name          VARCHAR(50)              NOT NULL,
    equipment_serial_number VARCHAR(50)              NOT NULL,
    assigned_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at             TIMESTAMP WITH TIME ZONE,

    constraint fk_assignment_equipment
        foreign key (equipment_id)
            references equipments (id)
            on delete restrict,

    constraint fk_assignment_user
        foreign key (user_id)
            references users (id)
            on delete restrict
);