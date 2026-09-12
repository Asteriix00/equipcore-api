# Security

## Overview

**EquipCore API** is secured with a **stateless, JWT-based authentication** system.

There is no **server-side session**, every request authenticates itself independently via a **Bearer token**.

Authorization is **role-based** (`ADMIN`, `TECHNICIAN`, `EMPLOYEE`), and passwords are **never stored in plain text**.

## Authentication Flow

On login, the API verifies the user's credentials and issues a **signed JWT**.

The token carries the minimum needed to identify and authorize the user :

- `sub` : the user's email
- `role` : the user's role
- `iat` / `exp` : issued-at and expiration timestamps

No **refresh token** is issued, access tokens are **short-lived (8 hours)**.

## Filter Chain

A custom `JwtFilter` runs **once per request**, before Spring Security's default authentication filter. It :

- **Skips** public endpoints (`/auth/**`) entirely.
- **Extracts and validates** the Bearer token when present.
- **Populates the security context** on success, or immediately returns a **structured JSON error** on an **invalid or expired token**.

**Sessions are STATELESS**, and **CSRF protection is turned off**, since it only protects cookie-based sessions, which this API does not use.

## Authorization

Roles are stored as **plain values** in the database (`ADMIN`, `TECHNICIAN`, `EMPLOYEE`).

The `ROLE_` prefix Spring Security expects is added **only in memory** when building the user's authorities, the database stays clean of framework-specific conventions.

Access control is enforced **per endpoint** with `@PreAuthorize`, rather than centralized in the security configuration, keeping authorization rules close to the code they protect.

## Error Handling

Authentication and authorization failures **never reach** the **application's global exception handler**, they're **intercepted earlier in the security filter chain**, so they're handled separately :

- **`CustomAuthenticationEntryPoint`** : no valid authentication at all → `401`.
- **`CustomAccessDeniedHandler`** : authenticated, but insufficient role → `403`.
- **`JwtFilter`** : for a token that's present but invalid or expired → `401`.

All three return the **same consistent JSON shape** (`message` + `errorCode`) used across the rest of the API.

## Password Encoding

Passwords are hashed with **BCrypt (strength 12)**, never stored or compared in plain text.

## Testing

The security layer is covered by **unit tests** and **integration tests** using an in-memory **H2 database**.