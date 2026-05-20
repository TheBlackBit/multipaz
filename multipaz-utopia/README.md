# multipaz-utopia

Utopia is the fictional world used across Multipaz demos and sample applications. This module contains shared document types, transaction types, and known-type extensions used by all Utopia-themed organizations.

## Module Structure

```
multipaz-utopia/
├── src/commonMain/kotlin/org/multipaz/utopia/
│   └── knowntypes/          # Shared document & transaction type definitions
│       ├── DocumentTypeRepositoryExt.kt
│       ├── BreweryPurchaseTransaction.kt
│       └── ...              # Other Utopia document types (boarding pass, movie ticket, etc.)
└── organizations/           # Self-contained demo apps per fictional organization
    └── brewery/             # Age-gated e-commerce demo
```

## Organizations

Each organization under `organizations/` is a standalone demo that showcases a real-world use case for verifiable credentials. They share the common Utopia document types defined in this module.

| Organization | Description | README |
|---|---|---|
| **Brewery** | Age-verified e-commerce checkout using mDL / eID credentials | [organizations/brewery/README.md](organizations/brewery/README.md) |

## Shared Known Types

The `knowntypes` package registers all Utopia document and transaction types into the `DocumentTypeRepository`. Call `DocumentTypeRepository.addUtopiaTypes()` in any server or app that needs them.

Currently registered types:

- `UtopiaBoardingPass`
- `UtopiaMovieTicket`
- `UtopiaNaturalization`
- `BreweryPurchaseTransaction`

## Adding a New Organization

1. Create a directory under `organizations/<org-name>/` with `backend/` and `frontend/` submodules.
2. Register the new Gradle modules in `settings.gradle.kts`.
3. Define any new document or transaction types in `src/commonMain/kotlin/org/multipaz/utopia/knowntypes/` and register them in `DocumentTypeRepositoryExt.kt`.
4. Add a `README.md` inside your organization directory and link it in the table above.
