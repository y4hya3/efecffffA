# GripForge - Grip Hand & Forearm Strength Tracker

A high-performance Android workout tracker engineered for grip strength athletes, climbers, arm wrestlers, and fitness enthusiasts to log series, sets, resistance levels, and repetitions with bilateral (Left vs. Right hand) analytics, interactive rest timing, and an activity calendar heatmap.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your selections in Phase 1, here are the core design and functional specifications aligned for GripForge:

- **Bilateral Resistance & Reps Tracking**: Each set records resistance (kg/lbs or gripper level/rating e.g. 40kg, 60kg, CoC #1) alongside repetition count, with one-tap toggle for Left Hand vs. Right Hand.
- **Visual Aesthetic & Styling**: Athletic Dark Mode utilizing deep carbon/titanium backgrounds (`#101317`), vibrant neon ember (`#FF5722` / `#FF7043`) for energy, electric cyan accents for right-hand vs. left-hand contrast, and high-legibility athletic typography.
- **Active Rest Timer**: Integrated countdown stopwatch with quick presets (30s, 60s, 90s, 120s) and vibration feedback between intense grip sets.
- **Persistence Architecture**: Fast and resilient on-device Room SQLite database reactive queries (`Flow`) ensuring seamless offline logging and instant state recovery without mandatory logins.

---

## 1. Overview & Core Concept

### What It Does
GripForge enables grip athletes to structure their grip sessions into **Series** (e.g., "Heavy Pinch Series", "Captains of Crush Warmup", "Crush Grip Pyramid") containing individual **Sets**. Users seamlessly dial in repetitions with instant `+`/`-` stepper buttons or direct numeric input, specify Left or Right hand, drag to reorder sets, and launch rest intervals. An **Analytics Dashboard** displays bilateral personal records (PRs), peak repetition days, interactive Weekly / Monthly / Yearly progression graphs, and a colored calendar heatmap.

### Target Audience
Grip strength trainers, rock climbers, bouldering athletes, powerlifters, arm wrestlers, and rehabilitation patients seeking to track bilateral grip symmetry, progressive overload, and training consistency.

### Key Value
- **Zero-Friction Logging**: Rapid rep adjustments, fast hand-switching, and flexible set reordering during intense workouts.
- **Bilateral Symmetry Insights**: Clear visual comparison between Left and Right hand performance to identify and resolve strength imbalances.
- **Consistency Motivation**: Habit-building visual calendar heatmap celebrating training streaks and workout volume.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                               Home Screen                              │
│  "Welcome back, Alex!" • Today's Quick Grip Summary • Hand Balance Bar │
│                                                                        │
│  [ + Start New Series ] ───► Series Creator Modal / Editor Sheet       │
│                                • Name (e.g. CoC Crush Grip)            │
│                                • Sets Table (L/R, Weight, Reps)        │
│                                • Drag handle & Stepper buttons         │
│                                • [ Finish & Log Series ]               │
│                                                                        │
│  [ Active Rest Stopwatch ] (Floating Mini-Bar / Expandable sheet)      │
│  Recent Series Cards with Bilateral Mini-Charts                        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Navigation Bottom Bar
┌───────────────────────────────────▼────────────────────────────────────┐
│                             Analytics Screen                           │
│  ┌──────────────────────┐ ┌──────────────────────┐                     │
│  │ Best Day Record Card │ │ Bilateral PRs (L / R)│                     │
│  │ 120 Reps (Oct 24)    │ │ L: 75kg • R: 80kg    │                     │
│  └──────────────────────┘ └──────────────────────┘                     │
│                                                                        │
│  Time Horizon Selector: [ Week ] [ Month ] [ Year ]                    │
│  Interactive Volume & Repetition Trend Chart (Canvas / Dual-Line)      │
│                                                                        │
│  Activity Heatmap Calendar (Contribution Grid with 5-tier intensity)   │
│  Detailed Log History with Delete / Edit capabilities                  │
└────────────────────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme Tokens
- **Background Atmosphere**: Pitch-black carbon (`#0C0E12`) with elevated surface cards (`#161A22` and `#1E232E`) and subtle border strokes (`#2A3241`).
- **Accent Primary (Ember Flame)**: `#FF5722` (primary actions, active tabs, Left Hand highlight `#FF6F00`).
- **Accent Secondary (Electric Cyan / Ice Blue)**: `#00E5FF` (Right Hand highlight, contrasting data curves, stopwatch countdown).
- **Surface & Text**: High-contrast white (`#F0F4F8`) for primary digits, muted cool slate (`#94A3B8`) for secondary labels.
- **Heatmap Color Gradient**: 5 cool levels from muted dark slate (`#1E2633`) through cyan-teal tiers up to radiant bright electric cyan (`#00E5FF`).

### Component Polish & Micro-Interactions
- **Repetition Steppers**: Large, thumb-friendly `[-]` and `[+]` tactile buttons with rapid click & direct keyboard tap-to-edit sheet.
- **Drag & Drop / Reorder**: Tactile drag handle icon with instant visual elevation and smooth animated set list reordering.
- **Rest Timer Bar**: Smooth circular progress indicator with play/pause/reset and quick +15s / -15s micro-adjustments.
- **Empty States**: Encouraging graphic and prompt when no series are logged yet, with a 1-tap "Try Quick Demo Series" button.

---

## 3. Key Product Decisions & Trade-Offs

### 1. Drag & Drop and Reorderable Set Mechanics
- **Chosen Approach**: Native Compose drag-and-drop gesture state paired with explicit one-touch Up/Down position nudging fallback for accessibility.
- **Why**: Allows natural tactile reordering while athletes are resting between sets without frustration or accidental drag cancellations.

### 2. High-Performance Canvas Charts vs. External Heavy Libraries
- **Chosen Approach**: Custom Jetpack Compose Canvas implementations for the Bilateral Trend Graph (Week/Month/Year) and Activity Heatmap Calendar.
- **Why**: Eliminates fragile third-party chart library compilation incompatibilities, guarantees smooth 60fps animations, supports responsive multi-window layout, and enables exact brand color styling.

### 3. Local-First Room SQLite Storage with Pre-Populated Starter Series
- **Chosen Approach**: Room Database with reactive Kotlin coroutines `Flow`, plus first-launch starter templates (e.g. Warmup Series, Pinch Grip, Crush Grip).
- **Why**: Instant app startup, completely offline capable for basement/garage gyms, zero login friction, and immediate user exploration.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                           UI Layer (Jetpack Compose)                          │
│                                                                                │
│  ┌─────────────────────────┐  ┌────────────────────────┐  ┌─────────────────┐  │
│  │ HomeScreen & SeriesCard │  │ SeriesEditorSheet & Set│  │ AnalyticsScreen │  │
│  │ • Greeting & Overview   │  │ • Drag Reorder         │  │ • PR Highlight  │  │
│  │ • Bilateral Mini-Charts │  │ • Rep Stepper & L/R    │  │ • Volume Graph  │  │
│  │ • Rest Stopwatch Card   │  │ • Weight / Level Input │  │ • Heatmap Grid  │  │
│  └────────────┬────────────┘  └───────────┬────────────┘  └────────┬────────┘  │
└───────────────┼───────────────────────────┼────────────────────────┼───────────┘
                │                           │                        │
                ▼                           ▼                        ▼
┌────────────────────────────────────────────────────────────────────────────────┐
│                       ViewModel Layer (GripViewModel)                          │
│  • Manages active series draft & editing state                                 │
│  • Bilateral metric computation (Left vs. Right PRs, total reps, max weights)  │
│  • Rest timer countdown coroutine job & audio/haptic trigger                   │
│  • Time Horizon filter state (Week, Month, Year) & Heatmap matrix builder      │
└───────────────────────────────────────┬────────────────────────────────────────┘
                                        │
                                        ▼
┌────────────────────────────────────────────────────────────────────────────────┐
│                    Repository & Room Database (GripDatabase)                   │
│  ┌────────────────────────┐ ┌──────────────────────┐ ┌───────────────────────┐ │
│  │ SeriesEntity           │ │ SetEntity            │ │ UserProfileEntity     │ │
│  │ • id, name, timestamp  │ │ • id, seriesId, hand │ │ • id, athleteName,    │ │
│  │ • totalReps, notes     │ │ • reps, weight, order│ │ • preferredUnit (kg)  │ │
│  └────────────────────────┘ └──────────────────────┘ └───────────────────────┘ │
└────────────────────────────────────────────────────────────────────────────────┘
```

### Entity Schemas
1. **UserProfileEntity**: `athleteName` (defaults to "Athlete" or user-configured), `preferredUnit` (kg / lbs), `createdTimestamp`.
2. **SeriesEntity**: `id` (auto-generated Int), `name` (e.g. "Heavy Crush Gripper"), `dateMillis`, `notes`.
3. **SetEntity**: `id` (auto-generated Int), `seriesId` (foreign key / indexed), `hand` ("LEFT" or "RIGHT"), `weightOrLevel` (Float/String), `reps` (Int), `orderIndex` (Int for ordering), `isCompleted` (Boolean).

---

## 5. Step-by-Step Implementation Strategy

1. **Platform Setup & Typography**:
   - Set app name in `metadata.json` and `strings.xml` to **GripForge**.
   - Install athletic Google Font (e.g., `Outfit` or `Montserrat`) via `font-util install` into `app/src/main/res/font/`.
   - Configure M3 Athletic Dark Theme palette (`Theme.kt`, `Color.kt`).
2. **Database & Data Layer**:
   - Create Room Entities (`GripSeries`, `GripSet`, `UserProfile`).
   - Create DAOs (`GripDao`) with reactive `Flow` queries for all series, sets, daily aggregated volume, and personal records.
   - Implement `GripRepository` with pre-populated demo data so first open is immediately engaging.
3. **ViewModel & State Management**:
   - Build `GripViewModel` handling series draft state, set additions, deletion, order swap, rep increment/decrement, rest stopwatch timer, and analytics aggregation (best day, bilateral PRs, calendar heatmap cells).
4. **UI Components**:
   - **SetRowComponent**: Hand selector chip (Left orange / Right cyan), weight input, rep counter with quick `+`/`-` buttons, and drag/reorder handle.
   - **RestTimerSheet**: Circular countdown timer with play/pause/reset.
   - **ProgressChartCanvas**: Bilateral comparison bar/line chart with Week/Month/Year filters.
   - **ActivityCalendarHeatmap**: Custom grid displaying the past 12-16 weeks with cool intensity shading.
   - **SeriesEditorDialog / Sheet**: Full-screen intuitive editor to create, duplicate, and log workouts.
5. **Screen Assembly & Verification**:
   - Connect **HomeScreen** and **AnalyticsScreen** via M3 NavigationBar.
   - Run compilation check (`compile_applet`) and ensure zero build warnings or errors.
