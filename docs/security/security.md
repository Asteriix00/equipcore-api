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
- `tokenVersion` : matched against the user's current value in the database on every request, to invalidate tokens issued before a logout
- `iat` / `exp` : issued-at and expiration timestamps

No **refresh token** is issued, access tokens are **short-lived (8 hours)**.

## Filter Chain

A custom `JwtFilter` runs **once per request**, before Spring Security's default authentication filter. It :

- **Skips** public endpoints (`/auth/**`) entirely.
- **Skips** the public login endpoint (`POST /auth`) so other verbs on the same path (like the logout endpoint) stay protected.
- **Extracts and validates** the Bearer token when present.
- **Populates the security context** on success, or immediately returns a **structured JSON error** on an **invalid or expired token**.

**Sessions are STATELESS**, and **CSRF protection is turned off**, since it only protects cookie-based sessions, which this API does not use.

## Logout Strategy

Since **access tokens** are **stateless** and never stored, logout doesn't rely on a blacklist. Instead, each user has a `tokenVersion` counter in the database, **incremented on every logout**.

Every request carries its `tokenVersion` as a claim. It's compared against the current database value, a mismatch means the token was issued before the last logout, and the request is treated as unauthenticated.

This invalidates **all** of a user's active tokens at once, across every device.

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