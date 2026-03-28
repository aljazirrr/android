# CLAUDE.md — FitLife Android Project

## Project Overview

**FitLife** is a comprehensive fitness tracking Android application built with modern Android development practices. It supports multi-user authentication, workout tracking, nutrition logging, body measurements, an exercise library, and user profiles.

- **Package**: `com.fitlife.app`
- **Min SDK**: 26 (Android 8.0) | **Compile/Target SDK**: 34
- **Language**: Kotlin 1.9.22
- **Build system**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`libs.versions.toml`)

---

## Repository Structure

```
android/
└── FitLife/                        # Single Android project
    ├── app/
    │   ├── build.gradle.kts        # App-level build config
    │   ├── google-services.json    # Firebase config (must be provided, not committed)
    │   └── src/main/java/com/fitlife/app/
    │       ├── FitLifeApplication.kt   # @HiltAndroidApp entry point + WorkManager config
    │       ├── MainActivity.kt         # Single activity, bottom nav, NavHost
    │       ├── core/
    │       │   ├── data/
    │       │   │   ├── local/          # Room Database + DAOs + Entities
    │       │   │   └── remote/firebase/ # FirestoreCollections constants
    │       │   ├── domain/model/       # Pure Kotlin domain models (User, Workout, Nutrition, Progress)
    │       │   ├── ui/
    │       │   │   ├── components/     # Reusable Composable components
    │       │   │   └── theme/          # Material 3 theme (FitLifeTheme)
    │       │   ├── notifications/      # FCM messaging service
    │       │   └── utils/
    │       │       ├── Enums.kt        # All shared enums (MuscleGroup, MealType, etc.)
    │       │       ├── Extensions.kt   # Kotlin extension functions
    │       │       └── Resource.kt     # Sealed class for async state
    │       ├── di/
    │       │   ├── AppModule.kt        # Room DB, Firebase singletons
    │       │   └── RepositoryModule.kt # Repository bindings
    │       ├── features/
    │       │   ├── auth/               # Login, Register, ForgotPassword
    │       │   ├── dashboard/          # Home screen with stats & quick actions
    │       │   ├── workout/            # Workout list + active workout session
    │       │   ├── exercises/          # Exercise library with detail view
    │       │   ├── nutrition/          # Daily food journal + add food
    │       │   ├── progress/           # Body measurements + add measurement
    │       │   └── profile/            # Profile view, edit profile, settings
    │       └── navigation/
    │           └── NavGraph.kt         # Sealed Screen routes + FitLifeNavGraph
    ├── build.gradle.kts            # Root build config (plugins only)
    ├── settings.gradle.kts         # Single-module project (:app)
    └── gradle/
        └── libs.versions.toml      # Version catalog for all dependencies
```

---

## Architecture

**MVVM + Clean Architecture** with a feature-based package structure.

### Layers (per feature)

```
features/<name>/
├── data/               # Repository implementation (local + remote)
│   └── <Name>RepositoryImpl.kt
├── domain/             # Repository interface
│   └── <Name>Repository.kt
└── presentation/       # ViewModel + Screens (Composables)
    ├── <Name>ViewModel.kt
    └── <Name>Screen.kt
```

### Key Patterns

- **`Resource<T>`** — Sealed class (`Success`, `Error`, `Loading`) used for all async operations throughout repositories and ViewModels.
- **StateFlow** — ViewModels expose `StateFlow<UiState>`, collected in Composables with `collectAsStateWithLifecycle()`.
- **UiState data classes** — Each screen/feature has a dedicated `data class XxxUiState(...)` describing all display state.
- **Hilt DI** — `@HiltViewModel` on all ViewModels; `@Inject constructor`; singletons in `AppModule` and `RepositoryModule`.
- **Navigation callbacks** — Screens receive navigation as lambda parameters (`onNavigateBack: () -> Unit`), never the `NavController` directly.
- **`remember { startDestination }`** — The NavHost `startDestination` must be wrapped in `remember { }` to prevent graph recreation crashes when auth state changes.

---

## Tech Stack

| Category | Library | Version |
|---|---|---|
| UI | Jetpack Compose BOM | 2024.02.00 |
| UI | Material 3 | via BOM |
| DI | Hilt | 2.50 |
| Navigation | Navigation Compose | 2.7.7 |
| Local DB | Room | 2.6.1 |
| Remote | Firebase (Auth, Firestore, Storage, Analytics, Messaging) | BOM 32.7.2 |
| Async | Coroutines + Flow | 1.7.3 |
| Image loading | Coil Compose | 2.5.0 |
| Charts | Vico (compose-m3) | 1.13.1 |
| Background | WorkManager | 2.9.0 |
| Logging | Timber | 5.0.1 |
| Animations | Lottie Compose | 6.3.0 |
| Permissions | Accompanist Permissions | 0.32.0 |
| Code gen | KSP | 1.9.22-1.0.17 |

---

## Navigation

All routes are defined in `NavGraph.kt` as a sealed class:

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object AddFood : Screen("add_food/{mealType}") {
        fun createRoute(mealType: MealType) = "add_food/${mealType.name}"
    }
    // ... etc
}
```

- Auth screens: `login`, `register`, `forgot_password`
- Main tabs (bottom nav): `dashboard`, `exercises`, `nutrition`, `progress`, `profile`
- Sub-screens: `workout`, `active_workout`, `exercise_detail/{exerciseId}`, `add_food/{mealType}`, `add_measurement`, `edit_profile`, `settings`

**Bottom nav** is shown only when `currentRoute` is one of the five main tab routes. Auth routes hide the bottom bar entirely.

**Transitions**: All route changes use `slideInHorizontally + fadeIn` / `slideOutHorizontally + fadeOut` with 300ms tween.

---

## Data Layer

### Room Database (`fitlife_db`, version 1)

Entities: `UserEntity`, `ExerciseEntity`, `WorkoutSessionEntity`, `NutritionLogEntity`, `FoodEntity`, `BodyMeasurementEntity`, `ProgressPhotoEntity`, `PersonalRecordEntity`, `DailyStatsEntity`

DAOs are all in `core/data/local/dao/Daos.kt`.

> **Note**: `fallbackToDestructiveMigration()` is set — schema changes will wipe the local database. Add explicit migrations when incrementing `version`.

### Firestore Collections

Defined as constants in `FirestoreCollections.kt`: `users`, `workout_sessions`, `workout_plans`, `nutrition_logs`, `body_measurements`, `progress_photos`, `personal_records`, `daily_stats`, `exercises`, `foods`.

Data is scoped per user: `users/{userId}/...`

### Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /exercises/{exerciseId} {
      allow read: if request.auth != null;
    }
  }
}
```

---

## Key Enums (`core/utils/Enums.kt`)

- `Gender`: MALE, FEMALE, NOT_SPECIFIED
- `FitnessLevel`: BEGINNER, INTERMEDIATE, ADVANCED, ATHLETE
- `MuscleGroup`: CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, FOREARMS, CORE, QUADRICEPS, HAMSTRINGS, GLUTES, CALVES, FULL_BODY, CARDIO
- `ExerciseType`: STRENGTH, CARDIO, FLEXIBILITY, BALANCE, SPORTS, OTHER
- `Equipment`: NONE, BARBELL, DUMBBELL, KETTLEBELL, RESISTANCE_BAND, MACHINE, CABLE, BODYWEIGHT, PULL_UP_BAR, BENCH, TREADMILL, BICYCLE, ROWING_MACHINE, OTHER
- `WorkoutStatus`: PLANNED, IN_PROGRESS, COMPLETED, SKIPPED
- `NutritionUnit`: GRAMS, ML, OZ, CUP, TABLESPOON, TEASPOON, PIECE
- `MealType`: BREAKFAST, LUNCH, DINNER, SNACK, PRE_WORKOUT, POST_WORKOUT

---

## Build Configuration

### Compiler flags (enabled globally)

```kotlin
"-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
"-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
"-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
```

These are applied project-wide — no need to add `@OptIn` annotations in individual files for these APIs.

### Build Types

- `debug`: `applicationId` = `com.fitlife.app.debug`, debuggable
- `release`: minification + resource shrinking enabled with ProGuard

### Java/Kotlin target: 17

Core library desugaring is enabled (`isCoreLibraryDesugaringEnabled = true`), so Java 8+ APIs are available on minSdk 26+.

---

## Firebase Setup

The project requires a `google-services.json` file in `app/`. To set up a new environment:

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package `com.fitlife.app`
3. Download and place `google-services.json` in `FitLife/app/`
4. Enable: Firebase Authentication (Email/Password + Google Sign-In), Firestore, Storage

---

## Development Conventions

### Adding a New Feature

1. Create `features/<name>/domain/<Name>Repository.kt` (interface)
2. Create `features/<name>/data/<Name>RepositoryImpl.kt` (implementation)
3. Register in `di/RepositoryModule.kt` with `@Provides @Singleton`
4. Create `features/<name>/presentation/<Name>ViewModel.kt` with `@HiltViewModel`
5. Create `features/<name>/presentation/<Name>Screen.kt` (Composable)
6. Add route to `Screen` sealed class in `NavGraph.kt`
7. Add `composable(Screen.Xxx.route) { ... }` in `FitLifeNavGraph`

### ViewModel Pattern

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    fun doSomething() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = repository.fetchData()) {
            is Resource.Success -> _uiState.update { it.copy(data = result.data, isLoading = false) }
            is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
            is Resource.Loading -> Unit
        }
    }
}
```

### Screen Pattern

```kotlin
@Composable
fun ExampleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### Logging

Use `Timber.d(...)`, `Timber.e(...)` etc. — never `Log.d`. Timber is planted only in debug builds.

---

## Permissions (AndroidManifest)

- `INTERNET`, `ACCESS_NETWORK_STATE` — Firebase/network calls
- `POST_NOTIFICATIONS` — Push notifications (runtime permission on API 33+)
- `CAMERA`, `READ_MEDIA_IMAGES` — Progress photos
- `ACTIVITY_RECOGNITION` — Step counter
- `VIBRATE` — Workout timers

The step counter sensor is declared as `required="false"`.

---

## Testing

Tests are minimal at this stage:

- Unit tests: `app/src/test/` (JUnit 4)
- Instrumented tests: `app/src/androidTest/` (Espresso + Compose UI test)

Run tests with:
```bash
cd FitLife
./gradlew test                    # unit tests
./gradlew connectedAndroidTest    # instrumented tests (requires device/emulator)
```

---

## Common Build Commands

```bash
cd FitLife

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint
./gradlew lint

# Clean build
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## Known Constraints & Gotchas

1. **NavHost `startDestination` must be `remember`-ed** — Recomputing it on each recomposition causes "destination not found" crashes. See `NavGraph.kt`.
2. **Room `fallbackToDestructiveMigration`** — Any schema change without an explicit migration will wipe user data on upgrade. Always add `Migration` objects when bumping `version` in `FitLifeDatabase`.
3. **`google-services.json` is required** — Build will fail without it. It is gitignored and must be provided per environment.
4. **Firestore offline** — Room is used as a local cache. Repositories typically write to Room first (single source of truth) and sync to Firestore async.
5. **Accompanist deprecation** — `accompanist-systemuicontroller` and `accompanist-permissions` (0.32.0) are from the Accompanist library which is being sunset. These should be migrated to first-party AndroidX APIs in future.
