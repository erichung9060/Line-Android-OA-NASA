# NASA Cosmos Messenger

An Android chat application featuring an AI assistant "Nova" that delivers NASA's Astronomy Picture of the Day (APOD) through a conversational interface.

## Features

### Core Features
- **Chat Interface**: Conversational UI with Nova AI assistant
- **Date Recognition**: Parse dates from user input (supports `yyyy/MM/dd` and `yyyy-MM-dd`)
- **APOD Integration**: Fetch and display NASA APOD content
- **Favorites Collection**: Save, browse, and delete favorite images
- **Offline Cache**: Store viewed APOD data locally for offline access
- **Dark Mode**: System-aware theme switching

### Bonus Features
- **Birthday Card Sharing**: Generate and share birthday cosmos cards with APOD imagery
- **Video Favorites Support**: Save video APODs with thumbnails and source links
- **Retry on Error**: Automatic retry option when network requests fail

## Architecture

This app follows **Clean Architecture** with 3 layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  • Jetpack Compose UI                                        │
│  • ViewModels (StateFlow-based)                             │
│  • Navigation Component                                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  • Use Cases (single responsibility)                        │
│  • Repository Interfaces                                    │
│  • Domain Models (pure Kotlin)                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  • Repository Implementations                               │
│  • Remote Data Source (Retrofit)                            │
│  • Local Data Source (Room)                                 │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Rule

```
Presentation → Domain ← Data
```

- **Presentation** depends on **Domain** only
- **Data** depends on **Domain** only
- **Domain** is pure Kotlin with no Android dependencies

### Tech Stack

| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (KSP) |
| Navigation | Navigation Compose (type-safe) |
| Networking | Retrofit + kotlinx.serialization |
| Database | Room |
| Image Loading | Coil 3.x |
| Async | Kotlin Coroutines + Flow |

## Supported Date Formats

| Format | Example |
|--------|---------|
| `yyyy/MM/dd` | 1990/08/08 |
| `yyyy-MM-dd` | 1990-08-08 |

**Date Range**: June 16, 1995 to today

If no date is detected in the message, Nova returns today's APOD.

## Setup

### Prerequisites

- Android Studio Ladybug or later
- JDK 17+
- NASA API key (free at [api.nasa.gov](https://api.nasa.gov/))

### For Development

1. Clone the repository
2. Get a free NASA API key from [api.nasa.gov](https://api.nasa.gov/)
3. Create or update `local.properties` in the project root:
   ```
   NASA_API_KEY=your_api_key_here
   ```
4. Build and run: `./gradlew assembleDebug`

### For Code Reviewers

1. Clone the repository
2. Get a free NASA API key from [api.nasa.gov](https://api.nasa.gov/) (instant registration, no approval needed)
3. Create `local.properties` in the project root (if it doesn't exist)
4. Add: `NASA_API_KEY=your_api_key_here`
5. Sync Gradle and build

> **Note:** The NASA APOD API is free with a rate limit of 1000 requests/hour. You can also use `DEMO_KEY` as the API key for quick testing, but it has stricter limits (30 requests/day).

## Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest

# Lint check
./gradlew lint
```

## Project Structure

```
app/src/main/java/com/example/nasacosmosmessenger/
├── di/                     # Hilt DI modules
├── data/
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # Retrofit API, DTOs
│   ├── mapper/             # DTO ↔ Domain ↔ Entity mapping
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Pure Kotlin domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Use cases (single responsibility)
├── presentation/
│   ├── chat/               # Chat screen, ViewModel, components
│   ├── favorites/          # Favorites screen, ViewModel, components
│   ├── common/             # Shared UI components
│   ├── navigation/         # NavGraph and routes
│   └── util/               # BirthdayCardGenerator
└── ui/theme/               # Color, Typography, Theme
```

## Demo

[Screen recording to be added]

## License

This project was created for the LINE 2026 Android TechFresh Pre-Assessment.
