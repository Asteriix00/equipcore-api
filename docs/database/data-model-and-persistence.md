# Data Model and Persistence

## Overview
The conceptual data model (MCD) is built around **three core entities :**
* `users`
* `equipments`
* `categories`

Connected by **two associations :**
* `tracking every equipment assignment and return over time`
* `classifying equipment into a category`

<img src="docs/database/MCD.drawio.svg" alt="Conceptual Data Model" width="100%"/>

## Entities

| Entity | Attributes |
|---     |---         |
| **users** | id, first_name, last_name, email, password, role, is_enabled, created_at, updated_at |
| **equipments** | id, name, serial_number, status, description, purchase_date, category_id, created_at, updated_at |
| **categories** | id, name, created_at, updated_at |
| **assignments** | id, user_id, user_full_name, user_email, equipment_id, equipment_name, equipment_serial_number, assigned_at, returned_at |

`assignments` is the physical table born from translating the `users ↔ equipments many-to-many association`.

In addition to the `user_id` and `equipment_id` **foreign keys**, it stores a **snapshot** of the **user's name and email**, and the **equipment's name and serial number** at the **time of assignment**. This ensures an accurate and immutable audit trail, even if the corresponding users or equipments records are later **modified or soft-deleted**.

## Associations

`Is assigned to (users ↔ equipments) :` `Many-to-many relationship` carrying two attributes : `assigned_at` and `returned_at`, one user hold `0,N` equipment over time, and one piece of equipment pass `0,N` users over time, while keeping a full audit trail of every assignment.

`Belongs to (categories ↔ equipments) :` `One-to-many relationship` one category has `0,N` equipment, and one equipment belongs to exactly `1,1` category.

## Enumeration strategy

`role` and `status` are conceptually **enumerations**, but they are **not implemented as native SQL enum types**, instead they're stored as `VARCHAR` **sized to the longest value in the set**, and **validated at the application layer**.

| Column | Values | Column type |
|---|---|---|
| `role` | `ADMIN`, `TECHNICIAN`, `EMPLOYEE` | `VARCHAR(10)` |
| `status` | `AVAILABLE`, `ASSIGNED`, `RETIRED` | `VARCHAR(9)` |

## Flyway migrations

| Migration | Purpose |
|---|---|
| `V1__initial_schema.sql` | Creates the **4 core tables :** `categories`, `users`, `equipments`, `assignments`|
| `V2__seed_default_categories.sql` | Seeds the **10 default categories :** `LAPTOP`, `DESKTOP`, `MONITOR`, `KEYBOARD`, `MOUSE`, `SMARTPHONE`, `TABLET`, `PROJECTOR`, `PRINTER`, `HEADSET` |
