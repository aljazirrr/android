# FitLife - Aplicație Fitness Completă pentru Android

## Arhitectura proiectului

```
FitLife/
├── app/
│   ├── src/main/java/com/fitlife/app/
│   │   ├── FitLifeApplication.kt          # App entry point + Hilt
│   │   ├── MainActivity.kt                # Main activity + Bottom Nav
│   │   ├── core/
│   │   │   ├── data/
│   │   │   │   ├── local/                 # Room Database
│   │   │   │   │   ├── FitLifeDatabase.kt
│   │   │   │   │   ├── dao/               # Data Access Objects
│   │   │   │   │   └── entities/          # Room Entities
│   │   │   │   └── remote/firebase/       # Firestore Collections
│   │   │   ├── domain/model/              # Domain Models
│   │   │   ├── ui/
│   │   │   │   ├── components/            # Reusable UI components
│   │   │   │   └── theme/                 # Material 3 Theme
│   │   │   ├── notifications/             # FCM Service
│   │   │   └── utils/                     # Extensions, Helpers
│   │   ├── di/                            # Hilt DI Modules
│   │   ├── features/
│   │   │   ├── auth/                      # Authentication (multi-user)
│   │   │   ├── dashboard/                 # Home Dashboard
│   │   │   ├── workout/                   # Workout Tracking
│   │   │   ├── exercises/                 # Exercise Library (20+ exercises)
│   │   │   ├── nutrition/                 # Nutrition & Calorie Tracking
│   │   │   ├── progress/                  # Body Measurements & Progress
│   │   │   └── profile/                   # User Profile & Settings
│   │   └── navigation/                    # Navigation Graph
```

## Features

### Autentificare Multi-User
- Login/Register cu email și parolă
- Google Sign-In
- Resetare parolă
- Sesiuni separate per utilizator

### Dashboard
- Salut personalizat + streak-uri
- Stats rapide (pași, calorii, antrenamente)
- Inel caloric cu macros
- Progres săptămânal vizual
- Citate motivaționale

### Antrenamente
- Sesiuni de antrenament live cu timer
- Tracking seturi/repetări/greutăți
- Timer de odihnă între seturi
- Istoric complet
- Rating antrenamente

### Bibliotecă Exerciții (20+)
- Exerciții predefinite pentru toate grupele musculare
- Filtrare după mușchi, echipament, tip
- Căutare
- Exerciții custom per utilizator

### Nutriție
- Jurnal alimentar zilnic
- 15+ alimente predefinite
- Tracking macronutrienți (P/C/G)
- Monitorizare hidratare
- Target calorii personalizat

### Progres
- Măsurători corporale (greutate, IMC, %grăsime, circumferințe)
- Fotografii de progres
- Recorduri personale
- Grafice evoluție

### Profil
- Profil complet (vârstă, înălțime, greutate, nivel fitness)
- Obiective personalizate
- Setări notificări
- Deconectare

## Stack Tehnic

- **Kotlin** + **Jetpack Compose** (Material Design 3)
- **Firebase Auth** (autentificare multi-user)
- **Firebase Firestore** (baze de date cloud)
- **Firebase Storage** (fotografii)
- **Room Database** (cache local offline)
- **Hilt** (Dependency Injection)
- **Coroutines + Flow** (programare asincronă)
- **Navigation Compose** (navigare)
- **Vico** (grafice)
- **Coil** (imagini)
- **WorkManager** (task-uri background)
- **MVVM + Clean Architecture**

## Setup

1. Creează un proiect Firebase la [console.firebase.google.com](https://console.firebase.google.com)
2. Adaugă aplicația Android cu package `com.fitlife.app`
3. Descarcă `google-services.json` și înlocuiește fișierul din `app/`
4. Activează Firebase Authentication (Email + Google)
5. Activează Firestore Database
6. Activează Firebase Storage
7. Build și rulează!

## Firestore Rules

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
