# Denti-Code Desktop

A standalone JavaFX desktop equivalent of [`denti-code-u2`](../denti-code-u2). Same dental-clinic domain (admin / doctor / patient), but everything runs on-device against an embedded SQLite database — no Node services, no API gateway.

## What you get

| Area | Status |
|------|--------|
| Login (role-based redirect) | Full |
| Admin / Doctor / Patient portals (sidebar shell, header, language switcher, logout) | Full |
| Admin dashboard (KPIs, status breakdown) | Full |
| Doctor dashboard (upcoming visits, KPIs) | Full |
| Patient dashboard (greeting, demographics, upcoming visits) | Full |
| **Patients** module: search, create, edit | Full |
| **Appointments** module: list, create, status update, performed-actions sub-table, doctor detail view, patient self-service (confirm / reschedule / cancel) | Full |
| Doctors directory | Read-only list |
| Inventory (stock + movements) | Read-only list, service layer fully wired (create/adjust/delete on the service) |
| Settings (display language) | Full |
| Profile (read-only card) | Full |
| Calendar / payments form / inventory editor / avatar upload | Stubs (TODO) |
| Internationalization (English + Spanish) | Full |

The service layer covers every flow from the web app — `AuthService`, `PatientService`, `DoctorService`, `AppointmentService` (incl. patient confirm / cancel / reschedule), `PerformedActionService`, `ProcedureService`, `InventoryService`, `PaymentService`. UI for Inventory editing and Payments forms can layer on top of the existing services without further backend work.

## Architecture

A pragmatic clean-architecture layout with three concentric layers:

```
ui/                     <- JavaFX views (presentation)
  login/                <- LoginView
  shell/                <- PortalShell, NavItem
  admin/  doctor/  patient/   <- per-role portals
  common/               <- views shared across roles (PatientsView, AppointmentsView, etc.)

domain/
  model/                <- JPA entities (User, Patient, Doctor, Appointment, ...)
  service/              <- use cases (AuthService, PatientService, ...)

infra/
  repo/                 <- Repositories (Hibernate JPA queries)
  security/             <- PasswordHasher (BCrypt)

core/                   <- Cross-cutting plumbing
  Config                <- application.properties + system overrides
  Database              <- HikariCP + Hibernate EMF + Flyway
  AppContext            <- single composition root (hand-rolled DI)
  Session               <- bindable current user
  EventBus              <- in-app pub/sub for view refresh
  I18n                  <- bindable locale + ResourceBundle
  Seeder                <- first-run demo data
```

Why hand-rolled DI? Keeps the dependency graph explicit at one point (`AppContext`), makes startup deterministic, and avoids a heavy framework for a small desktop app.

## Tech stack

- Java 21 (toolchain enforced)
- JavaFX 21 (controls, fxml, graphics) via `org.openjfx.javafxplugin`
- AtlantaFX (PrimerLight) for a modern, themeable look
- Hibernate ORM 6.5 + Jakarta Persistence 3.1
- Hibernate community SQLiteDialect
- Flyway 10 for SQL migrations (`src/main/resources/db/migration`)
- HikariCP for connection pooling
- jBCrypt for password hashing
- Jackson for JSON columns (`facilities_used`)
- SLF4J + Logback

## Migrations & data

Flyway runs automatically every launch. The current baseline is:

- `V1__init_schema.sql` — full relational schema (users, roles, doctors, patients, appointments, performed_actions, procedure_types, treatment_facilities, consultories, material_inventory_lines, inventory_movements, payments)
- `V2__reference_data.sql` — static catalogs (procedure types, treatment facilities, consultories)

To add a change, drop a new SQL file:

```
src/main/resources/db/migration/V3__add_something.sql
```

The application's database lives at `~/.denticode-desktop/denti-code.db` by default (override with `-Dapp.dataDir=…`).

### First-run demo data

When `app.demoMode=true` (default) and the database has no users yet, `Seeder` mirrors the seed users from `denti-code-u2`:

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@denti-code.com` | `Password123!` |
| Doctor | `susan.storm@denti-code.com` | `Password123!` |
| Doctor | `peter.parker@denti-code.com` | `Password123!` |
| Patient | `patient1@example.com` | `Password123!` |
| Patient | `patient2@example.com` | `Password123!` |
| Patient | `patient3@example.com` | `Password123!` |
| Patient | `patient4@example.com` | `Password123!` |
| Patient | `patient5@example.com` | `Password123!` |

A handful of sample appointments (past + future, mixed statuses) are also inserted so the dashboards aren't empty.

## Running

```bash
./gradlew run
```

The app expects internet access on first build to fetch dependencies through Gradle. Subsequent runs are fully offline.

To reset the local database, delete `~/.denticode-desktop/denti-code.db` and relaunch.

### Convenience launcher (Linux/macOS)

```bash
chmod +x ./start.sh
./start.sh
```

Reset DB + relaunch:

```bash
./start.sh --reset-db
```

## Project layout

```
denti-code-desktop/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/
├── README.md
└── src/main/
    ├── java/com/denticode/desktop/
    │   ├── Main.java
    │   ├── core/        (Config, Database, Session, EventBus, I18n, AppContext, Seeder)
    │   ├── domain/
    │   │   ├── model/   (12 JPA entities)
    │   │   └── service/ (8 use-case services)
    │   ├── infra/
    │   │   ├── repo/    (11 repositories)
    │   │   └── security/(PasswordHasher)
    │   └── ui/
    │       ├── Router.java
    │       ├── login/, shell/, admin/, doctor/, patient/, common/
    └── resources/
        ├── application.properties
        ├── logback.xml
        ├── META-INF/persistence.xml
        ├── db/migration/V1__init_schema.sql, V2__reference_data.sql
        ├── i18n/messages_en.properties, messages_es.properties
        └── styles/app.css
```

## Mapping back to denti-code-u2

| Web (Next.js) | Desktop (JavaFX) |
|---|---|
| `(auth)/login/page.tsx` | `ui/login/LoginView` |
| `Admin/Doctor/PatientPortalShell.tsx` | `ui/shell/PortalShell` + `ui/{admin,doctor,patient}/*Portal` |
| Redux `authSlice` + `authApiSlice` | `domain/service/AuthService` + `core/Session` |
| Redux `patientsApiSlice` | `domain/service/PatientService` + `infra/repo/PatientRepository` |
| Redux `appointmentsApiSlice` (incl. performed actions) | `AppointmentService` + `PerformedActionService` |
| Redux `doctorsApiSlice` | `DoctorService` + `DoctorRepository` |
| Redux `proceduresApiSlice` | `ProcedureService` (procedure types + treatment facilities) |
| Redux `inventoryApiSlice` | `InventoryService` |
| Redux `paymentsApiSlice` | `PaymentService` |
| `i18n/locales/{en,es}.json` | `resources/i18n/messages_{en,es}.properties` |
| `LanguageSwitcher.tsx` | `ui/common/LanguageSwitcher` |
| AppointmentStatus tag styles | CSS `tag-status-*` classes in `styles/app.css` |
| Patient self-service rules (`patientAppointmentActions.ts`) | `AppointmentService.patientConfirm`, `patientCancel`, `reschedule` |

## Next steps (deliberately deferred)

These are the features the skeleton intentionally stubs; each plugs into existing services without backend changes:

- Inventory create-line / adjust dialogs (call `InventoryService.createLine` / `adjust` / `deleteLine`)
- Patient detail screen for the doctor portal with a payments form (`PaymentService.record`)
- Doctor calendar (week / day views)
- Avatar upload (read a file, copy under `~/.denticode-desktop/avatars/`, set `avatarUrl`)
- Settings → password change (`AuthService.changePassword`)
