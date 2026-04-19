# NASA Cosmos Messenger - Implementation Roadmap

> **Last Updated:** 2026-04-19  
> **Reference:** [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## Overview

This document defines the phased implementation plan for NASA Cosmos Messenger. It follows the approved architecture specification and the Android pre-assessment requirements. When this roadmap and implementation details conflict, **the architecture specification is the source of truth**.

### Phase Summary

| Phase | Name | Duration | Focus |
|-------|------|----------|-------|
| 1 | Foundation | Day 1 | Project setup, DI, theme, navigation shell |
| 2 | Data & Nova Experience | Day 2-3 | APOD API, Room cache, domain use cases, chat UI, date parsing, chat persistence |
| 3 | Favorites & Sharing | Day 4-5 | Favorites grid, video support, birthday card sharing |
| 4 | Polish & QA | Day 6-7 | Error handling, dark mode polish, testing, docs, demo |

---

## Phase 1 — Foundation

**Goal:** Establish project infrastructure and application shell for all subsequent features.

**Duration:** Day 1

### 1.1 Task Breakdown

| # | Task | Priority | Deliverable |
|---|------|----------|-------------|
| 1.1.1 | Project dependency setup | P0 | `build.gradle.kts` configured |
| 1.1.2 | Hilt application wiring | P0 | `CosmosMessengerApp.kt`, DI modules |
| 1.1.3 | Theme setup | P0 | `Theme.kt`, `Color.kt`, `Type.kt` |
| 1.1.4 | Navigation structure | P0 | `NavGraph.kt`, `Screen.kt` |
| 1.1.5 | Main shell with bottom navigation | P0 | `MainScreen.kt` with Nova / Favorites |
| 1.1.6 | Placeholder feature screens | P0 | `ChatScreen.kt`, `FavoritesScreen.kt` |

### 1.2 Dependencies

```kotlin
dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Image Loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Unit testing
    // Unit testing (aligned with architecture spec: JUnit 5 + Mockk)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    // Android / UI testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
}
```

### 1.3 Key Implementation Notes

- Use the approved 3-layer Clean Architecture structure.
- Bottom navigation remains the primary navigation model for the two required pages.
- Preserve state when switching tabs.
- Dark mode support is included from the start through the shared theme layer.

### 1.4 Acceptance Criteria

- [ ] App launches into a shell with two destinations: Nova and Favorites
- [ ] Hilt is initialized correctly
- [ ] Navigation works between both bottom navigation destinations
- [ ] Theme supports light and dark mode

---

## Phase 2 — Data & Nova Experience

**Goal:** Deliver the core Nova chat flow with APOD integration, date routing, offline cache, chat persistence, and the required domain use cases defined by the architecture spec.

**Duration:** Day 2-3

### 2.1 Task Breakdown

| # | Task | Priority | Deliverable |
|---|------|----------|-------------|
| 2.1.1 | NASA APOD API integration | P0 | `NasaApodApi.kt`, `ApodResponse.kt` |
| 2.1.2 | API key injection | P0 | `ApiKeyInterceptor.kt` |
| 2.1.3 | APOD Room cache | P0 | `AppDatabase.kt`, `ApodCacheDao.kt`, `ApodCacheEntity.kt` |
| 2.1.4 | APOD mapper | P0 | `ApodMapper.kt` |
| 2.1.5 | APOD repository | P0 | `ApodRepository.kt`, `ApodRepositoryImpl.kt` |
| 2.1.6 | APOD retrieval use cases | P0 | `GetTodayApodUseCase.kt`, `GetApodByDateUseCase.kt` |
| 2.1.7 | Date parsing use case | P0 | `ParseDateUseCase.kt` |
| 2.1.8 | Chat persistence layer | P0 | `ChatMessageDao.kt`, `ChatMessageEntity.kt`, `ChatRepository.kt`, `ChatRepositoryImpl.kt` |
| 2.1.9 | Chat persistence use cases | P0 | `ObserveChatHistoryUseCase.kt`, `RestoreChatHistoryUseCase.kt`, `SaveChatMessageUseCase.kt` |
| 2.1.10 | Chat UI components | P0 | `ChatScreen.kt`, `ChatBubble.kt`, `ApodContent.kt`, `MessageInput.kt`, `NovaAvatar.kt` |
| 2.1.11 | Chat state management | P0 | `ChatViewModel.kt`, `ChatUiState.kt` |
| 2.1.12 | Loading and error UI | P0 | `LoadingIndicator.kt`, `ErrorState.kt` |

### 2.2 Date Parsing Rules

Supported formats:

- `yyyy/MM/dd`
- `yyyy-MM-dd`

Routing rules:

1. If a supported date is found and it is within the APOD range, fetch that date's APOD.
2. If no supported date is found, fetch today's APOD.
3. If the parsed date is outside the APOD range, treat it as **no parseable date** for routing and fall back to today's APOD.

Implementation note:

- `ChatViewModel` must route through `ParseDateUseCase`, `GetApodByDateUseCase`, and `GetTodayApodUseCase` rather than calling the repository directly, matching the approved domain-layer data flow.

### 2.3 APOD Fetch and Cache Strategy

1. Check `apod_cache` for the requested date.
2. Return cached content immediately on hit.
3. Fetch from network on miss.
4. Persist successful responses into `apod_cache`.
5. If offline and cached content exists, return cached content.
6. If offline and no cached content exists, return an error state.

### 2.4 Chat Persistence Strategy

1. Persist each user message immediately after send.
2. Persist each Nova response after APOD resolution completes.
3. Store `apod_date` for Nova responses that include APOD content.
4. Restore previous messages on app launch.
5. Rehydrate Nova APOD messages from `apod_cache`.
6. If a historical Nova message references an APOD no longer present in cache, keep the text message and mark the attachment unavailable instead of dropping the message.

### 2.5 Key Data Structures

```kotlin
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val apodDate: String?,
    val isFromUser: Boolean,
    val timestamp: Long
)
```

```kotlin
interface ChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun clearHistory()
}
```

### 2.6 Acceptance Criteria

- [ ] User can send messages in the Nova chat
- [ ] Nova shows a greeting on first launch
- [ ] User messages align right; Nova messages align left
- [ ] `1990/08/08` routes to APOD-by-date
- [ ] `1990-08-08` routes to APOD-by-date
- [ ] Messages without a parseable date return today's APOD
- [ ] Out-of-range dates fall back to today's APOD in routing
- [ ] Nova APOD responses include title, date, explanation, and image content when `media_type == image`
- [ ] Video APOD responses display a safe link-first presentation, with thumbnail handling only when available
- [ ] Message list auto-scrolls smoothly after user send and after Nova reply
- [ ] Loading state displays a Nova typing indicator
- [ ] Error state renders as a Nova-side failure message with retry-friendly wording
- [ ] APOD responses are cached in Room
- [ ] Cached APOD data can be shown offline for previously viewed dates
- [ ] Chat history is persisted locally
- [ ] Chat history is restored on app restart
- [ ] Offline browsing of previous chat messages works

### 2.7 Testing Tasks

| # | Test Category | Test Cases | Deliverable |
|---|---------------|------------|-------------|
| 2.7.1 | Date parser unit tests | Supported formats, no-date routing, out-of-range fallback | `DateParserTest.kt` |
| 2.7.2 | APOD use case tests | Today/date routing behavior and repository delegation | `GetApodUseCaseTest.kt` |
| 2.7.3 | APOD repository tests | Cache hit/miss, network failure, offline fallback | `ApodRepositoryTest.kt` |
| 2.7.4 | APOD mapper tests | DTO/domain/entity conversions | `ApodMapperTest.kt` |
| 2.7.5 | Chat ViewModel tests | Send flow, loading, routing, restore flow, error handling | `ChatViewModelTest.kt` |
| 2.7.6 | Chat DAO tests | Insert, order, query, clear | `ChatMessageDaoTest.kt` |
| 2.7.7 | Chat repository tests | Persistence, rehydration, restart survival | `ChatRepositoryTest.kt` |
| 2.7.8 | Chat Compose UI tests | Critical send/scroll/render flows | `ChatScreenTest.kt` |

Representative required cases:

```kotlin
class DateParserTest {
    @Test fun `parse valid yyyy-MM-dd returns date`()
    @Test fun `parse valid yyyy_MM_dd returns date`()
    @Test fun `message without supported date returns no date detected`()
    @Test fun `date before APOD start is treated as no parseable date for routing`()
    @Test fun `future date is treated as no parseable date for routing`()
}
```

```kotlin
class GetApodUseCaseTest {
    @Test fun `get today use case delegates to repository without explicit date`()
    @Test fun `get by date use case delegates to repository with requested date`()
}
```

```kotlin
class ApodRepositoryTest {
    @Test fun `cache hit returns cached data without network call`()
    @Test fun `cache miss fetches network and caches result`()
    @Test fun `network failure with cache returns cached data`()
    @Test fun `network failure without cache returns error`()
    @Test fun `video URL mapping derives expected thumbnail when supported`()
    @Test fun `non-YouTube video falls back safely without crash`()
}
```

---

## Phase 3 — Favorites & Sharing

**Goal:** Complete favorites management, video favorites support, and birthday card sharing.

**Duration:** Day 4-5

### 3.1 Task Breakdown

| # | Task | Priority | Deliverable |
|---|------|----------|-------------|
| 3.1.1 | Favorites Room table | P0 | `FavoriteDao.kt`, `FavoriteEntity.kt` |
| 3.1.2 | Favorites repository | P0 | `FavoriteRepository.kt`, `FavoriteRepositoryImpl.kt` |
| 3.1.3 | Favorites use cases | P0 | `SaveFavoriteUseCase.kt`, `GetFavoritesUseCase.kt`, `DeleteFavoriteUseCase.kt` |
| 3.1.4 | Long-press add-to-favorites flow | P0 | Context menu in chat |
| 3.1.5 | Favorites screen and state | P0 | `FavoritesScreen.kt`, `FavoritesViewModel.kt`, `FavoritesUiState.kt` |
| 3.1.6 | Favorite card UI | P0 | `FavoriteCard.kt`, `FavoriteOverflowMenu.kt` |
| 3.1.7 | Empty state UI | P0 | `EmptyState.kt` |
| 3.1.8 | Video favorites support | P0 | Thumbnail preview, play/link indicator, source-link action |
| 3.1.9 | Birthday card generator | P0 | `BirthdayCardGenerator.kt` |
| 3.1.10 | Share utilities and FileProvider | P0 | `ShareUtils.kt`, `file_paths.xml` |

### 3.2 Favorites Schema

```kotlin
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val hdUrl: String?,
    val mediaType: String,
    val thumbnailUrl: String?,
    val copyright: String?,
    val savedAt: Long
)
```

### 3.3 Interaction Rules

- Long-press on an APOD chat message shows an action to add it to favorites.
- Tapping an APOD image or thumbnail in Nova or Favorites generates and shares a birthday card immediately.
- Favorites cards show thumbnail preview for video entries.
- Favorites overflow menu provides delete and open-source-link actions for video entries.
- Duplicate favorites overwrite by `date` and do not create duplicates.

### 3.4 Birthday Card Implementation Notes

- `BirthdayCardGenerator` remains in `presentation/util/` because it depends on Android APIs.
- For image APODs, load the image bitmap and render it into the generated card.
- For video APODs, derive a YouTube thumbnail when possible.
- For non-YouTube videos, generate a text-only fallback card and preserve the source link separately.

### 3.5 Acceptance Criteria

- [ ] Long-press on APOD message exposes add-to-favorites action
- [ ] Favorite save succeeds and shows confirmation feedback
- [ ] Favorites tab shows a 2-column grid of saved items
- [ ] Favorites show image or thumbnail, title, date, and star indicator
- [ ] Tapping an APOD image or thumbnail in Nova generates and opens share flow immediately
- [ ] Tapping an APOD image or thumbnail in Favorites generates and opens share flow immediately
- [ ] Video favorites show thumbnail preview and play/link indicator
- [ ] Video favorite overflow menu can open the source link
- [ ] Favorite delete works correctly
- [ ] Empty state is shown when no favorites exist
- [ ] Previously saved favorites remain accessible offline

### 3.6 Testing Tasks

| # | Test Category | Test Cases | Deliverable |
|---|---------------|------------|-------------|
| 3.6.1 | Favorites DAO tests | Insert, replace, delete, sort, existence checks | `FavoriteDaoTest.kt` |
| 3.6.2 | Favorites repository tests | Save, observe, delete, video metadata mapping | `FavoriteRepositoryTest.kt` |
| 3.6.3 | Favorites ViewModel tests | Load state, delete flow, UI state updates | `FavoritesViewModelTest.kt` |
| 3.6.4 | Share/card tests | Video fallback, generated-card contract | `BirthdayCardGeneratorTest.kt` |
| 3.6.5 | Favorites Compose UI tests | Grid rendering, empty state, overflow actions | `FavoritesScreenTest.kt` |

Representative required cases:

```kotlin
class FavoriteDaoTest {
    @Test fun `insert favorite and retrieve returns same data`()
    @Test fun `insert duplicate date replaces existing entry`()
    @Test fun `delete by date removes entry`()
    @Test fun `getAllFavorites returns items sorted by savedAt desc`()
    @Test fun `video favorite preserves thumbnail and media type`()
}
```

---

## Phase 4 — Polish & QA

**Goal:** Finalize production-readiness, testing coverage, and submission assets.

**Duration:** Day 6-7

### 4.1 Task Breakdown

| # | Task | Priority | Deliverable |
|---|------|----------|-------------|
| 4.1.1 | Error handling polish | P0 | Inline, fullscreen, snackbar states aligned with spec |
| 4.1.2 | Dark mode polish | P0 | System-aware visuals verified |
| 4.1.3 | Accessibility pass | P1 | Content descriptions, touch targets, contrast review |
| 4.1.4 | Compose UI test completion | P0 | Critical flow tests passing |
| 4.1.5 | README completion | P0 | Architecture, supported formats, completed bonus features |
| 4.1.6 | Screen recording | P0 | Demo of date recognition, favorites, bonus features |
| 4.1.7 | Final code review and cleanup | P0 | No compiler warnings, no lint errors |
| 4.1.8 | GitHub submission prep | P0 | Repository ready with full history |

### 4.2 Error Handling Alignment

- Network errors: show cached APOD if available, otherwise error state.
- API errors: show retryable message.
- Media errors: fall back to text-only share card when thumbnail generation is unavailable.
- Database errors: log and show generic failure feedback.

### 4.3 Documentation Requirements

README must include:

- Architecture explanation and rationale
- Supported date formats
- Completed bonus features

Submission package must include:

- GitHub repository with commit history
- Screen recording

### 4.4 Final Acceptance Criteria

- [ ] Clean 3-layer architecture is implemented
- [ ] Presentation layer depends on domain only, data layer depends on domain only, and APOD retrieval flows through dedicated domain use cases
- [ ] All P0 features from the architecture spec are complete
- [ ] Offline cache works for previously viewed APOD data
- [ ] Birthday card sharing works from Nova and Favorites image/thumbnail taps
- [ ] Video favorites support works with thumbnails and source-link actions
- [ ] Dark mode works correctly
- [ ] Critical tests pass
- [ ] README and demo assets are ready for submission

---

## Appendix A: Complete File Checklist

```
app/src/main/java/com/example/nasacosmosmessenger/
│
├── CosmosMessengerApp.kt
│
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
│
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── NasaApodApi.kt
│   │   ├── dto/
│   │   │   └── ApodResponse.kt
│   │   └── interceptor/
│   │       └── ApiKeyInterceptor.kt
│   │
│   ├── local/
│   │   ├── database/
│   │   │   └── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── ApodCacheDao.kt
│   │   │   ├── FavoriteDao.kt
│   │   │   └── ChatMessageDao.kt
│   │   └── entity/
│   │       ├── ApodCacheEntity.kt
│   │       ├── FavoriteEntity.kt
│   │       └── ChatMessageEntity.kt
│   │
│   ├── mapper/
│   │   └── ApodMapper.kt
│   │
│   └── repository/
│       ├── ApodRepositoryImpl.kt
│       ├── FavoriteRepositoryImpl.kt
│       └── ChatRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Apod.kt
│   │   ├── ChatMessage.kt
│   │   └── Resource.kt
│   │
│   ├── repository/
│   │   ├── ApodRepository.kt
│   │   ├── FavoriteRepository.kt
│   │   └── ChatRepository.kt
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
│   ├── MainActivity.kt
│   ├── MainScreen.kt
│   │
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   │
│   ├── chat/
│   │   ├── ChatScreen.kt
│   │   ├── ChatViewModel.kt
│   │   ├── ChatUiState.kt
│   │   └── components/
│   │       ├── ChatBubble.kt
│   │       ├── ApodContent.kt
│   │       ├── MessageInput.kt
│   │       └── NovaAvatar.kt
│   │
│   ├── favorites/
│   │   ├── FavoritesScreen.kt
│   │   ├── FavoritesViewModel.kt
│   │   ├── FavoritesUiState.kt
│   │   └── components/
│   │       ├── FavoriteCard.kt
│   │       └── FavoriteOverflowMenu.kt
│   │
│   ├── common/
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorState.kt
│   │   └── EmptyState.kt
│   │
│   └── util/
│       └── BirthdayCardGenerator.kt
│
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
│
└── util/
    ├── DateFormatter.kt
    └── ShareUtils.kt

app/src/main/res/
└── xml/
    └── file_paths.xml
```

---

## Appendix B: API Key Configuration

Store the NASA API key in `local.properties`:

```properties
NASA_API_KEY=your_key_here
```

Expose it through Gradle:

```kotlin
buildConfigField("String", "NASA_API_KEY", "\"${properties["NASA_API_KEY"]}\"")
```

For reviewer setup, document that a valid API key is required and that `DEMO_KEY` has request limits.

---

## Appendix C: README Checklist

- Architecture explanation and rationale
- Supported date formats list
- Completed bonus features
- Reviewer setup instructions
- Link to screen recording
