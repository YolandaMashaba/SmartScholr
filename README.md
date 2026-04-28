# SmartScholr

**Student budget planner** — Android app for tracking income and expenses in South African Rand (R), with custom categories, month filtering, and local-only data (Room + DataStore).

---

## Features (current build)

### Authentication & session

- **Splash** — Gradient background, decorative circles, brand logo (`res/drawable-nodpi/splash_logo.png`, sourced from `assets/images/logo.png`), two-tone title (Smart / Scholr), **Get Started** → login. If a session already exists, opens **Home** directly.
- **Register** — Name, email, password, terms checkbox, primary **Sign Up**, optional **Continue with Google** (placeholder), link to login. Email is stored **normalized** (trim + `Locale.ROOT` lowercase) as the account key (`User.username`).
- **Login** — Email + password, forgot password / Google placeholders, **Sign In**. User lookup is **case-insensitive** (`LOWER(username)` in SQL). Toasts distinguish **no account**, **wrong password**, and generic errors.
- **Session** — `SessionStore` (DataStore Preferences) holds `session_user_id`. **Logout** clears session and returns to login.

### Home (dashboard)

- Teal/orange theme: header with month chips (current year), summary cards (income / spent / balance), **Recent transactions** list, **Spending by category** horizontal bars, **Add transaction** (description, amount, category spinner, date + start/end time, income vs expense).
- Month filter drives aggregates and lists; ledger rows are filtered by **`startTimeMillis`** within the selected month.
- Currency displayed as **R** with locale-appropriate number formatting.

### Data & persistence

- **Room** database `smartscholr.db`: `User`, `CategoryEntity`, `LedgerEntry`.
- **PBKDF2** password hashing (`PasswordHasher`); verify path catches decode/crypto failures instead of crashing.
- **Repositories**: `AuthRepository`, `LedgerRepository` (default categories seeded per user, month snapshots, counts).

---

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | XML layouts, Material 3 (`Theme.Material3.Light` + custom brand colors), `AppCompatActivity`, RecyclerView |
| Async | Kotlin coroutines, `lifecycleScope` |
| DB | Room 2.6 + KSP |
| Session | DataStore Preferences |
| Min / target SDK | 24 / 36 (see `app/build.gradle.kts`) |

**Note:** The app uses **Activities + repositories**, not full **ViewModel**-based MVVM everywhere. You can introduce `ViewModel` + `StateFlow` in a later refactor without changing the DB API.

---

## Project structure (main package `com.example.smartscholr`)

```
com.example.smartscholr
├── SmartScholrApplication.kt   # Room, DataStore, repositories
├── SplashActivity.kt
├── LoginActivity.kt
├── RegisterActivity.kt
├── HomeActivity.kt
├── data/
│   ├── AppDatabase.kt
│   ├── User.kt, UserDao.kt
│   ├── CategoryEntity.kt, CategoryDao.kt
│   ├── LedgerEntry.kt, LedgerDao.kt
│   ├── AuthRepository.kt
│   └── LedgerRepository.kt
├── session/SessionStore.kt
├── security/PasswordHasher.kt
└── ui/                         # RecyclerView adapters
    ├── TransactionAdapter.kt
    └── CategorySpendAdapter.kt
```

**Branding assets**

- **Source artwork:** `app/src/main/assets/images/logo.png` — copy or replace `app/src/main/res/drawable-nodpi/splash_logo.png` when the logo changes so splash, launcher, and any `@drawable/splash_logo` references stay aligned.
- **Launcher icon**
  - **API 26+ (adaptive):** `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` use `@drawable/ic_launcher_background` (brand teal), `@drawable/ic_launcher_foreground` (layer-list centering `splash_logo` in the adaptive safe zone), and `@drawable/ic_splash_book` for **Android 13+ themed / monochrome** icons.
  - **API 24–25:** bitmap launchers in `mipmap-mdpi` … `mipmap-xxxhdpi` as `ic_launcher.png` / `ic_launcher_round.png`. Regenerate those PNGs from updated `splash_logo` if you change the mark (same nominal sizes: 48, 72, 96, 144, 192 px).

---

## Build & run

1. Open the project in **Android Studio** (Giraffe+ with AGP 9 / Kotlin 2.x as in `libs.versions.toml`).
2. **Sync Gradle**; use **JDK 17+** as required by the Android Gradle Plugin.
3. Run **`app`** on an emulator or device:  
   `./gradlew :app:assembleDebug`  
4. **CI:** GitHub Actions (see `.github/workflows/`) — build should pass on push/PR.

**Gradle note:** `gradle.properties` may set `android.disallowKotlinSourceSets=false` for KSP with the built-in Kotlin plugin; do not remove without verifying KSP still compiles.

---

## Room schema (summary)

| Table | Purpose |
|-------|---------|
| `users` | `username` (unique, email as key, lowercased on write), salt/hash, `displayName`, optional `email` |
| `categories` | Per-user category names; unique `(userId, name)` |
| `ledger_entries` | Income/expense lines: `userId`, `categoryId`, `amount`, `isExpense`, `description`, `dateMillis`, `startTimeMillis`, `endTimeMillis` |

Month rollups use `startTimeMillis` for range queries. `CategoryExpenseSum` Pojo uses explicit `@ColumnInfo` for Room query mapping.

**DB version:** 1 (no migration path yet; `fallbackToDestructiveMigration` is not enabled—bump version + migration if you change entities).

---

## Changelog (documented implementation history)

This section records the **major changes** introduced in the current product direction (transactional home, auth redesign, stability). Exact commit order may vary.

### Product & UX

- Replaced placeholder “module” activities with a real **auth + home** flow.
- **Splash** aligned to design: vertical gradient, background circles, logo tile, two-tone **SmartScholr** title, orange CTA; session short-circuit to Home when logged in.
- **Login / Register** card UI: teal header, outlined fields with leading icons, orange primary actions, “or continue with” + Google button (stub toasts), footer links.
- **Home** dashboard: month selector, summary cards with icons, material cards for sections, recent list + category progress, add transaction form (date, time range, amount, category, income/expense).
- Forced **light** brand experience: `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)` and light `values-night` theme matching day (optional: allow true dark mode later).
- **Status / nav bar** themed (splash vs auth).

### Data & auth

- **Room** entities and DAOs: `User`, `CategoryEntity`, `LedgerEntry`; `AuthRepository` + `LedgerRepository`.
- **Register** stores **email as `username`** (normalized); **Login** uses **`findByUsernameNormalized`** with `LOWER(username) = :key` to avoid case-mismatch sign-in failures.
- **Session** via **DataStore**; `SessionStore` exposes `currentUserIdOrNull()` for activities.
- **Password** verification hardened: try/catch in `PasswordHasher.verify` (Base64 / crypto errors → `false`, not process crash).
- **Register/Login** return `Result` and catch throwables where needed; `User` insert id validated `> 0` before setting session.

### Home & UI stability

- `CategoryExpenseSum` Room Pojo: `@ColumnInfo` for `categoryId` / `total`.
- `HomeActivity` init: session read on **IO**, try/catch around startup; `refreshDashboard` try/catch; guard after `isDestroyed` / `isFinishing` when updating UI.
- RecyclerViews in **ScrollView**: **fixed heights** + `nestedScrollingEnabled=true` to avoid pathological `wrap_content` measurement; **CategorySpendAdapter** guards **NaN** progress.
- **Login** toasts: **no account** / **wrong password** / generic.

### Build

- **KSP** for Room compiler; `ksp` in `app/build.gradle.kts`; `libs.versions.toml` includes Room, coroutines, DataStore, RecyclerView, lifecycle, ConstraintLayout.

### Documentation

- This **README** update: end-to-end feature list, structure, schema, build notes, and changelog.

### Branding

- **Launcher icon** uses the real SmartScholr logo (`splash_logo`) for adaptive and legacy mipmaps instead of the default Android placeholder; background uses brand teal (`auth_teal`).

---

## Git workflow (team)

1. **main** — release / submission; do not push carelessly.  
2. **develop** — integration default.  
3. **Feature branches** — `git checkout develop && git pull && git checkout -b feature/...`  
4. Open **PRs** into `develop`; wait for **GitHub Actions** green before merge.

**If you add dependencies**, update `libs.versions.toml` / `app/build.gradle.kts` and tell the team so everyone syncs the same versions.

---

## Roadmap ideas (not implemented)

- Real **Google Sign-In** / **password reset** flows.  
- **ViewModel** + **Navigation Component** for clearer state.  
- **Room migrations** when schema changes.  
- **Reports** module (charts, exports) per original module plan.  
- **Dark theme** that matches brand (if product turns off `MODE_NIGHT_NO`).

---

## License / credits

Project context: collaborative student work (see original module assignments in git history). Update this section if you adopt a formal license.
