# SmartScholr
Student Budget & Expense Tracker

Welcome to the Smart Scholar repository. This project is a collaborative Android application designed to help students manage their finances, track expenses, and visualize their spending habits.

Tech Stack
Language: Kotlin

- Architecture: MVVM (Model-View-ViewModel)

- Database: Room SQLite

- CI/CD: GitHub Actions (Automated Builds & Testing)

- UI: XML / Empty Views Activity

Project Structure & Assignments
- com.example.smartscholr.auth (Nkoka): Login, Registration, & User Profiles
- com.example.smartscholr.expenses: Expense Entry, Categories, & Budgeting
- com.example.smartscholr.reports: Charts, Data Filtering, & Photo/Receipts
- com.example.smartscholr.data (Sifiso): Shared Room Database & Entities

Git Workflow (Important!)

We are using a Develop-first strategy. Please follow these rules to keep the project stable:

1. Main Branch: Reserved for final, submission-ready code. Do not push here.

2. Develop Branch: The default branch for integration.

3. Feature Branches: For every task, create a new branch from develop.

    - git checkout develop

    - git pull origin develop

    - git checkout -b feature-name

Submitting Work
- When your task is done, push your branch to GitHub.

- Open a Pull Request (PR) from your branch into develop.

- GitHub Actions will automatically run a build check. If you see a Red X, you must fix the errors before we can merge your code.

Getting Started
1. Open Android Studio.

2. Select File > New > Project from Version Control.

3. Paste this Repo URL: https://github.com/YolandaMashaba/SmartScholr.git

4. Wait for Gradle Sync to finish.

5. Switch your view to the Android tab to see the package structure.

Continuous Integration

This project uses GitHub Actions. Every time you push code or open a PR, the system will:

1. Set up the Android environment.

2. Check for syntax errors.

3. Run Unit Tests.

4. Build a debug APK.

5. Status: Check the Actions tab on GitHub to see the build history.

Note: If you add new libraries, please inform the Sifiso and Nkoka so we can update the baseline dependencies for everyone.
