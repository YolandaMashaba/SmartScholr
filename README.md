# Student Budget & Expense Tracker — SmartScholr

Welcome to the SmartScholr repository. This project is a collaborative Android application designed to help **university students** manage their personal finances — logging income and expenses, organizing spending by category, visualizing budgets, and staying motivated through a built-in gamification system (XP, levels, and daily streaks).

## What the App Does

SmartScholr lets students:
- Log income and expense transactions with descriptions, categories, dates, and optional receipt photos
- View a home dashboard summarizing Total Income, Total Spent, and Balance for the current period
- Browse recent transactions and a "Spending by Category" breakdown
- Set monthly budget limits per category
- Earn **XP and level up** by logging transactions, logging income, staying under budget, and maintaining daily logging streaks
- Securely register and log in with salted/hashed password storage (no plaintext credentials)

## Who It's For

Students who want a lightweight, private, on-device budgeting tool without needing an internet connection or third-party financial account access — all data stays local in an encrypted/secured Room database on the device.

## Design Decisions

- **MVVM architecture** separates UI (Activities/XML layouts) from business logic (Repositories) and data access (Room DAOs), making each module independently testable.
- **Room as the single source of truth** — `AppDatabase` defines four entities (`User`, `CategoryEntity`, `LedgerEntry`, `XpEntity`) with foreign key relationships enforcing referential integrity (e.g., deleting a user cascades to their categories and XP record; deleting a category sets related transactions to "uncategorized" rather than deleting transaction history).
- **Coroutines + suspend functions** across all DAOs ensure database operations never block the main thread.
- **Salted, hashed password storage** (`PasswordHasher`) — no plaintext credentials are ever persisted.
- **Gamification as a separate concern** — `XpRepository.addXp()` handles XP awarding and streak calculation atomically (wrapped in a database transaction) using calendar-day comparisons, decoupled from the transaction-logging flow that triggers it.
- **Schema migrations over destructive resets** — the project avoids `fallbackToDestructiveMigration()` to preserve user data across app updates; `MIGRATION_1_2`/`MIGRATION_2_3` and an upcoming `MIGRATION_3_4` (introducing integer-based currency storage and additional foreign keys) are implemented as explicit, tested `Migration` objects.

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (SQLite), with Kotlin coroutines for async access
- **CI/CD:** GitHub Actions (automated builds and tests on every push/PR)
- **UI:** XML layouts / Empty Views Activity
- **Charts:** MPAndroidChart (Spending by Category visualizations)
- **Image loading:** Coil (receipt photo display)

## Custom Features

This project includes two custom features beyond the core budgeting functionality:

1. **XP & Leveling System** — Users earn experience points for financial habits: +10 XP for logging a transaction, +15 XP for logging income, +25 XP for staying under a category's monthly budget, and +25 XP for maintaining a daily logging streak. XP accumulates toward levels (e.g., "Penny Saver" at Level 1), displayed via a circular progress indicator and level-up screen.

2. **Daily Streak Tracking** — The app tracks consecutive days of activity logging using calendar-day comparison (not a rolling 24-hour window), displayed as a streak counter and day-by-day visual indicator, encouraging consistent budgeting habits.

## Project Structure & Module Ownership

- `com.example.smartscholr.auth` (Nkoka) — Login, registration, and user profile management
- `com.example.smartscholr.expenses` (Nduh) — Expense entry, category management, and budgeting
- `com.example.smartscholr.reports` (Mhlengi) — Charts, data filtering, and receipt photo handling
- `com.example.smartscholr.data` (Sifiso) — Shared Room database, entities, DAOs, and repositories
- `com.example.smartscholr.security` — Password hashing and credential security

## Git Workflow

We use a **Develop-first strategy**:

1. **Main branch** — reserved for final, submission-ready code. Do not push here directly.
2. **Develop branch** — the default branch for ongoing integration.
3. **Feature branches** — for every task, branch off `develop`:
