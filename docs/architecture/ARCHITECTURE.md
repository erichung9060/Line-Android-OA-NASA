# NASA Cosmos Messenger - Architecture Specification

> **Last Updated:** 2026-04-19  
> **Status:** Approved for Implementation

---

## 1. Executive Summary

**Product:** NASA Cosmos Messenger - An Android chat application featuring an AI assistant "Nova" that delivers NASA's Astronomy Picture of the Day (APOD) through conversational interface.

**Target:** LINE 2026 Android TechFresh Pre-Assessment

**Timeline:** 1 week

**Core Value Proposition:** Users interact with Nova via natural conversation. Nova responds with APOD content based on dates mentioned, allowing users to explore the cosmos through an intuitive chat experience.

---

## 2. Product Requirements

### 2.1 Feature Matrix

| Feature | Priority | Description |
|---------|----------|-------------|
| Chat Interface | P0 | Conversational UI with Nova AI assistant |
| Date Recognition | P0 | Parse multiple date formats from user input |
| APOD Integration | P0 | Fetch and display NASA APOD content |
| Favorites Collection | P0 | Save, browse, and delete favorite images |
| Offline Cache | P0 | Store viewed APOD data locally for offline access |
| Birthday Card Sharing | P0 | Generate shareable image card with APOD + date |
| Video Favorites Support | P0 | Save video APODs, show thumbnails, and open source links |
| Dark Mode | P0 | System-aware theme switching |

### 2.2 User Stories

**US-1: Chat with Nova**
> As a user, I can send messages to Nova and receive responses containing APOD images and descriptions.

**US-2: Date-based APOD Lookup**
> As a user, I can input a date (e.g., my birthday) and Nova will show me what the universe looked like on that day.

**US-2A: Default APOD Reply**
> As a user, when I send any message that does not contain a parseable date, Nova replies with today's APOD.

**US-3: Save Favorites**
> As a user, I can long-press any APOD message to save it to my favorites collection.

**US-4: Manage Favorites**
> As a user, I can browse my saved favorites in a grid view and delete items I no longer want.

**US-5: Offline Viewing**
> As a user, I can view previously loaded APOD content even without internet connection.

**US-6: Share Birthday Card**
> As a user, I can tap any APOD image or thumbnail in Nova or Favorites to generate a "Birthday Cosmos Card" and share it via system share sheet.

---

## 3. Architecture Overview

### 3.1 Architecture Pattern

**Clean Architecture (3-Layer)**

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  • Jetpack Compose UI                                        │
│  • ViewModels (StateFlow-based)                             │
│  • UI State (Immutable data classes)                        │
│  • Navigation                                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ Domain Models
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  • Use Cases (single responsibility)                        │
│  • Repository Interfaces                                    │
│  • Domain Models (pure Kotlin)                              │
│  • Business Logic                                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ Entity ↔ Domain Mapping
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  • Repository Implementations                               │
│  • Remote Data Source (Retrofit)                            │
│  • Local Data Source (Room)                                 │
│  • DTOs and Entities                                        │
│  • Mappers                                                  │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Design Principles

| Principle | Application |
|-----------|-------------|
| **Single Responsibility** | Each class has one reason to change |
| **Dependency Inversion** | Domain layer defines interfaces, Data layer implements |
| **DRY** | Shared logic extracted to utility classes |
| **YAGNI** | No speculative features; implement only what's needed |
| **Immutability** | UI State and Domain Models are immutable data classes |

### 3.3 Dependency Rule

```
Presentation → Domain ← Data
```

- **Presentation** knows about **Domain** only
- **Data** knows about **Domain** only
- **Domain** knows about nothing (pure Kotlin, no Android dependencies)

---

## 4. Package Structure

```
com.example.nasacosmosmessenger/
│
├── di/                              # Dependency Injection (Hilt modules)
│   ├── AppModule.kt                 # Application-scoped dependencies
│   ├── NetworkModule.kt             # Retrofit, OkHttp
│   └── DatabaseModule.kt            # Room database
│
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── NasaApodApi.kt       # Retrofit interface
│   │   ├── dto/
│   │   │   └── ApodResponse.kt      # API response DTO
│   │   └── interceptor/
│   │       └── ApiKeyInterceptor.kt # Inject API key
│   │
│   ├── local/
│   │   ├── database/
│   │   │   └── AppDatabase.kt       # Room database
│   │   ├── dao/
│   │   │   ├── ApodCacheDao.kt      # APOD cache DAO
│   │   │   ├── FavoriteDao.kt       # Favorites DAO
│   │   │   └── ChatMessageDao.kt    # Chat history DAO
│   │   └── entity/
│   │       ├── ApodCacheEntity.kt   # Cached APOD
│   │       ├── FavoriteEntity.kt    # Favorite item
│   │       └── ChatMessageEntity.kt # Persisted chat message
│   │
│   ├── mapper/
│   │   └── ApodMapper.kt            # DTO ↔ Domain ↔ Entity
│   │
│   └── repository/
│       ├── ApodRepositoryImpl.kt    # APOD data operations
│       ├── FavoriteRepositoryImpl.kt
│       └── ChatRepositoryImpl.kt    # Chat history operations
│
├── domain/
│   ├── model/
│   │   ├── Apod.kt                  # Domain model
│   │   ├── ChatMessage.kt           # Chat message model
│   │   └── Resource.kt              # Wrapper: Loading/Success/Error
│   │
│   ├── repository/
│   │   ├── ApodRepository.kt        # Interface
│   │   ├── FavoriteRepository.kt    # Interface
│   │   └── ChatRepository.kt        # Interface for chat persistence
│   │
│   └── usecase/
│       ├── GetTodayApodUseCase.kt
│       ├── GetApodByDateUseCase.kt
│       ├── ParseDateUseCase.kt
│       ├── ObserveChatHistoryUseCase.kt
│       ├── RestoreChatHistoryUseCase.kt
│       ├── SaveChatMessageUseCase.kt
│       ├── SaveFavoriteUseCase.kt
│       ├── GetFavoritesUseCase.kt
│       └── DeleteFavoriteUseCase.kt
│
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt              # Navigation graph
│   │   └── Screen.kt                # Route definitions
│   │
│   ├── chat/
│   │   ├── ChatScreen.kt            # Chat tab composable
│   │   ├── ChatViewModel.kt         # Chat state management
│   │   ├── ChatUiState.kt           # Immutable UI state
│   │   └── components/
│   │       ├── ChatBubble.kt        # Message bubble
│   │       ├── ApodContent.kt       # APOD display in bubble
│   │       ├── MessageInput.kt      # Text input bar
│   │       └── NovaAvatar.kt        # Nova icon
│   │
│   ├── favorites/
│   │   ├── FavoritesScreen.kt       # Favorites tab composable
│   │   ├── FavoritesViewModel.kt
│   │   ├── FavoritesUiState.kt
│   │   └── components/
│   │       ├── FavoriteCard.kt      # Grid item card
│   │       └── FavoriteOverflowMenu.kt
│   │
│   ├── common/
│   │   ├── LoadingIndicator.kt      # Shimmer / spinner
│   │   ├── ErrorState.kt            # Error UI
│   │   └── EmptyState.kt            # Empty list UI
│   │
│   ├── util/
│   │   └── BirthdayCardGenerator.kt # Android-specific (Context, Bitmap, Canvas)
│   │
│   └── MainScreen.kt                # Bottom navigation host
│
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt                     # Light/Dark theme
│   └── Type.kt
│
├── util/
│   ├── DateFormatter.kt             # Display formatting
│   └── ShareUtils.kt                # Image sharing helpers
│
└── CosmosMessengerApp.kt            # Application class (@HiltAndroidApp)
```

---

## 5. Technology Stack

### 5.1 Core Dependencies

| Category | Technology | Rationale |
|----------|------------|-----------|
| **Language** | Kotlin 1.9+ | Required |
| **Min SDK** | 26 (Android 8.0) | Pre-configured |
| **UI** | Jetpack Compose + Material 3 | Modern declarative UI |
| **Navigation** | Navigation Compose | Type-safe navigation |
| **DI** | Hilt | Google recommended, compile-time safe |
| **Network** | Retrofit + OkHttp | Industry standard |
| **Serialization** | Kotlinx Serialization | Kotlin-native, faster than Gson |
| **Image Loading** | Coil | Kotlin-first, Compose integration |
| **Database** | Room | Offline cache + favorites |
| **Async** | Coroutines + Flow | Modern async patterns |

### 5.2 Testing Dependencies

| Category | Technology |
|----------|------------|
| **Unit Test** | JUnit 5 + Mockk |
| **Coroutine Test** | kotlinx-coroutines-test |
| **UI Test** | Compose Testing |

---

## 6. Data Models

### 6.1 Domain Models

**Apod** (Core domain entity)
```
Apod {
  date: LocalDate           // APOD date (unique identifier)
  title: String             // Image title
  explanation: String       // Description text
  url: String               // Standard resolution URL
  hdUrl: String?            // High-definition URL (nullable)
  mediaType: MediaType      // IMAGE or VIDEO
  thumbnailUrl: String?     // Derived or remote thumbnail for video preview
  copyright: String?        // Copyright holder (nullable)
}

enum MediaType { IMAGE, VIDEO }
```

**ChatMessage** (Chat conversation)
```
ChatMessage {
  id: String                // UUID
  content: String           // Text content
  apod: Apod?               // Attached APOD (nullable)
  isFromUser: Boolean       // true = user, false = Nova
  timestamp: Instant        // Message time
}
```

**Resource<T>** (Async state wrapper)
```
sealed Resource<T> {
  Loading
  Success(data: T)
  Error(message: String, cause: Throwable?)
}
```

### 6.2 API Contract

**Endpoint:** `GET https://api.nasa.gov/planetary/apod`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| api_key | String | Yes | NASA API key or "DEMO_KEY" |
| date | String | No | YYYY-MM-DD format (defaults to today) |

**Response (200 OK):**
```json
{
  "date": "2024-01-15",
  "title": "Orion Nebula",
  "explanation": "The great nebula...",
  "url": "https://apod.nasa.gov/apod/image/...",
  "hdurl": "https://apod.nasa.gov/apod/image/.../hd.jpg",
  "media_type": "image",
  "copyright": "NASA"
}
```

**Error Response (4xx/5xx):**
```json
{
  "error": {
    "code": "API_KEY_INVALID",
    "message": "Invalid API key"
  }
}
```

**Constraints:**
- APOD available from **1995-06-16** to **today**
- DEMO_KEY note: follow the assessment constraint of **30 requests/day**
- Some days have `media_type: "video"` (YouTube embeds)

### 6.3 Database Schema

**Table: apod_cache**
```
apod_cache {
  date: TEXT PRIMARY KEY    -- YYYY-MM-DD
  title: TEXT NOT NULL
  explanation: TEXT NOT NULL
  url: TEXT NOT NULL
  hd_url: TEXT              -- nullable
  media_type: TEXT NOT NULL -- "image" or "video"
  thumbnail_url: TEXT       -- nullable, used for video preview
  copyright: TEXT           -- nullable
  cached_at: INTEGER        -- Unix timestamp
}
```

**Table: favorites**
```
favorites {
  date: TEXT PRIMARY KEY    -- YYYY-MM-DD (unique constraint)
  title: TEXT NOT NULL
  explanation: TEXT NOT NULL
  url: TEXT NOT NULL
  hd_url: TEXT
  media_type: TEXT NOT NULL
  thumbnail_url: TEXT
  copyright: TEXT
  saved_at: INTEGER         -- Unix timestamp for sorting
}
```

**Table: chat_messages**
```
chat_messages {
  id: TEXT PRIMARY KEY      -- UUID
  content: TEXT NOT NULL    -- Message text content
  apod_date: TEXT           -- FK to APOD date (nullable, only for Nova responses with APOD)
  is_from_user: INTEGER     -- 1 = user, 0 = Nova
  timestamp: INTEGER        -- Unix timestamp for ordering
}
```

> **Note:** Chat messages are persisted to enable offline browsing of conversation history. The `apod_date` field links to cached APOD data for Nova responses that include an image.

---

## 7. Component Specifications

### 7.1 Chat Screen

**Layout Structure:**
```
┌─────────────────────────────────────┐
│ [Nova Icon]  Nova                   │  ← TopAppBar (optional)
├─────────────────────────────────────┤
│                                     │
│  ┌────────────────────┐             │  ← Nova message (left)
│  │ 🤖 Welcome! Enter  │             │
│  │ a date to explore  │             │
│  └────────────────────┘             │
│                                     │
│             ┌────────────────────┐  │  ← User message (right)
│             │ 1990/08/08         │  │
│             └────────────────────┘  │
│                                     │
│  ┌────────────────────┐             │  ← Nova APOD response
│  │ 🤖 On that day...  │             │
│  │ ┌────────────────┐ │             │
│  │ │   [APOD IMG]   │ │             │
│  │ └────────────────┘ │             │
│  │ Title: Orion...    │             │
│  └────────────────────┘             │
│                                     │
├─────────────────────────────────────┤
│ [📷] [Message input...    ] [Send]  │  ← Input bar
└─────────────────────────────────────┘
```

**Behaviors:**
- Message list smoothly scrolls to bottom after user send and after Nova reply
- Long-press on APOD message shows "Add to Favorites" option
- Loading state: Nova "typing" indicator
- Error state: Nova apologizes with error reason

### 7.2 Favorites Screen

**Layout Structure:**
```
┌─────────────────────────────────────┐
│ Favorites                          │  ← TopAppBar
├─────────────────────────────────────┤
│ ┌─────────┐  ┌─────────┐           │
│ │ [IMG]   │  │ [IMG]   │           │  ← 2-column grid
│ │ ────────│  │ ────────│           │
│ │ Title   │  │ Title   │           │
│ │ Date    │  │ Date    │           │
│ └─────────┘  └─────────┘           │
│                                     │
│ ┌─────────┐  ┌─────────┐           │
│ │ [IMG]   │  │ [IMG]   │           │
│ │ ────────│  │ ────────│           │
│ │ Title   │  │ Title   │           │
│ │ Date    │  │ Date    │           │
│ └─────────┘  └─────────┘           │
│                                     │
│         [Empty State]               │  ← When no favorites
│    "No favorites yet!"              │
│                                     │
└─────────────────────────────────────┘
```

**Behaviors:**
- Tap APOD image/thumbnail: Generate and share birthday card immediately
- Card overflow menu (top-right): Delete favorite; open source link for video items
- Video cards show thumbnail preview and play/link indicator
- Empty state shown when list is empty
- Cards show star indicator

### 7.3 Bottom Navigation

```
┌───────────────────────────────────────┐
│     [🚀 Nova]      │    [⭐ Favorites] │
└───────────────────────────────────────┘
```

- Two pages: Nova (chat) and Favorites (collection)
- Navigation remains bottom navigation even though the product language says "2 tabs"
- Preserve state when switching tabs

---

## 8. Data Flow

### 8.1 Chat Flow (Message Routing)

```
User Input: "Show me 1990/08/08"
       │
       ▼
┌──────────────────┐
│ ChatViewModel    │
│ onSendMessage()  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     ┌──────────────────┐
│ ParseDateUseCase │────▶│ Extracts LocalDate│
└────────┬─────────┘     │ or null           │
         │               └──────────────────┘
         │
         ├── valid date ─────────────▶ GetApodByDateUseCase(date)
         │
         └── no parseable date ──────▶ GetTodayApodUseCase()
                                          │
                                          ▼
                                ┌──────────────────┐
                                │ ApodRepository   │
                                │ getApod(date)    │
                                │ 1. Check cache   │
                                │ 2. If miss, fetch│
                                │ 3. Save to cache │
                                └────────┬─────────┘
                                         │
                                         ▼
                                ┌──────────────────┐
                                │ ChatViewModel    │
                                │ Add Nova message │
                                │ with Apod data   │
                                └──────────────────┘
```

### 8.2 Offline Cache Strategy

**Offline-First APOD Cache + Persisted Chat History**

**APOD Cache Flow**

1. **Query**: Check Room cache for requested date
2. **Cache Hit**: Return cached data immediately
3. **Cache Miss**: Fetch from network
4. **Success**: Save to cache and return data
5. **Cache Miss + Network Fail**: Return error

```
APOD Request(date)
     │
     ├─── Cache Hit ───▶ Return cached ──▶ UI displays
     │
     └─── Cache Miss ──▶ Fetch network
                              │
                              ├─── Success ──▶ Cache + Return
                              │
                              └─── Fail ────▶ Return Error
```

**Chat History Persistence Flow**

1. Persist every user message immediately after send
2. Persist every Nova response after APOD result is resolved
3. Store `apod_date` on Nova messages that include APOD content
4. Rehydrate chat history on app launch by joining `chat_messages` with cached APOD data
5. If a historic Nova message references an APOD missing from cache, show the text message and mark APOD attachment unavailable instead of dropping the message

```
App Launch
    │
    ▼
┌──────────────────────────┐
│ RestoreChatHistoryUseCase│
│ / ObserveChatHistory     │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│ ChatRepository           │
│ getMessagesWithApod()    │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│ Room                     │
│ chat_messages LEFT JOIN  │
│ apod_cache ON apod_date  │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│ ChatViewModel            │
│ rebuilds message list    │
│ before user interaction  │
└──────────────────────────┘
```

### 8.3 Favorite Flow

```
Long Press on APOD Message
         │
         ▼
┌──────────────────┐
│ Show Context Menu│
│ "Add to Favorites│
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│SaveFavoriteCase  │
│ execute(apod)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│FavoriteRepository│
│ insert(favorite) │  ──▶ Room INSERT OR REPLACE
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Show Toast/      │
│ Snackbar confirm │
└──────────────────┘
```

### 8.4 Message Persistence Flow

```
User taps Send
        │
        ▼
┌──────────────────┐
│ ChatViewModel    │
│ add user bubble  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│SaveChatMessage   │
│UseCase(user)     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ ChatRepository   │
│ insert(message)  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Resolve APOD     │
│ valid date /     │
│ today fallback   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│SaveChatMessage   │
│UseCase(nova)     │
│ + optional apod  │
└──────────────────┘
```

### 8.5 Share Card Flow

```
User taps APOD image or thumbnail
         │
         ▼
┌────────────────────────┐
│ BirthdayCardGenerator  │
│ build(apod)            │
└───────────┬────────────┘
            │
            ├── image APOD ─────────▶ Use image URL/bitmap
            │
            └── video APOD ─────────▶ Derive YouTube thumbnail from URL
                                      │
                                      └── non-YouTube fallback: text-only card
            │
            ▼
┌────────────────────────┐
│ ShareUtils             │
│ shareGeneratedCard()   │
└────────────────────────┘
```

---

## 9. Date Parsing Specification

### 9.1 Supported Formats

| Format | Example | Priority |
|--------|---------|----------|
| `yyyy/MM/dd` | 1990/08/08 | P0 (required) |
| `yyyy-MM-dd` | 1990-08-08 | P0 (required) |

### 9.2 Validation Rules

1. **Date Range**: 1995-06-16 to today (inclusive)
2. **Message Routing**: If any supported date can be extracted from the message, fetch that date's APOD
3. **Default Fallback**: If no supported date can be extracted, fetch today's APOD
4. **Simplified Parsing**: Chat routing only distinguishes between "valid date" and "no parseable date"
5. **Out-of-Range Input**: Inputs outside the APOD range are treated as "no parseable date" in the current phase

### 9.3 Error Messages (Nova Responses)

| Scenario | Nova Response |
|----------|---------------|
| No date found | Returns today's APOD |
| API error | "Oops! I couldn't reach NASA right now. Please try again." |
| Video type | Shows thumbnail preview and source link |

---

## 10. Birthday Card Generation

### 10.1 Card Layout

```
┌─────────────────────────────────────┐
│                                     │
│         [APOD IMAGE]                │
│         (cropped square)            │
│                                     │
├─────────────────────────────────────┤
│  🌌 Your Birthday Cosmos            │
│                                     │
│  On [DATE], the universe            │
│  looked like this:                  │
│                                     │
│  "[APOD TITLE]"                     │
│                                     │
│  ─────────────────────────          │
│  NASA Cosmos Messenger              │
└─────────────────────────────────────┘
```

### 10.2 Generation Process

1. Load APOD image bitmap from Coil cache
2. If APOD is video, derive a YouTube thumbnail from the video URL; for non-YouTube videos, use a text-only fallback card
3. Create Canvas with card dimensions (1080x1350 recommended)
4. Draw image or video thumbnail (center crop) when available
5. Draw text overlay with gradient background
6. Save to app cache directory
7. Create FileProvider URI
8. Launch system share intent

---

## 11. Error Handling Strategy

### 11.1 Error Categories

| Category | Example | Handling |
|----------|---------|----------|
| **Network** | No internet, timeout | Show cached data if available; else error UI |
| **API** | Rate limit, invalid key | Show error message with retry option |
| **Media** | Missing video thumbnail | Fall back to text-only share card; keep source link available |
| **Database** | Room exception | Log error, show generic message |

### 11.2 UI Error States

- **Inline Error**: For chat messages (Nova apologizes)
- **Full Screen Error**: For favorites when no data
- **Snackbar**: For non-critical errors (save failed)

---

## 12. Testing Strategy

### 12.1 Test Coverage Requirements

| Layer | Type | Coverage Target |
|-------|------|-----------------|
| Domain | Unit Tests | UseCases, DateParser |
| Data | Unit Tests | Mappers, Repository logic |
| Presentation | UI Tests | Critical user flows |

### 12.2 Key Test Cases

**DateParser Tests:**
- Parse all supported formats
- Treat non-parseable input as fallback to today's APOD
- Treat out-of-range input as fallback to today's APOD

**Repository Tests:**
- Cache hit returns local data
- Cache miss fetches from network
- Network failure with cache returns cached data
- Network failure without cache returns error
- Video URL mapping derives expected YouTube thumbnail
- Non-YouTube video falls back without crash

**ViewModel Tests:**
- Initial state is correct
- Send message updates UI state
- Error handling updates error state
- Non-date message returns today's APOD
- Message containing date text routes to date-based APOD

---

## 13. Coding Conventions

### 13.1 Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Package | lowercase | `com.example.feature` |
| Class | PascalCase | `ChatViewModel` |
| Function | camelCase | `getApodByDate()` |
| Constant | SCREAMING_SNAKE | `MAX_CACHE_SIZE` |
| Composable | PascalCase | `ChatBubble()` |

### 13.2 Code Organization

- One class per file (except sealed classes)
- Related extensions in `*Extensions.kt` files
- Constants in companion objects or top-level

### 13.3 Compose Guidelines

- State hoisting: State owned by ViewModel, UI is stateless
- Preview functions for all reusable components
- Modifier as first optional parameter

### 13.4 Commit Message Format

```
type(scope): description

[optional body]
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

Examples:
- `feat(chat): add message bubble component`
- `fix(api): handle video media type correctly`
- `test(parser): add date format edge cases`

---

## 14. Implementation Phases

### Phase 1: Foundation (Day 1)
- Project setup with all dependencies
- Hilt configuration
- Theme setup (light/dark)
- Navigation structure

### Phase 2: Data Layer (Day 2)
- NASA API integration
- Room database setup
- Repository implementations

### Phase 3: Chat Feature (Day 3)
- Chat UI components
- Date parsing logic
- Nova response logic
- Auto-scroll behavior

### Phase 4: Favorites Feature (Day 4)
- Favorites UI (grid)
- Save/delete operations
- Overflow menu actions

### Phase 5: Polish (Day 5)
- Offline caching
- Birthday card sharing
- Video thumbnail derivation/fallback
- Dark mode polish
- Error handling
- Loading states

### Phase 6: QA (Day 6)
- Bug fixes
- Edge case verification
- Reviewer-path polish

### Phase 7: Documentation (Day 7)
- README completion
- Screen recording
- Final review

---

## 15. Deliverables Checklist

### Code
- [ ] Clean 3-layer architecture implemented
- [ ] All P0 features functional
- [ ] Offline caching working
- [ ] Birthday card sharing working
- [ ] Video favorites with thumbnails and source-link actions working
- [ ] Dark mode support
- [ ] No compiler warnings
- [ ] No lint errors

### Documentation
- [ ] README with architecture explanation
- [ ] README with date format list
- [ ] README with bonus features description
- [ ] Technical decisions documented

### Submission
- [ ] GitHub repository with full commit history
- [ ] Screen recording demonstrating all features
- [ ] Atomic, descriptive commit messages

---

## Appendix A: API Key Configuration

Store API key in `local.properties` (git-ignored):

```properties
NASA_API_KEY=your_key_here
```

Read in `build.gradle.kts`:

```kotlin
buildConfigField("String", "NASA_API_KEY", "\"${properties["NASA_API_KEY"]}\"")
```

For submission, document that reviewers should add their own key.

---

## Appendix B: Resource Files

**Strings to define:**
- `app_name`: "Cosmos Messenger"
- `tab_nova`: "Nova"
- `tab_favorites`: "Favorites"
- `nova_greeting`: "Hi! I'm Nova. Enter a date and I'll show you the cosmos!"
- `error_network`: "Couldn't connect to NASA. Check your internet."
- `favorites_empty`: "No favorites yet. Long-press any image to save it!"
- `share_card_title`: "Share Birthday Cosmos"

---

**End of Architecture Specification**
